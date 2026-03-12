package controller.admin;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import DAO.OrderDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
import Model.Order;

@WebServlet("/pages/admin/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        UserDAO userDAO = new UserDAO();
        ProductDAO productDAO = new ProductDAO();
        OrderDAO orderDAO = new OrderDAO();
        
        // Thống kê chính
        request.setAttribute("totalUsers", userDAO.countUsers());
        request.setAttribute("totalProducts", productDAO.getTotalProducts());
        request.setAttribute("pendingOrders", orderDAO.countPendingOrders());
        request.setAttribute("todayRevenue", orderDAO.getTodayRevenue());
        
        // Lấy danh sách đơn hàng gần đây
        List<Order> recentOrders = orderDAO.getAllOrders();
        if (recentOrders.size() > 5) {
            recentOrders = recentOrders.subList(0, 5);
        }
        request.setAttribute("recentOrders", recentOrders);
        
        request.getRequestDispatcher("/pages/admin/dashboard.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
