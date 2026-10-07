package com.petshop.repository;

import com.petshop.model.CustomerRepurchaseSuggestion;
import com.petshop.model.Order;
import com.petshop.model.OrderItem;
import com.petshop.model.OrderLog;
import com.petshop.model.OrderStatusHistory;
import java.math.BigDecimal;
import java.util.List;

public interface OrderRepositoryCustom {

    int saveOrder(Order order);

    boolean saveOrderItem(OrderItem item);

    List<Order> getAllOrders();

    List<Order> getOrdersPage(int page, int size, String statusFilter, String keyword);

    int countOrders(String statusFilter, String keyword);

    Order getOrderById(int orderId);

    List<OrderItem> getOrderItems(int orderId);

    boolean updateStatus(int orderId, String status);

    boolean updateStatus(int orderId, String status, int changedByUserId);

    int countPendingOrders();

    int countOrdersAwaitingPaymentVerification();

    boolean updatePaymentVerification(int orderId, String verificationStatus, String verificationMessage);

    boolean updatePaymentVerification(int orderId, String verificationStatus, String verificationMessage,
                                      int actorUserId);

    boolean updatePaymentStatus(int orderId, boolean paid);

    boolean markAwaitingPaymentOrderPaid(int orderId);

    boolean releaseReservedStockForOrder(int orderId);

    boolean finalizeReservedStockForOrder(int orderId);

    List<Order> getOrdersByUserId(int userId);

    boolean cancelOrderByUser(int orderId, int userId);

    boolean isWithinCancellationWindow(int orderId);

    List<OrderStatusHistory> getStatusHistory(int orderId);

    List<OrderLog> getOrderLogs(int orderId);

    List<CustomerRepurchaseSuggestion> getRepurchaseSuggestions(int userId, int minDaysSincePurchase, int limit);

    BigDecimal getTodayRevenue();

    int countPendingOrdersByUserId(int userId);

    int countCompletedOrdersByUserId(int userId);

    BigDecimal getTotalSpentByUserId(int userId);

    int countOrdersByUserId(int userId);

    boolean updateOrderPaymentStatus(int orderId, String status);

    boolean markOnlinePaymentAwaiting(int orderId, String paymentMethod);

    boolean markOnlinePaymentPaid(int orderId, String paymentMethod);

    boolean markOnlinePaymentPaidAndFinalize(int orderId, String paymentMethod);

    boolean updateGhnStatus(int orderId, String ghnStatus, String ghnTrackingCode);

    boolean updateGhnInfo(int orderId, String ghnOrderId, String ghnTrackingCode,
                          String ghnStatus, String errorMessage);

    List<Order> getOrdersPendingGhnPush();

    List<Order> getOrdersForGhnSync();

    void autoCompleteDeliveredOrders();

    boolean markOrderAsPaid(int orderId);

    boolean updateOrderStatusRaw(int orderId, String status);

    boolean updateOrderStatus(int orderId, String status);

    // Payment-transaction lifecycle moved here from PaymentTransactionDAO
    // (single direction: OrderRepoImpl -> PaymentTransactionRepository).
    int expirePendingTransactions();

    void attachLatestToOrders(List<Order> orders);

    void applyTransaction(Order order, com.petshop.model.PaymentTransaction transaction);
}
