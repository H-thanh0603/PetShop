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
import Model.OrderStatusHistory;
import Model.User;

@WebServlet("/admin/orders")
public class ManageOrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AdminActionLogDAO actionLog = new AdminActionLogDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");
        OrderDAO dao = new OrderDAO();
        
        if ("view".equals(action)) {
            int orderId = Integer.parseInt(request.getParameter("id"));
            Order order = dao.getOrderById(orderId);
            List<OrderStatusHistory> statusHistory = dao.getStatusHistory(orderId);
            request.setAttribute("order", order);
            request.setAttribute("statusHistory", statusHistory);
            request.getRequestDispatcher("/pages/admin/order-detail.jsp").forward(request, response);
            return;
        }

        // Pagination
        int page = 1;
        int size = 20;
        try { page = Math.max(1, Integer.parseInt(request.getParameter("page"))); } catch (Exception ignored) {}
        try { size = Math.max(1, Integer.parseInt(request.getParameter("size"))); } catch (Exception ignored) {}

        List<Order> list = dao.getOrdersPage(page, size, status, keyword);
        int totalOrders = dao.countOrders(status, keyword);
        int totalPages = (int) Math.ceil((double) totalOrders / size);

        request.setAttribute("orders", list);
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/pages/admin/orders.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        OrderDAO dao = new OrderDAO();
        HttpSession session = request.getSession();
        User admin = (User) session.getAttribute("user");
        int adminId = admin != null ? admin.getId() : 1;

        if ("updateStatus".equals(action)) {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            String newStatus = request.getParameter("status");
            // Get old status for logging
            Order existing = dao.getOrderById(orderId);
            String oldStatus = existing != null ? existing.getStatus() : "Unknown";
            if (dao.updateStatus(orderId, newStatus, adminId)) {
                actionLog.log(adminId, "UPDATE_ORDER_STATUS", "order", orderId,
                        "Status changed from " + oldStatus + " to " + newStatus);
                session.setAttribute("message", "Cập nhật trạng thái đơn hàng thành công!");
                session.setAttribute("messageType", "success");
            } else {
                session.setAttribute("message", "Cập nhật trạng thái thất bại!");
                session.setAttribute("messageType", "error");
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin/orders");
    }
}
