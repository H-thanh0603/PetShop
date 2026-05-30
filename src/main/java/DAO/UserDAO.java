package DAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.UUID;

import Context.DBContext;
import Model.User;
import Util.PasswordUtil;

public class UserDAO {

    private static final Logger log = LoggerFactory.getLogger(UserDAO.class);

    // Helper: map ResultSet to User (dùng constructor 8 tham số)
    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("fullname"),
            rs.getString("email"),
            rs.getString("role"),
            rs.getString("phone"),
            rs.getString("address")
        );
        try { user.setCreatedAt(rs.getTimestamp("created_at")); } catch (Exception e) {}
        try { user.setStatus(rs.getBoolean("status")); } catch (Exception e) { user.setStatus(true); }
        try { user.setDiscountUsed(rs.getBoolean("has_used_discount")); } catch (Exception e) { user.setDiscountUsed(false); }
        try { user.setFailedLoginAttempts(rs.getInt("failed_login_attempts")); } catch (Exception e) {}
        try { user.setLockedUntil(rs.getTimestamp("locked_until")); } catch (Exception e) {}
        return user;
    }
    
    // Kiểm tra đăng nhập bằng username (hỗ trợ BCrypt)
    public User login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (PasswordUtil.verifyPassword(password, storedPassword)) {
                        return mapUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return null;
    }
    
    // Kiểm tra đăng nhập bằng email (hỗ trợ BCrypt)
    public User loginByEmail(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (PasswordUtil.verifyPassword(password, storedPassword)) {
                        return mapUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return null;
    }
    
    // Kiểm tra đăng nhập bằng email hoặc username (hỗ trợ BCrypt)
    public User loginByEmailOrUsername(String emailOrUsername, String password) {
        String query = "SELECT * FROM users WHERE email = ? OR username = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, emailOrUsername);
            ps.setString(2, emailOrUsername);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (PasswordUtil.verifyPassword(password, storedPassword)) {
                        return mapUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return null;
    }
    
    public boolean checkUsernameExists(String username) {
        String query = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    // Đăng ký user mới (hash password với BCrypt)
    public boolean register(String username, String password, String fullname, String email) {
        String hashedPassword = PasswordUtil.hashPassword(password);
        String query = "INSERT INTO users (username, password, fullname, email, role) VALUES (?, ?, ?, ?, 'user')";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.setString(3, fullname);
            ps.setString(4, email);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    public int countUsers() {
        String query = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { log.error("DB error", e); }
        return 0;
    }
    
    public User getUserById(int id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (Exception e) { log.error("DB error", e); }
        return null;
    }

    public boolean markDiscountAsUsed(Connection conn, int userId) throws SQLException {
        String query = "UPDATE users SET has_used_discount = 1 WHERE id = ? AND has_used_discount = 0";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }
    
    public String getEmailByUserId(int userId) {
        User user = getUserById(userId);
        return user != null ? user.getEmail() : null;
    }
    
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (Exception e) { log.error("DB error", e); }
        return null;
    }
    
    public boolean checkEmailExists(String email) { return getUserByEmail(email) != null; }
    
    public boolean checkPhoneExists(String phone) {
        String query = "SELECT 1 FROM users WHERE phone = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    // Cập nhật mật khẩu (hash với BCrypt)
    public boolean updatePassword(String email, String newPassword) {
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        String query = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    public boolean saveResetToken(String email, String token) {
        String query = "UPDATE users SET reset_token = ?, reset_token_expiry = ? WHERE email = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, token);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000));
            ps.setString(3, email);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    public User getUserByResetToken(String token) {
        String query = "SELECT * FROM users WHERE reset_token = ? AND reset_token_expiry > ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, token);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (Exception e) { log.error("DB error", e); }
        return null;
    }
    
    public boolean clearResetToken(String email) {
        String query = "UPDATE users SET reset_token = NULL, reset_token_expiry = NULL WHERE email = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    // ========== ADMIN FUNCTIONS ==========
    
    public java.util.List<User> getAllUsers() {
        java.util.List<User> list = new java.util.ArrayList<>();
        String query = "SELECT * FROM users ORDER BY id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapUser(rs)); }
        } catch (Exception e) { log.error("DB error", e); }
        return list;
    }
    
    public java.util.List<User> getUsersByRole(String role) {
        java.util.List<User> list = new java.util.ArrayList<>();
        String query = "SELECT * FROM users WHERE role = ? ORDER BY id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { list.add(mapUser(rs)); }
            }
        } catch (Exception e) { log.error("DB error", e); }
        return list;
    }
    
    public boolean updateUserRole(int userId, String role) {
        String query = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, role); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    public boolean deleteUser(int userId) {
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }

    public boolean deactivateUser(int userId) {
        String query = "UPDATE users SET status = 0 WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    public int countUsersByRole(String role) {
        String query = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception e) { log.error("DB error", e); }
        return 0;
    }
    
    public User getUserFullById(int id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    try { user.setCreatedAt(rs.getTimestamp("created_at")); } catch (Exception e) {}
                    return user;
                }
            }
        } catch (Exception e) { log.error("DB error", e); }
        return null;
    }
    
    public boolean updateUser(int userId, String fullname, String email, String phone, String address) {
        String query = "UPDATE users SET fullname = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, fullname); ps.setString(2, email);
            ps.setString(3, phone); ps.setString(4, address); ps.setInt(5, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    public java.util.List<User> getAllUsersWithStats() {
        java.util.List<User> list = new java.util.ArrayList<>();
        String query = "SELECT u.*, " +
                       "(SELECT COUNT(*) FROM orders WHERE user_id = u.id) as order_count, " +
                       "(SELECT SUM(total_amount) FROM orders WHERE user_id = u.id AND status != 'Cancelled') as total_spent " +
                       "FROM users u ORDER BY u.id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = mapUser(rs);
                try { user.setCreatedAt(rs.getTimestamp("created_at")); } catch (Exception e) {}
                try { user.setStatus(rs.getBoolean("status")); } catch (Exception e) { user.setStatus(true); }
                try { user.setOrderCount(rs.getInt("order_count")); } catch (Exception e) {}
                try { user.setTotalSpent(rs.getBigDecimal("total_spent")); } catch (Exception e) {}
                list.add(user);
            }
        } catch (Exception e) { log.error("DB error", e); }
        return list;
    }
    
    public java.util.List<User> searchUsers(String keyword, String role) {
        java.util.List<User> list = new java.util.ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT u.*, " +
            "(SELECT COUNT(*) FROM orders WHERE user_id = u.id) as order_count, " +
            "(SELECT SUM(total_amount) FROM orders WHERE user_id = u.id AND status != 'Cancelled') as total_spent " +
            "FROM users u WHERE 1=1 ");
        if (keyword != null && !keyword.isEmpty()) {
            query.append("AND (u.fullname LIKE ? OR u.email LIKE ? OR u.phone LIKE ? OR u.username LIKE ?) ");
        }
        if (role != null && !role.isEmpty()) { query.append("AND u.role = ? "); }
        query.append("ORDER BY u.id DESC");
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isEmpty()) {
                String p = "%" + keyword + "%";
                ps.setString(idx++, p); ps.setString(idx++, p); ps.setString(idx++, p); ps.setString(idx++, p);
            }
            if (role != null && !role.isEmpty()) { ps.setString(idx++, role); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id")); user.setUsername(rs.getString("username"));
                    user.setFullname(rs.getString("fullname")); user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    try { user.setCreatedAt(rs.getTimestamp("created_at")); } catch (Exception e) {}
                    try { user.setPhone(rs.getString("phone")); } catch (Exception e) {}
                    try { user.setStatus(rs.getBoolean("status")); } catch (Exception e) { user.setStatus(true); }
                    try { user.setDiscountUsed(rs.getBoolean("has_used_discount")); } catch (Exception e) { user.setDiscountUsed(false); }
                    try { user.setOrderCount(rs.getInt("order_count")); } catch (Exception e) {}
                    try { user.setTotalSpent(rs.getBigDecimal("total_spent")); } catch (Exception e) {}
                    list.add(user);
                }
            }
        } catch (Exception e) { log.error("DB error", e); }
        return list;
    }
    
    public int countNewUsersThisWeek() {
        String query = "SELECT COUNT(*) FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { log.error("DB error", e); }
        return 0;
    }
    
    public boolean updateUserStatus(int userId, String status) {
        String query = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, status); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    // Reset mật khẩu (hash với BCrypt)
    public boolean resetUserPassword(int userId, String newPassword) {
        if (!PasswordUtil.isStrongPassword(newPassword)) {
            return false;
        }
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        String query = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, hashedPassword); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    // Thêm user mới (admin tạo - hash password)
    public boolean addUser(String username, String password, String fullname, String email, String phone, String role) {
        if (!PasswordUtil.isStrongPassword(password)) {
            return false;
        }
        String hashedPassword = PasswordUtil.hashPassword(password);
        String query = "INSERT INTO users (username, password, fullname, email, phone, role, status) VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username); ps.setString(2, hashedPassword);
            ps.setString(3, fullname); ps.setString(4, email);
            ps.setString(5, phone); ps.setString(6, role);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    public boolean HaveEmail(String email) {
        String query = "SELECT count(*) FROM users WHERE email = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { return rs.getInt(1) == 0; }
            }
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }
    
    public void insertUser(String name, String email) {
        String query = "INSERT INTO users (username, email, fullname, role, status, password) VALUES (?, ?, ?, 'user', 1, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, buildUniqueUsername(conn, name, email));
            ps.setString(2, email);
            ps.setString(3, name);
            ps.setString(4, PasswordUtil.hashPassword(UUID.randomUUID().toString()));
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private String buildUniqueUsername(Connection conn, String name, String email) throws SQLException {
        String base = null;
        if (email != null && email.contains("@")) {
            base = email.substring(0, email.indexOf('@'));
        }
        if (base == null || base.isBlank()) {
            base = name != null ? name : "user";
        }

        base = base.toLowerCase()
                .replaceAll("[^a-z0-9_]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        if (base.isBlank()) {
            base = "user";
        }

        String candidate = base;
        int suffix = 1;
        while (checkUsernameExists(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    public void updateProfile(int id, String fullname, String phone) {
        String sql = "UPDATE users SET fullname=?, phone=? WHERE id=?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fullname); ps.setString(2, phone); ps.setInt(3, id);
            ps.executeUpdate();
        } catch (Exception e) { log.error("DB error", e); }
    }

    public boolean updateProfileAndEmail(int id, String fullname, String phone, String email) {
        String sql = "UPDATE users SET fullname=?, phone=?, email=? WHERE id=?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fullname);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return false;
    }

    public boolean isEmailTakenByAnotherUser(String email, int userId) {
        String sql = "SELECT 1 FROM users WHERE email = ? AND id <> ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return false;
    }

    public boolean isPhoneTakenByAnotherUser(String phone, int userId) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE phone = ? AND id <> ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return false;
    }
    
    // Migrate mật khẩu cũ sang BCrypt (chạy một lần)
    public void migratePasswordsToBCrypt() {
        String selectQuery = "SELECT id, password FROM users WHERE password NOT LIKE '$2%'";
        String updateQuery = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement selectPs = conn.prepareStatement(selectQuery);
             ResultSet rs = selectPs.executeQuery()) {
            while (rs.next()) {
                int userId = rs.getInt("id");
                String plainPassword = rs.getString("password");
                if (plainPassword == null || plainPassword.isEmpty() || plainPassword.equals("null")) continue;
                String hashedPassword = PasswordUtil.hashPassword(plainPassword);
                try (PreparedStatement updatePs = conn.prepareStatement(updateQuery)) {
                    updatePs.setString(1, hashedPassword); updatePs.setInt(2, userId);
                    updatePs.executeUpdate();
                }
            }
        } catch (Exception e) { log.error("DB error", e); }
    }
    
    // ========== BRUTE-FORCE PROTECTION ==========
    
    public int getFailedLoginAttempts(String emailOrUsername) {
        String query = "SELECT failed_login_attempts FROM users WHERE email = ? OR username = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, emailOrUsername); ps.setString(2, emailOrUsername);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception e) { log.error("DB error", e); }
        return 0;
    }

    public Timestamp getLockedUntil(String emailOrUsername) {
        String query = "SELECT locked_until FROM users WHERE email = ? OR username = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, emailOrUsername); ps.setString(2, emailOrUsername);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getTimestamp(1); }
        } catch (Exception e) { log.error("DB error", e); }
        return null;
    }

    public void incrementFailedAttempts(String emailOrUsername) {
        String query = "UPDATE users SET failed_login_attempts = failed_login_attempts + 1 WHERE email = ? OR username = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, emailOrUsername); ps.setString(2, emailOrUsername); ps.executeUpdate();
        } catch (Exception e) { log.error("DB error", e); }
    }

    public void lockAccount(String emailOrUsername, int lockoutMinutes) {
        String query = "UPDATE users SET locked_until = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE email = ? OR username = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, lockoutMinutes); ps.setString(2, emailOrUsername); ps.setString(3, emailOrUsername); ps.executeUpdate();
        } catch (Exception e) { log.error("DB error", e); }
    }

    public void resetFailedAttempts(String emailOrUsername) {
        String query = "UPDATE users SET failed_login_attempts = 0, locked_until = NULL WHERE email = ? OR username = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, emailOrUsername); ps.setString(2, emailOrUsername); ps.executeUpdate();
        } catch (Exception e) { log.error("DB error", e); }
    }

    public boolean isAccountLocked(String emailOrUsername) {
        Timestamp lockedUntil = getLockedUntil(emailOrUsername);
        return lockedUntil != null && lockedUntil.after(new Timestamp(System.currentTimeMillis()));
    }

    // ========== EMAIL VERIFICATION ==========

    public void saveVerificationToken(int userId, String token, Timestamp expiry) {
        String sql = "UPDATE users SET verification_token = ?, verification_token_expiry = ?, email_verified = FALSE WHERE id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token); ps.setTimestamp(2, expiry); ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (Exception e) { log.error("DB error", e); }
    }

    /** Returns the user if token is valid and not expired; null otherwise. */
    public User getUserByVerificationToken(String token) {
        String sql = "SELECT * FROM users WHERE verification_token = ? AND verification_token_expiry > NOW()";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapUser(rs); }
        } catch (Exception e) { log.error("DB error", e); }
        return null;
    }

    /** Returns the user if token exists but is expired; null otherwise. */
    public User getUserByExpiredVerificationToken(String token) {
        String sql = "SELECT * FROM users WHERE verification_token = ? AND verification_token_expiry <= NOW()";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapUser(rs); }
        } catch (Exception e) { log.error("DB error", e); }
        return null;
    }

    public boolean markEmailVerified(int userId) {
        String sql = "UPDATE users SET email_verified = TRUE, verification_token = NULL, verification_token_expiry = NULL WHERE id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId); return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }

    public boolean isEmailVerified(String email) {
        String sql = "SELECT email_verified FROM users WHERE email = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getBoolean(1); }
        } catch (Exception e) { log.error("DB error", e); }
        return false; // fail-safe: treat as unverified
    }
}

