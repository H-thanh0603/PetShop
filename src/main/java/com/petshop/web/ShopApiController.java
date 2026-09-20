package com.petshop.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import Context.DBContext;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import DAO.ProductDAO;
import DAO.ReviewDAO;
import Model.Order;
import Model.PaymentTransaction;
import Model.Product;
import Model.Review;
import Model.User;
import Util.ValidationUtil;
import Util.VnpayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Small shop endpoints: /api/search-autocomplete (JSON) and
 * /vnpay-return (VNPay landing). Checkout itself moves in phase 7b.
 */
@Controller
public class ShopApiController {

    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;
    private final PaymentTransactionDAO paymentTransactionDAO;
    private final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(ShopApiController.class);

    public ShopApiController() {
        this(new ProductDAO(), new OrderDAO(), new PaymentTransactionDAO());
    }

    ShopApiController(ProductDAO productDAO, OrderDAO orderDAO, PaymentTransactionDAO paymentTransactionDAO) {
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
        this.paymentTransactionDAO = paymentTransactionDAO;
    }

    @GetMapping(value = "/api/search-autocomplete", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String searchAutocomplete(@RequestParam(value = "q", required = false) String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "[]";
        }

        keyword = keyword.trim();
        keyword = keyword.substring(0, Math.min(keyword.length(), 100));

        // Giới hạn 8 kết quả cho autocomplete
        List<Product> products = productDAO.searchProductsLimit(keyword, 8);

        // Chuyển đổi sang JSON đơn giản (chỉ lấy id, name, image, price, stock)
        List<Map<String, Object>> results = new ArrayList<>();
        for (Product p : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("image", p.getImage() != null ? p.getImage() : "");
            item.put("price", p.getEffectivePrice());
            item.put("stock", p.getAvailablePurchaseQuantity());
            item.put("hasPromotion", p.hasPromotion());
            item.put("oldPrice", p.getOriginalPrice());
            results.add(item);
        }

        return gson.toJson(results);
    }

    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model, HttpSession session) {
        boolean validSignature = VnpayUtil.verifyReturn(request);

        String orderIdRaw = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        String providerTransactionId = request.getParameter("vnp_TransactionNo");
        BigDecimal amount = parseVnpayAmount(request.getParameter("vnp_Amount"));

        if (!validSignature || orderIdRaw == null) {
            model.addAttribute("paymentStatus", "failed");
            model.addAttribute("paymentMessage", "Giao dich khong hop le.");
            return "pages/shop/payment-failed";
        }

        int orderId = Integer.parseInt(orderIdRaw);
        Order order = orderDAO.getOrderById(orderId);
        if (order == null || amount == null || amount.setScale(2, RoundingMode.HALF_UP).compareTo(order.getTotalAmount().setScale(2, RoundingMode.HALF_UP)) != 0) {
            paymentTransactionDAO.updateLatestProviderResultForOrder(
                    orderId,
                    "VNPAY",
                    providerTransactionId,
                    amount,
                    buildProviderMetadata(request),
                    "FAILED",
                    "MISMATCH",
                    "VNPAY amount does not match order total."
            );
            model.addAttribute("orderId", orderId);
            model.addAttribute("paymentStatus", 0);
            model.addAttribute("paymentMessage", "So tien thanh toan VNPay khong khop don hang.");
            return "pages/shop/payment-failed";
        }

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            boolean recorded = paymentTransactionDAO.updateLatestProviderResultForOrder(
                    orderId,
                    "VNPAY",
                    providerTransactionId,
                    amount,
                    buildProviderMetadata(request),
                    "VERIFIED",
                    "VERIFIED",
                    "VNPAY payment verified."
            );
            if (!recorded) {
                try (Connection conn = DBContext.getConnection()) {
                    PaymentTransaction newTx = new PaymentTransaction();
                    newTx.setOrderId(orderId);
                    newTx.setProviderKey("VNPAY");
                    newTx.setAmount(amount);
                    newTx.setAmountReceived(amount);
                    newTx.setCurrency("VND");
                    newTx.setProviderTransactionId(providerTransactionId);
                    newTx.setProviderMetadata(buildProviderMetadata(request));
                    newTx.setStatus("VERIFIED");
                    newTx.setVerificationStatus("VERIFIED");
                    newTx.setVerificationMessage("VNPAY payment verified on return.");
                    newTx.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    newTx.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    paymentTransactionDAO.save(conn, newTx);
                } catch (Exception ex) {
                    logger.error("Unexpected error", ex);
                }
            }
            if (!orderDAO.markOnlinePaymentPaidAndFinalize(orderId, "VNPAY")) {
                model.addAttribute("paymentStatus", "failed");
                model.addAttribute("paymentMessage", "Khong cap nhat duoc trang thai don hang VNPay.");
                return "pages/shop/payment-failed";
            }

            session.setAttribute("paymentMethod", "VNPAY");
            session.setAttribute("paymentStatus", 1);
            session.setAttribute("successOrderId", orderId);

            return "redirect:/order-success";
        }
        paymentTransactionDAO.updateLatestProviderResultForOrder(
                orderId,
                "VNPAY",
                providerTransactionId,
                amount,
                buildProviderMetadata(request),
                "FAILED",
                "FAILED",
                "VNPAY payment was not completed."
        );
        orderDAO.markOnlinePaymentAwaiting(orderId, "VNPAY");

        model.addAttribute("orderId", orderId);
        model.addAttribute("paymentStatus", 0);
        model.addAttribute("paymentMessage", "Don hang dang cho thanh toan VNPay hoac ban da huy giao dich.");
        return "pages/shop/payment-failed";
    }

    private BigDecimal parseVnpayAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(rawAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildProviderMetadata(HttpServletRequest request) {
        return "responseCode=" + safe(request.getParameter("vnp_ResponseCode"))
                + ";transactionStatus=" + safe(request.getParameter("vnp_TransactionStatus"))
                + ";bankCode=" + safe(request.getParameter("vnp_BankCode"))
                + ";payDate=" + safe(request.getParameter("vnp_PayDate"));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @PostMapping("/add-review")
    public String addReview(
            @RequestParam(value = "productId", required = false) String productIdRaw,
            @RequestParam(value = "rating", required = false) String ratingRaw,
            @RequestParam(value = "comment", required = false) String commentRaw,
            HttpServletRequest request,
            HttpSession session) {
        // Check VNPay-style id parse errors + login first (same redirect targets)
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:" + request.getContextPath() + "/login";
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdRaw);
        } catch (Exception e) {
            session.setAttribute("error", "Không thể gửi đánh giá.");
            return "redirect:" + request.getContextPath() + "/home";
        }

        try {
            int rating = Integer.parseInt(ratingRaw);
            String comment = commentRaw == null ? null : commentRaw.trim();

            // Sanitize: strip HTML tags
            comment = ValidationUtil.stripHtmlTags(comment);

            // Validate max length
            if (!ValidationUtil.validateMaxLength(comment, 1000)) {
                session.setAttribute("error", "Nội dung đánh giá không được vượt quá 1000 ký tự.");
                return "redirect:" + request.getContextPath() + "/product-detail?id=" + productId;
            }

            ReviewDAO dao = new ReviewDAO();

            if (rating < 1 || rating > 5) {
                session.setAttribute("error", "Số sao đánh giá phải từ 1 đến 5.");
                return "redirect:" + request.getContextPath() + "/product-detail?id=" + productId;
            }

            if (comment == null || comment.isBlank()) {
                session.setAttribute("error", "Vui lòng nhập nội dung đánh giá.");
                return "redirect:" + request.getContextPath() + "/product-detail?id=" + productId;
            }

            if (!dao.hasUserPurchasedProduct(user.getId(), productId)) {
                session.setAttribute("reviewError", "Chỉ khách hàng đã mua và nhận sản phẩm mới có thể đánh giá.");
                return "redirect:" + request.getContextPath() + "/product-detail?id=" + productId;
            }

            if (dao.hasUserReviewedProduct(user.getId(), productId)) {
                session.setAttribute("error", "Bạn đã đánh giá sản phẩm này rồi.");
                return "redirect:" + request.getContextPath() + "/product-detail?id=" + productId;
            }

            // Rate limit: max 5 reviews per 60 minutes
            int reviewsInLastHour = dao.countReviewsByUserInLastHour(user.getId());
            if (reviewsInLastHour >= 5) {
                session.setAttribute("error", "Bạn đã gửi quá nhiều đánh giá. Vui lòng thử lại sau.");
                return "redirect:" + request.getContextPath() + "/product-detail?id=" + productId;
            }

            // Duplicate detection: same comment in last 24 hours
            if (dao.hasDuplicateRecentComment(user.getId(), comment)) {
                session.setAttribute("error", "Nội dung đánh giá trùng lặp. Vui lòng viết đánh giá khác.");
                return "redirect:" + request.getContextPath() + "/product-detail?id=" + productId;
            }

            Review review = new Review();
            review.setProductId(productId);
            review.setUserId(user.getId());
            review.setRating(rating);
            review.setComment(comment);

            // 4. Lưu vào DB
            if (dao.addReview(review)) {
                session.setAttribute("success", "Đánh giá của bạn đã được gửi thành công.");
            } else {
                session.setAttribute("error", "Không thể gửi đánh giá. Vui lòng thử lại.");
            }

            // 5. Quay lại trang chi tiết
            return "redirect:" + request.getContextPath() + "/product-detail?id=" + productId;

        } catch (Exception e) {
            LoggerFactory.getLogger(ShopApiController.class)
                    .error("Error submitting review for product id={}", productIdRaw, e);
            session.setAttribute("error", "Không thể gửi đánh giá.");
            return "redirect:" + request.getContextPath() + "/home";
        }
    }
}
