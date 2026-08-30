package controller.admin;

import DAO.ReportDAO;
import Model.Order;
import Model.Product;
import Model.Review;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Order> pendingOrders = new ArrayList<>();
        for (Order order : reportDAO.getRecentOrders(10)) {
            if ("Pending".equalsIgnoreCase(order.getStatus())) {
                pendingOrders.add(order);
            }
        }
        List<Product> lowStockProducts = reportDAO.getLowStockProducts(10, 10);
        List<Review> lowRatingReviews = reportDAO.getRecentLowRatingReviews(10);
        List<Map<String, Object>> storedNotifications = reportDAO.getStoredNotifications(10);

        request.setAttribute("pendingOrders", pendingOrders);
        request.setAttribute("lowStockProducts", lowStockProducts);
        request.setAttribute("lowRatingReviews", lowRatingReviews);
        request.setAttribute("storedNotifications", storedNotifications);
        request.setAttribute("pendingOrderCount", pendingOrders.size());
        request.setAttribute("lowStockCount", lowStockProducts.size());
        request.setAttribute("lowRatingCount", lowRatingReviews.size());

        request.getRequestDispatcher("/pages/admin/notifications.jsp").forward(request, response);
    }
}
