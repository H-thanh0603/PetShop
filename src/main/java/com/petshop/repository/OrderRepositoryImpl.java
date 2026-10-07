package com.petshop.repository;

import com.petshop.model.CustomerRepurchaseSuggestion;
import com.petshop.model.Order;
import com.petshop.model.OrderItem;
import com.petshop.model.OrderLog;
import com.petshop.model.OrderStatus;
import com.petshop.model.OrderStatusHistory;
import com.petshop.model.PaymentTransaction;
import com.petshop.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(OrderRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Write methods run inside a programmatic template instead of a
     * declarative {@code @Transactional}: a caught {@link DataAccessException}
     * mid-write must roll the work back and surface {@code false}. With a
     * declarative boundary, any inner repository proxy that saw the exception
     * has already marked the shared transaction rollback-only, so the boundary
     * commit would throw {@code UnexpectedRollbackException} instead of
     * returning {@code false}. The template rolls back eagerly on any escaping
     * exception — the direct port of the JDBC DAO's {@code conn.rollback()}.
     */
    private final TransactionTemplate tx;

    /** Business-failure marker thrown out of an {@link #inTx} body. */
    private static final class TxFailedException extends DataAccessException {
        private TxFailedException() {
            super("order write failed; rolling back");
        }
    }

    private <T> T inTx(java.util.function.Supplier<T> body) {
        return tx.execute(s -> body.get());
    }

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderLogRepository orderLogRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CouponRepository couponRepository;
    private final PromotionRepository promotionRepository;

    @Autowired
    public OrderRepositoryImpl(@org.springframework.context.annotation.Lazy OrderRepository orderRepository,
                               ProductRepository productRepository,
                               OrderLogRepository orderLogRepository,
                               OrderStatusHistoryRepository orderStatusHistoryRepository,
                               InventoryBatchRepository inventoryBatchRepository,
                               PaymentTransactionRepository paymentTransactionRepository,
                               CouponRepository couponRepository,
                               PromotionRepository promotionRepository,
                               PlatformTransactionManager txManager) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderLogRepository = orderLogRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.couponRepository = couponRepository;
        this.promotionRepository = promotionRepository;
        this.tx = new TransactionTemplate(txManager);
    }

    private static void rollbackOnly() {
        try {
            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
        } catch (Exception ignored) {
            // No ambient transaction (should not happen for these methods).
        }
    }

    // ---- shared helpers (mirror the DAO's mapping + batch loaders) ----

    private void fillTransients(Order order) {
        if (order == null) {
            return;
        }
        if (order.getUser() != null) {
            order.setCustomerFullname(order.getUser().getFullname());
            order.setCustomerPhone(order.getUser().getPhone());
        }
    }

    private OrderItem mapItem(Object[] row) {
        // Explicit columns: oi.* (13: id, order_id, product_id, quantity, price,
        // original_price, final_price, discount_amount, promotion_id,
        // promotion_name, promotion_type, product_name_snapshot,
        // product_image_snapshot) + product_name + product_image.
        OrderItem item = new OrderItem();
        item.setId(((Number) row[0]).intValue());
        item.setOrderId(((Number) row[1]).intValue());
        item.setProductId(((Number) row[2]).intValue());
        item.setQuantity(((Number) row[3]).intValue());
        item.setPrice(row[4] == null ? null : new BigDecimal(row[4].toString()));
        item.setOriginalPrice(row[5] == null ? null : new BigDecimal(row[5].toString()));
        item.setFinalPrice(row[6] == null ? null : new BigDecimal(row[6].toString()));
        item.setDiscountAmount(row[7] == null ? null : new BigDecimal(row[7].toString()));
        item.setPromotionId(row[8] == null ? null : ((Number) row[8]).intValue());
        item.setPromotionName((String) row[9]);
        item.setPromotionType((String) row[10]);
        item.setProductNameSnapshot((String) row[11]);
        item.setProductImageSnapshot((String) row[12]);
        Product product = new Product();
        product.setId(item.getProductId());
        product.setName((String) row[13]);
        product.setImage((String) row[14]);
        item.setProduct(product);
        return item;
    }

    private static final String ITEM_COLUMNS =
            "oi.id, oi.order_id, oi.product_id, oi.quantity, oi.price, oi.original_price, "
            + "oi.final_price, oi.discount_amount, oi.promotion_id, oi.promotion_name, "
            + "oi.promotion_type, oi.product_name_snapshot, oi.product_image_snapshot";

    private List<OrderItem> loadItemsNative(List<Integer> orderIds) {
        if (orderIds.isEmpty()) {
            return List.of();
        }
        String placeholders = orderIds.stream().map(i -> "?").collect(java.util.stream.Collectors.joining(","));
        jakarta.persistence.Query query = entityManager.createNativeQuery(
                "SELECT " + ITEM_COLUMNS + ", "
                        + "COALESCE(oi.product_name_snapshot, p.name) as product_name, "
                        + "COALESCE(oi.product_image_snapshot, p.image) as product_image "
                        + "FROM order_items oi LEFT JOIN products p ON oi.product_id = p.id "
                        + "WHERE oi.order_id IN (" + placeholders + ")");
        int idx = 1;
        for (int id : orderIds) {
            query.setParameter(idx++, id);
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<OrderItem> items = new ArrayList<>();
        for (Object[] row : rows) {
            items.add(mapItem(row));
        }
        return items;
    }

    private void loadItemsForOrders(List<Order> orders) {
        if (orders.isEmpty()) {
            return;
        }
        List<Integer> ids = orders.stream().map(Order::getId).toList();
        Map<Integer, List<OrderItem>> itemMap = new HashMap<>();
        for (OrderItem item : loadItemsNative(ids)) {
            itemMap.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
        }
        for (Order order : orders) {
            order.setItems(itemMap.getOrDefault(order.getId(), new ArrayList<>()));
        }
    }

    private void attachSignatureStatusToOrders(List<Order> orders) {
        if (orders.isEmpty()) {
            return;
        }
        String placeholders = orders.stream().map(o -> "?").collect(java.util.stream.Collectors.joining(","));
        jakarta.persistence.Query query = entityManager.createNativeQuery(
                "SELECT o.id, os.id AS sign_id, osig.verify_status FROM orders o "
                        + "LEFT JOIN order_signs os ON os.order_id = o.id "
                        + "LEFT JOIN order_signatures osig ON osig.order_id = o.id "
                        + "WHERE o.id IN (" + placeholders + ")");
        int idx = 1;
        for (Order order : orders) {
            query.setParameter(idx++, order.getId());
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        Map<Integer, Order> orderMap = new HashMap<>();
        for (Order order : orders) {
            orderMap.put(order.getId(), order);
        }
        java.util.Set<Integer> handled = new java.util.HashSet<>();
        for (Object[] row : rows) {
            int orderId = ((Number) row[0]).intValue();
            if (!handled.add(orderId)) {
                continue; // first matching row wins (DAO break semantics)
            }
            Order order = orderMap.get(orderId);
            if (order == null) {
                continue;
            }
            String signId = row[1] == null ? null : row[1].toString();
            String verifyStatus = row[2] == null ? null : row[2].toString();
            if (signId == null) {
                order.setSignatureStatus("none");
                order.setSignatureStatusCssClass("text-muted");
                order.setSignatureStatusLabel("—");
            } else if ("verified".equalsIgnoreCase(verifyStatus)) {
                order.setSignatureStatus("verified");
                order.setSignatureStatusCssClass("text-success");
                order.setSignatureStatusLabel("Đã ký ✓");
            } else if ("failed".equalsIgnoreCase(verifyStatus)) {
                order.setSignatureStatus("failed");
                order.setSignatureStatusCssClass("text-danger");
                order.setSignatureStatusLabel("Thất bại ✗");
            } else {
                order.setSignatureStatus("pending");
                order.setSignatureStatusCssClass("text-warning");
                order.setSignatureStatusLabel("Chờ ký");
            }
        }
    }

    // ---- creates ----

    @Override
    public int saveOrder(Order order) {
        try {
            return inTx(() -> {
                Order managed = new Order();
                managed.setUserId(order.getUserId());
                managed.setFullname(order.getRecipientFullname());
                managed.setPhone(order.getRecipientPhone());
                managed.setAddress(order.getShippingAddress());
                managed.setRecipientFullname(order.getRecipientFullname());
                managed.setRecipientPhone(order.getRecipientPhone());
                managed.setShippingAddress(order.getShippingAddress());
                managed.setNote(order.getNote());
                managed.setSubtotal(order.getSubtotal());
                managed.setShippingFee(order.getShippingFee());
                managed.setDiscountAmount(order.getDiscountAmount());
                managed.setTotalAmount(order.getTotalAmount());
                managed.setStatus(order.getStatus());
                managed.setPayment_method(order.getPayment_method());
                managed.setPayment_status(order.getPayment_status());
                managed.setCreatedAt(order.getCreatedAt());
                entityManager.persist(managed);
                entityManager.flush();
                int orderId = managed.getId();
                // Detach so later fetches in the same transaction hit the database
                // fresh (a managed instance would skip JOIN FETCH initialization).
                entityManager.detach(managed);
                if (!orderLogRepository.insert(orderId, "CUSTOMER", order.getUserId(),
                        "CREATE_ORDER", null, order.getStatus(), "Khách tạo đơn")) {
                    throw new TxFailedException();
                }
                return orderId;
            });
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return -1;
        }
    }

    @Override
    public boolean saveOrderItem(OrderItem item) {
        try {
            return Boolean.TRUE.equals(inTx(() -> {
                OrderItem managed = new OrderItem();
                managed.setOrderId(item.getOrderId());
                managed.setProductId(item.getProductId());
                managed.setQuantity(item.getQuantity());
                managed.setPrice(item.getPrice());
                managed.setOriginalPrice(item.getOriginalPrice());
                managed.setFinalPrice(item.getFinalPrice());
                managed.setDiscountAmount(item.getDiscountAmount());
                managed.setPromotionId(item.getPromotionId());
                managed.setPromotionName(item.getPromotionName());
                managed.setPromotionType(item.getPromotionType());
                managed.setProductNameSnapshot(item.getProductNameSnapshot());
                managed.setProductImageSnapshot(item.getProductImageSnapshot());
                entityManager.persist(managed);
                entityManager.flush();
                return true;
            }));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    // ---- reads ----

    @Override
    @Transactional
    public List<Order> getAllOrders() {
        try {
            expirePendingTransactions();
            List<Order> list = orderRepository.findAllWithUser();
            for (Order order : list) {
                fillTransients(order);
            }
            if (!list.isEmpty()) {
                loadItemsForOrders(list);
                attachLatestToOrders(list);
            }
            return list;
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return List.of();
        }
    }

    @Override
    @Transactional
    public List<Order> getOrdersPage(int page, int size, String statusFilter, String keyword) {
        try {
            expirePendingTransactions();
            StringBuilder sql = new StringBuilder(
                    "SELECT o.id FROM orders o JOIN users u ON u.id = o.user_id WHERE 1=1");
            List<Object> params = new ArrayList<>();
            if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
                sql.append(" AND o.status = ?");
                params.add(statusFilter);
            }
            if (keyword != null && !keyword.isEmpty()) {
                sql.append(" AND (CAST(o.id AS CHAR) LIKE ? OR u.fullname LIKE ? "
                        + "OR COALESCE(o.recipient_fullname, o.fullname) LIKE ? "
                        + "OR COALESCE(o.recipient_phone, o.phone) LIKE ? "
                        + "OR COALESCE(o.shipping_address, o.address) LIKE ?)");
                String kw = "%" + keyword + "%";
                params.add(kw);
                params.add(kw);
                params.add(kw);
                params.add(kw);
                params.add(kw);
            }
            sql.append(" ORDER BY o.createdAt DESC LIMIT ? OFFSET ?");
            jakarta.persistence.Query query = entityManager.createNativeQuery(sql.toString());
            int idx = 1;
            for (Object param : params) {
                query.setParameter(idx++, param);
            }
            query.setParameter(idx++, size);
            query.setParameter(idx, Math.max(0, (page - 1) * size));
            @SuppressWarnings("unchecked")
            List<Number> ids = query.getResultList();
            List<Order> list = new ArrayList<>();
            for (Number id : ids) {
                Order order = orderRepository.findByIdWithUser(id.intValue());
                if (order != null) {
                    fillTransients(order);
                    list.add(order);
                }
            }
            if (!list.isEmpty()) {
                loadItemsForOrders(list);
                attachLatestToOrders(list);
                attachSignatureStatusToOrders(list);
            }
            return list;
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return List.of();
        }
    }

    @Override
    @Transactional
    public int countOrders(String statusFilter, String keyword) {
        try {
            expirePendingTransactions();
            StringBuilder sql = new StringBuilder(
                    "SELECT COUNT(*) FROM orders o JOIN users u ON u.id = o.user_id WHERE 1=1");
            List<Object> params = new ArrayList<>();
            if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
                sql.append(" AND o.status = ?");
                params.add(statusFilter);
            }
            if (keyword != null && !keyword.isEmpty()) {
                sql.append(" AND (CAST(o.id AS CHAR) LIKE ? OR u.fullname LIKE ? "
                        + "OR COALESCE(o.recipient_fullname, o.fullname) LIKE ? "
                        + "OR COALESCE(o.recipient_phone, o.phone) LIKE ? "
                        + "OR COALESCE(o.shipping_address, o.address) LIKE ?)");
                String kw = "%" + keyword + "%";
                params.add(kw);
                params.add(kw);
                params.add(kw);
                params.add(kw);
                params.add(kw);
            }
            jakarta.persistence.Query query = entityManager.createNativeQuery(sql.toString());
            int idx = 1;
            for (Object param : params) {
                query.setParameter(idx++, param);
            }
            Object result = query.getSingleResult();
            return result == null ? 0 : ((Number) result).intValue();
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return 0;
        }
    }

    @Override
    @Transactional
    public Order getOrderById(int orderId) {
        try {
            expirePendingTransactions();
            Order order = orderRepository.findByIdWithUser(orderId);
            if (order == null) {
                return null;
            }
            fillTransients(order);
            order.setItems(loadItemsNative(List.of(orderId)));
            PaymentTransaction latest =
                    paymentTransactionRepository.findLatestByOrderId(orderId);
            applyTransaction(order, latest);
            return order;
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return null;
        }
    }

    @Override
    @Transactional
    public List<OrderItem> getOrderItems(int orderId) {
        try {
            return loadItemsNative(List.of(orderId));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public List<Order> getOrdersByUserId(int userId) {
        try {
            expirePendingTransactions();
            List<Order> list = orderRepository.findByUserIdWithUser(userId);
            for (Order order : list) {
                fillTransients(order);
            }
            if (!list.isEmpty()) {
                loadItemsForOrders(list);
                attachLatestToOrders(list);
            }
            return list;
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return List.of();
        }
    }

    @Override
    @Transactional
    public List<OrderStatusHistory> getStatusHistory(int orderId) {
        return orderStatusHistoryRepository.getHistoryByOrderId(orderId);
    }

    @Override
    @Transactional
    public List<OrderLog> getOrderLogs(int orderId) {
        return orderLogRepository.getByOrderId(orderId);
    }

    @Override
    @Transactional
    public List<CustomerRepurchaseSuggestion> getRepurchaseSuggestions(int userId, int minDaysSincePurchase,
                                                                      int limit) {
        List<CustomerRepurchaseSuggestion> suggestions = new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT o.id, oi.product_id, p.name, oi.quantity, DATEDIFF(NOW(), o.createdAt) "
                            + "FROM orders o JOIN order_items oi ON oi.order_id = o.id "
                            + "JOIN products p ON p.id = oi.product_id "
                            + "WHERE o.user_id = ? AND o.status = 'Completed' "
                            + "AND o.createdAt <= DATE_SUB(NOW(), INTERVAL ? DAY) "
                            + "AND p.is_active = 1 AND p.stock > 0 "
                            + "AND (LOWER(p.name) LIKE '%cát%' OR LOWER(p.name) LIKE '%cat vệ sinh%' "
                            + "OR LOWER(p.name) LIKE '%hạt%' OR LOWER(p.name) LIKE '%pate%' "
                            + "OR LOWER(p.name) LIKE '%bánh thưởng%' OR LOWER(p.name) LIKE '%snack%' "
                            + "OR LOWER(p.name) LIKE '%sữa tắm%' OR LOWER(p.category) LIKE '%thức ăn%' "
                            + "OR LOWER(p.category) LIKE '%cát vệ sinh%') "
                            + "ORDER BY o.createdAt DESC, oi.id ASC LIMIT ?")
                    .setParameter(1, userId)
                    .setParameter(2, minDaysSincePurchase)
                    .setParameter(3, limit)
                    .getResultList();
            for (Object[] row : rows) {
                CustomerRepurchaseSuggestion suggestion = new CustomerRepurchaseSuggestion();
                suggestion.setOrderId(((Number) row[0]).intValue());
                suggestion.setProductId(((Number) row[1]).intValue());
                suggestion.setProductName((String) row[2]);
                suggestion.setQuantity(((Number) row[3]).intValue());
                suggestion.setDaysSincePurchase(((Number) row[4]).intValue());
                suggestions.add(suggestion);
            }
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
        }
        return suggestions;
    }

    @Override
    @Transactional
    public BigDecimal getTodayRevenue() {
        try {
            BigDecimal result = orderRepository.todayRevenueNative();
            return result != null ? result : BigDecimal.ZERO;
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return BigDecimal.ZERO;
        }
    }

    @Override
    @Transactional
    public int countPendingOrders() {
        try {
            return orderRepository.countPendingNative();
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return 0;
        }
    }

    @Override
    @Transactional
    public int countOrdersAwaitingPaymentVerification() {
        try {
            expirePendingTransactions();
            Object result = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM payment_transactions pt JOIN ("
                            + "SELECT MAX(id) AS latest_id FROM payment_transactions GROUP BY order_id"
                            + ") latest ON latest.latest_id = pt.id "
                            + "WHERE pt.verification_status = 'PENDING'")
                    .getSingleResult();
            return result == null ? 0 : ((Number) result).intValue();
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return 0;
        }
    }

    @Override
    @Transactional
    public int countPendingOrdersByUserId(int userId) {
        try {
            return orderRepository.countPendingByUserNative(userId);
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return 0;
        }
    }

    @Override
    @Transactional
    public int countCompletedOrdersByUserId(int userId) {
        try {
            return orderRepository.countCompletedByUserNative(userId);
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return 0;
        }
    }

    @Override
    @Transactional
    public BigDecimal getTotalSpentByUserId(int userId) {
        try {
            BigDecimal result = orderRepository.totalSpentByUserNative(userId);
            return result != null ? result : BigDecimal.ZERO;
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return BigDecimal.ZERO;
        }
    }

    @Override
    @Transactional
    public int countOrdersByUserId(int userId) {
        try {
            return orderRepository.countByUserNative(userId);
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return 0;
        }
    }

    @Override
    @Transactional
    public boolean isWithinCancellationWindow(int orderId) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null || order.getCreatedAt() == null) {
                return false;
            }
            long elapsedSeconds = (System.currentTimeMillis() - order.getCreatedAt().getTime()) / 1000;
            return elapsedSeconds <= 3600;
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
            return false;
        }
    }

    // ---- writes ----

    @Override
    public boolean updateStatus(int orderId, String status) {
        try {
            return updateStatus(orderId, status, -1);
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean updateStatus(int orderId, String status, int changedByUserId) {
        try {
            return Boolean.TRUE.equals(inTx(() -> {
                Order locked = orderRepository.findByIdForUpdate(orderId);
                if (locked == null) {
                    return false;
                }
                String currentStatus = locked.getStatus();
                String currentPaymentMethod = locked.getPayment_method();
                boolean currentPaymentPaid = locked.getPayment_status();
                int orderOwnerId = locked.getUserId();
                OrderStatus fromStatus = OrderStatus.fromString(currentStatus);
                OrderStatus toStatus = OrderStatus.fromString(status);
                if (fromStatus == null || toStatus == null || !fromStatus.canTransitionTo(toStatus)) {
                    log.warn("Invalid order status transition");
                    return false;
                }
                if (requiresPaidOnlineOrder(currentPaymentMethod, currentPaymentPaid, toStatus)) {
                    log.warn("Rejected unpaid online order transition");
                    return false;
                }
                boolean wasCancelled = "Cancelled".equalsIgnoreCase(currentStatus);
                boolean willBeCancelled = "Cancelled".equalsIgnoreCase(status);
                boolean willFinalizeCodPayment = isCodPayment(currentPaymentMethod)
                        && !currentPaymentPaid
                        && ("Delivered".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status));
                if (!wasCancelled && willBeCancelled) {
                    if (!releaseReservedStockForOrder(orderId)) {
                        return false;
                    }
                } else if (wasCancelled && !willBeCancelled) {
                    for (OrderItem item : loadItemsNative(List.of(orderId))) {
                        if (!productRepository.reserveStockAmbient(item.getProductId(), item.getQuantity())) {
                            return false;
                        }
                    }
                } else if (willFinalizeCodPayment) {
                    if (!finalizeReservedStockForOrder(orderId)) {
                        return false;
                    }
                }
                if (orderRepository.setStatusNative(orderId, status) <= 0) {
                    throw new TxFailedException();
                }
                int actor = changedByUserId > 0 ? changedByUserId : 1;
                String actorType = "SYSTEM";
                if (changedByUserId > 0) {
                    actorType = (changedByUserId == orderOwnerId) ? "CUSTOMER" : "ADMIN";
                }
                if (!orderStatusHistoryRepository.insertHistory(orderId, currentStatus, status, actor)) {
                    throw new TxFailedException();
                }
                if (!orderLogRepository.insert(orderId, actorType, actor, "UPDATE_STATUS",
                        currentStatus, status, "Update status")) {
                    throw new TxFailedException();
                }
                if (willFinalizeCodPayment && !updatePaymentStatus(orderId, true)) {
                    throw new TxFailedException();
                }
                return true;
            }));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    private static boolean requiresPaidOnlineOrder(String paymentMethod, boolean paymentPaid,
                                                  OrderStatus targetStatus) {
        if (paymentPaid || isCodPayment(paymentMethod)) {
            return false;
        }
        return targetStatus == OrderStatus.CONFIRMED
                || targetStatus == OrderStatus.SHIPPING
                || targetStatus == OrderStatus.DELIVERED
                || targetStatus == OrderStatus.COMPLETED;
    }

    private static boolean isCodPayment(String paymentMethod) {
        return paymentMethod == null || paymentMethod.isBlank() || "COD".equalsIgnoreCase(paymentMethod);
    }

    @Override
    public boolean updatePaymentVerification(int orderId, String verificationStatus, String verificationMessage) {
        try {
            return updatePaymentVerification(orderId, verificationStatus, verificationMessage, 1);
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean updatePaymentVerification(int orderId, String verificationStatus,
                                             String verificationMessage, int actorUserId) {
        String normalizedStatus = normalizeVerificationStatus(verificationStatus);
        if (normalizedStatus == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(inTx(() -> {
                PaymentTransaction transaction =
                        paymentTransactionRepository.findLatestByOrderIdForUpdate(orderId);
                if (transaction == null) {
                    return false;
                }
                String oldVerificationStatus = transaction.getVerificationStatus();
                String transactionStatus = mapTransactionStatus(normalizedStatus);
                boolean paid = "VERIFIED".equals(normalizedStatus);
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                Timestamp verifiedAt = paid ? now : null;
                if (!paymentTransactionRepository.updateVerificationStatusTx(transaction.getId(),
                        transactionStatus, normalizedStatus,
                        normalizeVerificationMessage(verificationMessage, normalizedStatus), now, verifiedAt)) {
                    return false;
                }
                if (!updatePaymentStatus(orderId, paid)) {
                    throw new TxFailedException();
                }
                if (paid) {
                    if (!markAwaitingPaymentOrderPaid(orderId)) {
                        throw new TxFailedException();
                    }
                    if (!finalizeReservedStockForOrder(orderId)) {
                        throw new TxFailedException();
                    }
                } else if ("FAILED".equals(normalizedStatus) || "EXPIRED".equals(normalizedStatus)) {
                    if (!releaseReservedStockForOrder(orderId)) {
                        throw new TxFailedException();
                    }
                }
                if (!orderLogRepository.insert(orderId, "ADMIN", actorUserId,
                        "UPDATE_PAYMENT_VERIFICATION", oldVerificationStatus, normalizedStatus,
                        normalizeVerificationMessage(verificationMessage, normalizedStatus))) {
                    throw new TxFailedException();
                }
                return true;
            }));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    private static String normalizeVerificationStatus(String verificationStatus) {
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

    private static String mapTransactionStatus(String verificationStatus) {
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

    private static String normalizeVerificationMessage(String verificationMessage, String verificationStatus) {
        String trimmed = verificationMessage == null ? "" : verificationMessage.trim();
        if (!trimmed.isEmpty()) {
            return trimmed;
        }
        switch (verificationStatus) {
            case "VERIFIED":
                return "Admin confirmed bank transfer payment.";
            case "FAILED":
                return "Admin marked reconciliation unmatched.";
            case "EXPIRED":
                return "Bank transfer payment wait expired.";
            case "PENDING":
            default:
                return "Waiting for bank transfer reconciliation.";
        }
    }

    @Override
    public boolean updatePaymentStatus(int orderId, boolean paid) {
        try {
            return Boolean.TRUE.equals(inTx(() -> orderRepository.setPaymentStatusNative(orderId, paid) > 0));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean markAwaitingPaymentOrderPaid(int orderId) {
        try {
            return Boolean.TRUE.equals(inTx(() -> orderRepository.markAwaitingPaidNative(orderId, "Paid", "Awaiting Payment") > 0));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean releaseReservedStockForOrder(int orderId) {
        try {
            return Boolean.TRUE.equals(inTx(() -> {
                for (OrderItem item : loadItemsNative(List.of(orderId))) {
                    if ("FLASH_SALE".equalsIgnoreCase(item.getPromotionType()) && item.getPromotionId() != null) {
                        if (promotionRepository.releaseFlashSaleQuantity(
                                item.getPromotionId(), item.getProductId(), item.getQuantity()) <= 0) {
                            throw new TxFailedException();
                        }
                    }
                    if (!productRepository.releaseReservedStockAmbient(item.getProductId(), item.getQuantity())) {
                        return false;
                    }
                }
                return true;
            }));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean finalizeReservedStockForOrder(int orderId) {
        try {
            return Boolean.TRUE.equals(inTx(() -> {
                Order order = orderRepository.findByIdForUpdate(orderId);
                int actorUserId = order == null ? 1 : order.getUserId();
                for (OrderItem item : loadItemsNative(List.of(orderId))) {
                    if (!productRepository.finalizeReservedStockAmbient(item.getProductId(), item.getQuantity())) {
                        return false;
                    }
                    if (inventoryBatchRepository.hasTrackedBatchesForProduct(item.getProductId())) {
                        boolean consumed = inventoryBatchRepository.consumeProductStock(
                                item.getProductId(), item.getQuantity(), orderId, actorUserId,
                                "Finalize reserved stock for order #" + orderId);
                        if (!consumed) {
                            log.warn("Insufficient batch stock for product id={} while finalizing order #{}. Proceeding anyway.",
                                    item.getProductId(), orderId);
                        }
                    }
                }
                return true;
            }));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean cancelOrderByUser(int orderId, int userId) {
        try {
            return Boolean.TRUE.equals(inTx(() -> {
                Order locked = orderRepository.findByIdAndUserIdForUpdate(orderId, userId);
                if (locked == null) {
                    return false;
                }
                String currentStatus = locked.getStatus();
                Timestamp createdAt = locked.getCreatedAt();
                OrderStatus fromStatus = OrderStatus.fromString(currentStatus);
                if (fromStatus == null || !fromStatus.canTransitionTo(OrderStatus.CANCELLED)) {
                    return false;
                }
                if (createdAt != null) {
                    long elapsedSeconds = (System.currentTimeMillis() - createdAt.getTime()) / 1000;
                    if (elapsedSeconds > 3600) {
                        return false;
                    }
                }
                if (!releaseReservedStockForOrder(orderId)) {
                    throw new TxFailedException();
                }
                if (orderRepository.setStatusNative(orderId, "Cancelled") <= 0) {
                    throw new TxFailedException();
                }
                if (!orderStatusHistoryRepository.insertHistory(orderId, currentStatus, "Cancelled", userId)) {
                    throw new TxFailedException();
                }
                if (!orderLogRepository.insert(orderId, "CUSTOMER", userId,
                        "CANCEL_ORDER", currentStatus, "Cancelled", "Customer cancelled order")) {
                    throw new TxFailedException();
                }
                return true;
            }));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean updateOrderPaymentStatus(int orderId, String status) {
        try {
            return Boolean.TRUE.equals(inTx(() -> orderRepository.setPaymentStatusStringNative(orderId, status) > 0));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean markOnlinePaymentAwaiting(int orderId, String paymentMethod) {
        try {
            return Boolean.TRUE.equals(inTx(() -> orderRepository.markOnlinePaymentNative(orderId, "Awaiting Payment", paymentMethod, false) > 0));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean markOnlinePaymentPaid(int orderId, String paymentMethod) {
        try {
            return Boolean.TRUE.equals(inTx(() -> orderRepository.markOnlinePaymentNative(orderId, "Paid", paymentMethod, true) > 0));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean markOnlinePaymentPaidAndFinalize(int orderId, String paymentMethod) {
        try {
            return Boolean.TRUE.equals(inTx(() -> {
                Order locked = orderRepository.findByIdForUpdate(orderId);
                if (locked == null) {
                    return false;
                }
                boolean alreadyPaid = locked.getPayment_status();
                if (orderRepository.markOnlinePaymentNative(orderId, "Paid", paymentMethod, true) <= 0) {
                    throw new TxFailedException();
                }
                if (!alreadyPaid && !finalizeReservedStockForOrder(orderId)) {
                    throw new TxFailedException();
                }
                return true;
            }));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateGhnInfo(int orderId, String ghnOrderId, String ghnTrackingCode,
                                 String ghnStatus, String errorMessage) {
        // The ghn_* columns do not exist on schema (drift): the old code threw
        // and returned false. Preserved exactly.
        return false;
    }

    @Override
    @Transactional
    public boolean updateGhnStatus(int orderId, String ghnStatus, String ghnTrackingCode) {
        // Same drift as updateGhnInfo: always false, like the old code.
        return false;
    }

    @Override
    @Transactional
    public List<Order> getOrdersPendingGhnPush() {
        // Same drift: the old query threw on missing ghn_* columns -> empty.
        return List.of();
    }

    @Override
    @Transactional
    public List<Order> getOrdersForGhnSync() {
        // Same drift as above.
        return List.of();
    }

    @Override
    public void autoCompleteDeliveredOrders() {
        try {
            inTx(() -> {
                List<Order> delivered = orderRepository.findDeliveredBefore(
                        new Timestamp(System.currentTimeMillis() - 86400000L));
                for (Order order : delivered) {
                    updateStatus(order.getId(), "Completed", 1);
                }
                return null;
            });
        } catch (RuntimeException e) {
            log.error("Auto-complete delivered orders error", e);
        }
    }

    @Override
    public boolean markOrderAsPaid(int orderId) {
        try {
            return Boolean.TRUE.equals(inTx(() -> orderRepository.markOrderAsPaidNative(orderId) > 0));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean updateOrderStatusRaw(int orderId, String status) {
        try {
            return Boolean.TRUE.equals(inTx(() -> orderRepository.setStatusNative(orderId, status) > 0));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    @Override
    public boolean updateOrderStatus(int orderId, String status) {
        try {
            return Boolean.TRUE.equals(inTx(() -> orderRepository.setStatusNative(orderId, status) > 0));
        } catch (RuntimeException e) {
            log.error("DB error", e);
            return false;
        }
    }

    // ---- payment-transaction lifecycle (moved from PaymentTransactionDAO) ----

    @Override
    public int expirePendingTransactions() {
        int pendingMinutes = com.petshop.util.AppConfig.getInt("payment.bank.pending-minutes", 10);
        try {
            Integer updated = inTx(() -> {
                List<PaymentTransaction> expired =
                        paymentTransactionRepository.findExpiredPending(pendingMinutes);
                int count = 0;
                for (PaymentTransaction transaction : expired) {
                    transaction.setStatus("EXPIRED");
                    transaction.setVerificationStatus("EXPIRED");
                    if (transaction.getVerificationMessage() == null
                            || transaction.getVerificationMessage().isBlank()) {
                        transaction.setVerificationMessage("Bank transfer payment wait expired.");
                    }
                    transaction.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    paymentTransactionRepository.save(transaction);
                    int orderId = transaction.getOrderId();
                    if (!releaseReservedStockForOrder(orderId)
                            || !updatePaymentStatus(orderId, false)
                            || !orderLogRepository.insert(orderId, "SYSTEM", null,
                            "PAYMENT_EXPIRED", "PENDING_VERIFICATION", "EXPIRED",
                            "Payment hold expired, reserved stock released.")) {
                        throw new TxFailedException();
                    }
                    count++;
                }
                return count;
            });
            return updated == null ? 0 : updated;
        } catch (RuntimeException e) {
            log.error("Failed to expire pending payment transactions", e);
            return 0;
        }
    }

    @Override
    @Transactional
    public void attachLatestToOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        try {
            List<Integer> ids = orders.stream().map(Order::getId).toList();
            Map<Integer, PaymentTransaction> latestByOrder = new HashMap<>();
            for (PaymentTransaction transaction : paymentTransactionRepository.findLatestForOrders(ids)) {
                latestByOrder.put(transaction.getOrderId(), transaction);
            }
            for (Order order : orders) {
                applyTransaction(order, latestByOrder.get(order.getId()));
            }
        } catch (RuntimeException e) {
            log.error("DB error", e);
            rollbackOnly();
        }
    }

    @Override
    @Transactional
    public void applyTransaction(Order order, PaymentTransaction transaction) {
        if (order == null || transaction == null) {
            return;
        }
        order.setPaymentTransactionStatus(transaction.getStatus());
        order.setPaymentVerificationStatus(transaction.getVerificationStatus());
        order.setPaymentReference(transaction.getTransferReference());
        order.setPaymentVerificationMessage(transaction.getVerificationMessage());
        order.setPaymentVerifiedAt(transaction.getVerifiedAt());
    }
}
