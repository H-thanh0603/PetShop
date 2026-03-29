package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import Context.DBContext;
import Model.Product;

/**
 * DAO cho Wishlist - Sản phẩm yêu thích
 */
public class WishlistDAO {
    
    private ProductDAO productDAO = new ProductDAO();
    
    // Thêm sản phẩm vào wishlist
    public boolean addToWishlist(int userId, int productId) {
        String query = "INSERT IGNORE INTO wishlist (user_id, product_id) VALUES (?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Xóa sản phẩm khỏi wishlist
    public boolean removeFromWishlist(int userId, int productId) {
        String query = "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Kiểm tra sản phẩm có trong wishlist không
    public boolean isInWishlist(int userId, int productId) {
        String query = "SELECT 1 FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Lấy danh sách wishlist của user
    public List<Product> getWishlistByUserId(int userId) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT product_id FROM wishlist WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product product = productDAO.getProductById(rs.getInt("product_id"));
                if (product != null) {
                    list.add(product);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Đếm số sản phẩm trong wishlist
    public int countWishlist(int userId) {
        String query = "SELECT COUNT(*) FROM wishlist WHERE user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Xóa toàn bộ wishlist của user
    public boolean clearWishlist(int userId) {
        String query = "DELETE FROM wishlist WHERE user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
