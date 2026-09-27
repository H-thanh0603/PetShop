package com.petshop.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import DAO.AdminActionLogDAO;
import DAO.ReviewDAO;
import Model.Review;
import Model.User;
import com.petshop.util.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces ReviewModerationServlet (/pages/admin/reviews) 1:1 — same list
 * page with maxRating filter, same delete/refresh/reply flows.
 * AuthorizationFilter still guards all admin paths.
 */
@Controller
public class AdminReviewController {

    private static final Logger logger = LoggerFactory.getLogger(AdminReviewController.class);

    private final ReviewDAO reviewDAO;
    private final AdminActionLogDAO actionLog;

    public AdminReviewController() {
        this(new ReviewDAO(), new AdminActionLogDAO());
    }

    AdminReviewController(ReviewDAO reviewDAO, AdminActionLogDAO actionLog) {
        this.reviewDAO = reviewDAO;
        this.actionLog = actionLog;
    }

    @GetMapping("/pages/admin/reviews")
    public String reviews(
            @RequestParam(value = "maxRating", required = false) String maxRatingRaw,
            Model model) {
        List<Review> reviews;

        // Read optional maxRating filter
        Integer maxRating = ValidationUtil.parseIntOrNull(maxRatingRaw);
        int selectedMaxRating = 0;

        if (maxRating != null && maxRating >= 1 && maxRating <= 5) {
            reviews = reviewDAO.getReviewsByMaxRating(maxRating);
            selectedMaxRating = maxRating;
        } else {
            reviews = reviewDAO.getAllReviews();
        }

        // Calculate stats
        int totalReviews = reviews.size();
        int lowRatingCount = 0;
        for (Review r : reviews) {
            if (r.getRating() <= 2) {
                lowRatingCount++;
            }
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("lowRatingCount", lowRatingCount);
        model.addAttribute("selectedMaxRating", selectedMaxRating);

        return "pages/admin/reviews";
    }

    @PostMapping("/pages/admin/reviews")
    public String reviewsPost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "reviewId", required = false) String reviewIdRaw,
            @RequestParam(value = "status", required = false) String statusRaw,
            @RequestParam(value = "adminReply", required = false) String adminReply,
            HttpServletRequest request,
            HttpSession session) {
        User user = (User) session.getAttribute("user");

        String message = "";
        String messageType = "success";

        try {
            if ("delete".equals(action)) {
                Integer reviewId = ValidationUtil.parseIntOrNull(reviewIdRaw);

                if (reviewId == null) {
                    message = "Review không hợp lệ.";
                    messageType = "error";
                } else if (reviewDAO.deleteReview(reviewId)) {
                    actionLog.log(user.getId(), "DELETE_REVIEW", "review", reviewId, null);
                    message = "Xóa review thành công!";
                } else {
                    message = "Xóa review thất bại.";
                    messageType = "error";
                }

            } else if ("refresh".equals(action)) {
                Integer reviewId = ValidationUtil.parseIntOrNull(reviewIdRaw);
                Integer statusValue = ValidationUtil.parseIntOrNull(statusRaw);

                if (reviewId == null || statusValue == null || (statusValue != 0 && statusValue != 1)) {
                    message = "Dữ liệu cập nhật trạng thái không hợp lệ.";
                    messageType = "error";
                } else {
                    boolean status = statusValue == 1;

                    if (reviewDAO.updateReviewStatus(reviewId, status)) {
                        actionLog.log(user.getId(), "UPDATE_REVIEW_STATUS", "review", reviewId, null);
                        message = "Cập nhật trạng thái review thành công!";
                    } else {
                        message = "Cập nhật trạng thái review thất bại.";
                        messageType = "error";
                    }
                }

            } else if ("reply".equals(action)) {
                Integer reviewId = ValidationUtil.parseIntOrNull(reviewIdRaw);

                if (reviewId == null || adminReply == null || adminReply.trim().isEmpty()) {
                    message = "Nội dung trả lời không hợp lệ.";
                    messageType = "error";
                } else {
                    if (reviewDAO.replyReview(reviewId, adminReply.trim())) {
                        actionLog.log(user.getId(), "REPLY_REVIEW", "review", reviewId, null);
                        message = "Trả lời review thành công!";
                    } else {
                        message = "Trả lời review thất bại.";
                        messageType = "error";
                    }
                }
            } else {
                message = "Hành động không hợp lệ.";
                messageType = "error";
            }

        } catch (Exception e) {
            logger.error("Unexpected error", e);
            message = "Có lỗi xảy ra khi xử lý review.";
            messageType = "error";
        }

        session.setAttribute("message", message);
        session.setAttribute("messageType", messageType);

        return "redirect:" + request.getContextPath() + "/pages/admin/reviews";
    }
}
