package controller.shop;

import DAO.OrderDAO;
import Model.CustomerRepurchaseSuggestion;
import Model.Order;
import Model.OrderStatus;
import Model.User;
import Util.VnpayUtil;
import services.ReorderService;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/my-orders")
public class MyOrdersServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();
    private final ReorderService reorderService = new ReorderService();
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            session.setAttribute("redirectAfterLogin", request.getContextPath() + "/my-orders");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String action = request.getParameter("action");
        String statusFilter = request.getParameter("status");
        if (statusFilter == null || statusFilter.trim().isEmpty()) {
            statusFilter = "all";
        }
        String keyword = request.getParameter("keyword");
        if (keyword != null) keyword = keyword.trim();

        if ("view".equals(action)) {
            int orderId;
            try {
                orderId = Integer.parseInt(request.getParameter("id"));
            } catch (NumberFormatException e) {
                session.setAttribute("error", "Mã đơn hàng không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/my-orders");
                return;
            }
            Order order = orderDAO.getOrderById(orderId);

            if (order != null && order.getUserId() == user.getId()) {
                request.setAttribute("order", order);
                request.getRequestDispatcher("/pages/shop/order-detail.jsp").forward(request, response);
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/my-orders");
                return;
            }
        }

        orderDAO.autoCompleteDeliveredOrders(); // Tự động hoàn thành các đơn đã giao
        int countPending = orderDAO.countPendingOrdersByUserId(user.getId());
        int countCompleted = orderDAO.countCompletedOrdersByUserId(user.getId());

        // Gợi ý mua lại sản phẩm
        List<CustomerRepurchaseSuggestion> repurchaseSuggestions = orderDAO.getRepurchaseSuggestions(user.getId(), 30, 5);

        int currentPage = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                currentPage = Integer.parseInt(pageParam);
                if (currentPage < 1) currentPage = 1;
            } catch (NumberFormatException e) {
                currentPage = 1;
            }
        }
        int offset = (currentPage - 1) * PAGE_SIZE;

        // LƯU Ý: Để tối ưu nhất, bạn nên bổ sung tham số `keyword` vào hàm count và hàm pagination trong OrderDAO.
        // Tạm thời cấu hình theo cấu trúc phân trang tầng DB:
        int totalOrders = orderDAO.countOrdersByUserId(user.getId(), statusFilter);
        int totalPages = (int) Math.ceil((double) totalOrders / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        List<Order> list = orderDAO.getOrdersByUserIdWithPagination(user.getId(), statusFilter, offset, PAGE_SIZE);

        request.setAttribute("countPending", countPending);
        request.setAttribute("countCompleted", countCompleted);
        request.setAttribute("orders", list);
        request.setAttribute("repurchaseSuggestions", repurchaseSuggestions);
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("selectedStatus", statusFilter);
        request.setAttribute("currentStatus", statusFilter); // Tương thích với cả 2 jsp
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher("/pages/shop/my-orders.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String action = request.getParameter("action");

        if ("cancel".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
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
        }
        else if ("reorder".equals(action)) {
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
            return;
        }
        else if ("confirmReceipt".equals(action)) {
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
        }
        else if ("repay".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                Order order = orderDAO.getOrderById(orderId);

                if (order == null || order.getUserId() != user.getId() || !order.isRepayable()) {
                    session.setAttribute("toastMessage", "Đơn hàng không thể thanh toán lại.");
                    session.setAttribute("toastType", "error");
                    response.sendRedirect(request.getContextPath() + "/my-orders");
                    return;
                }

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

                String vnpayUrl = VnpayUtil.createPaymentUrl(request, orderId, order.getTotalAmount());
                response.sendRedirect(vnpayUrl);
                return;
            } catch (Exception e) {
                session.setAttribute("error", "Có lỗi xảy ra khi thanh toán lại đơn hàng.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/my-orders");
    }
}