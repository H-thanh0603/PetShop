package controller.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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

        // New Stat Cards Metrics & Growth
        BigDecimal todayRevenue = reportDAO.getTodayRevenue();
        BigDecimal yesterdayRevenue = reportDAO.getYesterdayRevenue();
        double todayRevenueGrowth = calculateGrowth(todayRevenue.doubleValue(), yesterdayRevenue.doubleValue());
        request.setAttribute("todayRevenue", todayRevenue);
        request.setAttribute("todayRevenueGrowth", todayRevenueGrowth);

        int todayOrders = reportDAO.getTodayOrdersCount();
        int yesterdayOrders = reportDAO.getYesterdayOrdersCount();
        double todayOrdersGrowth = calculateGrowth(todayOrders, yesterdayOrders);
        request.setAttribute("todayOrders", todayOrders);
        request.setAttribute("todayOrdersGrowth", todayOrdersGrowth);

        int totalPending = reportDAO.getPendingOrdersCount();
        int todayPending = reportDAO.getTodayPendingOrdersCount();
        int yesterdayPending = reportDAO.getYesterdayPendingOrdersCount();
        double pendingOrdersGrowth = calculateGrowth(todayPending, yesterdayPending);
        request.setAttribute("totalPending", totalPending);
        request.setAttribute("pendingOrdersGrowth", pendingOrdersGrowth);

        int totalAwaitingPayment = reportDAO.getAwaitingPaymentOrdersCount();
        int todayAwaiting = reportDAO.getTodayAwaitingPaymentCount();
        int yesterdayAwaiting = reportDAO.getYesterdayAwaitingPaymentCount();
        double awaitingPaymentGrowth = calculateGrowth(todayAwaiting, yesterdayAwaiting);
        request.setAttribute("totalAwaitingPayment", totalAwaitingPayment);
        request.setAttribute("awaitingPaymentGrowth", awaitingPaymentGrowth);

        // New Alerts Metrics
        BigDecimal awaitingPaymentAmount = reportDAO.getAwaitingPaymentOrdersAmount();
        int reconciliationCount = reportDAO.getReconciliationOrdersCount();
        BigDecimal thisWeekRev = reportDAO.getThisWeekRevenue();
        BigDecimal lastWeekRev = reportDAO.getLastWeekRevenue();
        double weeklyRevenueGrowth = calculateGrowth(thisWeekRev.doubleValue(), lastWeekRev.doubleValue());
        
        request.setAttribute("awaitingPaymentAmount", awaitingPaymentAmount);
        request.setAttribute("reconciliationCount", reconciliationCount);
        request.setAttribute("weeklyRevenueGrowth", weeklyRevenueGrowth);

        // Low stock threshold
        int lowStockCount = overview.getOrDefault("lowStockProducts", 0);
        request.setAttribute("lowStockCount", lowStockCount);

        List<Order> recentOrders = reportDAO.getRecentOrders(5);
        List<Product> lowStockProducts = reportDAO.getLowStockProducts(10, 5);
        List<Review> recentReviews = reportDAO.getRecentReviews(5);
        List<Map<String, Object>> topProducts = reportDAO.getTopSellingProducts(5);

        request.setAttribute("recentOrders", recentOrders);
        request.setAttribute("lowStockProducts", lowStockProducts);
        request.setAttribute("recentReviews", recentReviews);
        request.setAttribute("topProducts", topProducts);

        // 1. Chart 1: Orders Trend Line Chart (Last 7 days, filled with 0)
        List<Map<String, Object>> raw7Days = reportDAO.getLast7DaysOrders();
        List<Map<String, Object>> filled7Days = fillLast7Days(raw7Days);
        request.setAttribute("last7DaysJson", toJson7Days(filled7Days));

        // 1. Chart 1: Orders by Month (Bar Chart)
        List<Map<String, Object>> monthlyOrders = reportDAO.getOrdersByMonthWithStatus(year);
        request.setAttribute("monthlyOrdersJson", toJsonMonthlyOrders(monthlyOrders));

        // 2. Chart 2: Order status (Existing)
        List<Map<String, Object>> orderStatus = reportDAO.getOrdersByStatus();
        request.setAttribute("orderStatusJson", toJsonCount(orderStatus, "status"));

        // 3. Chart 3: Payment methods (Bar Chart)
        List<Map<String, Object>> paymentMethods = reportDAO.getPaymentMethodsCount();
        request.setAttribute("paymentMethodsJson", toJsonPaymentMethods(paymentMethods));

        // 4. Chart 4: Payment transactions status (Doughnut)
        Map<String, Integer> paymentStatus = reportDAO.getPaymentStatusOverview();
        request.setAttribute("paymentStatusJson", toJsonPaymentStatus(paymentStatus));

        // Legacy/Monthly revenue chart data
        List<Map<String, Object>> revenueByMonth = reportDAO.getRevenueByMonth(year);
        request.setAttribute("revenueByMonthJson", toJsonRevenue(revenueByMonth));

        request.getRequestDispatcher("/pages/admin/dashboard.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }

    private double calculateGrowth(double today, double yesterday) {
        if (yesterday <= 0) {
            return today > 0 ? 100.0 : 0.0;
        }
        return ((today - yesterday) / yesterday) * 100.0;
    }

    private List<Map<String, Object>> fillLast7Days(List<Map<String, Object>> dbList) {
        List<Map<String, Object>> result = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        
        Map<String, Integer> dbMap = new HashMap<>();
        for (Map<String, Object> item : dbList) {
            dbMap.put((String) item.get("date"), (Integer) item.get("count"));
        }
        
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate d = today.minusDays(i);
            String dStr = d.toString();
            int count = dbMap.getOrDefault(dStr, 0);
            
            Map<String, Object> map = new HashMap<>();
            map.put("date", d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")));
            map.put("count", count);
            result.add(map);
        }
        return result;
    }

    private String toJson7Days(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = list.get(i);
            sb.append("{\"date\":\"").append(item.get("date"))
              .append("\",\"count\":").append(item.get("count")).append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonPaymentMethods(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = list.get(i);
            String method = item.get("method") != null ? item.get("method").toString().replace("\"", "\\\"") : "COD";
            sb.append("{\"method\":\"").append(method)
              .append("\",\"count\":").append(item.get("count")).append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonPaymentStatus(Map<String, Integer> map) {
        return String.format(
            "{\"paid\":%d,\"unpaid\":%d,\"reconciliation\":%d,\"failed\":%d,\"refunded\":%d}",
            map.getOrDefault("paid", 0),
            map.getOrDefault("unpaid", 0),
            map.getOrDefault("reconciliation", 0),
            map.getOrDefault("failed", 0),
            map.getOrDefault("refunded", 0)
        );
    }

    private String toJsonMonthlyOrders(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = list.get(i);
            sb.append("{\"month\":").append(item.get("month"))
              .append(",\"count\":").append(item.get("total") != null ? item.get("total") : 0).append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
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

