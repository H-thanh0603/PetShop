package services;

import Context.DBContext;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
import Model.CartItem;
import Model.Product;
import Model.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real-database integration test for concurrent checkout. Requires a Docker
 * daemon (Testcontainers); on machines without one the class is skipped, so
 * `gradlew test` still passes everywhere.
 *
 * It exercises the actual race: two customers checkout the same product at
 * the same time while only one can get the remaining stock. Invariants
 * asserted: no oversell, exactly one order placed, stock never negative.
 */
@Testcontainers(disabledWithoutDocker = true)
class CheckoutConcurrencyTest {

    @Container
    static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName("petvaccine")
                    .withUsername("petshop")
                    .withPassword("petshop");

    static DataSource dataSource;

    private static final int STOCK = 10;
    private static final int BUY_QUANTITY = STOCK; // both carts want everything

    @BeforeAll
    static void setUp() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(MYSQL.getJdbcUrl());
        // root: needed for SET GLOBAL (general log) during debugging; the
        // container always provisions root with the configured password.
        config.setUsername("root");
        config.setPassword(MYSQL.getPassword());
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);

        // Point the legacy static accessor at the container database, then run
        // the same migrations production runs (V1 legacy idempotent baseline).
        DBContext.setSpringDataSource(dataSource);
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load()
                .migrate();

        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("SET GLOBAL log_output = 'TABLE'");
            stmt.execute("SET GLOBAL general_log = 'ON'");
            stmt.execute("INSERT INTO products (name, price, stock) VALUES ('Concurrency Test Product', 10000, " + STOCK + ")");
            stmt.execute("INSERT INTO users (username, password, fullname, email, phone, role, status) VALUES " +
                    "('conc_user_a', 'x', 'User A', 'a@conc.test', '0900000001', 'user', 'active'), " +
                    "('conc_user_b', 'x', 'User B', 'b@conc.test', '0900000002', 'user', 'active')");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to seed test data", e);
        }
    }

    @AfterAll
    static void tearDown() {
        if (dataSource instanceof HikariDataSource hikari) {
            hikari.close();
        }
    }

    @Test
    void concurrentCheckoutOfLastStockNeverOversells() throws Exception {
        int productId = queryInt("SELECT id FROM products WHERE name = 'Concurrency Test Product' ORDER BY id DESC LIMIT 1");
        var users = fetchConcUsers();
        assertEquals(2, users.size());

        CheckoutService checkoutService = new CheckoutService(
                new ProductDAO(), new UserDAO(), new CouponDao(),
                new OrderDAO(), new PaymentTransactionDAO(),
                new CartDAO(), new OrderEmailService());

        Callable<CheckoutResult> firstCheckout =
                checkoutFor(checkoutService, productId, users.get(0));
        Callable<CheckoutResult> secondCheckout =
                checkoutFor(checkoutService, productId, users.get(1));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<CheckoutResult> first = executor.submit(firstCheckout);
            Future<CheckoutResult> second = executor.submit(secondCheckout);
            CheckoutResult r1 = first.get(60, TimeUnit.SECONDS);
            CheckoutResult r2 = second.get(60, TimeUnit.SECONDS);
            System.out.println("CONC-DEBUG r1(user=" + users.get(0).getId() + ") success=" + r1.isSuccess()
                    + " orderId=" + r1.getOrderId() + " msg=" + r1.getMessage());
            System.out.println("CONC-DEBUG r2(user=" + users.get(1).getId() + ") success=" + r2.isSuccess()
                    + " orderId=" + r2.getOrderId() + " msg=" + r2.getMessage());

            long successes = (r1.isSuccess() ? 1 : 0) + (r2.isSuccess() ? 1 : 0);
            assertEquals(1, successes,
                    "exactly one checkout must win: r1=" + r1.isSuccess() + ":" + r1.getMessage()
                            + " r2=" + r2.isSuccess() + ":" + r2.getMessage());

            int stock = queryInt("SELECT stock FROM products WHERE id = " + productId);
            assertEquals(0, stock, "stock must be exactly consumed, never negative");
            assertTrue(stock >= 0, "stock must never go negative");
            int orderCount = queryInt("SELECT COUNT(*) FROM orders WHERE status != 'Cancelled'");
            assertEquals(1, orderCount, "exactly one non-cancelled order may exist; orders="
                    + queryOrders());
            int orderedQty = queryInt(
                    "SELECT COALESCE(SUM(quantity), 0) FROM order_items WHERE product_id = " + productId);
            assertEquals(BUY_QUANTITY, orderedQty, "order items must match the winning checkout only");
        } finally {
            executor.shutdownNow();
        }
    }

    private static String queryOrders() throws Exception {
        // Dump the recent order-related statements from the general log table
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT CONVERT(argument USING utf8mb4) FROM mysql.general_log " +
                             "WHERE command_type = 'Query' AND (" +
                             "argument LIKE 'INSERT INTO orders%' OR argument = 'ROLLBACK' OR " +
                             "argument = 'COMMIT' OR argument LIKE 'UPDATE products%') " +
                             "ORDER BY event_time DESC LIMIT 40")) {
            StringBuilder sb = new StringBuilder(" | QUERIES: ");
            while (rs.next()) {
                sb.append("<<").append(rs.getString(1), 0, Math.min(90, rs.getString(1).length())).append(">> ");
            }
            return sb.toString();
        } catch (Exception e) {
            return "(general log unavailable: " + e.getMessage() + ")";
        }
    }

    private static Callable<CheckoutResult> checkoutFor(CheckoutService checkoutService,
                                                        int productId, User user) {
        return () -> {
            Map<Integer, CartItem> cart = new HashMap<>();
            Product product = new Product();
            product.setId(productId);
            product.setName("Concurrency Test Product");
            product.setPrice(BigDecimal.valueOf(10_000));
            cart.put(productId, new CartItem(product, BUY_QUANTITY));
            return checkoutService.processCheckout(
                    user, cart, user.getFullname(), user.getPhone(),
                    "Test address", null, null, "cod", 0, null);
        };
    }

    private static int queryInt(String sql) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static java.util.List<User> fetchConcUsers() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, fullname, phone FROM users WHERE email LIKE '%@conc.test' ORDER BY id")) {
            java.util.List<User> users = new java.util.ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullname(rs.getString("fullname"));
                user.setPhone(rs.getString("phone"));
                users.add(user);
            }
            return users;
        }
    }
}
