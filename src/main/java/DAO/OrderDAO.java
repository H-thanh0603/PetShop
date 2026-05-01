package DAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import Context.DBContext;
import Model.Order;
import Model.OrderItem;
import Model.OrderStatus;
import Model.OrderStatusHistory;
import Model.PaymentTransaction;
import Model.Product;

public class OrderDAO {

    private static final Logger log = LoggerFactory.getLogger(OrderDAO.class);
    private final PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();

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

    public int saveOrder(Order order) {
        try (Connection conn = DBContext.getConnection()) {
            return saveOrder(conn, order);
        } catch (Exception e) {
            log.error("DB error", e);
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
            ps.setBigDecimal(6, order.getTotalAmount());
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
            log.error("DB error", e);
        }
        return false;
    }

    public boolean saveOrderItem(Connection conn, OrderItem item) throws Exception {
        String query = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getPrice());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String query = "SELECT * FROM orders ORDER BY createdAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = mapOrder(rs);
                order.setItems(getOrderItems(conn, order.getId()));
                list.add(order);
            }
            paymentTransactionDAO.attachLatestToOrders(conn, list);
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return list;
    }

    /**
     * Paginated order list with optional status/keyword filter.
     * Returns only the requested page; items are loaded in a single batch query.
     */
    public List<Order> getOrdersPage(int page, int size, String statusFilter, String keyword) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE 1=1");
        if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND status = ?");
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (CAST(id AS CHAR) LIKE ? OR fullname LIKE ? OR phone LIKE ? OR address LIKE ?)");
        }
        sql.append(" ORDER BY createdAt DESC LIMIT ? OFFSET ?");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
                ps.setString(idx++, statusFilter);
            }
            if (keyword != null && !keyword.isEmpty()) {
                String kw = "%" + keyword + "%";
                ps.setString(idx++, kw); ps.setString(idx++, kw);
                ps.setString(idx++, kw); ps.setString(idx++, kw);
            }
            ps.setInt(idx++, size);
            ps.setInt(idx, Math.max(0, (page - 1) * size));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }

            // Batch load items for all orders in this page
            if (!list.isEmpty()) {
                loadItemsForOrders(conn, list);
                paymentTransactionDAO.attachLatestToOrders(conn, list);
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return list;
    }

    public int countOrders(String statusFilter, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE 1=1");
        if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND status = ?");
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (CAST(id AS CHAR) LIKE ? OR fullname LIKE ? OR phone LIKE ? OR address LIKE ?)");
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
                ps.setString(idx++, statusFilter);
            }
            if (keyword != null && !keyword.isEmpty()) {
                String kw = "%" + keyword + "%";
                ps.setString(idx++, kw); ps.setString(idx++, kw);
                ps.setString(idx++, kw); ps.setString(idx++, kw);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return 0;
    }

    /**
     * Batch-load order items for a list of orders in a single SQL query (N+1 fix).
     */
    private void loadItemsForOrders(Connection conn, List<Order> orders) throws Exception {
        if (orders.isEmpty()) return;
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        String sql = "SELECT oi.*, p.name as product_name, p.image as product_image " +
                     "FROM order_items oi JOIN products p ON oi.product_id = p.id " +
                     "WHERE oi.order_id IN (" + placeholders + ")";
        java.util.Map<Integer, List<OrderItem>> itemMap = new java.util.HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < orders.size(); i++) {
                ps.setInt(i + 1, orders.get(i).getId());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getBigDecimal("price"));
                    Product p = new Product();
                    p.setId(rs.getInt("product_id"));
                    p.setName(rs.getString("product_name"));
                    p.setImage(rs.getString("product_image"));
                    item.setProduct(p);
                    itemMap.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
                }
            }
        }
        for (Order order : orders) {
            order.setItems(itemMap.getOrDefault(order.getId(), new ArrayList<>()));
        }
    }

    public Order getOrderById(int orderId) {
        String query = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(getOrderItems(conn, orderId));
                    paymentTransactionDAO.applyTransaction(order, paymentTransactionDAO.getLatestByOrderId(conn, orderId));
                    return order;
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return null;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        try (Connection conn = DBContext.getConnection()) {
            return getOrderItems(conn, orderId);
        } catch (Exception e) {
            log.error("DB error", e);
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
                    item.setPrice(rs.getBigDecimal("price"));

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
        return updateStatus(orderId, status, -1);
    }

    public boolean updateStatus(int orderId, String status, int changedByUserId) {
        ProductDAO productDAO = new ProductDAO();
        OrderStatusHistoryDAO historyDAO = new OrderStatusHistoryDAO();
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

                // --- State-machine validation ---
                OrderStatus fromStatus = OrderStatus.fromString(currentStatus);
                OrderStatus toStatus   = OrderStatus.fromString(status);
                if (fromStatus == null || toStatus == null || !fromStatus.canTransitionTo(toStatus)) {
                    log.warn("Invalid order status transition: {} → {} (orderId={})", currentStatus, status, orderId);
                    conn.rollback();
                    return false;
                }
                // --------------------------------

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

                // Record audit trail
                int actor = changedByUserId > 0 ? changedByUserId : 1; // fallback to system user
                if (!historyDAO.insertHistory(conn, orderId, currentStatus, status, actor)) {
                    conn.rollback();
                    return false;
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
            log.error("DB error", e);
        }
        return false;
    }

    public int countPendingOrders() {
        String query = "SELECT COUNT(*) FROM orders WHERE status = 'Pending'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return 0;
    }

    public int countOrdersAwaitingPaymentVerification() {
        String query = "SELECT COUNT(*) " +
                "FROM payment_transactions pt " +
                "JOIN (" +
                "  SELECT MAX(id) AS latest_id FROM payment_transactions GROUP BY order_id" +
                ") latest ON latest.latest_id = pt.id " +
                "WHERE pt.verification_status = 'PENDING'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return 0;
    }

    public boolean updatePaymentVerification(int orderId, String verificationStatus, String verificationMessage) {
        String normalizedStatus = normalizeVerificationStatus(verificationStatus);
        if (normalizedStatus == null) {
            return false;
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PaymentTransaction transaction = paymentTransactionDAO.getLatestByOrderIdForUpdate(conn, orderId);
                if (transaction == null) {
                    conn.rollback();
                    return false;
                }

                String transactionStatus = mapTransactionStatus(normalizedStatus);
                boolean paid = "VERIFIED".equals(normalizedStatus);
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                Timestamp verifiedAt = paid ? now : null;

                if (!paymentTransactionDAO.updateVerificationStatus(
                        conn,
                        transaction.getId(),
                        transactionStatus,
                        normalizedStatus,
                        normalizeVerificationMessage(verificationMessage, normalizedStatus),
                        now,
                        verifiedAt
                )) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET payment_status = ? WHERE id = ?")) {
                    ps.setBoolean(1, paid);
                    ps.setInt(2, orderId);
                    if (ps.executeUpdate() <= 0) {
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
            log.error("DB error", e);
        }
        return false;
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> list = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE user_id = ? ORDER BY createdAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
            // Batch-load all order items in a single query to avoid N+1
            if (!list.isEmpty()) {
                loadItemsForOrders(conn, list);
                paymentTransactionDAO.attachLatestToOrders(conn, list);
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return list;
    }

    public boolean cancelOrderByUser(int orderId, int userId) {
        ProductDAO productDAO = new ProductDAO();
        OrderStatusHistoryDAO historyDAO = new OrderStatusHistoryDAO();
        // Lock the row atomically to prevent concurrent cancellations / status changes
        String lockQuery = "SELECT created_at, status FROM orders WHERE id = ? AND user_id = ? FOR UPDATE";
        String updateQuery = "UPDATE orders SET status = 'Cancelled' WHERE id = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String currentStatus;
                java.sql.Timestamp createdAt;

                // 1. Lock and read the row inside the transaction
                try (PreparedStatement ps = conn.prepareStatement(lockQuery)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        currentStatus = rs.getString("status");
                        createdAt = rs.getTimestamp("created_at");
                    }
                }

                // 2. Validate status (must be Pending or Confirmed)
                OrderStatus fromStatus = OrderStatus.fromString(currentStatus);
                if (fromStatus == null || !fromStatus.canTransitionTo(OrderStatus.CANCELLED)) {
                    conn.rollback();
                    return false;
                }

                // 3. Enforce 1-hour cancellation window inside the transaction
                if (createdAt != null) {
                    long elapsedSeconds = (System.currentTimeMillis() - createdAt.getTime()) / 1000;
                    if (elapsedSeconds > 3600) {
                        conn.rollback();
                        return false; // past cancellation window
                    }
                }

                // 4. Restore stock for each cancelled item
                for (OrderItem item : getOrderItems(conn, orderId)) {
                    if (!productDAO.increaseStock(conn, item.getProductId(), item.getQuantity())) {
                        conn.rollback();
                        return false;
                    }
                }

                // 5. Update the order status to Cancelled
                try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                    ps.setInt(1, orderId);
                    if (ps.executeUpdate() <= 0) {
                        conn.rollback();
                        return false;
                    }
                }

                // 6. Record status history
                if (!historyDAO.insertHistory(conn, orderId, currentStatus, "Cancelled", userId)) {
                    conn.rollback();
                    return false;
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
            log.error("DB error", e);
        }
        return false;
    }

    /**
     * Check if an order is still within the 1-hour cancellation window.
     */
    public boolean isWithinCancellationWindow(int orderId) {
        String sql = "SELECT createdAt FROM orders WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.sql.Timestamp createdAt = rs.getTimestamp("createdAt");
                    if (createdAt != null) {
                        long elapsedSeconds = (System.currentTimeMillis() - createdAt.getTime()) / 1000;
                        return elapsedSeconds <= 3600;
                    }
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return false;
    }

    public List<OrderStatusHistory> getStatusHistory(int orderId) {
        return new OrderStatusHistoryDAO().getHistoryByOrderId(orderId);
    }

    public BigDecimal getTodayRevenue() {
        String query = "SELECT SUM(total_amount) FROM orders WHERE DATE(createdAt) = CURDATE() AND status != 'Cancelled'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal result = rs.getBigDecimal(1);
                return result != null ? result : BigDecimal.ZERO;
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return BigDecimal.ZERO;
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
            log.error("DB error", e);
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
            log.error("DB error", e);
        }

        return count;
    }

    /**
     * Tổng chi tiêu của user (chỉ tính đơn Completed).
     */
    public BigDecimal getTotalSpentByUserId(int userId) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE user_id = ? AND status = 'Completed'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal result = rs.getBigDecimal(1);
                    return result != null ? result : BigDecimal.ZERO;
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Tổng số đơn hàng của user.
     */
    public int countOrdersByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return 0;
    }

    private String normalizeVerificationStatus(String verificationStatus) {
        if (verificationStatus == null) {
            return null;
        }
        switch (verificationStatus.trim().toUpperCase()) {
            case "PENDING":
            case "VERIFIED":
            case "FAILED":
                return verificationStatus.trim().toUpperCase();
            default:
                return null;
        }
    }

    private String mapTransactionStatus(String verificationStatus) {
        switch (verificationStatus) {
            case "VERIFIED":
                return "VERIFIED";
            case "FAILED":
                return "FAILED";
            case "PENDING":
            default:
                return "PENDING_VERIFICATION";
        }
    }

    private String normalizeVerificationMessage(String verificationMessage, String verificationStatus) {
        String trimmed = verificationMessage == null ? "" : verificationMessage.trim();
        if (!trimmed.isEmpty()) {
            return trimmed;
        }
        switch (verificationStatus) {
            case "VERIFIED":
                return "Admin đã xác nhận thanh toán chuyển khoản.";
            case "FAILED":
                return "Admin đánh dấu đối soát chưa khớp.";
            case "PENDING":
            default:
                return "Đang chờ đối soát thanh toán chuyển khoản.";
        }
    }
}

