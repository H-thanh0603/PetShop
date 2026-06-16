package DAO;

import Context.DBContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationDAO {
    private static final Logger log = LoggerFactory.getLogger(NotificationDAO.class);

    public List<Map<String, Object>> getNotificationsByUserId(int userId, int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("userId", rs.getInt("user_id"));
                    map.put("title", rs.getString("title"));
                    map.put("message", rs.getString("message"));
                    map.put("type", rs.getString("type"));
                    map.put("link", rs.getString("link"));
                    map.put("isRead", rs.getBoolean("is_read"));
                    map.put("createdAt", rs.getTimestamp("created_at").toString());
                    list.add(map);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching notifications for user={}", userId, e);
        }
        return list;
    }

    public int getUnreadCountByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            log.error("Error counting unread notifications for user={}", userId, e);
        }
        return 0;
    }

    public boolean create(int userId, String title, String message, String type, String link) {
        String sql = "INSERT INTO notifications (user_id, title, message, type, link, is_read) VALUES (?, ?, ?, ?, ?, FALSE)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setString(4, type);
            ps.setString(5, link);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error creating notification for user={}", userId, e);
        }
        return false;
    }

    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error marking all notifications as read for user={}", userId, e);
        }
        return false;
    }
}
