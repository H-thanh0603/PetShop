package com.petshop.repository;

import com.petshop.model.Order;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Atomicity proofs in a FULL application context (aspect-managed
 * transactions). The @DataJpaTest slices run in TestContext-managed
 * transactions where programmatic setRollbackOnly is invisible, so rollback
 * can only be proven here: no @Transactional on the tests, each repository
 * call gets its own real transaction that must roll back on failure.
 *
 * Unique seeds + explicit cleanup keep the shared databases hermetic.
 */
@SpringBootTest
class OrderTxAtomicityIT {

    @Autowired
    private OrderRepository orders;

    @Autowired
    private DataSource dataSource;

    private int seedUser() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            long stamp = System.nanoTime();
            stmt.executeUpdate("INSERT INTO users (username, password, fullname) VALUES ('atomicuser_"
                    + stamp + "', 'x', 'Atomic User')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void deleteOrder(int orderId) {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM order_logs WHERE order_id = " + orderId);
            stmt.executeUpdate("DELETE FROM order_status_history WHERE order_id = " + orderId);
            stmt.executeUpdate("DELETE FROM order_items WHERE order_id = " + orderId);
            stmt.executeUpdate("DELETE FROM orders WHERE id = " + orderId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void failedStatusUpdateLeavesStatusUnchanged() throws Exception {
        int userId = seedUser();
        Order order = new Order();
        order.setUserId(userId);
        order.setFullname("Atomic User");
        order.setPhone("090");
        order.setAddress("addr");
        order.setRecipientFullname("Atomic User");
        order.setRecipientPhone("090");
        order.setShippingAddress("addr");
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setStatus("Pending");
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        int orderId = orders.saveOrder(order);
        try {
            // Nonexistent actor violates the history FK: must return false AND
            // leave the order row untouched (full rollback of setStatus).
            assertEquals(false, orders.updateStatus(orderId, "Confirmed", Integer.MAX_VALUE));
            Order reloaded = orders.getOrderById(orderId);
            assertEquals("Pending", reloaded.getStatus());
        } finally {
            deleteOrder(orderId);
        }
    }

    @Test
    void failedCancelLeavesOrderUntouched() throws Exception {
        int userId = seedUser();
        Order order = new Order();
        order.setUserId(userId);
        order.setFullname("Atomic User");
        order.setPhone("090");
        order.setAddress("addr");
        order.setRecipientFullname("Atomic User");
        order.setRecipientPhone("090");
        order.setShippingAddress("addr");
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setStatus("Pending");
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        int orderId = orders.saveOrder(order);
        try {
            // Wrong user cannot cancel: false + status unchanged.
            assertEquals(false, orders.cancelOrderByUser(orderId, -999));
            assertEquals("Pending", orders.getOrderById(orderId).getStatus());
        } finally {
            deleteOrder(orderId);
        }
    }
}
