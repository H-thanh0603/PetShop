package DAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import Context.DBContext;
import Model.Order;
import Model.OrderItem;
import Model.OrderLog;
import Model.OrderStatus;
import Model.OrderStatusHistory;
import Model.PaymentTransaction;
import Model.Product;
import Model.CustomerRepurchaseSuggestion;

public class OrderDAO {

    private static final Logger log = LoggerFactory.getLogger(OrderDAO.class);
    private final PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
    private static final String ORDER_SELECT =
            "SELECT o.*, " +
            "COALESCE(o.recipient_fullname, o.fullname) AS mapped_recipient_fullname, " +
            "COALESCE(o.recipient_phone, o.phone) AS mapped_recipient_phone, " +
            "COALESCE(o.shipping_address, o.address) AS mapped_shipping_address, " +
            "u.fullname AS customer_fullname, u.phone AS customer_phone " +
            "FROM orders o JOIN users u ON u.id = o.user_id ";

    private Order mapOrder(ResultSet rs) throws Exception {
        Order order = new Order(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("mapped_recipient_fullname"),
                rs.getString("mapped_recipient_phone"),
                rs.getString("mapped_shipping_address"),
                rs.getString("note"),
                rs.getBigDecimal("total_amount"),
                rs.getString("status"),
                rs.getTimestamp("createdAt"),
                rs.getString("payment_method"),
                rs.getBoolean("payment_status")
        );
        order.setStatusUpdatedAt(rs.getTimestamp("status_updated_at"));
        order.setCustomerFullname(rs.getString("customer_fullname"));
        order.setCustomerPhone(rs.getString("customer_phone"));
        try {
            order.setSubtotal(rs.getBigDecimal("subtotal"));
            order.setShippingFee(rs.getBigDecimal("shipping_fee"));
            order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        } catch (Exception ignored) {
        }
        // GHN shipping fields
        try {
            order.setGhnOrderId(rs.getString("ghn_order_id"));
            order.setGhnTrackingCode(rs.getString("ghn_tracking_code"));
            order.setGhnStatus(rs.getString("ghn_status"));
            order.setGhnPushedAt(rs.getTimestamp("ghn_pushed_at"));
            order.setGhnLastSyncAt(rs.getTimestamp("ghn_last_sync_at"));
            order.setGhnErrorMessage(rs.getString("ghn_error_message"));
        } catch (Exception ignored) {
        }
        return order;
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
        try {
            return saveOrderWithOptionalTotals(conn, order);
        } catch (SQLSyntaxErrorException e) {
            if (isUnknownColumn(e, "subtotal", "shipping_fee", "discount_amount")) {
                log.warn("orders total snapshot columns are missing; falling back to legacy order insert");
                return saveOrderLegacy(conn, order);
            }
            throw e;
        }
    }

    private int saveOrderWithOptionalTotals(Connection conn, Order order) throws Exception {
        String query = "INSERT INTO orders (user_id, fullname, phone, address, recipient_fullname, recipient_phone, shipping_address, note, subtotal, shipping_fee, discount_amount, total_amount, status, payment_method, payment_status, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getUserId());
            ps.setString(2, order.getRecipientFullname());
            ps.setString(3, order.getRecipientPhone());
            ps.setString(4, order.getShippingAddress());
            ps.setString(5, order.getRecipientFullname());
            ps.setString(6, order.getRecipientPhone());
            ps.setString(7, order.getShippingAddress());
            ps.setString(8, order.getNote());
            ps.setBigDecimal(9, order.getSubtotal());
            ps.setBigDecimal(10, order.getShippingFee());
            ps.setBigDecimal(11, order.getDiscountAmount());
            ps.setBigDecimal(12, order.getTotalAmount());
            ps.setString(13, order.getStatus());
            ps.setString(14, order.getPayment_method());
            ps.setBoolean(15, order.getPayment_status());
            ps.setTimestamp(16, order.getCreatedAt());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    new OrderLogDAO().insert(conn, orderId, "CUSTOMER", order.getUserId(),
                            "CREATE_ORDER", null, order.getStatus(), "Khách tạo đơn");
                    return orderId;
                }
            }
        }
        return -1;
    }

    private int saveOrderLegacy(Connection conn, Order order) throws Exception {
        String query = "INSERT INTO orders (user_id, fullname, phone, address, recipient_fullname, recipient_phone, shipping_address, note, total_amount, status, payment_method, payment_status, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getUserId());
            ps.setString(2, order.getRecipientFullname());
            ps.setString(3, order.getRecipientPhone());
            ps.setString(4, order.getShippingAddress());
            ps.setString(5, order.getRecipientFullname());
            ps.setString(6, order.getRecipientPhone());
            ps.setString(7, order.getShippingAddress());
            ps.setString(8, order.getNote());
            ps.setBigDecimal(9, order.getTotalAmount());
            ps.setString(10, order.getStatus());
            ps.setString(11, order.getPayment_method());
            ps.setBoolean(12, order.getPayment_status());
            ps.setTimestamp(13, order.getCreatedAt());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    new OrderLogDAO().insert(conn, orderId, "CUSTOMER", order.getUserId(),
                            "CREATE_ORDER", null, order.getStatus(), "Khách tạo đơn");
                    return orderId;
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
        try {
            return saveOrderItemWithOptionalSnapshots(conn, item);
        } catch (SQLSyntaxErrorException e) {
            if (isUnknownColumn(e, "product_name_snapshot", "product_image_snapshot")) {
                log.warn("order item product snapshot columns are missing; falling back to legacy order item insert");
                return saveOrderItemLegacy(conn, item);
            }
            throw e;
        }
    }

    private boolean saveOrderItemWithOptionalSnapshots(Connection conn, OrderItem item) throws Exception {
        String query = "INSERT INTO order_items (order_id, product_id, quantity, price, original_price, final_price, discount_amount, promotion_id, promotion_name, promotion_type, product_name_snapshot, product_image_snapshot) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getPrice());
            ps.setBigDecimal(5, item.getOriginalPrice());
            ps.setBigDecimal(6, item.getFinalPrice());
            ps.setBigDecimal(7, item.getDiscountAmount());
            ps.setObject(8, item.getPromotionId());
            ps.setString(9, item.getPromotionName());
            ps.setString(10, item.getPromotionType());
            ps.setString(11, item.getProductNameSnapshot());
            ps.setString(12, item.getProductImageSnapshot());
            return ps.executeUpdate() > 0;
        }
    }

    private boolean saveOrderItemLegacy(Connection conn, OrderItem item) throws Exception {
        String query = "INSERT INTO order_items (order_id, product_id, quantity, price, original_price, final_price, discount_amount, promotion_id, promotion_name, promotion_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getPrice());
            ps.setBigDecimal(5, item.getOriginalPrice());
            ps.setBigDecimal(6, item.getFinalPrice());
            ps.setBigDecimal(7, item.getDiscountAmount());
            ps.setObject(8, item.getPromotionId());
            ps.setString(9, item.getPromotionName());
            ps.setString(10, item.getPromotionType());
            return ps.executeUpdate() > 0;
        }
    }

    private boolean isUnknownColumn(SQLSyntaxErrorException e, String... columnNames) {
        if (!"42S22".equals(e.getSQLState()) && e.getErrorCode() != 1054) {
            return false;
        }
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        for (String columnName : columnNames) {
            if (message.contains(columnName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String query = ORDER_SELECT + "ORDER BY o.createdAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            paymentTransactionDAO.expirePendingTransactions();
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
        StringBuilder sql = new StringBuilder(ORDER_SELECT + "WHERE 1=1");
        if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND o.status = ?");
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (CAST(o.id AS CHAR) LIKE ? OR u.fullname LIKE ? OR COALESCE(o.recipient_fullname, o.fullname) LIKE ? OR COALESCE(o.recipient_phone, o.phone) LIKE ? OR COALESCE(o.shipping_address, o.address) LIKE ?)");
        }
        sql.append(" ORDER BY o.createdAt DESC LIMIT ? OFFSET ?");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            paymentTransactionDAO.expirePendingTransactions();
            int idx = 1;
            if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
                ps.setString(idx++, statusFilter);
            }
            if (keyword != null && !keyword.isEmpty()) {
                String kw = "%" + keyword + "%";
                ps.setString(idx++, kw); ps.setString(idx++, kw);
                ps.setString(idx++, kw); ps.setString(idx++, kw);
                ps.setString(idx++, kw);
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
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders o JOIN users u ON u.id = o.user_id WHERE 1=1");
        if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND o.status = ?");
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (CAST(o.id AS CHAR) LIKE ? OR u.fullname LIKE ? OR COALESCE(o.recipient_fullname, o.fullname) LIKE ? OR COALESCE(o.recipient_phone, o.phone) LIKE ? OR COALESCE(o.shipping_address, o.address) LIKE ?)");
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            paymentTransactionDAO.expirePendingTransactions();
            int idx = 1;
            if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
                ps.setString(idx++, statusFilter);
            }
            if (keyword != null && !keyword.isEmpty()) {
                String kw = "%" + keyword + "%";
                ps.setString(idx++, kw); ps.setString(idx++, kw);
                ps.setString(idx++, kw); ps.setString(idx++, kw);
                ps.setString(idx++, kw);
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
        String sql = "SELECT oi.*, COALESCE(oi.product_name_snapshot, p.name) as product_name, COALESCE(oi.product_image_snapshot, p.image) as product_image " +
                     "FROM order_items oi LEFT JOIN products p ON oi.product_id = p.id " +
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
                    item.setOriginalPrice(rs.getBigDecimal("original_price"));
                    item.setFinalPrice(rs.getBigDecimal("final_price"));
                    item.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                    item.setPromotionId((Integer) rs.getObject("promotion_id"));
                    item.setPromotionName(rs.getString("promotion_name"));
                    item.setPromotionType(rs.getString("promotion_type"));
                    item.setProductNameSnapshot(rs.getString("product_name"));
                    item.setProductImageSnapshot(rs.getString("product_image"));
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
        String query = ORDER_SELECT + "WHERE o.id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            paymentTransactionDAO.expirePendingTransactions();
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
        String query = "SELECT oi.*, COALESCE(oi.product_name_snapshot, p.name) as product_name, COALESCE(oi.product_image_snapshot, p.image) as product_image " +
                       "FROM order_items oi LEFT JOIN products p ON oi.product_id = p.id " +
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
                    item.setOriginalPrice(rs.getBigDecimal("original_price"));
                    item.setFinalPrice(rs.getBigDecimal("final_price"));
                    item.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                    item.setPromotionId((Integer) rs.getObject("promotion_id"));
                    item.setPromotionName(rs.getString("promotion_name"));
                    item.setPromotionType(rs.getString("promotion_type"));
                    item.setProductNameSnapshot(rs.getString("product_name"));
                    item.setProductImageSnapshot(rs.getString("product_image"));

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
        String lockOrderQuery = "SELECT status, payment_method, payment_status, user_id FROM orders WHERE id = ? FOR UPDATE";
        String updateStatusQuery = "UPDATE orders SET status = ? WHERE id = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String currentStatus = null;
                String currentPaymentMethod = null;
                boolean currentPaymentPaid = false;
                int orderOwnerId = -1;
                try (PreparedStatement lockPs = conn.prepareStatement(lockOrderQuery)) {
                    lockPs.setInt(1, orderId);
                    try (ResultSet rs = lockPs.executeQuery()) {
                        if (rs.next()) {
                            currentStatus = rs.getString("status");
                            currentPaymentMethod = rs.getString("payment_method");
                            currentPaymentPaid = rs.getBoolean("payment_status");
                            orderOwnerId = rs.getInt("user_id");
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
                if (requiresPaidOnlineOrder(currentPaymentMethod, currentPaymentPaid, toStatus)) {
                    log.warn("Rejected unpaid online order transition: paymentMethod={} paid={} target={} orderId={}",
                            currentPaymentMethod, currentPaymentPaid, status, orderId);
                    conn.rollback();
                    return false;
                }
                // --------------------------------

                boolean wasCancelled = "Cancelled".equalsIgnoreCase(currentStatus);
                boolean willBeCancelled = "Cancelled".equalsIgnoreCase(status);
                boolean willFinalizeCodPayment = isCodPayment(currentPaymentMethod)
                        && !currentPaymentPaid
                        && ("Delivered".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status));

                // Keep stock and status updates in one transaction so admin actions
                // cannot leave inventory out of sync with the order state.
                if (!wasCancelled && willBeCancelled) {
                    if (!releaseReservedStockForOrder(conn, orderId)) {
                        conn.rollback();
                        return false;
                    }
                } else if (wasCancelled && !willBeCancelled) {
                    // Re-reserve stock when a cancelled order is opened again.
                    for (OrderItem item : getOrderItems(conn, orderId)) {
                        if (!productDAO.reserveStock(conn, item.getProductId(), item.getQuantity())) {
                            conn.rollback();
                            return false;
                        }
                    }
                } else if (willFinalizeCodPayment) {
                    if (!finalizeReservedStockForOrder(conn, orderId)) {
                        conn.rollback();
                        return false;
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
                String actorType = "SYSTEM";
                if (changedByUserId > 0) {
                    actorType = (changedByUserId == orderOwnerId) ? "CUSTOMER" : "ADMIN";
                }

                if (!historyDAO.insertHistory(conn, orderId, currentStatus, status, actor)) {
                    conn.rollback();
                    return false;
                }
                if (!new OrderLogDAO().insert(conn, orderId, actorType,
                        actor, "UPDATE_STATUS", currentStatus, status, "Cập nhật trạng thái đơn hàng")) {
                    conn.rollback();
                    return false;
                }
                if (willFinalizeCodPayment && !updatePaymentStatus(conn, orderId, true)) {
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

    private boolean requiresPaidOnlineOrder(String paymentMethod, boolean paymentPaid, OrderStatus targetStatus) {
        if (paymentPaid || isCodPayment(paymentMethod)) {
            return false;
        }
        return targetStatus == OrderStatus.CONFIRMED
                || targetStatus == OrderStatus.SHIPPING
                || targetStatus == OrderStatus.DELIVERED
                || targetStatus == OrderStatus.COMPLETED;
    }

    private boolean isCodPayment(String paymentMethod) {
        return paymentMethod == null || paymentMethod.isBlank() || "COD".equalsIgnoreCase(paymentMethod);
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
        paymentTransactionDAO.expirePendingTransactions();
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
        return updatePaymentVerification(orderId, verificationStatus, verificationMessage, 1);
    }

    public boolean updatePaymentVerification(int orderId, String verificationStatus,
                                             String verificationMessage, int actorUserId) {
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
                String oldVerificationStatus = transaction.getVerificationStatus();

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

                if (!updatePaymentStatus(conn, orderId, paid)) {
                    conn.rollback();
                    return false;
                }

                if (paid) {
                    if (!markAwaitingPaymentOrderPaid(conn, orderId)) {
                        conn.rollback();
                        return false;
                    }
                    if (!finalizeReservedStockForOrder(conn, orderId)) {
                        conn.rollback();
                        return false;
                    }
                } else if ("FAILED".equals(normalizedStatus) || "EXPIRED".equals(normalizedStatus)) {
                    if (!releaseReservedStockForOrder(conn, orderId)) {
                        conn.rollback();
                        return false;
                    }
                }
                if (!new OrderLogDAO().insert(conn, orderId, "ADMIN", actorUserId,
                        "UPDATE_PAYMENT_VERIFICATION", oldVerificationStatus, normalizedStatus,
                        normalizeVerificationMessage(verificationMessage, normalizedStatus))) {
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

    public boolean updatePaymentStatus(Connection conn, int orderId, boolean paid) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE orders SET payment_status = ? WHERE id = ?")) {
            ps.setBoolean(1, paid);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean markAwaitingPaymentOrderPaid(Connection conn, int orderId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE orders SET status = ? WHERE id = ? AND status = ?")) {
            ps.setString(1, "Paid");
            ps.setInt(2, orderId);
            ps.setString(3, "Awaiting Payment");
            ps.executeUpdate();
            return true;
        }
    }

    public boolean releaseReservedStockForOrder(Connection conn, int orderId) throws Exception {
        ProductDAO productDAO = new ProductDAO();
        PromotionDAO promotionDAO = new PromotionDAO();
        for (OrderItem item : getOrderItems(conn, orderId)) {
            if ("FLASH_SALE".equalsIgnoreCase(item.getPromotionType()) && item.getPromotionId() != null) {
                if (!promotionDAO.releaseFlashSaleQuantity(conn, item.getPromotionId(), item.getProductId(), item.getQuantity())) {
                    return false;
                }
            }
            if (!productDAO.releaseReservedStock(conn, item.getProductId(), item.getQuantity())) {
                return false;
            }
        }
        return true;
    }

    public boolean finalizeReservedStockForOrder(Connection conn, int orderId) throws Exception {
        ProductDAO productDAO = new ProductDAO();
        InventoryBatchDAO inventoryBatchDAO = new InventoryBatchDAO();
        Order order = getOrderByIdForUpdate(conn, orderId);
        int actorUserId = order == null ? 1 : order.getUserId();
        for (OrderItem item : getOrderItems(conn, orderId)) {
            if (!productDAO.finalizeReservedStock(conn, item.getProductId(), item.getQuantity())) {
                return false;
            }
            if (inventoryBatchDAO.hasTrackedBatchesForProduct(conn, item.getProductId())) {
                boolean consumed = inventoryBatchDAO.consumeProductStock(
                    conn,
                    item.getProductId(),
                    item.getQuantity(),
                    orderId,
                    actorUserId,
                    "Finalize reserved stock for order #" + orderId
                );
                if (!consumed) {
                    log.warn("Insufficient batch stock for product id={} while finalizing order #{}. Proceeding anyway.", item.getProductId(), orderId);
                }
            }
        }
        return true;
    }

    private Order getOrderByIdForUpdate(Connection conn, int orderId) throws Exception {
        String query = ORDER_SELECT + "WHERE o.id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
            }
        }
        return null;
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> list = new ArrayList<>();
        String query = ORDER_SELECT + "WHERE o.user_id = ? ORDER BY o.createdAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            paymentTransactionDAO.expirePendingTransactions();
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
        String lockQuery = "SELECT createdAt, status FROM orders WHERE id = ? AND user_id = ? FOR UPDATE";
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
                        createdAt = rs.getTimestamp("createdAt");
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

                // 4. Release reserved stock for each cancelled item
                if (!releaseReservedStockForOrder(conn, orderId)) {
                    conn.rollback();
                    return false;
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
                if (!new OrderLogDAO().insert(conn, orderId, "CUSTOMER", userId,
                        "CANCEL_ORDER", currentStatus, "Cancelled", "Khách hủy đơn")) {
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

    public List<OrderLog> getOrderLogs(int orderId) {
        return new OrderLogDAO().getByOrderId(orderId);
    }

    public List<CustomerRepurchaseSuggestion> getRepurchaseSuggestions(int userId, int minDaysSincePurchase, int limit) {
        List<CustomerRepurchaseSuggestion> suggestions = new ArrayList<>();
        String sql = "SELECT o.id AS order_id, oi.product_id, p.name AS product_name, oi.quantity, " +
                "DATEDIFF(NOW(), o.createdAt) AS days_since_purchase " +
                "FROM orders o " +
                "JOIN order_items oi ON oi.order_id = o.id " +
                "JOIN products p ON p.id = oi.product_id " +
                "WHERE o.user_id = ? AND o.status = 'Completed' " +
                "AND o.createdAt <= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                "AND p.is_active = 1 AND p.stock > 0 " +
                "AND (" +
                "LOWER(p.name) LIKE '%cát%' OR LOWER(p.name) LIKE '%cat vệ sinh%' " +
                "OR LOWER(p.name) LIKE '%hạt%' OR LOWER(p.name) LIKE '%pate%' " +
                "OR LOWER(p.name) LIKE '%bánh thưởng%' OR LOWER(p.name) LIKE '%snack%' " +
                "OR LOWER(p.name) LIKE '%sữa tắm%' OR LOWER(p.category) LIKE '%thức ăn%' " +
                "OR LOWER(p.category) LIKE '%cát vệ sinh%'" +
                ") " +
                "ORDER BY o.createdAt DESC, oi.id ASC LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, minDaysSincePurchase);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CustomerRepurchaseSuggestion suggestion = new CustomerRepurchaseSuggestion();
                    suggestion.setOrderId(rs.getInt("order_id"));
                    suggestion.setProductId(rs.getInt("product_id"));
                    suggestion.setProductName(rs.getString("product_name"));
                    suggestion.setQuantity(rs.getInt("quantity"));
                    suggestion.setDaysSincePurchase(rs.getInt("days_since_purchase"));
                    suggestions.add(suggestion);
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
        return suggestions;
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
            case "EXPIRED":
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
            case "EXPIRED":
                return "EXPIRED";
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
            case "EXPIRED":
                return "Quá thời gian chờ thanh toán chuyển khoản.";
            case "PENDING":
            default:
                return "Đang chờ đối soát thanh toán chuyển khoản.";
        }
    }
    public boolean updateOrderPaymentStatus(int orderId, String status) {
        String sql = """
        UPDATE orders
        SET payment_status = ?
        WHERE id = ?
    """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean markOnlinePaymentAwaiting(int orderId, String paymentMethod) {
        String sql = """
        UPDATE orders
        SET status = ?,
            payment_method = ?,
            payment_status = ?
        WHERE id = ?
    """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "Awaiting Payment");
            ps.setString(2, paymentMethod);
            ps.setBoolean(3, false);
            ps.setInt(4, orderId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markOnlinePaymentPaid(int orderId, String paymentMethod) {
        String sql = """
        UPDATE orders
        SET status = ?,
            payment_method = ?,
            payment_status = ?
        WHERE id = ?
    """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "Paid");
            ps.setString(2, paymentMethod);
            ps.setBoolean(3, true);
            ps.setInt(4, orderId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markOnlinePaymentPaidAndFinalize(int orderId, String paymentMethod) {
        String lockSql = "SELECT payment_status FROM orders WHERE id = ? FOR UPDATE";
        String updateSql = """
        UPDATE orders
        SET status = ?,
            payment_method = ?,
            payment_status = ?
        WHERE id = ?
    """;

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Boolean alreadyPaid = null;
                try (PreparedStatement lockPs = conn.prepareStatement(lockSql)) {
                    lockPs.setInt(1, orderId);
                    try (ResultSet rs = lockPs.executeQuery()) {
                        if (rs.next()) {
                            alreadyPaid = rs.getBoolean("payment_status");
                        }
                    }
                }

                if (alreadyPaid == null) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, "Paid");
                    ps.setString(2, paymentMethod);
                    ps.setBoolean(3, true);
                    ps.setInt(4, orderId);
                    if (ps.executeUpdate() <= 0) {
                        conn.rollback();
                        return false;
                    }
                }

                if (!alreadyPaid && !finalizeReservedStockForOrder(conn, orderId)) {
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
            return false;
        }
    }

    // ========== GHN shipping integration ==========

    /**
     * Update GHN shipping info after pushing order to GHN.
     */
    public boolean updateGhnInfo(int orderId, String ghnOrderId, String ghnTrackingCode,
                                  String ghnStatus, String errorMessage) {
        String sql = """
            UPDATE orders
            SET ghn_order_id = ?,
                ghn_tracking_code = ?,
                ghn_status = ?,
                ghn_pushed_at = CASE WHEN ? IS NOT NULL THEN NOW() ELSE ghn_pushed_at END,
                ghn_last_sync_at = NOW(),
                ghn_error_message = ?
            WHERE id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ghnOrderId);
            ps.setString(2, ghnTrackingCode);
            ps.setString(3, ghnStatus);
            ps.setString(4, ghnOrderId);
            ps.setString(5, errorMessage);
            ps.setInt(6, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("updateGhnInfo error for order {}", orderId, e);
            return false;
        }
    }

    /**
     * Update GHN status from sync/callback.
     */
    public boolean updateGhnStatus(int orderId, String ghnStatus, String ghnTrackingCode) {
        String sql = """
            UPDATE orders
            SET ghn_status = ?,
                ghn_tracking_code = COALESCE(?, ghn_tracking_code),
                ghn_last_sync_at = NOW()
            WHERE id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ghnStatus);
            ps.setString(2, ghnTrackingCode);
            ps.setInt(3, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("updateGhnStatus error for order {}", orderId, e);
            return false;
        }
    }

    /**
     * Find orders that need to be pushed to GHN (Confirmed status, not yet pushed).
     */
    public List<Order> getOrdersPendingGhnPush() {
        List<Order> list = new ArrayList<>();
        String sql = """
            SELECT o.*,
                   u.fullname AS customer_fullname,
                   u.phone AS customer_phone
            FROM orders o
            LEFT JOIN users u ON o.user_id = u.id
            WHERE o.status = 'Confirmed'
              AND o.ghn_order_id IS NULL
            ORDER BY o.createdAt ASC
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapOrder(rs));
            }
        } catch (Exception e) {
            log.error("getOrdersPendingGhnPush error", e);
        }
        return list;
    }

    /**
     * Find orders already pushed to GHN that need status sync.
     */
    public List<Order> getOrdersForGhnSync() {
        List<Order> list = new ArrayList<>();
        String sql = """
            SELECT o.*,
                   u.fullname AS customer_fullname,
                   u.phone AS customer_phone
            FROM orders o
            LEFT JOIN users u ON o.user_id = u.id
            WHERE o.ghn_order_id IS NOT NULL
              AND o.status NOT IN ('Delivered', 'Completed', 'Cancelled')
            ORDER BY o.ghn_last_sync_at ASC
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapOrder(rs));
            }
        } catch (Exception e) {
            log.error("getOrdersForGhnSync error", e);
        }
        return list;
    }

    public void autoCompleteDeliveredOrders() {
        String sql = "SELECT id FROM orders WHERE status = 'Delivered' " +
                "AND status_updated_at < NOW() - INTERVAL 1 DAY";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int orderId = rs.getInt("id");
                updateStatus(orderId, "Completed", 1);
            }
        } catch (Exception e) {
            log.error("Auto-complete delivered orders error", e);
        }
    }
    public boolean markOrderAsPaid(int orderId) {

    String sql =
            "UPDATE orders " +
            "SET status = 'Paid', " +
            "payment_status = TRUE " +
            "WHERE id = ?";

    try (Connection conn = DBContext.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, orderId);

        return ps.executeUpdate() > 0;

    } catch (Exception e) {
        log.error("DB error", e);
    }

    return false;
}
    public boolean updateOrderStatusRaw(int orderId, String status) {

    String sql = "UPDATE orders SET status = ? WHERE id = ?";

    try (Connection conn = DBContext.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, status);
        ps.setInt(2, orderId);

        return ps.executeUpdate() > 0;

    } catch (Exception e) {
        log.error("DB error", e);
    }

    return false;
}
}
