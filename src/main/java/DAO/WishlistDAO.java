package DAO;

import Context.DBContext;
import Model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WishlistDAO {

    private static final Logger logger = LoggerFactory.getLogger(WishlistDAO.class);

    public List<Product> getWishlistProductsByUserId(int userId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.* FROM wishlist w " +
                "JOIN products p ON w.product_id = p.id " +
                "WHERE w.user_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setImage(rs.getString("image"));
                    product.setStock(rs.getInt("stock"));

                    product.setWishlisted(true);
                    products.add(product);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching wishlist products for user id={}", userId, e);
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

    public boolean toggleWishlistAndReturnState(int userId, int productId) throws SQLException {
        String checkSql = "SELECT 1 FROM wishlist WHERE user_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO wishlist (user_id, product_id) VALUES (?, ?)";
        String deleteSql = "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DBContext.getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                boolean wasInWishlist;
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        wasInWishlist = rs.next();
                    }
                }

                String updateSql = wasInWishlist ? deleteSql : insertSql;
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, productId);
                    int affectedRows = ps.executeUpdate();
                    if (affectedRows != 1) {
                        throw new SQLException("Wishlist update affected " + affectedRows + " rows.");
                    }
                }

                conn.commit();
                return !wasInWishlist;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        }
    }
}
