package controller.admin;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import DAO.ReportDAO;
import Model.Order;
import Model.Product;
import Model.Review;

@WebServlet("/pages/admin/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ReportDAO reportDAO = new ReportDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        Map<String, Integer> overview = reportDAO.getOverviewStats();
        request.setAttribute("overview", overview);
        request.setAttribute("totalRevenue", reportDAO.getTotalRevenue());
        request.setAttribute("currentMonthRevenue", reportDAO.getCurrentMonthRevenue());
        request.setAttribute("completedOrders", reportDAO.getCompletedOrdersCount());

        List<Order> recentOrders = reportDAO.getRecentOrders(5);
        List<Product> lowStockProducts = reportDAO.getLowStockProducts(10, 5);
        List<Review> recentReviews = reportDAO.getRecentReviews(5);
        List<Map<String, Object>> topProducts = reportDAO.getTopSellingProducts(5);

        request.setAttribute("recentOrders", recentOrders);
        request.setAttribute("lowStockProducts", lowStockProducts);
        request.setAttribute("recentReviews", recentReviews);
        request.setAttribute("topProducts", topProducts);

        request.getRequestDispatcher("/pages/admin/dashboard.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
