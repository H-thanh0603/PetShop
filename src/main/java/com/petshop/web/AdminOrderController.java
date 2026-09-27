package com.petshop.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petshop.repository.AdminActionLogRepository;
import com.petshop.repository.NotificationRepository;
import com.petshop.dao.OrderDAO;
import com.petshop.model.Order;
import com.petshop.model.OrderLog;
import com.petshop.model.OrderStatusHistory;
import com.petshop.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import services.ShippingService;
import tools.jackson.databind.JsonNode;

/**
 * Replaces ManageOrderServlet (/admin/orders) 1:1 — same list/view pages,
 * same updateStatus/pushToGhn/syncGhnStatus/updatePaymentVerification flows,
 * same session flash messages and redirects. AuthorizationFilter still guards
 * all admin paths.
 */
@Controller
public class AdminOrderController {

    private final AdminActionLogRepository actionLog;
    private final OrderDAO orderDAO;
    private final ShippingService shippingService;
    private final NotificationRepository notificationDAO;

    @Autowired
    public AdminOrderController(AdminActionLogRepository actionLog, OrderDAO orderDAO,
                                ShippingService shippingService, NotificationRepository notificationDAO) {
        this.actionLog = actionLog;
        this.orderDAO = orderDAO;
        this.shippingService = shippingService;
        this.notificationDAO = notificationDAO;
    }


    @GetMapping("/admin/orders")
    public String orders(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false) String pageRaw,
            @RequestParam(value = "size", required = false) String sizeRaw,
            @RequestParam(value = "id", required = false) String idRaw,
            Model model,
            HttpServletRequest request,
            HttpSession session) {
        if ("view".equals(action)) {
            int orderId;
            try {
                orderId = Integer.parseInt(idRaw);
            } catch (NumberFormatException e) {
                session.setAttribute("message", "Mã đơn hàng không hợp lệ.");
                session.setAttribute("messageType", "error");
                return "redirect:" + request.getContextPath() + "/admin/orders";
            }
            Order order = orderDAO.getOrderById(orderId);
            List<OrderStatusHistory> statusHistory = orderDAO.getStatusHistory(orderId);
            List<OrderLog> orderLogs = orderDAO.getOrderLogs(orderId);
            model.addAttribute("order", order);
            model.addAttribute("statusHistory", statusHistory);
            model.addAttribute("orderLogs", orderLogs);
            return "pages/admin/order-detail";
        }

        // Pagination
        int page = 1;
        int size = 20;
        try { page = Math.max(1, Integer.parseInt(pageRaw)); } catch (Exception ignored) {}
        try { size = Math.max(1, Integer.parseInt(sizeRaw)); } catch (Exception ignored) {}

        List<Order> list = orderDAO.getOrdersPage(page, size, status, keyword);
        int totalOrders = orderDAO.countOrders(status, keyword);
        int totalPages = (int) Math.ceil((double) totalOrders / size);

        model.addAttribute("orders", list);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pendingPaymentReviewCount", orderDAO.countOrdersAwaitingPaymentVerification());
        return "pages/admin/orders";
    }

    @PostMapping("/admin/orders")
    public String ordersPost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "orderId", required = false) String orderIdRaw,
            @RequestParam(value = "status", required = false) String newStatus,
            @RequestParam(value = "verificationStatus", required = false) String verificationStatus,
            @RequestParam(value = "verificationMessage", required = false) String verificationMessage,
            @RequestParam(value = "returnTo", required = false) String returnTo,
            HttpServletRequest request,
            HttpSession session) {
        User admin = (User) session.getAttribute("user");
        int adminId = admin != null ? admin.getId() : 1;
        String adminRole = admin != null ? admin.getRole() : "";

        if ("updateStatus".equals(action)) {
            int orderId;
            try {
                orderId = Integer.parseInt(orderIdRaw);
            } catch (NumberFormatException e) {
                session.setAttribute("message", "Mã đơn hàng không hợp lệ.");
                session.setAttribute("messageType", "error");
                return "redirect:" + request.getContextPath() + "/admin/orders";
            }

            // Role-based validation for shippers
            if ("shipper".equals(adminRole)) {
                if (!"Shipping".equals(newStatus) && !"Delivered".equals(newStatus)) {
                    session.setAttribute("message", "Shipper chỉ có thể cập nhật trạng thái là 'Đang giao' hoặc 'Đã giao hàng'.");
                    session.setAttribute("messageType", "error");
                    return "redirect:" + request.getContextPath() + "/admin/orders";
                }
            }

            // Get old status for logging
            Order existing = orderDAO.getOrderById(orderId);
            String oldStatus = existing != null ? existing.getStatus() : "Unknown";
            if (orderDAO.updateStatus(orderId, newStatus, adminId)) {
                actionLog.log(adminId, "UPDATE_ORDER_STATUS", "order", orderId,
                        "Status changed from " + oldStatus + " to " + newStatus);

                // Send notification to user
                if (existing != null) {
                    notificationDAO.create(
                        existing.getUserId(),
                        "Cập nhật đơn hàng #" + orderId,
                        "Đơn hàng của bạn đã được chuyển trạng thái sang '" + getStatusLabelVietnamese(newStatus) + "'.",
                        "order",
                        request.getContextPath() + "/my-orders?action=view&id=" + orderId
                    );
                }

                session.setAttribute("message", "Cập nhật trạng thái đơn hàng thành công!");
                session.setAttribute("messageType", "success");
            } else {
                session.setAttribute("message", "Cập nhật trạng thái thất bại!");
                session.setAttribute("messageType", "error");
            }
        }

        if ("pushToGhn".equals(action)) {
            int orderId;
            try {
                orderId = Integer.parseInt(orderIdRaw);
            } catch (NumberFormatException e) {
                session.setAttribute("message", "Mã đơn hàng không hợp lệ.");
                session.setAttribute("messageType", "error");
                return "redirect:" + request.getContextPath() + "/admin/orders";
            }

            Order order = orderDAO.getOrderById(orderId);
            if (order == null) {
                session.setAttribute("message", "Không tìm thấy đơn hàng.");
                session.setAttribute("messageType", "error");
                return "redirect:" + request.getContextPath() + "/admin/orders";
            }

            if (order.getGhnOrderId() != null) {
                session.setAttribute("message", "Đơn hàng đã được đẩy lên GHN rồi.");
                session.setAttribute("messageType", "warning");
            } else {
                try {
                    JsonNode ghnResult = shippingService.createGhnOrder(order);
                    String ghnOrderId = ghnResult.get("order_code") != null
                            ? ghnResult.path("order_code").asString() : "";
                    String ghnTrackingCode = ghnResult.get("sort_code") != null
                            ? ghnResult.path("sort_code").asString() : "";
                    String ghnStatus = "picking";

                    orderDAO.updateGhnInfo(orderId, ghnOrderId, ghnTrackingCode, ghnStatus, null);
                    actionLog.log(adminId, "PUSH_TO_GHN", "order", orderId,
                            "Pushed to GHN: order_code=" + ghnOrderId);

                    session.setAttribute("message", "Đẩy đơn hàng lên GHN thành công! Mã GHN: " + ghnOrderId);
                    session.setAttribute("messageType", "success");
                } catch (Exception ex) {
                    orderDAO.updateGhnInfo(orderId, null, null, null, ex.getMessage());
                    actionLog.log(adminId, "PUSH_TO_GHN_FAILED", "order", orderId,
                            "Failed: " + ex.getMessage());
                    session.setAttribute("message", "Đẩy lên GHN thất bại: " + ex.getMessage());
                    session.setAttribute("messageType", "error");
                }
            }
        }

        if ("syncGhnStatus".equals(action)) {
            int orderId;
            try {
                orderId = Integer.parseInt(orderIdRaw);
            } catch (NumberFormatException e) {
                session.setAttribute("message", "Mã đơn hàng không hợp lệ.");
                session.setAttribute("messageType", "error");
                return "redirect:" + request.getContextPath() + "/admin/orders";
            }

            Order order = orderDAO.getOrderById(orderId);
            if (order == null || order.getGhnOrderId() == null) {
                session.setAttribute("message", "Đơn hàng chưa được đẩy lên GHN.");
                session.setAttribute("messageType", "warning");
            } else {
                try {
                    String ghnStatus = shippingService.syncGhnStatus(order.getGhnOrderId());
                    String localStatus = ShippingService.mapGhnStatusToLocal(ghnStatus);

                    orderDAO.updateGhnStatus(orderId, ghnStatus, null);

                    if (localStatus != null && !localStatus.equals(order.getStatus())) {
                        orderDAO.updateStatus(orderId, localStatus, adminId);
                        actionLog.log(adminId, "SYNC_GHN_STATUS", "order", orderId,
                                "GHN status: " + ghnStatus + " -> local: " + localStatus);
                    } else {
                        actionLog.log(adminId, "SYNC_GHN_STATUS", "order", orderId,
                                "GHN status: " + ghnStatus + " (no local change)");
                    }

                    session.setAttribute("message", "Đồng bộ GHN thành công! Trạng thái: " + ghnStatus);
                    session.setAttribute("messageType", "success");
                } catch (Exception ex) {
                    orderDAO.updateGhnInfo(orderId, null, null, null, ex.getMessage());
                    session.setAttribute("message", "Đồng bộ GHN thất bại: " + ex.getMessage());
                    session.setAttribute("messageType", "error");
                }
            }
        }

        if ("updatePaymentVerification".equals(action)) {
            int orderId;
            try {
                orderId = Integer.parseInt(orderIdRaw);
            } catch (NumberFormatException e) {
                session.setAttribute("message", "Mã đơn hàng không hợp lệ.");
                session.setAttribute("messageType", "error");
                return "redirect:" + request.getContextPath() + "/admin/orders";
            }

            if (orderDAO.updatePaymentVerification(orderId, verificationStatus, verificationMessage)) {
                actionLog.log(adminId, "UPDATE_PAYMENT_VERIFICATION", "order", orderId,
                        "Payment verification updated to " + verificationStatus);
                session.setAttribute("message", "Đã cập nhật trạng thái đối soát thanh toán.");
                session.setAttribute("messageType", "success");
            } else {
                session.setAttribute("message", "Không thể cập nhật trạng thái đối soát thanh toán.");
                session.setAttribute("messageType", "error");
            }
        }

        if ("detail".equalsIgnoreCase(returnTo)) {
            return "redirect:" + request.getContextPath() + "/admin/orders?action=view&id=" + orderIdRaw;
        }
        return "redirect:" + request.getContextPath() + "/admin/orders";
    }

    private String getStatusLabelVietnamese(String status) {
        if ("Pending".equals(status)) return "Chờ xử lý";
        if ("Confirmed".equals(status)) return "Đã xác nhận";
        if ("Paid".equals(status)) return "Đã thanh toán";
        if ("Shipping".equals(status)) return "Đang giao";
        if ("Delivered".equals(status)) return "Đã giao hàng";
        if ("Completed".equals(status)) return "Hoàn thành";
        if ("Cancelled".equals(status)) return "Đã hủy";
        return status;
    }
}
