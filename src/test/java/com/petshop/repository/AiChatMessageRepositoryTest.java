package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.AiChatMessage;
import com.petshop.model.AiChatSession;
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
class AiChatMessageRepositoryTest {

    @Autowired
    private AiChatMessageRepository messages;

    @Autowired
    private AiChatSessionRepository sessions;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int seedUser() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('" + "aichatmsguser_" + System.nanoTime() + "', 'x')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int openSession() throws Exception {
        AiChatSession session = new AiChatSession();
        session.setUserId(seedUser());
        session.setStatus("OPEN");
        session.setNeedAdminSupport(false);
        return sessions.create(session);
    }

    @Test
    void createThenGetMessagesBySessionId() throws Exception {
        int sessionId = openSession();
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setSenderType("USER");
        message.setMessage("hello");
        assertTrue(messages.create(message, sessions));
        assertEquals(1, messages.getMessagesBySessionId(sessionId).size());
    }

    @Test
    void markMessagesAsReadThenUnreadCountIsZero() throws Exception {
        int sessionId = openSession();
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setSenderType("USER");
        message.setMessage("hello");
        messages.create(message, sessions);
        assertEquals(1, messages.getUnreadCountBySessionId(sessionId, "USER"));
        assertTrue(messages.markMessagesAsReadBool(sessionId, "USER"));
        assertEquals(0, messages.getUnreadCountBySessionId(sessionId, "USER"));
    }

    @Test
    void countUserMessagesTodayCountsOnlyUserSenderType() throws Exception {
        int userId = seedUser();
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setStatus("OPEN");
        session.setNeedAdminSupport(false);
        int sessionId = sessions.create(session);
        AiChatMessage user = new AiChatMessage();
        user.setSessionId(sessionId);
        user.setSenderType("USER");
        user.setMessage("hi");
        messages.create(user, sessions);
        AiChatMessage ai = new AiChatMessage();
        ai.setSessionId(sessionId);
        ai.setSenderType("AI");
        ai.setMessage("hello back");
        messages.create(ai, sessions);
        assertEquals(1, messages.countUserMessagesToday(userId));
    }
}
