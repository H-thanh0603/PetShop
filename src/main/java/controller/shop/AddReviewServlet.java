package controller.shop;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.ReviewDAO;
import Model.Review;
import Model.User;
import Util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/add-review")
public class AddReviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(AddReviewServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            // 1. Kiểm tra đăng nhập
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // 2. Lấy dữ liệu từ Form
            int productId = Integer.parseInt(request.getParameter("productId"));
            int rating = Integer.parseInt(request.getParameter("rating"));
            String comment = request.getParameter("comment");
            if (comment != null) {
                comment = comment.trim();
            }
            
            // Sanitize: strip HTML tags
            comment = ValidationUtil.stripHtmlTags(comment);
            
            // Validate max length
            if (!ValidationUtil.validateMaxLength(comment, 1000)) {
                session.setAttribute("error", "Nội dung đánh giá không được vượt quá 1000 ký tự.");
                response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
                return;
            }

            ReviewDAO dao = new ReviewDAO();

            if (rating < 1 || rating > 5) {
                session.setAttribute("error", "Số sao đánh giá phải từ 1 đến 5.");
                response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
                return;
            }

            if (comment == null || comment.isBlank()) {
                session.setAttribute("error", "Vui lòng nhập nội dung đánh giá.");
                response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
                return;
            }

            if (!dao.hasUserPurchasedProduct(user.getId(), productId)) {
                session.setAttribute("reviewError", "Chỉ khách hàng đã mua và nhận sản phẩm mới có thể đánh giá.");
                response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
                return;
            }

            if (dao.hasUserReviewedProduct(user.getId(), productId)) {
                session.setAttribute("error", "Bạn đã đánh giá sản phẩm này rồi.");
                response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
                return;
            }

            // Rate limit: max 5 reviews per 60 minutes
            int reviewsInLastHour = dao.countReviewsByUserInLastHour(user.getId());
            if (reviewsInLastHour >= 5) {
                session.setAttribute("error", "Bạn đã gửi quá nhiều đánh giá. Vui lòng thử lại sau.");
                response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
                return;
            }

            // Duplicate detection: same comment in last 24 hours
            if (dao.hasDuplicateRecentComment(user.getId(), comment)) {
                session.setAttribute("error", "Nội dung đánh giá trùng lặp. Vui lòng viết đánh giá khác.");
                response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
                return;
            }

            Review review = new Review();
            review.setProductId(productId);
            review.setUserId(user.getId()); // Giả sử User model có hàm getId()
            review.setRating(rating);
            review.setComment(comment);
            
            // 4. Lưu vào DB
            if (dao.addReview(review)) {
                session.setAttribute("success", "Đánh giá của bạn đã được gửi thành công.");
            } else {
                session.setAttribute("error", "Không thể gửi đánh giá. Vui lòng thử lại.");
            }

            // 5. Quay lại trang chi tiết
            response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
            
        } catch (Exception e) {
            logger.error("Error submitting review for product id={}", request.getParameter("productId"), e);
            request.getSession().setAttribute("error", "Không thể gửi đánh giá.");
            response.sendRedirect(request.getContextPath() + "/home");
        }
    }
}
