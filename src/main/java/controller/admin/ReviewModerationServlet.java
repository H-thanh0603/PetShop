package controller.admin;

import java.io.IOException;
import java.util.List;

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

@WebServlet("/pages/admin/reviews")
public class ReviewModerationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check admin session
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        ReviewDAO dao = new ReviewDAO();
        List<Review> reviews;

        // Read optional maxRating filter
        String maxRatingStr = request.getParameter("maxRating");
        Integer maxRating = ValidationUtil.parseIntOrNull(maxRatingStr);
        int selectedMaxRating = 0;

        if (maxRating != null && maxRating >= 1 && maxRating <= 5) {
            reviews = dao.getReviewsByMaxRating(maxRating);
            selectedMaxRating = maxRating;
        } else {
            reviews = dao.getAllReviews();
        }

        // Calculate stats
        int totalReviews = reviews.size();
        int lowRatingCount = 0;
        for (Review r : reviews) {
            if (r.getRating() <= 2) {
                lowRatingCount++;
            }
        }

        request.setAttribute("reviews", reviews);
        request.setAttribute("totalReviews", totalReviews);
        request.setAttribute("lowRatingCount", lowRatingCount);
        request.setAttribute("selectedMaxRating", selectedMaxRating);

        request.getRequestDispatcher("/pages/admin/reviews.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // Check admin session
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        String message;
        String messageType = "success";

        if ("delete".equals(action)) {
            String reviewIdStr = request.getParameter("reviewId");
            Integer reviewId = ValidationUtil.parseIntOrNull(reviewIdStr);

            if (reviewId == null) {
                message = "Review không tồn tại hoặc đã bị xóa.";
                messageType = "error";
            } else {
                ReviewDAO dao = new ReviewDAO();
                if (dao.deleteReview(reviewId)) {
                    message = "Xóa review thành công!";
                } else {
                    message = "Review không tồn tại hoặc đã bị xóa.";
                    messageType = "error";
                }
            }
        } else {
            message = "Hành động không hợp lệ!";
            messageType = "error";
        }

        session.setAttribute("message", message);
        session.setAttribute("messageType", messageType);
        response.sendRedirect(request.getContextPath() + "/pages/admin/reviews");
    }
}
