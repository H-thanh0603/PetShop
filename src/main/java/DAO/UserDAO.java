package DAO;

import java.sql.*;

import Context.DBContext;
import Model.FbAccount.Account;
import Model.User;

public class UserDAO {
    
    // Kiểm tra đăng nhập bằng username
    public User login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, username);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        rs.getString("role"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Kiểm tra đăng nhập bằng email
    public User loginByEmail(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, email);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        rs.getString("role"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Kiểm tra đăng nhập bằng email hoặc username
    public User loginByEmailOrUsername(String emailOrUsername, String password) {
        String query = "SELECT * FROM users WHERE (email = ? OR username = ?) AND password = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, emailOrUsername);
            ps.setString(2, emailOrUsername);
            ps.setString(3, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        rs.getString("role"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Kiểm tra username đã tồn tại
    public boolean checkUsernameExists(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Đăng ký user mới
    public boolean register(String username, String password, String fullname, String email) {
        String query = "INSERT INTO users (username, password, fullname, email, role) VALUES (?, ?, ?, ?, 'user')";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullname);
            ps.setString(4, email);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Đếm tổng số users
    public int countUsers() {
        String query = "SELECT COUNT(*) FROM users";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Lấy user theo ID
    public User getUserById(int id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        rs.getString("role"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Lấy email theo user ID
    public String getEmailByUserId(int userId) {
        User user = getUserById(userId);
        return user != null ? user.getEmail() : null;
    }
    
    // Lấy user theo email
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        rs.getString("role"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Kiểm tra email đã tồn tại
    public boolean checkEmailExists(String email) {
        return getUserByEmail(email) != null;
    }
    
    // Kiểm tra số điện thoại đã tồn tại
    public boolean checkPhoneExists(String phone) {
        String query = "SELECT 1 FROM users WHERE phone = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Cập nhật mật khẩu
    public boolean updatePassword(String email, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Lưu reset token
    public boolean saveResetToken(String email, String token) {
        String query = "UPDATE users SET reset_token = ?, reset_token_expiry = ? WHERE email = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, token);
            // Token hết hạn sau 30 phút
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000));
            ps.setString(3, email);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Kiểm tra reset token hợp lệ
    public User getUserByResetToken(String token) {
        String query = "SELECT * FROM users WHERE reset_token = ? AND reset_token_expiry > ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, token);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        rs.getString("role"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Xóa reset token sau khi đổi mật khẩu
    public boolean clearResetToken(String email) {
        String query = "UPDATE users SET reset_token = NULL, reset_token_expiry = NULL WHERE email = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, email);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Đăng ký user mới (chỉ với email, không cần username/password)
//    public User registerWithEmail(String email, String fullname) {
//        // Tạo username từ email
//        String username = email.split("@")[0] + "_" + System.currentTimeMillis() % 10000;
//        String query = "INSERT INTO users (username, password, fullname, email, role) VALUES (?, '', ?, ?, 'user')";
//        try (Connection conn = new DBContext().getConnection();
//             PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
//
//            ps.setString(1, username);
//            ps.setString(2, fullname);
//            ps.setString(3, email);
//
//            if (ps.executeUpdate() > 0) {
//                try (ResultSet rs = ps.getGeneratedKeys()) {
//                    if (rs.next()) {
//                        return new User(rs.getInt(1), username, "", fullname, email, "user");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
    
    // ========== ADMIN FUNCTIONS ==========
    
    // Lấy tất cả users
    public java.util.List<User> getAllUsers() {
        java.util.List<User> list = new java.util.ArrayList<>();
        String query = "SELECT * FROM users ORDER BY id DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("fullname"),
                    rs.getString("email"),
                    rs.getString("role"),
                        rs.getString("phone"),
                        rs.getString("address")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Lấy users theo role
    public java.util.List<User> getUsersByRole(String role) {
        java.util.List<User> list = new java.util.ArrayList<>();
        String query = "SELECT * FROM users WHERE role = ? ORDER BY id DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        rs.getString("role"),
                            rs.getString("phone"),
                            rs.getString("address")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Cập nhật role user
    public boolean updateUserRole(int userId, String role) {
        String query = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, role);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Xóa user
    public boolean deleteUser(int userId) {
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Đếm users theo role
    public int countUsersByRole(String role) {
        String query = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Lấy user đầy đủ thông tin theo ID
    public User getUserFullById(int id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setFullname(rs.getString("fullname"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    
                    // Kiểm tra cột phone và address có tồn tại không
                    try {
                        user.setPhone(rs.getString("phone"));
                    } catch (Exception e) {
                        user.setPhone(null);
                    }
                    try {
                        user.setAddress(rs.getString("address"));
                    } catch (Exception e) {
                        user.setAddress(null);
                    }
                    
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Cập nhật thông tin user
    public boolean updateUser(int userId, String fullname, String email, String phone, String address) {
        String query = "UPDATE users SET fullname = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, fullname);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setInt(5, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Lấy tất cả users với thống kê
    public java.util.List<User> getAllUsersWithStats() {
        java.util.List<User> list = new java.util.ArrayList<>();
        String query = "SELECT u.*, " +
                       "(SELECT COUNT(*) FROM orders WHERE user_id = u.id) as order_count, " +
                       "(SELECT SUM(total_amount) FROM orders WHERE user_id = u.id AND status != 'Cancelled') as total_spent " +
                       "FROM users u ORDER BY u.id DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullname(rs.getString("fullname"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                
                try { user.setPhone(rs.getString("phone")); } catch (Exception e) {}
                try { user.setAddress(rs.getString("address")); } catch (Exception e) {}
                try { user.setStatus(rs.getBoolean("status")); } catch (Exception e) { user.setStatus(true); }
                try { user.setOrderCount(rs.getInt("order_count")); } catch (Exception e) {}
                try { user.setTotalSpent(rs.getDouble("total_spent")); } catch (Exception e) {}
                
                list.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Tìm kiếm users
    public java.util.List<User> searchUsers(String keyword, String role) {
        java.util.List<User> list = new java.util.ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT u.*, " +
            "(SELECT COUNT(*) FROM orders WHERE user_id = u.id) as order_count, " +
            "(SELECT SUM(total_amount) FROM orders WHERE user_id = u.id AND status != 0) as total_spent " +
            "FROM users u WHERE 1=1 ");
        
        if (keyword != null && !keyword.isEmpty()) {
            query.append("AND (u.fullname LIKE ? OR u.email LIKE ? OR u.phone LIKE ? OR u.username LIKE ?) ");
        }
        if (role != null && !role.isEmpty()) {
            query.append("AND u.role = ? ");
        }
        query.append("ORDER BY u.id DESC");
        
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {
            
            int paramIndex = 1;
            if (keyword != null && !keyword.isEmpty()) {
                String searchPattern = "%" + keyword + "%";
                ps.setString(paramIndex++, searchPattern);
                ps.setString(paramIndex++, searchPattern);
                ps.setString(paramIndex++, searchPattern);
                ps.setString(paramIndex++, searchPattern);
            }
            if (role != null && !role.isEmpty()) {
                ps.setString(paramIndex++, role);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullname(rs.getString("fullname"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    try { user.setPhone(rs.getString("phone")); } catch (Exception e) {}
                    try { user.setStatus(rs.getBoolean("status")); } catch (Exception e) { user.setStatus(true); }
                    try { user.setOrderCount(rs.getInt("order_count")); } catch (Exception e) {}
                    try { user.setTotalSpent(rs.getDouble("total_spent")); } catch (Exception e) {}
                    list.add(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Đếm user mới trong tuần
    public int countNewUsersThisWeek() {
        String query = "SELECT COUNT(*) FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Cập nhật trạng thái user (khóa/mở)
    public boolean updateUserStatus(int userId, String status) {
        String query = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Reset mật khẩu
    public boolean resetUserPassword(int userId, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Thêm user mới (admin tạo)
    public boolean addUser(String username, String password, String fullname, String email, String phone, String role) {
        String query = "INSERT INTO users (username, password, fullname, email, phone, role, status) VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullname);
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.setString(6, role);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean HaveEmail(String email){
        String query = "SELECT count(*) FROM users WHERE email = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count == 0) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public void insertUser(String name, String email) {
        String query = "insert into users (username, email, role, status, password) VALUES (?, ?, ?, 1, ?)";
        try(Connection connection = new DBContext().getConnection();
        PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, "user");
            ps.setString(4, "null");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateProfile(int id, String fullname, String phone){
        String sql = "UPDATE users SET fullname=?, phone=? WHERE id=?";
        try(Connection con = DBContext.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, fullname);
            ps.setString(2, phone);
            ps.setInt(3, id);
            ps.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void updateAddress(int userId, String address) {
        String sql = "UPDATE users SET address = ? WHERE id = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, address);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}