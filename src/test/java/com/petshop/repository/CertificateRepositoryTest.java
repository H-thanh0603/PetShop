package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.Certificate;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CertificateRepositoryTest {

    @Autowired
    private CertificateRepository repository;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int[] seedUserAndOrder() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            long stamp = System.nanoTime();
            String userSql = "INSERT INTO users (username, password, fullname, email, phone, address) VALUES ('certuser_" + stamp + "', 'x', 'Cert User', 'certuser_" + stamp + "@test.local', '090', 'addr')";
            stmt.executeUpdate(userSql,
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            int userId;
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                userId = keys.getInt(1);
            }
            stmt.executeUpdate("INSERT INTO orders (user_id, fullname, phone, address, recipient_fullname, recipient_phone, shipping_address, subtotal, shipping_fee, discount_amount, total_amount, status, payment_status) VALUES ("
                    + userId + ", 'Cert User', '090', 'addr', 'Cert User', '090', 'addr', 100, 0, 0, 100, 'PENDING', 0)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            int orderId;
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                orderId = keys.getInt(1);
            }
            return new int[]{userId, orderId};
        }
    }

    @Test
    void saveThenFindByOrderId() throws Exception {
        int[] ids = seedUserAndOrder();
        assertTrue(repository.save(ids[1], ids[0], "ORD-1", "PEM-DATA", "CN=test", Timestamp.from(Instant.now().plusSeconds(3600))));
        Certificate found = repository.findByOrderId(ids[1]);
        assertNotNull(found);
        assertEquals("ORD-1", found.getOrderCode());
        assertEquals(ids[0], found.getUserId());
    }
}
