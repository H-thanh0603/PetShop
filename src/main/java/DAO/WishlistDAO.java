package DAO;

import Context.DBContext;
import Model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WishlistDAO {

    private static final Logger logger = LoggerFactory.getLogger(WishlistDAO.class);

    public List<Product> getWishlistProductsByUserId(int userId) {
        List<Product> products = new ArrayList<>();
        ProductDAO productDAO = new ProductDAO();

        for (Integer productId : getWishlistProductIdsByUserId(userId)) {
            Product product = productDAO.getProductById(productId);
            if (product != null) {
                product.setWishlisted(true);
                products.add(product);
            }
        }
        return products;
    }

    public Set<Integer> getWishlistProductIdsByUserId(int userId) {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT product_id FROM wishlist WHERE user_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("product_id"));
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching wishlist product ids for user id={}", userId, e);
        }

        return ids;
    }

    public boolean isInWishlist(int userId, int productId) {
        String sql = "SELECT 1 FROM wishlist WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            logger.error("Error checking wishlist for user id={} product id={}", userId, productId, e);
        }

        return false;
    }

    public boolean addToWishlist(int userId, int productId) {
        String sql = "INSERT INTO wishlist (user_id, product_id) VALUES (?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            logger.error("Error adding to wishlist user id={} product id={}", userId, productId, e);
        }

        return false;
    }

    public boolean removeFromWishlist(int userId, int productId) {
        String sql = "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            logger.error("Error removing from wishlist user id={} product id={}", userId, productId, e);
        }

        return false;
    }

    public boolean toggleWishlist(int userId, int productId) {
        if (isInWishlist(userId, productId)) {
            return removeFromWishlist(userId, productId);
        }
        return addToWishlist(userId, productId);
    }
}
