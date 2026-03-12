package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Context.DBContext;

public class ReportDAO {

    // Thống kê tổng quan cho Dashboard
    public Map<String, Integer> getOverviewStats() {
        Map<String, Integer> stats = new HashMap<>();
        String queryUsers = "SELECT COUNT(*) FROM users WHERE role = 'user'";
        String queryProducts = "SELECT COUNT(*) FROM products";
        String queryPendingOrders = "SELECT COUNT(*) FROM orders WHERE status = 'Pending'";
        
        try (Connection conn = new DBContext().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(queryUsers);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.put("totalUsers", rs.getInt(1));
            }
            try (PreparedStatement ps = conn.prepareStatement(queryProducts);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.put("totalProducts", rs.getInt(1));
            }
            try (PreparedStatement ps = conn.prepareStatement(queryPendingOrders);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.put("pendingOrders", rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    // Doanh thu theo tháng trong năm
    public List<Map<String, Object>> getRevenueByMonth(int year) {
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT MONTH(created_at) as month, SUM(total_amount) as revenue " +
                       "FROM orders " +
                       "WHERE YEAR(created_at) = ? AND status != 'Cancelled' " +
                       "GROUP BY MONTH(created_at) " +
                       "ORDER BY MONTH(created_at)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("month", rs.getInt("month"));
                    map.put("revenue", rs.getDouble("revenue"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Số lượng đơn hàng theo trạng thái
    public List<Map<String, Object>> getOrdersByStatus() {
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT status, COUNT(*) as count FROM orders GROUP BY status";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("status", rs.getString("status"));
                map.put("count", rs.getInt("count"));
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Top sản phẩm bán chạy
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT p.name, SUM(oi.quantity) as total_sold " +
                       "FROM order_items oi " +
                       "JOIN products p ON oi.product_id = p.id " +
                       "JOIN orders o ON oi.order_id = o.id " +
                       "WHERE o.status != 'Cancelled' " +
                       "GROUP BY p.id " +
                       "ORDER BY total_sold DESC " +
                       "LIMIT ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("product", rs.getString("name"));
                    map.put("count", rs.getInt("total_sold"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Đơn hàng theo tháng (biểu đồ đường)
    public List<Map<String, Object>> getOrdersByMonthWithStatus(int year) {
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT MONTH(created_at) as month, " +
                       "SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END) as pending, " +
                       "SUM(CASE WHEN status = 'Completed' THEN 1 ELSE 0 END) as completed, " +
                       "COUNT(*) as total " +
                       "FROM orders " +
                       "WHERE YEAR(created_at) = ? " +
                       "GROUP BY MONTH(created_at) " +
                       "ORDER BY MONTH(created_at)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("month", rs.getInt("month"));
                    map.put("pending", rs.getInt("pending"));
                    map.put("completed", rs.getInt("completed"));
                    map.put("total", rs.getInt("total"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
