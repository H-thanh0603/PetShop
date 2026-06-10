package DAO;

import Context.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class ChatDAO {

    // Lưu tin nhắn vào DB
    public boolean saveMessage(int senderId, int receiverId, String message, boolean isAdmin) {
        String sql = "INSERT INTO chat_messages (sender_id, receiver_id, message, is_admin_sender) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, message);
            ps.setBoolean(4, isAdmin);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy lịch sử chat giữa user và admin
    public List<Map<String, Object>> getHistory(int userId, int adminId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages " +
                "WHERE (sender_id = ? AND receiver_id = ?) " +
                "   OR (sender_id = ? AND receiver_id = ?) " +
                "ORDER BY created_at ASC LIMIT 100";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, adminId);
            ps.setInt(3, adminId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("senderId", rs.getInt("sender_id"));
                    row.put("receiverId", rs.getInt("receiver_id"));
                    row.put("message", rs.getString("message"));
                    row.put("isAdmin", rs.getBoolean("is_admin_sender"));
                    row.put("createdAt", rs.getTimestamp("created_at").toString());
                    list.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy danh sách user đã nhắn tin (dành cho Admin)
    public List<Map<String, Object>> getUsersWithMessages() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT DISTINCT u.id, u.fullname, u.email, " +
                "(SELECT message FROM chat_messages " +
                " WHERE (sender_id = u.id OR receiver_id = u.id) " +
                " ORDER BY created_at DESC LIMIT 1) AS last_message, " +
                "(SELECT created_at FROM chat_messages " +
                " WHERE (sender_id = u.id OR receiver_id = u.id) " +
                " ORDER BY created_at DESC LIMIT 1) AS last_time " +
                "FROM users u " +
                "JOIN chat_messages cm ON (cm.sender_id = u.id OR cm.receiver_id = u.id) " +
                "WHERE u.role = 'user' " +
                "ORDER BY last_time DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("fullname", rs.getString("fullname"));
                row.put("email", rs.getString("email"));
                row.put("lastMessage", rs.getString("last_message"));
                row.put("lastTime", rs.getTimestamp("last_time") != null ?
                        rs.getTimestamp("last_time").toString() : "");
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}