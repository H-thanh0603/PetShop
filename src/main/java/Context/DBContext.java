package Context;

import DAO.DBProperties;
import Util.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database context using HikariCP connection pool.
 * Pool is initialized once at class load time.
 */
public class DBContext {

    private static final HikariDataSource DATA_SOURCE;

    static {
        HikariConfig config = new HikariConfig();

        String host     = DBProperties.host();
        int    port     = DBProperties.port();
        String dbname   = DBProperties.dbname();
        String username = DBProperties.username();
        String password = DBProperties.password();
        String option   = DBProperties.option();

        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbname + "?" + option);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool sizing — tuned for production workloads (Task 11.2 verified)
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30_000);   // 30 seconds
        config.setIdleTimeout(600_000);         // 10 minutes
        config.setMaxLifetime(1_800_000);       // 30 minutes
        config.setPoolName("PetShopPool");

        // Validation
        config.setConnectionTestQuery("SELECT 1");

        DATA_SOURCE = new HikariDataSource(config);

        runMigrations();
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    /**
     * Shutdown the pool (called on application undeploy).
     */
    public static void shutdown() {
        if (DATA_SOURCE != null && !DATA_SOURCE.isClosed()) {
            DATA_SOURCE.close();
        }
    }

    /**
     * Run schema migrations on startup.
     */
    private static void runMigrations() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            addColumnIfMissing(conn, stmt, "products", "weight", "INT NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, stmt, "products", "is_active", "TINYINT(1) NOT NULL DEFAULT 1");
            addColumnIfMissing(conn, stmt, "products", "stock", "INT NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, stmt, "products", "category", "VARCHAR(255) NULL");
            addColumnIfMissing(conn, stmt, "products", "pet_type_id", "INT NULL");
            addColumnIfMissing(conn, stmt, "users", "email_verified", "BOOLEAN NOT NULL DEFAULT FALSE");
            addColumnIfMissing(conn, stmt, "users", "verification_token", "VARCHAR(255) NULL");
            addColumnIfMissing(conn, stmt, "users", "verification_token_expiry", "TIMESTAMP NULL");
            addColumnIfMissing(conn, stmt, "users", "failed_login_attempts", "INT NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, stmt, "users", "locked_until", "DATETIME NULL DEFAULT NULL");
            addColumnIfMissing(conn, stmt, "orders", "payment_method", "VARCHAR(50) DEFAULT 'COD'");
            addColumnIfMissing(conn, stmt, "orders", "payment_status", "TINYINT(1) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, stmt, "orders", "createdAt", "TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP");
            executeIgnore(stmt, "UPDATE orders SET createdAt = created_at WHERE createdAt IS NULL AND created_at IS NOT NULL");
            stmt.execute("CREATE TABLE IF NOT EXISTS payment_transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "order_id INT NOT NULL," +
                    "user_id INT NOT NULL," +
                    "provider_key VARCHAR(50) NOT NULL," +
                    "provider_display_name VARCHAR(100) NULL," +
                    "amount DECIMAL(15,2) NOT NULL," +
                    "currency VARCHAR(10) NOT NULL DEFAULT 'VND'," +
                    "transfer_reference VARCHAR(100) NULL," +
                    "provider_transaction_id VARCHAR(255) NULL," +
                    "status VARCHAR(50) NOT NULL," +
                    "verification_status VARCHAR(50) NULL," +
                    "verification_message VARCHAR(255) NULL," +
                    "provider_metadata TEXT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "verified_at TIMESTAMP NULL," +
                    "INDEX idx_payment_transactions_order_id (order_id)," +
                    "INDEX idx_payment_transactions_status (status)," +
                    "CONSTRAINT fk_payment_transactions_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_payment_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS security_events (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "event_type VARCHAR(100) NOT NULL," +
                    "principal VARCHAR(255) NULL," +
                    "ip_address VARCHAR(64) NULL," +
                    "details VARCHAR(500) NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_security_events_type_created (event_type, created_at)," +
                    "INDEX idx_security_events_principal_created (principal, created_at)" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "contact_name VARCHAR(255) NULL," +
                    "phone VARCHAR(50) NULL," +
                    "email VARCHAR(255) NULL," +
                    "address VARCHAR(500) NULL," +
                    "notes VARCHAR(500) NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS stock_imports (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "supplier_id INT NULL," +
                    "receipt_code VARCHAR(100) NOT NULL," +
                    "received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "note VARCHAR(500) NULL," +
                    "created_by INT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "UNIQUE KEY uk_stock_imports_receipt_code (receipt_code)," +
                    "CONSTRAINT fk_stock_imports_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL," +
                    "CONSTRAINT fk_stock_imports_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_batches (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "product_id INT NOT NULL," +
                    "stock_import_id INT NULL," +
                    "supplier_id INT NULL," +
                    "batch_code VARCHAR(100) NOT NULL," +
                    "received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "expiry_date DATE NULL," +
                    "received_quantity INT NOT NULL," +
                    "remaining_quantity INT NOT NULL," +
                    "unit_cost DECIMAL(15,2) NOT NULL DEFAULT 0," +
                    "note VARCHAR(500) NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "UNIQUE KEY uk_inventory_batches_batch_code (batch_code)," +
                    "INDEX idx_inventory_batches_product_expiry (product_id, expiry_date)," +
                    "INDEX idx_inventory_batches_remaining_received (remaining_quantity, received_at)," +
                    "CONSTRAINT fk_inventory_batches_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_inventory_batches_stock_import FOREIGN KEY (stock_import_id) REFERENCES stock_imports(id) ON DELETE SET NULL," +
                    "CONSTRAINT fk_inventory_batches_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS stock_movements (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "product_id INT NOT NULL," +
                    "inventory_batch_id INT NULL," +
                    "order_id INT NULL," +
                    "movement_type VARCHAR(50) NOT NULL," +
                    "quantity INT NOT NULL," +
                    "reference_code VARCHAR(100) NULL," +
                    "note VARCHAR(500) NULL," +
                    "created_by INT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_stock_movements_product_created (product_id, created_at)," +
                    "INDEX idx_stock_movements_type_created (movement_type, created_at)," +
                    "CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_stock_movements_batch FOREIGN KEY (inventory_batch_id) REFERENCES inventory_batches(id) ON DELETE SET NULL," +
                    "CONSTRAINT fk_stock_movements_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL," +
                    "CONSTRAINT fk_stock_movements_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL" +
                    ")");
            executeIgnore(stmt, "ALTER TABLE payment_transactions ADD COLUMN expires_at TIMESTAMP NULL");
            executeIgnore(stmt, "ALTER TABLE payment_transactions ADD COLUMN amount_received DECIMAL(15,2) NULL");
            executeIgnore(stmt, "ALTER TABLE payment_transactions ADD COLUMN bank_content VARCHAR(500) NULL");
            executeIgnore(stmt, "CREATE UNIQUE INDEX uk_payment_transactions_provider_transaction_id ON payment_transactions (provider_transaction_id)");
            executeIgnore(stmt, "CREATE INDEX idx_payment_transactions_verification_status ON payment_transactions (verification_status)");
            executeIgnore(stmt, "CREATE INDEX idx_payment_transactions_transfer_reference ON payment_transactions (transfer_reference)");
            stmt.execute("CREATE TABLE IF NOT EXISTS bank_webhook_events (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "provider_transaction_id VARCHAR(255) NOT NULL," +
                    "amount DECIMAL(15,2) NOT NULL," +
                    "bank_content VARCHAR(500) NULL," +
                    "bank_account VARCHAR(100) NULL," +
                    "payment_transaction_id INT NULL," +
                    "status VARCHAR(50) NOT NULL," +
                    "raw_payload TEXT NULL," +
                    "received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY uk_bank_webhook_events_provider_transaction_id (provider_transaction_id)," +
                    "INDEX idx_bank_webhook_events_status_received (status, received_at)," +
                    "INDEX idx_bank_webhook_events_payment_transaction (payment_transaction_id)," +
                    "CONSTRAINT fk_bank_webhook_events_payment_transaction " +
                    "FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions(id) ON DELETE SET NULL" +
                    ")");
            executeIgnore(stmt, "CREATE INDEX idx_products_active_category_pet_price ON products (is_active, category(100), pet_type_id, price, discount, id)");
            executeIgnore(stmt, "CREATE INDEX idx_orders_status_created_user ON orders (status, createdAt, user_id)");
            executeIgnore(stmt, "CREATE INDEX idx_users_email_username_locked ON users (email, username, locked_until)");
            addColumnIfMissing(conn, stmt, "addresses", "is_default", "TINYINT(1) NOT NULL DEFAULT 0");
            // Mark all existing users as verified (they registered before email verification was added)
            stmt.execute("UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE AND verification_token IS NULL");
            int pendingMinutes = AppConfig.getInt("payment.bank.pending-minutes", 10);
            executeIgnore(stmt, "UPDATE payment_transactions " +
                    "SET expires_at = DATE_ADD(created_at, INTERVAL " + pendingMinutes + " MINUTE) " +
                    "WHERE expires_at IS NULL AND status = 'PENDING_VERIFICATION'");
            System.out.println("[DBContext] Migrations applied.");
        } catch (Exception e) {
            System.err.println("[DBContext] Migration warning: " + e.getMessage());
        }
    }

    private static void addColumnIfMissing(Connection conn, Statement stmt,
                                           String tableName, String columnName,
                                           String columnDefinition) {
        try {
            if (!columnExists(conn, tableName, columnName)) {
                stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
            }
        } catch (Exception e) {
            System.err.println("[DBContext] Migration warning for "
                    + tableName + "." + columnName + ": " + e.getMessage());
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void executeIgnore(Statement stmt, String sql) {
        try {
            stmt.execute(sql);
        } catch (Exception ignored) {
        }
    }
}
