package DAO;

import Context.DBContext;
import Model.Order;
import Model.Product;
import Model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReportDAO.class);

    public Map<String, Integer> getOverviewStats() {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT " +
                "(SELECT COUNT(*) FROM users WHERE role = 'user') AS total_users, " +
                "(SELECT COUNT(*) FROM products) AS total_products, " +
                "(SELECT COUNT(*) FROM orders WHERE status = 'Pending') AS pending_orders, " +
                "(SELECT COUNT(*) FROM orders WHERE status = 'Completed') AS completed_orders, " +
                "(SELECT COUNT(*) FROM products WHERE stock > 0 AND stock < 10) AS low_stock_products, " +
                "(SELECT COUNT(*) FROM reviews WHERE rating <= 2) AS low_rating_reviews";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.put("totalUsers", rs.getInt("total_users"));
                stats.put("totalProducts", rs.getInt("total_products"));
                stats.put("pendingOrders", rs.getInt("pending_orders"));
                stats.put("completedOrders", rs.getInt("completed_orders"));
                stats.put("lowStockProducts", rs.getInt("low_stock_products"));
                stats.put("lowRatingReviews", rs.getInt("low_rating_reviews"));
            }
        } catch (Exception e) {
            logger.error("Error fetching overview stats", e);
        }
        return stats;
    }

    public List<Map<String, Object>> getRevenueByMonth(int year) {
        // Reads the pre-aggregated daily rollup instead of scanning orders.
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT MONTH(sale_date) AS month, COALESCE(SUM(revenue), 0) AS revenue " +
                "FROM daily_sales_summary " +
                "WHERE YEAR(sale_date) = ? " +
                "GROUP BY MONTH(sale_date) " +
                "ORDER BY MONTH(sale_date)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("month", rs.getInt("month"));
                    map.put("revenue", rs.getBigDecimal("revenue"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching revenue by month year={}", year, e);
        }
        return list;
    }

    public List<Map<String, Object>> getOrdersByStatus() {
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT status, COUNT(*) AS count FROM orders GROUP BY status ORDER BY count DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("status", rs.getString("status"));
                map.put("count", rs.getInt("count"));
                list.add(map);
            }
        } catch (Exception e) {
            logger.error("Error fetching orders by status", e);
        }
        return list;
    }

    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT p.id, p.name, SUM(oi.quantity) AS total_sold, SUM(oi.quantity * oi.price) AS revenue " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE o.status != 'Cancelled' " +
                "GROUP BY p.id, p.name " +
                "ORDER BY total_sold DESC, revenue DESC " +
                "LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", rs.getInt("id"));
                    map.put("product", rs.getString("name"));
                    map.put("count", rs.getInt("total_sold"));
                    map.put("revenue", rs.getBigDecimal("revenue"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching top selling products limit={}", limit, e);
        }
        return list;
    }

    public List<Map<String, Object>> getOrdersByMonthWithStatus(int year) {
        // Reads the pre-aggregated daily rollup instead of scanning orders.
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT MONTH(sale_date) AS month, " +
                "SUM(pending_orders) AS pending, " +
                "SUM(completed_orders) AS completed, " +
                "SUM(cancelled_orders) AS cancelled, " +
                "SUM(total_orders) AS total " +
                "FROM daily_sales_summary " +
                "WHERE YEAR(sale_date) = ? " +
                "GROUP BY MONTH(sale_date) " +
                "ORDER BY MONTH(sale_date)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("month", rs.getInt("month"));
                    map.put("pending", rs.getInt("pending"));
                    map.put("completed", rs.getInt("completed"));
                    map.put("cancelled", rs.getInt("cancelled"));
                    map.put("total", rs.getInt("total"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching orders by month with status year={}", year, e);
        }
        return list;
    }

    public BigDecimal getTotalRevenue() {
        // Pre-aggregated rollup; may lag up to the scheduler interval.
        String query = "SELECT COALESCE(SUM(revenue), 0) FROM daily_sales_summary";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal result = rs.getBigDecimal(1);
                return result != null ? result : BigDecimal.ZERO;
            }
        } catch (Exception e) {
            logger.error("Error fetching total revenue", e);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getCurrentMonthRevenue() {
        // Pre-aggregated rollup; may lag up to the scheduler interval.
        String query = "SELECT COALESCE(SUM(revenue), 0) FROM daily_sales_summary " +
                "WHERE sale_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01')";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal result = rs.getBigDecimal(1);
                return result != null ? result : BigDecimal.ZERO;
            }
        } catch (Exception e) {
            logger.error("Error fetching current month revenue", e);
        }
        return BigDecimal.ZERO;
    }

    public int getCompletedOrdersCount() {
        String query = "SELECT COUNT(*) FROM orders WHERE status = 'Completed'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            logger.error("Error counting completed orders", e);
        }
        return 0;
    }

    public List<Order> getRecentOrders(int limit) {
        List<Order> list = new ArrayList<>();
        String query = "SELECT * FROM orders ORDER BY createdAt DESC LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching recent orders limit={}", limit, e);
        }
        return list;
    }

    public List<Product> getLowStockProducts(int threshold, int limit) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT p.*, COALESCE(AVG(r.rating), 0) AS average_rating, COUNT(r.id) AS review_count " +
                "FROM products p " +
                "LEFT JOIN reviews r ON r.product_id = p.id " +
                "WHERE p.stock > 0 AND p.stock <= ? " +
                "GROUP BY p.id " +
                "ORDER BY p.stock ASC, p.id DESC LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, threshold);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                ProductDAO productDAO = new ProductDAO();
                while (rs.next()) {
                    list.add(productDAO.getProductById(conn, rs.getInt("id")));
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching low stock products threshold={} limit={}", threshold, limit, e);
        }
        return list;
    }

    public List<Review> getRecentLowRatingReviews(int limit) {
        List<Review> list = new ArrayList<>();
        String query = "SELECT r.*, u.fullname, p.name AS product_name " +
                "FROM reviews r " +
                "JOIN users u ON u.id = r.user_id " +
                "JOIN products p ON p.id = r.product_id " +
                "WHERE r.rating <= 2 " +
                "ORDER BY r.created_at DESC LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review review = mapReview(rs);
                    review.setProductName(rs.getString("product_name"));
                    list.add(review);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching recent low rating reviews limit={}", limit, e);
        }
        return list;
    }

    public List<Review> getRecentReviews(int limit) {
        List<Review> list = new ArrayList<>();
        String query = "SELECT r.*, u.fullname, p.name AS product_name " +
                "FROM reviews r " +
                "JOIN users u ON u.id = r.user_id " +
                "JOIN products p ON p.id = r.product_id " +
                "ORDER BY r.created_at DESC LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review review = mapReview(rs);
                    review.setProductName(rs.getString("product_name"));
                    list.add(review);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching recent reviews limit={}", limit, e);
        }
        return list;
    }

    public List<Map<String, Object>> getTopCustomers(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT u.id, u.fullname, u.email, COUNT(o.id) AS total_orders, " +
                "COALESCE(SUM(CASE WHEN o.status != 'Cancelled' THEN o.total_amount ELSE 0 END), 0) AS total_spent " +
                "FROM users u " +
                "JOIN orders o ON o.user_id = u.id " +
                "WHERE u.role = 'user' " +
                "GROUP BY u.id, u.fullname, u.email " +
                "ORDER BY total_spent DESC, total_orders DESC " +
                "LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", rs.getInt("id"));
                    map.put("fullname", rs.getString("fullname"));
                    map.put("email", rs.getString("email"));
                    map.put("totalOrders", rs.getInt("total_orders"));
                    map.put("totalSpent", rs.getBigDecimal("total_spent"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching top customers limit={}", limit, e);
        }
        return list;
    }

    public List<Map<String, Object>> getCouponUsage(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT code, used, quantity, is_active, discount_type, discount_value, discount_percent " +
                "FROM coupons ORDER BY used DESC, quantity DESC LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", rs.getString("code"));
                    map.put("used", rs.getInt("used"));
                    map.put("quantity", rs.getInt("quantity"));
                    map.put("active", rs.getBoolean("is_active"));
                    map.put("discountType", rs.getString("discount_type"));
                    map.put("discountValue", rs.getBigDecimal("discount_value"));
                    map.put("discountPercent", rs.getInt("discount_percent"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching coupon usage limit={}", limit, e);
        }
        return list;
    }

    public List<Map<String, Object>> getStoredNotifications(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT n.id, n.title, n.message, n.type, n.link, n.is_read, n.created_at, u.fullname " +
                "FROM notifications n JOIN users u ON u.id = n.user_id " +
                "ORDER BY n.created_at DESC LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("title", rs.getString("title"));
                    map.put("message", rs.getString("message"));
                    map.put("type", rs.getString("type"));
                    map.put("link", rs.getString("link"));
                    map.put("isRead", rs.getBoolean("is_read"));
                    map.put("createdAt", rs.getTimestamp("created_at"));
                    map.put("fullname", rs.getString("fullname"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching stored notifications limit={}", limit, e);
        }
        return list;
    }

    private Order mapOrder(ResultSet rs) throws Exception {
        return new Order(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("fullname"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("note"),
                rs.getBigDecimal("total_amount"),
                rs.getString("status"),
                rs.getTimestamp("createdAt"),
                rs.getString("payment_method"),
                rs.getBoolean("payment_status")
        );
    }

    private Review mapReview(ResultSet rs) throws Exception {
        Review review = new Review();
        review.setId(rs.getInt("id"));
        review.setProductId(rs.getInt("product_id"));
        review.setUserId(rs.getInt("user_id"));
        review.setUserName(rs.getString("fullname"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setCreatedAt(rs.getDate("created_at"));
        return review;
    }
}
