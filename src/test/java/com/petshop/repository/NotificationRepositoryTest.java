package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.Notification;
import java.util.List;
import java.util.Map;
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
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int seedUser() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            long stamp = System.nanoTime();
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('notifuser_" + stamp + "', 'x')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    @Test
    void createThenListAndCount() throws Exception {
        int userId = seedUser();
        assertTrue(repository.create(userId, "Title", "Body", "INFO", "/link"));
        assertTrue(repository.create(userId, "Title2", "Body2", "INFO", "/link2"));
        List<Map<String, Object>> list = repository.getNotificationsByUserId(userId, 10);
        assertEquals(2, list.size());
        assertEquals("Title", list.get(0).get("title"));
        assertEquals(userId, list.get(0).get("userId"));
        assertEquals(2, repository.getUnreadCountByUserId(userId));
    }

    @Test
    void markAllAsReadClearsUnreadCount() throws Exception {
        int userId = seedUser();
        repository.create(userId, "T", "B", "INFO", null);
        assertEquals(1, repository.getUnreadCountByUserId(userId));
        assertTrue(repository.markAllAsReadBool(userId));
        assertEquals(0, repository.getUnreadCountByUserId(userId));
    }

    @Test
    void listMapsHaveExpectedKeys() throws Exception {
        int userId = seedUser();
        repository.create(userId, "T", "B", "WARN", "/l");
        Map<String, Object> map = repository.getNotificationsByUserId(userId, 1).get(0);
        for (String key : new String[]{"id", "userId", "title", "message", "type", "link", "isRead", "createdAt"}) {
            assertTrue(map.containsKey(key), "missing key " + key);
        }
    }
}
