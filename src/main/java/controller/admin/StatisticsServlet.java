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

@WebServlet("/admin/statistics")
public class StatisticsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String yearParam = request.getParameter("year");
        int year = yearParam != null ? Integer.parseInt(yearParam) : Calendar.getInstance().get(Calendar.YEAR);
        
        Map<String, Integer> overview = reportDAO.getOverviewStats();
        request.setAttribute("overview", overview);
        
        // 1. Doanh thu theo tháng
        List<Map<String, Object>> revenueByMonth = reportDAO.getRevenueByMonth(year);
        request.setAttribute("revenueByMonthJson", toJsonRevenue(revenueByMonth));
        
        // 2. Sản phẩm bán chạy
        List<Map<String, Object>> topProducts = reportDAO.getTopSellingProducts(5);
        request.setAttribute("topProductsJson", toJsonCount(topProducts, "product"));
        
        // 3. Trạng thái đơn hàng
        List<Map<String, Object>> orderStatus = reportDAO.getOrdersByStatus();
        request.setAttribute("orderStatusJson", toJsonCount(orderStatus, "status"));
        
        // 4. Đơn hàng theo tháng
        List<Map<String, Object>> ordersByMonth = reportDAO.getOrdersByMonthWithStatus(year);
        request.setAttribute("ordersByMonthJson", toJsonOrders(ordersByMonth));
        
        request.setAttribute("selectedYear", year);
        request.setAttribute("currentYear", Calendar.getInstance().get(Calendar.YEAR));
        
        request.getRequestDispatcher("/pages/admin/statistics.jsp").forward(request, response);
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
    
    private String toJsonOrders(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = list.get(i);
            sb.append("{\"month\":").append(item.get("month"))
              .append(",\"pending\":").append(item.get("pending"))
              .append(",\"completed\":").append(item.get("completed"))
              .append(",\"total\":").append(item.get("total")).append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
