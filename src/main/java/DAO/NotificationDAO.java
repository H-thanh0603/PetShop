package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import Context.DBContext;
import Model.Notification;

/**
 * DAO cho Push Notifications
 */
public class NotificationDAO {

    // Tạo notification mới
    public boolean createNotification(Notification notification) {
        String query = "INSERT INTO notifications (user_id, title, message, type, link) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getType());
            ps.setString(5, notification.getLink());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Lấy notifications của user (mới nhất trước)
    public List<Notification> getNotificationsByUserId(int userId, int limit) {
        List<Notification> list = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapNotification(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Đếm notifications chưa đọc
    public int countUnread(int userId) {
        String query = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // Đánh dấu đã đọc
    public boolean markAsRead(int notificationId, int userId) {
        String query = "UPDATE notifications SET is_read = 1 WHERE id = ? AND user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Đánh dấu tất cả đã đọc
    public boolean markAllAsRead(int userId) {
        String query = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Gửi notification cho tất cả users (broadcast)
    public void broadcastNotification(String title, String message, String type, String link) {
        String query = "INSERT INTO notifications (user_id, title, message, type, link) " +
                       "SELECT id, ?, ?, ?, ? FROM users WHERE role = 'user'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, title);
            ps.setString(2, message);
            ps.setString(3, type);
            ps.setString(4, link);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Notification mapNotification(ResultSet rs) throws Exception {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setLink(rs.getString("link"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    }
}
