package controller.shop;

import DAO.OrderDAO;
import Model.CustomerRepurchaseSuggestion;
import Model.Order;
import Model.OrderStatus;
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
        dao.autoCompleteDeliveredOrders();
        List<Order> allOrders = dao.getOrdersByUserId(user.getId());
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
                    session.setAttribute("toastMessage", "Đã quá thời gian hủy đơn hàng (1 giờ kể từ khi đặt).");
                    session.setAttribute("toastType", "warning");
                } else if (orderDAO.cancelOrderByUser(orderId, user.getId())) {
                    session.setAttribute("toastMessage", "Đơn hàng đã được hủy thành công.");
                    session.setAttribute("toastType", "success");
                } else {
                    session.setAttribute("toastMessage", "Không thể hủy đơn hàng này. Có thể trạng thái đơn hàng đã thay đổi.");
                    session.setAttribute("toastType", "error");
                }
            } catch (Exception e) {
                session.setAttribute("toastMessage", "Có lỗi xảy ra khi hủy đơn hàng.");
                session.setAttribute("toastType", "error");
            }
        } else if ("reorder".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                if (reorderService.reorderToCart(user.getId(), orderId)) {
                    session.setAttribute("toastMessage", "Đã thêm lại sản phẩm từ đơn cũ vào giỏ hàng.");
                    session.setAttribute("toastType", "success");
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }
                session.setAttribute("toastMessage", "Không thể mua lại đơn hàng này.");
                session.setAttribute("toastType", "error");
            } catch (Exception e) {
                session.setAttribute("toastMessage", "Có lỗi xảy ra khi mua lại đơn hàng.");
                session.setAttribute("toastType", "error");
            }
        } else if ("confirmReceipt".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                Order order = orderDAO.getOrderById(orderId);
                if (order != null && order.getUserId() == user.getId()) {
                    OrderStatus currentStatus = OrderStatus.fromString(order.getStatus());
                    if (currentStatus == OrderStatus.DELIVERED) {
                        if (orderDAO.updateStatus(orderId, OrderStatus.COMPLETED.getDisplayName(), user.getId())) {
                            session.setAttribute("toastMessage", "Cảm ơn bạn đã xác nhận. Đơn hàng đã được hoàn tất.");
                            session.setAttribute("toastType", "success");
                        } else {
                            session.setAttribute("toastMessage", "Không thể xác nhận đơn hàng này. Vui lòng thử lại sau.");
                            session.setAttribute("toastType", "error");
                        }
                    } else if (currentStatus == OrderStatus.COMPLETED) {
                        session.setAttribute("toastMessage", "Đơn hàng này đã được hoàn thành trước đó.");
                        session.setAttribute("toastType", "info");
                    } else {
                        session.setAttribute("toastMessage", "Hành động không hợp lệ. Đơn hàng chưa ở trạng thái 'Đã giao hàng'.");
                        session.setAttribute("toastType", "warning");
                    }
                } else {
                    session.setAttribute("toastMessage", "Không tìm thấy đơn hàng hoặc bạn không có quyền thực hiện hành động này.");
                    session.setAttribute("toastType", "error");
                }
            } catch (Exception e) {
                session.setAttribute("toastMessage", "Có lỗi xảy ra khi xác nhận đơn hàng.");
                session.setAttribute("toastType", "error");
            }
        } else if ("repay".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                Order order = orderDAO.getOrderById(orderId);

                // Bảo mật + trạng thái: chỉ chủ đơn mới được thanh toán lại,
                // và đơn phải còn trong điều kiện thanh toán lại (chưa trả, còn hạn).
                if (order == null || order.getUserId() != user.getId() || !order.isRepayable()) {
                    session.setAttribute("toastMessage", "Đơn hàng không thể thanh toán lại.");
                    session.setAttribute("toastType", "error");
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
