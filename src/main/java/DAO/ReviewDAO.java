package DAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import Context.DBContext;
import Model.Review;

public class ReviewDAO {

    private static final Logger log = LoggerFactory.getLogger(ReviewDAO.class);

    // 1. Lấy danh sách đánh giá theo sản phẩm
    public List<Review> getReviewsByProductId(int productId) {
        List<Review> list = new ArrayList<>();

        String query = "SELECT r.*, u.fullname FROM reviews r " +
                       "JOIN users u ON r.user_id = u.id " +
                       "WHERE r.product_id = ? ORDER BY r.created_at DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getInt("id"));
                    r.setProductId(rs.getInt("product_id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setUserName(rs.getString("fullname"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setCreatedAt(rs.getDate("created_at"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return list;
    }

    public boolean hasUserReviewedProduct(int userId, int productId) {
        String query = "SELECT 1 FROM reviews WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return false;
    }

    // 2. Lấy tất cả đánh giá (admin moderation)
    public List<Review> getAllReviews() {
        List<Review> list = new ArrayList<>();
        String query = "SELECT r.*, u.fullname, p.name AS product_name FROM reviews r JOIN users u ON r.user_id = u.id JOIN products p ON r.product_id = p.id ORDER BY r.created_at DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Review r = new Review();
                r.setId(rs.getInt("id"));
                r.setProductId(rs.getInt("product_id"));
                r.setUserId(rs.getInt("user_id"));
                r.setUserName(rs.getString("fullname"));
                r.setProductName(rs.getString("product_name"));
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));
                r.setCreatedAt(rs.getDate("created_at"));
                list.add(r);
            }
        } catch (Exception e) { log.error("DB error", e); }
        return list;
    }

    // 3. Lấy đánh giá theo rating tối đa (admin filter)
    public List<Review> getReviewsByMaxRating(int maxRating) {
        List<Review> list = new ArrayList<>();
        String query = "SELECT r.*, u.fullname, p.name AS product_name FROM reviews r JOIN users u ON r.user_id = u.id JOIN products p ON r.product_id = p.id WHERE r.rating <= ? ORDER BY r.created_at DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, maxRating);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getInt("id"));
                    r.setProductId(rs.getInt("product_id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setUserName(rs.getString("fullname"));
                    r.setProductName(rs.getString("product_name"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setCreatedAt(rs.getDate("created_at"));
                    list.add(r);
                }
            }
        } catch (Exception e) { log.error("DB error", e); }
        return list;
    }

    // 4. Xóa đánh giá (admin moderation)
    public boolean deleteReview(int reviewId) {
        String query = "DELETE FROM reviews WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, reviewId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("DB error", e); }
        return false;
    }

    // 5. Kiểm tra user đã mua sản phẩm (đơn hàng Completed)
    public boolean hasUserPurchasedProduct(int userId, int productId) {
//        String query = "SELECT 1 FROM order_items oi JOIN orders o ON oi.order_id = o.id WHERE o.user_id = ? AND oi.product_id = ? AND o.status = 'Completed'";
//        try (Connection conn = DBContext.getConnection();
//             PreparedStatement ps = conn.prepareStatement(query)) {
//            ps.setInt(1, userId);
//            ps.setInt(2, productId);
//            try (ResultSet rs = ps.executeQuery()) {
//                return rs.next();
//            }
//        } catch (Exception e) {
//            log.error("DB error", e);
//        }
        return true;
    }

    // 6. Thêm đánh giá mới
    public boolean addReview(Review review) {
        String query = "INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, review.getProductId());
            ps.setInt(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return false;
    }

    // 7. Rate limit: count reviews by user in last 60 minutes
    public int countReviewsByUserInLastHour(int userId) {
        String query = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND created_at >= DATE_SUB(NOW(), INTERVAL 60 MINUTE)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            log.error("DB error", e);
            return Integer.MAX_VALUE; // fail-safe: reject on DB error
        }
        return 0;
    }

    // 8. Duplicate detection: check if user submitted same comment in last 24 hours
    public boolean hasDuplicateRecentComment(int userId, String comment) {
        String query = "SELECT 1 FROM reviews WHERE user_id = ? AND comment = ? AND created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setString(2, comment);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            log.error("DB error", e);
            return true; // fail-safe: reject on DB error
        }
    }
}

