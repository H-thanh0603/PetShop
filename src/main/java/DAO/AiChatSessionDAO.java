package DAO;

import Context.DBContext;
import Model.AiChatSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AiChatSessionDAO {
    private static final Logger log = LoggerFactory.getLogger(AiChatSessionDAO.class);

    private AiChatSession mapRow(ResultSet rs) throws SQLException {
        AiChatSession session = new AiChatSession(
                rs.getInt("id"),
                rs.getObject("user_id") != null ? rs.getInt("user_id") : null,
                rs.getString("guest_name"),
                rs.getString("guest_email"),
                rs.getString("status"),
                rs.getBoolean("need_admin_support"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
        try {
            session.setUserFullname(rs.getString("fullname"));
            session.setUserEmail(rs.getString("email"));
        } catch (Exception ignored) {
            // JOIN fields might not be present in all queries
        }
        return session;
    }

    public int create(AiChatSession session) {
        String sql = "INSERT INTO ai_chat_sessions (user_id, guest_name, guest_email, status, need_admin_support) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (session.getUserId() != null) {
                ps.setInt(1, session.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, session.getGuestName());
            ps.setString(3, session.getGuestEmail());
            ps.setString(4, session.getStatus() != null ? session.getStatus() : "OPEN");
            ps.setBoolean(5, session.isNeedAdminSupport());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error creating chat session", e);
        }
        return 0;
    }

    public AiChatSession getById(int id) {
        String sql = "SELECT s.*, u.fullname, u.email FROM ai_chat_sessions s " +
                     "LEFT JOIN users u ON s.user_id = u.id WHERE s.id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            log.error("Error getting chat session by id={}", id, e);
        }
        return null;
    }

    public AiChatSession getLatestOpenSessionByUserId(int userId) {
        String sql = "SELECT s.*, u.fullname, u.email FROM ai_chat_sessions s " +
                     "LEFT JOIN users u ON s.user_id = u.id WHERE s.user_id = ? AND s.status != 'CLOSED' ORDER BY s.id DESC LIMIT 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            log.error("Error getting open session for user_id={}", userId, e);
        }
        return null;
    }

    public List<AiChatSession> getSessionsForAdmin() {
        List<AiChatSession> list = new ArrayList<>();
        String sql = "SELECT s.*, u.fullname, u.email FROM ai_chat_sessions s " +
                     "LEFT JOIN users u ON s.user_id = u.id ORDER BY s.id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            log.error("Error fetching all sessions for admin", e);
        }
        return list;
    }

    public List<AiChatSession> getSessionsByUserId(int userId) {
        List<AiChatSession> list = new ArrayList<>();
        String sql = "SELECT s.*, u.fullname, u.email FROM ai_chat_sessions s " +
                     "LEFT JOIN users u ON s.user_id = u.id WHERE s.user_id = ? ORDER BY s.id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            log.error("Error fetching sessions for user_id={}", userId, e);
        }
        return list;
    }

    public List<AiChatSession> getWaitingAdminSessions() {
        List<AiChatSession> list = new ArrayList<>();
        String sql = "SELECT s.*, u.fullname, u.email FROM ai_chat_sessions s " +
                     "LEFT JOIN users u ON s.user_id = u.id WHERE s.need_admin_support = TRUE AND s.status = 'WAITING_ADMIN' ORDER BY s.updated_at DESC, s.id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            log.error("Error fetching waiting sessions", e);
        }
        return list;
    }

    public boolean updateStatus(int sessionId, String status, boolean needAdminSupport) {
        String sql = "UPDATE ai_chat_sessions SET status = ?, need_admin_support = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setBoolean(2, needAdminSupport);
            ps.setInt(3, sessionId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error updating status for session id={}", sessionId, e);
        }
        return false;
    }
}
