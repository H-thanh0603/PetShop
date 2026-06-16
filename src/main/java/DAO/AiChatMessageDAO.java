package DAO;

import Context.DBContext;
import Model.AiChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AiChatMessageDAO {
    private static final Logger log = LoggerFactory.getLogger(AiChatMessageDAO.class);

    private AiChatMessage mapRow(ResultSet rs) throws SQLException {
        return new AiChatMessage(
                rs.getInt("id"),
                rs.getInt("session_id"),
                rs.getString("sender_type"),
                rs.getString("message"),
                rs.getString("intent"),
                rs.getBigDecimal("confidence"),
                rs.getBoolean("need_admin_support"),
                rs.getString("suggested_admin_note"),
                rs.getTimestamp("created_at")
        );
    }

    public boolean create(AiChatMessage message) {
        String sql = "INSERT INTO ai_chat_messages (session_id, sender_type, message, intent, confidence, need_admin_support, suggested_admin_note) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getSessionId());
            ps.setString(2, message.getSenderType());
            ps.setString(3, message.getMessage());
            ps.setString(4, message.getIntent());
            ps.setBigDecimal(5, message.getConfidence());
            ps.setBoolean(6, message.isNeedAdminSupport());
            ps.setString(7, message.getSuggestedAdminNote());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                // If it's a user or admin reply, we also update the session's updated_at timestamp!
                updateSessionTimestamp(message.getSessionId());
                return true;
            }
        } catch (Exception e) {
            log.error("Error creating chat message", e);
        }
        return false;
    }

    public List<AiChatMessage> getMessagesBySessionId(int sessionId) {
        List<AiChatMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM ai_chat_messages WHERE session_id = ? ORDER BY id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            log.error("Error fetching messages for session_id={}", sessionId, e);
        }
        return list;
    }

    public List<AiChatMessage> getRecentMessagesBySessionId(int sessionId, int limit) {
        List<AiChatMessage> list = new ArrayList<>();
        // Get the latest ones, but order them ASC (oldest first) in the final list
        String sql = "SELECT * FROM (SELECT * FROM ai_chat_messages WHERE session_id = ? ORDER BY id DESC LIMIT ?) tmp ORDER BY id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            log.error("Error fetching recent messages for session_id={}", sessionId, e);
        }
        return list;
    }

    public boolean markMessagesAsRead(int sessionId, String senderType) {
        String sql = "UPDATE ai_chat_messages SET is_read = TRUE WHERE session_id = ? AND sender_type = ? AND is_read = FALSE";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setString(2, senderType);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error marking messages as read for session_id={}, sender_type={}", sessionId, senderType, e);
        }
        return false;
    }

    public int getUnreadCountBySessionId(int sessionId, String senderType) {
        String sql = "SELECT COUNT(*) FROM ai_chat_messages WHERE session_id = ? AND sender_type = ? AND is_read = FALSE";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setString(2, senderType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            log.error("Error getting unread count for session_id={}, sender_type={}", sessionId, senderType, e);
        }
        return 0;
    }

    private void updateSessionTimestamp(int sessionId) {
        String sql = "UPDATE ai_chat_sessions SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.executeUpdate();
        } catch (Exception e) {
            log.error("Error updating session timestamp for id={}", sessionId, e);
        }
    }
}
