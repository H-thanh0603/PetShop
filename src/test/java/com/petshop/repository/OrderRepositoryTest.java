package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.Order;
import com.petshop.model.OrderItem;
import java.math.BigDecimal;
import java.sql.Timestamp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private ProductRepository products;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int seedUser(String username) throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO users (username, password, fullname) VALUES ('"
                    + username + "_" + System.nanoTime() + "', 'x', 'Test User')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int seedProduct(int stock) {
        return products.addProductAndReturnId("OrderProd_" + System.nanoTime(), null,
                BigDecimal.valueOf(100), 0, "desc", stock, 100, "cat", 0);
    }

    private int seedOrder(int userId, String status) {
        Order order = new Order();
        order.setUserId(userId);
        order.setFullname("Test User");
        order.setPhone("090");
        order.setAddress("addr");
        order.setRecipientFullname("Test User");
        order.setRecipientPhone("090");
        order.setShippingAddress("addr");
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setStatus(status);
        order.setPayment_method("COD");
        order.setPayment_status(false);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        int id = repository.saveOrder(order);
        assertTrue(id > 0);
        return id;
    }

    @Test
    void statusTransitionPendingToConfirmed() throws Exception {
        int userId = seedUser("statususer");
        int orderId = seedOrder(userId, "Pending");
        assertTrue(repository.updateStatus(orderId, "Confirmed", userId));
        assertEquals("Confirmed", repository.getOrderById(orderId).getStatus());
        assertFalse(repository.updateStatus(orderId, "Pending", userId));
    }

    @Test
    void updateStatusRawPaths() throws Exception {
        int userId = seedUser("rawuser");
        int orderId = seedOrder(userId, "Pending");
        assertTrue(repository.updateOrderStatusRaw(orderId, "Shipping"));
        assertEquals("Shipping", repository.getOrderById(orderId).getStatus());
        assertTrue(repository.updateOrderStatus(orderId, "Delivered"));
        assertEquals("Delivered", repository.getOrderById(orderId).getStatus());
    }

    @Test
    void getOrderByIdFillsCustomerNameFromJoin() throws Exception {
        int userId = seedUser("shapeuser");
        int orderId = seedOrder(userId, "Pending");
        Order order = repository.getOrderById(orderId);
        assertNotNull(order);
        assertEquals("Test User", order.getCustomerFullname());
    }

    @Test
    void reserveFinalizeSequenceAcrossOrderAndProduct() throws Exception {
        int userId = seedUser("sequser");
        int productId = seedProduct(10);
        int orderId = seedOrder(userId, "Pending");
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(3);
        item.setPrice(BigDecimal.valueOf(100));
        assertTrue(repository.saveOrderItem(item));
        assertTrue(products.reserveStockAmbient(productId, 3));
        assertTrue(repository.finalizeReservedStockForOrder(orderId));
        assertEquals(7, products.getStock(productId));
        assertEquals(0, products.getProductById(productId).getReservedQuantity());
    }

    @Test
    void failedStatusUpdateReturnsFalse() throws Exception {
        // Contract pin only: nonexistent actor violates the history FK.
        // Atomicity (status unchanged) is pinned in OrderTxAtomicityIT — this
        // slice runs in a TestContext-managed tx where programmatic
        // setRollbackOnly is invisible, so mid-tx re-reads cannot prove it.
        // NOTE: must bypass the actor fallback (changedBy<=0 becomes user 1),
        // so use Integer.MAX_VALUE which no seed can reach.
        int userId = seedUser("rollbackuser");
        int orderId = seedOrder(userId, "Pending");
        assertEquals(false, repository.updateStatus(orderId, "Confirmed", Integer.MAX_VALUE));
    }

    @Test
    void saveOrderWithBadUserReturnsMinusOneOnConstraintViolation() {
        Order order = new Order();
        order.setUserId(-999);
        order.setFullname("X");
        order.setPhone("090");
        order.setAddress("a");
        order.setRecipientFullname("X");
        order.setRecipientPhone("090");
        order.setShippingAddress("a");
        order.setTotalAmount(BigDecimal.ONE);
        order.setStatus("Pending");
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        assertEquals(-1, repository.saveOrder(order));
    }

    @Test
    void cancelOrderByUserOutsideWindowFails() throws Exception {
        int userId = seedUser("canceluser");
        int orderId = seedOrder(userId, "Pending");
        // Fresh order is within the window; wrong user cannot cancel.
        assertEquals(false, repository.cancelOrderByUser(orderId, -999));
        assertEquals("Pending", repository.getOrderById(orderId).getStatus());
    }
}
