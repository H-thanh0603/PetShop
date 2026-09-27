package com.petshop.repository;

import com.petshop.model.AiChatSession;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Integer> {

    @Query("SELECT s FROM AiChatSession s WHERE s.userId = :userId AND s.status <> 'CLOSED' ORDER BY s.id DESC LIMIT 1")
    AiChatSession findLatestOpenByUserId(@Param("userId") int userId);

    List<AiChatSession> findAllByOrderByIdDesc();

    List<AiChatSession> findByUserIdOrderByIdDesc(int userId);

    List<AiChatSession> findByNeedAdminSupportTrueAndStatusOrderByUpdatedAtDescIdDesc(String status);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE AiChatSession s SET s.status = :status, s.needAdminSupport = :needAdmin WHERE s.id = :sessionId")
    int updateStatus(@Param("sessionId") int sessionId, @Param("status") String status, @Param("needAdmin") boolean needAdminSupport);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE ai_chat_sessions SET updated_at = CURRENT_TIMESTAMP WHERE id = :sessionId", nativeQuery = true)
    void touchUpdatedAt(@Param("sessionId") int sessionId);

    @Transactional
    default int create(AiChatSession session) {
        try {
            if (session.getStatus() == null) {
                session.setStatus("OPEN");
            }
            return save(session).getId();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AiChatSessionRepository.class)
                    .error("Error creating chat session", e);
            return 0;
        }
    }

    default AiChatSession getLatestOpenSessionByUserId(int userId) {
        return findLatestOpenByUserId(userId);
    }

    default List<AiChatSession> getSessionsForAdmin() {
        return findAllByOrderByIdDesc();
    }

    default List<AiChatSession> getSessionsByUserId(int userId) {
        return findByUserIdOrderByIdDesc(userId);
    }

    default List<AiChatSession> getWaitingAdminSessions() {
        return findByNeedAdminSupportTrueAndStatusOrderByUpdatedAtDescIdDesc("WAITING_ADMIN");
    }

    @Transactional
    default boolean updateStatusBool(int sessionId, String status, boolean needAdminSupport) {
        try {
            return updateStatus(sessionId, status, needAdminSupport) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AiChatSessionRepository.class)
                    .error("Error updating status for session id={}", sessionId, e);
            return false;
        }
    }
}
