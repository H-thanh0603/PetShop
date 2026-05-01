package DAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Context.DBContext;
import Model.CartItem;
import Model.OrderItem;
import Model.Product;

public class CartDAO {

    private static final Logger log = LoggerFactory.getLogger(CartDAO.class);

    private ProductDAO productDAO = new ProductDAO();
    
    // Lưu hoặc cập nhật item trong giỏ hàng
    public void saveCartItem(int userId, int productId, int quantity) {
        String query = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE quantity = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.setInt(4, quantity);
            ps.executeUpdate();
        } catch (Exception e) {
            log.error("DB error", e);
        }
    }
    
    // Cập nhật số lượng (cộng thêm)
    public void addToCart(int userId, int productId, int quantityToAdd) {
        Product product = productDAO.getProductById(productId);
        if (product == null || product.getStock() <= 0 || quantityToAdd <= 0) {
            return;
        }

        // Kiểm tra xem đã có trong giỏ chưa
        String checkQuery = "SELECT quantity FROM cart WHERE user_id = ? AND product_id = ?";
        String insertQuery = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        String updateQuery = "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        
        try (Connection conn = DBContext.getConnection()) {
            try (PreparedStatement checkPs = conn.prepareStatement(checkQuery)) {
                checkPs.setInt(1, userId);
                checkPs.setInt(2, productId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        int currentQuantity = rs.getInt("quantity");
                        int newQuantity = Math.min(currentQuantity + quantityToAdd, product.getStock());

                        // Đã có, cập nhật số lượng
                        try (PreparedStatement updatePs = conn.prepareStatement(updateQuery)) {
                            updatePs.setInt(1, newQuantity);
                            updatePs.setInt(2, userId);
                            updatePs.setInt(3, productId);
                            updatePs.executeUpdate();
                        }
                    } else {
                        int quantityToSave = Math.min(quantityToAdd, product.getStock());
                        if (quantityToSave <= 0) {
                            return;
                        }

                        // Chưa có, thêm mới
                        try (PreparedStatement insertPs = conn.prepareStatement(insertQuery)) {
                            insertPs.setInt(1, userId);
                            insertPs.setInt(2, productId);
                            insertPs.setInt(3, quantityToSave);
                            insertPs.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
    }
    
    // Xóa item khỏi giỏ hàng
    public void removeFromCart(int userId, int productId) {
        String query = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (Exception e) {
            log.error("DB error", e);
        }
    }
    
    // Cập nhật số lượng cụ thể
    public boolean updateCartQuantity(int userId, int productId, int newQuantity) {
        String query = "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            Product product = productDAO.getProductById(productId);
            if (product == null || product.getStock() <= 0) {
                return false;
            }

            // fix quantity < 1
            if (newQuantity < 1) {
                newQuantity = 1;
            }

            // Backstop ở tầng DAO để quantity trong DB không vượt stock hiện tại.
            if (newQuantity > product.getStock()) {
                newQuantity = product.getStock();
            }

            ps.setInt(1, newQuantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            log.error("DB error", e);
        }
        return false;
    }
    
    // Xóa toàn bộ giỏ hàng của user
    public void clearCart(int userId) {
        try (Connection conn = DBContext.getConnection()) {
            clearCart(conn, userId);
        } catch (Exception e) {
            log.error("DB error", e);
        }
    }

    public void clearCart(Connection conn, int userId) throws Exception {
        String query = "DELETE FROM cart WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void consumeCartItems(Connection conn, int userId, List<OrderItem> items) throws Exception {
        String selectQuery = "SELECT quantity FROM cart WHERE user_id = ? AND product_id = ?";
        String updateQuery = "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        String deleteQuery = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";

        try (PreparedStatement selectPs = conn.prepareStatement(selectQuery);
             PreparedStatement updatePs = conn.prepareStatement(updateQuery);
             PreparedStatement deletePs = conn.prepareStatement(deleteQuery)) {

            for (OrderItem item : items) {
                selectPs.setInt(1, userId);
                selectPs.setInt(2, item.getProductId());

                try (ResultSet rs = selectPs.executeQuery()) {
                    if (!rs.next()) {
                        continue;
                    }

                    int currentQuantity = rs.getInt("quantity");
                    int remainingQuantity = currentQuantity - item.getQuantity();

                    if (remainingQuantity > 0) {
                        updatePs.setInt(1, remainingQuantity);
                        updatePs.setInt(2, userId);
                        updatePs.setInt(3, item.getProductId());
                        updatePs.executeUpdate();
                    } else {
                        deletePs.setInt(1, userId);
                        deletePs.setInt(2, item.getProductId());
                        deletePs.executeUpdate();
                    }
                }
            }
        }
    }
    
    // Load giỏ hàng của user từ database
    public Map<Integer, CartItem> getCartByUserId(int userId) {
        Map<Integer, CartItem> cart = new HashMap<>();
        String selectQuery = "SELECT product_id, quantity FROM cart WHERE user_id = ?";
        String updateQuery = "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        String deleteQuery = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement selectPs = conn.prepareStatement(selectQuery);
             PreparedStatement updatePs = conn.prepareStatement(updateQuery);
             PreparedStatement deletePs = conn.prepareStatement(deleteQuery)) {
            selectPs.setInt(1, userId);

            try (ResultSet rs = selectPs.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    int quantity = rs.getInt("quantity");

                    Product product = productDAO.getProductById(conn, productId);
                    if (product == null || product.getStock() <= 0) {
                        deletePs.setInt(1, userId);
                        deletePs.setInt(2, productId);
                        deletePs.executeUpdate();
                        continue;
                    }

                    int safeQuantity = Math.max(1, Math.min(quantity, product.getStock()));
                    if (safeQuantity != quantity) {
                        updatePs.setInt(1, safeQuantity);
                        updatePs.setInt(2, userId);
                        updatePs.setInt(3, productId);
                        updatePs.executeUpdate();
                    }

                    cart.put(productId, new CartItem(product, safeQuantity));
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return cart;
    }
    
    // Đếm tổng số lượng sản phẩm trong giỏ
    public int getTotalQuantity(int userId) {
        String query = "SELECT SUM(quantity) as total FROM cart WHERE user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return 0;
    }
    
    // Sync giỏ hàng từ session vào database (khi user đăng nhập)
    public void syncCartFromSession(int userId, Map<Integer, CartItem> sessionCart) {
        if (sessionCart == null || sessionCart.isEmpty()) return;
        
        for (Map.Entry<Integer, CartItem> entry : sessionCart.entrySet()) {
            Product product = productDAO.getProductById(entry.getKey());
            if (product == null || product.getStock() <= 0) {
                continue;
            }

            // Khi sync từ session sau login, chỉ đẩy lên DB phần số lượng còn hợp lệ theo tồn kho hiện tại.
            int quantityToSync = Math.min(entry.getValue().getQuantity(), product.getStock());
            if (quantityToSync > 0) {
                addToCart(userId, entry.getKey(), quantityToSync);
            }
        }
    }
}

