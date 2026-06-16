package controller.shop;

import DAO.OrderDAO;
import Model.CustomerRepurchaseSuggestion;
import Model.Order;
import Model.User;
import Util.VnpayUtil;
import services.ReorderService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/my-orders")
public class MyOrdersServlet extends HttpServlet {
    private OrderDAO orderDAO = new OrderDAO();
    private ReorderService reorderService = new ReorderService();
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        String statusFilter = request.getParameter("status");
        String keyword = request.getParameter("keyword");
        OrderDAO dao = new OrderDAO();

        if ("view".equals(action)) {
            int orderId;
            try {
                orderId = Integer.parseInt(request.getParameter("id"));
            } catch (NumberFormatException e) {
                session.setAttribute("error", "Mã đơn hàng không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/my-orders");
                return;
            }
            Order order = dao.getOrderById(orderId);
            
            // Bảo mật: Chỉ cho phép xem nếu đơn hàng thuộc về user đang đăng nhập
            if (order != null && order.getUserId() == user.getId()) {
                request.setAttribute("order", order);
                request.getRequestDispatcher("/pages/shop/order-detail.jsp").forward(request, response);
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/my-orders");
                return;
            }
        }
        int countPending = orderDAO.countPendingOrdersByUserId(user.getId());
        int countCompleted = orderDAO.countCompletedOrdersByUserId(user.getId());
        List<Order> allOrders = dao.getOrdersByUserId(user.getId());
        //Tự động quét và huỷ đơn hàng treo quá 2h
        if (allOrders != null) {
            long currentTime = System.currentTimeMillis();
            long twoHoursInMilliseconds = 2 * 60 * 60 * 1000;

            for (Order o : allOrders) {
                // Kiểm tra trạng thái chờ thanh toán (ở đây là "Awaiting Payment")
                if ("Awaiting Payment".equalsIgnoreCase(o.getStatus()) && o.getCreatedAt() != null) {
                    long creationTime = o.getCreatedAt().getTime();

                    if (currentTime - creationTime > twoHoursInMilliseconds) {
                        // Gọi hàm cập nhật trạng thái trong OrderDAO sang 'Cancelled'
                        dao.updateStatus(o.getId(), "Cancelled");
                        o.setStatus("Cancelled"); // Cập nhật trực tiếp trên object để hiển thị giao diện đồng bộ
                    }
                }
            }
        }
        List<Order> list = filterOrders(allOrders, statusFilter, keyword);
        List<CustomerRepurchaseSuggestion> repurchaseSuggestions =
                dao.getRepurchaseSuggestions(user.getId(), 30, 5);
        request.setAttribute("countPending", countPending);
        request.setAttribute("countCompleted", countCompleted);
        request.setAttribute("orders", list);
        request.setAttribute("repurchaseSuggestions", repurchaseSuggestions);
        request.setAttribute("totalOrders", allOrders.size());
        request.setAttribute("selectedStatus", statusFilter);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/pages/shop/my-orders.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if ("cancel".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                // Check cancellation window first for a clear error message
                if (!orderDAO.isWithinCancellationWindow(orderId)) {
                    session.setAttribute("error", "Đã quá thời gian hủy đơn hàng.");
                } else if (orderDAO.cancelOrderByUser(orderId, user.getId())) {
                    session.setAttribute("success", "Đơn hàng đã được hủy thành công.");
                } else {
                    session.setAttribute("error", "Không thể hủy đơn hàng này.");
                }
            } catch (Exception e) {
                session.setAttribute("error", "Có lỗi xảy ra khi hủy đơn hàng.");
            }
        } else if ("reorder".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                if (reorderService.reorderToCart(user.getId(), orderId)) {
                    session.setAttribute("success", "Đã thêm lại sản phẩm từ đơn cũ vào giỏ hàng.");
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }
                session.setAttribute("error", "Không thể mua lại đơn hàng này.");
            } catch (Exception e) {
                session.setAttribute("error", "Có lỗi xảy ra khi mua lại đơn hàng.");
            }
        } else if ("confirmReceipt".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                Order order = orderDAO.getOrderById(orderId);
                if (order != null && order.getUserId() == user.getId() && "Delivered".equals(order.getStatus())) {
                    if (orderDAO.updateStatus(orderId, "Completed", user.getId())) {
                        session.setAttribute("success", "Cảm ơn bạn đã xác nhận. Đơn hàng đã được hoàn tất.");
                    } else {
                        session.setAttribute("error", "Không thể xác nhận đơn hàng này.");
                    }
                } else {
                    session.setAttribute("error", "Hành động không hợp lệ.");
                }
            } catch (Exception e) {
                session.setAttribute("error", "Có lỗi xảy ra khi xác nhận đơn hàng.");
            }
        } else if ("repay".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                Order order = orderDAO.getOrderById(orderId);

                // Bảo mật + trạng thái: chỉ chủ đơn mới được thanh toán lại,
                // và đơn phải còn trong điều kiện thanh toán lại (chưa trả, còn hạn).
                if (order == null || order.getUserId() != user.getId() || !order.isRepayable()) {
                    session.setAttribute("error", "Đơn hàng không thể thanh toán lại.");
                    response.sendRedirect(request.getContextPath() + "/my-orders");
                    return;
                }

                // Nạp lại dữ liệu cho trang order-success (đọc từ session).
                session.setAttribute("paymentMethod", "VNPAY");
                session.setAttribute("paymentStatus", 0);
                session.setAttribute("successOrderId", orderId);
                session.setAttribute("successUser", user);
                session.setAttribute("successTotalAmount", order.getTotalAmount());
                session.setAttribute("successFinalTotal", order.getTotalAmount());
                session.setAttribute("successShippingFee", 0);
                session.setAttribute("successDiscount", 0);
                session.setAttribute("successShippingAddress", order.getAddress());
                session.setAttribute("successOrderNote", order.getNote());
                session.setAttribute("successOrderItems", order.getItems());

                // Tái sử dụng đúng luồng VNPAY hiện có cho chính đơn hàng này.
                String vnpayUrl = VnpayUtil.createPaymentUrl(request, orderId, order.getTotalAmount());
                response.sendRedirect(vnpayUrl);
                return;
            } catch (Exception e) {
                session.setAttribute("error", "Có lỗi xảy ra khi thanh toán lại đơn hàng.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/my-orders");
    }

    private List<Order> filterOrders(List<Order> orders, String statusFilter, String keyword) {
        List<Order> filtered = new ArrayList<>();
        String normalizedStatus = statusFilter == null ? "" : statusFilter.trim();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();

        for (Order order : orders) {
            boolean matchesStatus = normalizedStatus.isEmpty() || "all".equalsIgnoreCase(normalizedStatus)
                    || order.getStatus().equalsIgnoreCase(normalizedStatus);
            boolean matchesKeyword = normalizedKeyword.isEmpty()
                    || String.valueOf(order.getId()).contains(normalizedKeyword)
                    || (order.getFullname() != null && order.getFullname().toLowerCase().contains(normalizedKeyword))
                    || (order.getPhone() != null && order.getPhone().toLowerCase().contains(normalizedKeyword))
                    || (order.getAddress() != null && order.getAddress().toLowerCase().contains(normalizedKeyword));
            if (matchesStatus && matchesKeyword) {
                filtered.add(order);
            }
        }
        return filtered;
    }
}
