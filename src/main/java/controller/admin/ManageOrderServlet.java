package controller.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import DAO.OrderDAO;
import Model.Order;

@WebServlet("/admin/orders")
public class ManageOrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");
        OrderDAO dao = new OrderDAO();
        
        if ("view".equals(action)) {
            int orderId = Integer.parseInt(request.getParameter("id"));
            Order order = dao.getOrderById(orderId);
            request.setAttribute("order", order);
            request.getRequestDispatcher("/pages/admin/order-detail.jsp").forward(request, response);
            return;
        }

        List<Order> allOrders = dao.getAllOrders();
        List<Order> list = filterOrders(allOrders, status, keyword);
        request.setAttribute("orders", list);
        request.setAttribute("totalOrders", allOrders.size());
        request.setAttribute("selectedStatus", status);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/pages/admin/orders.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        OrderDAO dao = new OrderDAO();
        HttpSession session = request.getSession();

        if ("updateStatus".equals(action)) {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            String status = request.getParameter("status");
            if (dao.updateStatus(orderId, status)) {
                session.setAttribute("message", "Cập nhật trạng thái đơn hàng thành công!");
                session.setAttribute("messageType", "success");
            } else {
                session.setAttribute("message", "Cập nhật trạng thái thất bại!");
                session.setAttribute("messageType", "error");
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin/orders");
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
