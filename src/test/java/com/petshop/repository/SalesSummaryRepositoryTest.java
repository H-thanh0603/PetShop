package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.DailySalesSummary;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SalesSummaryRepositoryTest {

    @Autowired
    private SalesSummaryRepository repository;

    @Autowired
    private javax.sql.DataSource dataSource;

    private void seedOrders(int count, String status) throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            long stamp = System.nanoTime();
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('salesuser_" + stamp + "', 'x')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            int userId;
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                userId = keys.getInt(1);
            }
            for (int i = 0; i < count; i++) {
                stmt.executeUpdate("INSERT INTO orders (user_id, fullname, phone, address, recipient_fullname, recipient_phone, shipping_address, subtotal, shipping_fee, discount_amount, total_amount, status, payment_status) VALUES ("
                        + userId + ", 'U', '090', 'a', 'U', '090', 'a', 100, 0, 0, 100, '" + status + "', 0)");
            }
        }
    }

    @Test
    void rebuildAllAggregatesSeededOrders() throws Exception {
        seedOrders(3, "Completed");
        seedOrders(1, "Cancelled");
        repository.rebuildAll();
        List<DailySalesSummary> rows = repository.findAll();
        assertTrue(rows.stream().mapToInt(DailySalesSummary::getTotalOrders).sum() >= 4);
        assertTrue(rows.stream().mapToInt(DailySalesSummary::getCompletedOrders).sum() >= 3);
    }

    @Test
    void refreshRecentKeepsOlderRows() throws Exception {
        seedOrders(2, "Pending");
        repository.rebuildAll();
        int before = repository.findAll().size();
        repository.refreshRecent(7);
        assertEquals(before, repository.findAll().size());
    }
}
