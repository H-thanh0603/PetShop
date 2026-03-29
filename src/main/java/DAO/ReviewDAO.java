package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import Context.DBContext;
import Model.Review;

public class ReviewDAO {

    // Lấy danh sách đánh giá theo sản phẩm
    public List<Review> getReviewsByProductId(int productId) {
        List<Review> list = new ArrayList<>();
        String query = "SELECT r.*, u.fullname FROM reviews r " +
                       "JOIN users u ON r.user_id = u.id " +
                       "WHERE r.product_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Thêm đánh giá mới
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
            e.printStackTrace();
        }
        return false;
    }

    // Kiểm tra user đã review sản phẩm chưa
    public boolean hasUserReviewed(int userId, int productId) {
        String query = "SELECT 1 FROM reviews WHERE user_id = ? AND product_id = ?";
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

    // Lấy rating trung bình của sản phẩm
    public double getAverageRating(int productId) {
        String query = "SELECT AVG(rating) as avg_rating FROM reviews WHERE product_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Math.round(rs.getDouble("avg_rating") * 10.0) / 10.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Đếm số review của sản phẩm
    public int countReviews(int productId) {
        String query = "SELECT COUNT(*) FROM reviews WHERE product_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Xóa review (admin hoặc chính user)
    public boolean deleteReview(int reviewId, int userId) {
        String query = "DELETE FROM reviews WHERE id = ? AND user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, reviewId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
