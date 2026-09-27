package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.AiChatSession;
import java.util.List;
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
class AiChatSessionRepositoryTest {

    @Autowired
    private AiChatSessionRepository repository;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int seedUser() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('" + "aichatuser_" + System.nanoTime() + "', 'x')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private AiChatSession newSession(Integer userId) {
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setStatus("OPEN");
        session.setNeedAdminSupport(false);
        return session;
    }

    @Test
    void createThenGetById() throws Exception {
        int userId = seedUser();
        int id = repository.create(newSession(userId));
        assertTrue(id > 0);
        AiChatSession found = repository.findById(id).orElse(null);
        assertNotNull(found);
        assertEquals(userId, found.getUserId());
        assertEquals("OPEN", found.getStatus());
    }

    @Test
    void getLatestOpenSessionSkipsClosed() throws Exception {
        int userId = seedUser();
        AiChatSession closed = newSession(userId);
        closed.setStatus("CLOSED");
        repository.create(closed);
        AiChatSession open = newSession(userId);
        repository.create(open);
        AiChatSession latest = repository.getLatestOpenSessionByUserId(userId);
        assertNotNull(latest);
        assertEquals("OPEN", latest.getStatus());
    }

    @Test
    void updateStatusChangesStatusAndFlag() {
        int id = repository.create(newSession(3));
        assertTrue(repository.updateStatusBool(id, "WAITING_ADMIN", true));
        AiChatSession found = repository.findById(id).orElse(null);
        assertNotNull(found);
        assertEquals("WAITING_ADMIN", found.getStatus());
        assertTrue(found.isNeedAdminSupport());
    }

    @Test
    void waitingAdminSessionsListsOnlyWaiting() {
        AiChatSession waiting = newSession(4);
        waiting.setStatus("WAITING_ADMIN");
        waiting.setNeedAdminSupport(true);
        int waitingId = repository.create(waiting);
        repository.create(newSession(4));
        List<AiChatSession> list = repository.getWaitingAdminSessions();
        assertTrue(list.stream().anyMatch(s -> s.getId() == waitingId));
        assertTrue(list.stream().allMatch(s -> "WAITING_ADMIN".equals(s.getStatus()) && s.isNeedAdminSupport()));
    }

    @Test
    void getByIdReturnsNullForMissing() {
        assertNull(repository.findById(-999).orElse(null));
    }
}
