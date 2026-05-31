package controller.admin;

import DAO.ReportDAO;
import Model.Product;
import Model.Review;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/reports")
public class ReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String yearParam = request.getParameter("year");
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int year = yearParam != null ? Integer.parseInt(yearParam) : currentYear;

        Map<String, Integer> overview = reportDAO.getOverviewStats();
        List<Map<String, Object>> topProducts = reportDAO.getTopSellingProducts(10);
        List<Map<String, Object>> topCustomers = reportDAO.getTopCustomers(10);
        List<Map<String, Object>> couponUsage = reportDAO.getCouponUsage(10);
        List<Map<String, Object>> orderStatus = reportDAO.getOrdersByStatus();
        List<Map<String, Object>> revenueByMonth = reportDAO.getRevenueByMonth(year);
        List<Product> lowStockProducts = reportDAO.getLowStockProducts(10, 10);
        List<Review> lowRatingReviews = reportDAO.getRecentLowRatingReviews(10);

        request.setAttribute("overview", overview);
        request.setAttribute("topProducts", topProducts);
        request.setAttribute("topCustomers", topCustomers);
        request.setAttribute("couponUsage", couponUsage);
        request.setAttribute("orderStatus", orderStatus);
        request.setAttribute("revenueByMonth", revenueByMonth);
        request.setAttribute("lowStockProducts", lowStockProducts);
        request.setAttribute("lowRatingReviews", lowRatingReviews);
        request.setAttribute("totalRevenue", reportDAO.getTotalRevenue());
        request.setAttribute("currentMonthRevenue", reportDAO.getCurrentMonthRevenue());
        request.setAttribute("completedOrders", reportDAO.getCompletedOrdersCount());
        request.setAttribute("selectedYear", year);
        request.setAttribute("currentYear", currentYear);

        request.getRequestDispatcher("/pages/admin/reports.jsp").forward(request, response);
    }
}
