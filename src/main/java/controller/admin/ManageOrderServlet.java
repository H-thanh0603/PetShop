package controller.admin;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import DAO.AdminActionLogDAO;
import DAO.OrderDAO;
import Model.Order;
import Model.OrderLog;
import Model.OrderStatusHistory;
import Model.User;
import services.ShippingService;
import com.google.gson.JsonObject;

@WebServlet("/admin/orders")
public class ManageOrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AdminActionLogDAO actionLog = new AdminActionLogDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private ShippingService shippingService = new ShippingService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");
        HttpSession session = request.getSession();
        
        if ("view".equals(action)) {
            int orderId;
            try {
                orderId = Integer.parseInt(request.getParameter("id"));
            } catch (NumberFormatException e) {
                session.setAttribute("message", "Mã đơn hàng không hợp lệ.");
                session.setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/admin/orders");
                return;
            }
            Order order = orderDAO.getOrderById(orderId);
            List<OrderStatusHistory> statusHistory = orderDAO.getStatusHistory(orderId);
            List<OrderLog> orderLogs = orderDAO.getOrderLogs(orderId);
            request.setAttribute("order", order);
            request.setAttribute("statusHistory", statusHistory);
            request.setAttribute("orderLogs", orderLogs);
            request.getRequestDispatcher("/pages/admin/order-detail.jsp").forward(request, response);
            return;
        }

        // Pagination
        int page = 1;
        int size = 20;
        try { page = Math.max(1, Integer.parseInt(request.getParameter("page"))); } catch (Exception ignored) {}
        try { size = Math.max(1, Integer.parseInt(request.getParameter("size"))); } catch (Exception ignored) {}

        List<Order> list = orderDAO.getOrdersPage(page, size, status, keyword);
        int totalOrders = orderDAO.countOrders(status, keyword);
        int totalPages = (int) Math.ceil((double) totalOrders / size);

        request.setAttribute("orders", list);
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("keyword", keyword);
        request.setAttribute("pendingPaymentReviewCount", orderDAO.countOrdersAwaitingPaymentVerification());
        request.getRequestDispatcher("/pages/admin/orders.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        User admin = (User) session.getAttribute("user");
        int adminId = admin != null ? admin.getId() : 1;
        String adminRole = admin != null ? admin.getRole() : "";

        if ("updateStatus".equals(action)) {
            int orderId;
            try {
                orderId = Integer.parseInt(request.getParameter("orderId"));
            } catch (NumberFormatException e) {
                session.setAttribute("message", "Mã đơn hàng không hợp lệ.");
                session.setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/admin/orders");
                return;
            }
            String newStatus = request.getParameter("status");

            // Role-based validation for shippers
            if ("shipper".equals(adminRole)) {
                if (!"Shipping".equals(newStatus) && !"Delivered".equals(newStatus)) {
                    session.setAttribute("message", "Shipper chỉ có thể cập nhật trạng thái là 'Đang giao' hoặc 'Đã giao hàng'.");
                    session.setAttribute("messageType", "error");
                    response.sendRedirect(request.getContextPath() + "/admin/orders");
                    return;
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
                    new DAO.NotificationDAO().create(
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
                orderId = Integer.parseInt(request.getParameter("orderId"));
            } catch (NumberFormatException e) {
                session.setAttribute("message", "Mã đơn hàng không hợp lệ.");
                session.setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/admin/orders");
                return;
            }

            Order order = orderDAO.getOrderById(orderId);
            if (order == null) {
                session.setAttribute("message", "Không tìm thấy đơn hàng.");
                session.setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/admin/orders");
                return;
            }

            if (order.getGhnOrderId() != null) {
                session.setAttribute("message", "Đơn hàng đã được đẩy lên GHN rồi.");
                session.setAttribute("messageType", "warning");
            } else {
                try {
                    JsonObject ghnResult = shippingService.createGhnOrder(order);
                    String ghnOrderId = ghnResult.get("order_code") != null
                            ? ghnResult.get("order_code").getAsString() : "";
                    String ghnTrackingCode = ghnResult.get("sort_code") != null
                            ? ghnResult.get("sort_code").getAsString() : "";
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
                orderId = Integer.parseInt(request.getParameter("orderId"));
            } catch (NumberFormatException e) {
                session.setAttribute("message", "Mã đơn hàng không hợp lệ.");
                session.setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/admin/orders");
                return;
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
                orderId = Integer.parseInt(request.getParameter("orderId"));
            } catch (NumberFormatException e) {
                session.setAttribute("message", "Mã đơn hàng không hợp lệ.");
                session.setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/admin/orders");
                return;
            }

            String verificationStatus = request.getParameter("verificationStatus");
            String verificationMessage = request.getParameter("verificationMessage");
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

        if ("detail".equalsIgnoreCase(request.getParameter("returnTo"))) {
            response.sendRedirect(request.getContextPath() + "/admin/orders?action=view&id=" + request.getParameter("orderId"));
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin/orders");
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
