package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import Context.DBContext;
import Model.Review;

public class ReviewDAO {

    // 1. Lấy danh sách đánh giá theo sản phẩm
    public List<Review> getReviewsByProductId(int productId) {
        List<Review> list = new ArrayList<>();
        
        // Cần JOIN với bảng Users để lấy tên người dùng (giả sử cột tên là 'fullname')
        // Nếu bảng Users của bạn cột tên là 'username' thì sửa 'u.fullname' thành 'u.username'
        String query = "SELECT r.*, u.fullname FROM Reviews r " +
                       "JOIN Users u ON r.user_id = u.id " +
                       "WHERE r.product_id = ? ORDER BY r.created_at DESC";
                       
        try {
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                // Tạo đối tượng Review khớp với Constructor của bạn
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
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Thêm đánh giá mới
    public void addReview(Review review) {
        // Chỉ insert các trường cần thiết, id tự tăng, created_at tự động
        String query = "INSERT INTO Reviews (product_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";
        try {
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, review.getProductId());
            ps.setInt(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            
            ps.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}