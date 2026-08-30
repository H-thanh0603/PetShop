package controller.admin;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.ReviewDAO;
import DAO.AdminActionLogDAO;
import Model.Review;
import Model.User;
import Util.ValidationUtil;

public class ReviewModerationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        String action = request.getParameter("action");
        String message = "";
        String messageType = "success";

        ReviewDAO reviewDAO = new ReviewDAO();

        try {
            if ("delete".equals(action)) {

                Integer reviewId = ValidationUtil.parseIntOrNull(request.getParameter("reviewId"));

                if (reviewId == null) {
                    message = "Review không hợp lệ.";
                    messageType = "error";
                } else if (reviewDAO.deleteReview(reviewId)) {
                    new AdminActionLogDAO().log(user.getId(), "DELETE_REVIEW", "review", reviewId, null);
                    message = "Xóa review thành công!";
                } else {
                    message = "Xóa review thất bại.";
                    messageType = "error";
                }

            } else if ("refresh".equals(action)) {

                Integer reviewId = ValidationUtil.parseIntOrNull(request.getParameter("reviewId"));
                Integer statusValue = ValidationUtil.parseIntOrNull(request.getParameter("status"));

                if (reviewId == null || statusValue == null || (statusValue != 0 && statusValue != 1)) {
                    message = "Dữ liệu cập nhật trạng thái không hợp lệ.";
                    messageType = "error";
                } else {
                    boolean status = statusValue == 1;

                    if (reviewDAO.updateReviewStatus(reviewId, status)) {
                        new AdminActionLogDAO().log(user.getId(), "UPDATE_REVIEW_STATUS", "review", reviewId, null);
                        message = "Cập nhật trạng thái review thành công!";
                    } else {
                        message = "Cập nhật trạng thái review thất bại.";
                        messageType = "error";
                    }
                }

            } else if ("reply".equals(action)) {
                Integer reviewId = ValidationUtil.parseIntOrNull(request.getParameter("reviewId"));
                String adminReply = request.getParameter("adminReply");

                if (reviewId == null || adminReply == null || adminReply.trim().isEmpty()) {
                    message = "Nội dung trả lời không hợp lệ.";
                    messageType = "error";
                } else {
                    ReviewDAO dao = new ReviewDAO();

                    if (dao.replyReview(reviewId, adminReply.trim())) {
                        new AdminActionLogDAO().log(user.getId(), "REPLY_REVIEW", "review", reviewId, null);
                        message = "Trả lời review thành công!";
                    } else {
                        message = "Trả lời review thất bại.";
                        messageType = "error";
                    }
                }
            }
            else {
                message = "Hành động không hợp lệ.";
                messageType = "error";
            }

        } catch (Exception e) {
            e.printStackTrace();
            message = "Có lỗi xảy ra khi xử lý review.";
            messageType = "error";
        }

        session.setAttribute("message", message);
        session.setAttribute("messageType", messageType);

        response.sendRedirect(request.getContextPath() + "/pages/admin/reviews");
    }
}
