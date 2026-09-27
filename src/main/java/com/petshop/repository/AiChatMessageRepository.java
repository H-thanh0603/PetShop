package com.petshop.repository;

import com.petshop.model.AiChatMessage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Integer> {

    List<AiChatMessage> findBySessionIdOrderByIdAsc(int sessionId);

    @Query(value = "SELECT * FROM (SELECT * FROM ai_chat_messages WHERE session_id = :sessionId ORDER BY id DESC LIMIT :limit) tmp ORDER BY id ASC",
            nativeQuery = true)
    List<AiChatMessage> findRecentBySessionId(@Param("sessionId") int sessionId, @Param("limit") int limit);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE AiChatMessage m SET m.read = true WHERE m.sessionId = :sessionId AND m.senderType = :senderType AND m.read = false")
    int markMessagesAsRead(@Param("sessionId") int sessionId, @Param("senderType") String senderType);

    @Query("SELECT COUNT(m) FROM AiChatMessage m WHERE m.sessionId = :sessionId AND m.senderType = :senderType AND m.read = false")
    int getUnreadCountBySessionId(@Param("sessionId") int sessionId, @Param("senderType") String senderType);

    @Query("SELECT COUNT(m) FROM AiChatMessage m WHERE m.sessionId = :sessionId")
    int countBySession(@Param("sessionId") int sessionId);

    @Query(value = "SELECT COUNT(*) FROM ai_chat_messages m "
            + "JOIN ai_chat_sessions s ON s.id = m.session_id "
            + "WHERE s.user_id = :userId AND m.sender_type = 'USER' AND m.created_at >= CURDATE()",
            nativeQuery = true)
    int countUserMessagesToday(@Param("userId") int userId);

    // Mirror of the DAO side-effect: creating a message bumps the parent
    // session's updated_at. Implemented in the service-facing default method
    // so call sites keep a single call.
    @Transactional
    default boolean create(AiChatMessage message, AiChatSessionRepository sessions) {
        try {
            save(message);
            if (sessions != null) {
                sessions.touchUpdatedAt(message.getSessionId());
            }
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AiChatMessageRepository.class)
                    .error("Error creating chat message", e);
            return false;
        }
    }

    @Transactional
    default boolean create(AiChatMessage message) {
        return create(message, null);
    }

    default List<AiChatMessage> getMessagesBySessionId(int sessionId) {
        return findBySessionIdOrderByIdAsc(sessionId);
    }

    default List<AiChatMessage> getRecentMessagesBySessionId(int sessionId, int limit) {
        return findRecentBySessionId(sessionId, limit);
    }

    default boolean markMessagesAsReadBool(int sessionId, String senderType) {
        try {
            return markMessagesAsRead(sessionId, senderType) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AiChatMessageRepository.class)
                    .error("Error marking messages as read for session_id={}, sender_type={}", sessionId, senderType, e);
            return false;
        }
    }
}
