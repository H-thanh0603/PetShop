package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.OrderSign;
import java.util.List;
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
class OrderSignRepositoryTest {

    @Autowired
    private OrderSignRepository repository;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int[] seedUserAndOrder() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            long stamp = System.nanoTime();
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('signuser_" + stamp + "', 'x')",
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

    @Test
    void saveThenFindByOrderId() throws Exception {
        int[] ids = seedUserAndOrder();
        assertTrue(repository.save(ids[1], ids[0], "{\"total\":100}", "hash123", "pubkey", "privkey"));
        OrderSign found = repository.findByOrderId(ids[1]);
        assertNotNull(found);
        assertEquals("hash123", found.getOrderHash());
    }

    @Test
    void findByUserIdListsSigns() throws Exception {
        int[] ids = seedUserAndOrder();
        repository.save(ids[1], ids[0], "{}", "h", "pub", "priv");
        List<OrderSign> list = repository.findByUserId(ids[0]);
        assertEquals(1, list.size());
    }
}
