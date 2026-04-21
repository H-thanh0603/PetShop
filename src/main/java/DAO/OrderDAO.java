package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import Context.DBContext;
import Model.Order;
import Model.OrderItem;
import Model.Product;

public class OrderDAO {

    private Order mapOrder(ResultSet rs) throws Exception {
        return new Order(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("fullname"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("note"),
                rs.getDouble("total_amount"),
                rs.getString("status"),
                rs.getTimestamp("createdAt"),
                rs.getString("payment_method"),
                rs.getBoolean("payment_status")
        );
    }

    public int saveOrder(Order order) {
        try (Connection conn = DBContext.getConnection()) {
            return saveOrder(conn, order);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int saveOrder(Connection conn, Order order) throws Exception {
        String query = "INSERT INTO orders (user_id, fullname, phone, address, note, total_amount, status, payment_method, payment_status, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getUserId());
            ps.setString(2, order.getFullname());
            ps.setString(3, order.getPhone());
            ps.setString(4, order.getAddress());
            ps.setString(5, order.getNote());
            ps.setDouble(6, order.getTotalAmount());
            ps.setString(7, "Pending");
            ps.setString(8, order.getPayment_method());
            ps.setBoolean(9, order.getPayment_status());
            ps.setTimestamp(10, order.getCreatedAt());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean saveOrderItem(OrderItem item) {
        try (Connection conn = DBContext.getConnection()) {
            return saveOrderItem(conn, item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean saveOrderItem(Connection conn, OrderItem item) throws Exception {
        String query = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getPrice());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String query = "SELECT * FROM orders ORDER BY createdAt DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = mapOrder(rs);
                order.setItems(getOrderItems(conn, order.getId()));
                list.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Order getOrderById(int orderId) {
        String query = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(getOrderItems(conn, orderId));
                    return order;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        try (Connection conn = DBContext.getConnection()) {
            return getOrderItems(conn, orderId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<OrderItem> getOrderItems(Connection conn, int orderId) throws Exception {
        List<OrderItem> list = new ArrayList<>();
        String query = "SELECT oi.*, p.name as product_name, p.image as product_image " +
                       "FROM order_items oi JOIN products p ON oi.product_id = p.id " +
                       "WHERE oi.order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getDouble("price"));

                    Product p = new Product();
                    p.setId(rs.getInt("product_id"));
                    p.setName(rs.getString("product_name"));
                    p.setImage(rs.getString("product_image"));
                    item.setProduct(p);

                    list.add(item);
                }
            }
        }
        return list;
    }

    public boolean updateStatus(int orderId, String status) {
        ProductDAO productDAO = new ProductDAO();
        String lockOrderQuery = "SELECT status FROM orders WHERE id = ? FOR UPDATE";
        String updateStatusQuery = "UPDATE orders SET status = ? WHERE id = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String currentStatus = null;
                try (PreparedStatement lockPs = conn.prepareStatement(lockOrderQuery)) {
                    lockPs.setInt(1, orderId);
                    try (ResultSet rs = lockPs.executeQuery()) {
                        if (rs.next()) {
                            currentStatus = rs.getString("status");
                        }
                    }
                }

                if (currentStatus == null) {
                    conn.rollback();
                    return false;
                }

                boolean wasCancelled = "Cancelled".equalsIgnoreCase(currentStatus);
                boolean willBeCancelled = "Cancelled".equalsIgnoreCase(status);

                // Keep stock and status updates in one transaction so admin actions
                // cannot leave inventory out of sync with the order state.
                if (!wasCancelled && willBeCancelled) {
                    // Restore stock only when the order is moved into Cancelled.
                    for (OrderItem item : getOrderItems(conn, orderId)) {
                        if (!productDAO.increaseStock(conn, item.getProductId(), item.getQuantity())) {
                            conn.rollback();
                            return false;
                        }
                    }
                } else if (wasCancelled && !willBeCancelled) {
                    // Re-reserve stock when a cancelled order is opened again.
                    for (OrderItem item : getOrderItems(conn, orderId)) {
                        if (!productDAO.decreaseStock(conn, item.getProductId(), item.getQuantity())) {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement updatePs = conn.prepareStatement(updateStatusQuery)) {
                    updatePs.setString(1, status);
                    updatePs.setInt(2, orderId);
                    if (updatePs.executeUpdate() <= 0) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countPendingOrders() {
        String query = "SELECT COUNT(*) FROM orders WHERE status = 'Pending'";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> list = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE user_id = ? ORDER BY createdAt DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(getOrderItems(conn, order.getId()));
                    list.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean cancelOrderByUser(int orderId, int userId) {
        String query = "SELECT status FROM orders WHERE id = ? AND user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String status = rs.getString("status");
                if (!"Pending".equalsIgnoreCase(status) && !"Confirmed".equalsIgnoreCase(status)) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return updateStatus(orderId, "Cancelled");
    }

    public double getTodayRevenue() {
        String query = "SELECT SUM(total_amount) FROM orders WHERE DATE(createdAt) = CURDATE() AND status != 'Cancelled'";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    // trả về so lượng order đang xử lý
    public int countPendingOrdersByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ? AND status = 'Pending'";
        int count = 0;

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }
    // trả về so lượng order đã hoàn thành
    public int countCompletedOrdersByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ? AND status = 'Completed'";
        int count = 0;

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }
}
