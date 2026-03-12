package controller.admin;

import java.io.IOException;
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
        OrderDAO dao = new OrderDAO();
        
        if ("view".equals(action)) {
            int orderId = Integer.parseInt(request.getParameter("id"));
            Order order = dao.getOrderById(orderId);
            request.setAttribute("order", order);
            request.getRequestDispatcher("/pages/admin/order-detail.jsp").forward(request, response);
            return;
        }

        List<Order> list = dao.getAllOrders();
        request.setAttribute("orders", list);
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
}
