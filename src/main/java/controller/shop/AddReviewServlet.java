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
import Model.User; // Import Model User của bạn

@WebServlet("/add-review")
public class AddReviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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

            // 3. Tạo đối tượng Review (Dùng Constructor rỗng rồi set để tránh lỗi)
            Review review = new Review();
            review.setProductId(productId);
            review.setUserId(user.getId()); // Giả sử User model có hàm getId()
            review.setRating(rating);
            review.setComment(comment);
            
            // 4. Lưu vào DB
            ReviewDAO dao = new ReviewDAO();
            dao.addReview(review);

            // 5. Quay lại trang chi tiết
            response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/home");
        }
    }
}