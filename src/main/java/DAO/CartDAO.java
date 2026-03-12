package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import Context.DBContext;
import Model.CartItem;
import Model.Product;

public class CartDAO {
    
    private ProductDAO productDAO = new ProductDAO();
    
    // Lưu hoặc cập nhật item trong giỏ hàng
    public void saveCartItem(int userId, int productId, int quantity) {
        String query = "INSERT INTO Cart (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE quantity = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.setInt(4, quantity);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Cập nhật số lượng (cộng thêm)
    public void addToCart(int userId, int productId, int quantityToAdd) {
        // Kiểm tra xem đã có trong giỏ chưa
        String checkQuery = "SELECT quantity FROM Cart WHERE user_id = ? AND product_id = ?";
        String insertQuery = "INSERT INTO Cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        String updateQuery = "UPDATE Cart SET quantity = quantity + ? WHERE user_id = ? AND product_id = ?";
        
        try (Connection conn = new DBContext().getConnection()) {
            PreparedStatement checkPs = conn.prepareStatement(checkQuery);
            checkPs.setInt(1, userId);
            checkPs.setInt(2, productId);
            ResultSet rs = checkPs.executeQuery();
            
            if (rs.next()) {
                // Đã có, cập nhật số lượng
                PreparedStatement updatePs = conn.prepareStatement(updateQuery);
                updatePs.setInt(1, quantityToAdd);
                updatePs.setInt(2, userId);
                updatePs.setInt(3, productId);
                updatePs.executeUpdate();
            } else {
                // Chưa có, thêm mới
                PreparedStatement insertPs = conn.prepareStatement(insertQuery);
                insertPs.setInt(1, userId);
                insertPs.setInt(2, productId);
                insertPs.setInt(3, quantityToAdd);
                insertPs.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Xóa item khỏi giỏ hàng
    public void removeFromCart(int userId, int productId) {
        String query = "DELETE FROM Cart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Cập nhật số lượng cụ thể
    public void updateCartQuantity(int userId, int productId, int newQuantity) {
        String query = "UPDATE Cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Xóa toàn bộ giỏ hàng của user
    public void clearCart(int userId) {
        String query = "DELETE FROM Cart WHERE user_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Load giỏ hàng của user từ database
    public Map<Integer, CartItem> getCartByUserId(int userId) {
        Map<Integer, CartItem> cart = new HashMap<>();
        String query = "SELECT product_id, quantity FROM Cart WHERE user_id = ?";
        
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");
                
                Product product = productDAO.getProductById(productId);
                if (product != null) {
                    cart.put(productId, new CartItem(product, quantity));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cart;
    }
    
    // Đếm tổng số lượng sản phẩm trong giỏ
    public int getTotalQuantity(int userId) {
        String query = "SELECT SUM(quantity) as total FROM Cart WHERE user_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Sync giỏ hàng từ session vào database (khi user đăng nhập)
    public void syncCartFromSession(int userId, Map<Integer, CartItem> sessionCart) {
        if (sessionCart == null || sessionCart.isEmpty()) return;
        
        for (Map.Entry<Integer, CartItem> entry : sessionCart.entrySet()) {
            addToCart(userId, entry.getKey(), entry.getValue().getQuantity());
        }
    }
}
