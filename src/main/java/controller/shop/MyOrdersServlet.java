package controller.shop;

import DAO.OrderDAO;
import Model.Order;
import Model.User;

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
    private OrderDAO orderDAO = new OrderDAO();
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        String action = request.getParameter("action");
        OrderDAO dao = new OrderDAO();

        if ("view".equals(action)) {
            int orderId = Integer.parseInt(request.getParameter("id"));
            Order order = dao.getOrderById(orderId);
            
            // Bảo mật: Chỉ cho phép xem nếu đơn hàng thuộc về user đang đăng nhập
            if (order != null && order.getUserId() == user.getId()) {
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
        List<Order> list = dao.getOrdersByUserId(user.getId());
        request.setAttribute("countPending", countPending);
        request.setAttribute("countCompleted", countCompleted);
        request.setAttribute("orders", list);
        request.getRequestDispatcher("/pages/shop/my-orders.jsp").forward(request, response);
    }
}
