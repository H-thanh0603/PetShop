package controller.admin;

import java.io.IOException;
import java.util.Calendar;
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

        int year = Calendar.getInstance().get(Calendar.YEAR);

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

        // Chart data
        List<Map<String, Object>> revenueByMonth = reportDAO.getRevenueByMonth(year);
        request.setAttribute("revenueByMonthJson", toJsonRevenue(revenueByMonth));

        List<Map<String, Object>> orderStatus = reportDAO.getOrdersByStatus();
        request.setAttribute("orderStatusJson", toJsonCount(orderStatus, "status"));

        request.getRequestDispatcher("/pages/admin/dashboard.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }

    private String toJsonRevenue(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = list.get(i);
            sb.append("{\"month\":").append(item.get("month"))
              .append(",\"revenue\":").append(item.get("revenue")).append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonCount(List<Map<String, Object>> list, String labelKey) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = list.get(i);
            String label = item.get(labelKey) != null ? item.get(labelKey).toString().replace("\"", "\\\"") : "";
            sb.append("{\"label\":\"").append(label)
              .append("\",\"count\":").append(item.get("count")).append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
