package controller.shop;

import DAO.OrderDAO;
import Model.Order;
import Model.User;

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
    private final OrderDAO orderDAO = new OrderDAO();
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        String action = request.getParameter("action");
        String statusFilter = request.getParameter("status");
        String keyword = request.getParameter("keyword");
        OrderDAO dao = new OrderDAO();

        if ("view".equals(action)) {
            int orderId = Integer.parseInt(request.getParameter("id"));
            Order order = orderDAO.getOrderById(orderId);
            
            // Bảo mật: Chỉ cho phép xem nếu đơn hàng thuộc về user đang đăng nhập
            if (order != null && order.getUserId() == user.getId()) {
                order.setItems(orderDAO.getOrderItems(orderId));
                request.setAttribute("order", order);
                request.getRequestDispatcher("/pages/shop/order-detail.jsp").forward(request, response);
                return;
            } else {
                response.sendRedirect("my-orders");
                return;
            }
        }
        int countPending = orderDAO.countPendingOrdersByUserId(user.getId());
        int countCompleted = orderDAO.countCompletedOrdersByUserId(user.getId());
        List<Order> allOrders = dao.getOrdersByUserId(user.getId());
        List<Order> list = filterOrders(allOrders, statusFilter, keyword);
        request.setAttribute("countPending", countPending);
        request.setAttribute("countCompleted", countCompleted);
        request.setAttribute("orders", list);
        request.setAttribute("totalOrders", allOrders.size());
        request.setAttribute("selectedStatus", statusFilter);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/pages/shop/my-orders.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        String action = request.getParameter("action");
        if ("cancel".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                if (orderDAO.cancelOrderByUser(orderId, user.getId())) {
                    session.setAttribute("success", "Đơn hàng đã được hủy thành công.");
                } else {
                    session.setAttribute("error", "Không thể hủy đơn hàng này.");
                }
            } catch (Exception e) {
                session.setAttribute("error", "Có lỗi xảy ra khi hủy đơn hàng.");
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
