package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.PaymentTransaction;
import java.math.BigDecimal;
import java.sql.Timestamp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentTransactionRepositoryTest {

    @Autowired
    private PaymentTransactionRepository repository;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int[] seedUserAndOrder() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            long stamp = System.nanoTime();
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('payuser_" + stamp + "', 'x')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            int userId;
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                userId = keys.getInt(1);
            }
            stmt.executeUpdate("INSERT INTO orders (user_id, fullname, phone, address, recipient_fullname, recipient_phone, shipping_address, subtotal, shipping_fee, discount_amount, total_amount, status, payment_status) VALUES ("
                    + userId + ", 'U', '090', 'a', 'U', '090', 'a', 100, 0, 0, 100, 'PENDING', 0)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            int orderId;
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                orderId = keys.getInt(1);
            }
            return new int[]{userId, orderId};
        }
    }

    private PaymentTransaction transaction(int orderId, int userId, String ref) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setOrderId(orderId);
        tx.setUserId(userId);
        tx.setProviderKey("BANK_TRANSFER");
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setCurrency("VND");
        tx.setTransferReference(ref);
        tx.setStatus("PENDING_VERIFICATION");
        tx.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        tx.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return tx;
    }

    @Test
    void saveThenGetLatest() throws Exception {
        int[] ids = seedUserAndOrder();
        int id = repository.saveTx(transaction(ids[1], ids[0], "REF-" + System.nanoTime()));
        assertTrue(id > 0);
        PaymentTransaction latest = repository.getLatestByOrderIdTx(ids[1]);
        assertNotNull(latest);
        assertEquals(id, latest.getId());
    }

    @Test
    void findPendingByContentMatchesLongestReference() throws Exception {
        int[] ids = seedUserAndOrder();
        long stamp = System.nanoTime();
        repository.saveTx(transaction(ids[1], ids[0], "PETSHOP-" + stamp));
        PaymentTransaction found = repository.findPendingByTransferReferenceInContentForUpdate(
                "Thanh toan PETSHOP-" + stamp + " cam on");
        assertNotNull(found);
        assertEquals(ids[1], found.getOrderId());
    }

    @Test
    void transferReferenceNormalizationIgnoresSeparatorsAndCase() {
        // Supersedes PaymentTransactionDAOTest (mock-free pure function, kept verbatim).
        assertEquals("PETSHOPU3622107",
                PaymentTransactionRepository.normalizeTransferReferenceToken("PETSHOP-U3-622107"));
        assertEquals("PETSHOPU3622107",
                PaymentTransactionRepository.normalizeTransferReferenceToken("petshopu3622107"));
        assertEquals("THANHTOANPETSHOPU3622107",
                PaymentTransactionRepository.normalizeTransferReferenceToken("Thanh toan PETSHOP U3 622107"));
    }

    @Test
    void updateLatestProviderResultFlow() throws Exception {
        int[] ids = seedUserAndOrder();
        int id = repository.saveTx(transaction(ids[1], ids[0], "REF2-" + System.nanoTime()));
        assertTrue(repository.updateLatestProviderResultForOrder(ids[1], "BANK_TRANSFER", "prov-1",
                BigDecimal.valueOf(100), "{}", "PAID", "VERIFIED", "ok"));
        PaymentTransaction updated = repository.getLatestByOrderIdTx(ids[1]);
        assertEquals("VERIFIED", updated.getVerificationStatus());
        assertNotNull(updated.getVerifiedAt());
        assertTrue(id > 0);
    }

    @Test
    void updateLatestProviderResultWrongProviderReturnsFalse() throws Exception {
        int[] ids = seedUserAndOrder();
        repository.saveTx(transaction(ids[1], ids[0], "REF3-" + System.nanoTime()));
        assertEquals(false, repository.updateLatestProviderResultForOrder(ids[1], "VNPAY", "prov-9",
                BigDecimal.valueOf(100), "{}", "PAID", "VERIFIED", "ok"));
    }
}
