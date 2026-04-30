package services.payment;

import Context.DBContext;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import DAO.UserDAO;
import Model.Order;
import Model.OrderItem;
import Model.PaymentTransaction;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentTransactionService {
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;

    private final OrderDAO orderDAO = new OrderDAO();
    private final CouponDao couponDao = new CouponDao();
    private final UserDAO userDAO = new UserDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();

    public ReservationResult reserveOnlinePayment(ReservationRequest request) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Order order = request.getOrder();

                if (request.isDiscountReserved()) {
                    if (!userDAO.markDiscountAsUsed(conn, order.getUserId())) {
                        conn.rollback();
                        return ReservationResult.failure("Tai khoan nay da su dung ma giam gia truoc do.");
                    }
                    if (request.getCouponId() != null && !couponDao.increaseUsedIfAvailable(conn, request.getCouponId())) {
                        conn.rollback();
                        return ReservationResult.failure("Ma giam gia da het luot su dung.");
                    }
                }

                int orderId = orderDAO.saveOrder(conn, order);
                if (orderId <= 0) {
                    conn.rollback();
                    return ReservationResult.failure("Khong tao duoc don hang tam cho thanh toan online.");
                }

                for (OrderItem item : request.getOrderItems()) {
                    item.setOrderId(orderId);
                    if (!orderDAO.saveOrderItem(conn, item)) {
                        conn.rollback();
                        return ReservationResult.failure("Khong luu duoc chi tiet don hang.");
                    }
                }

                PaymentTransaction tx = new PaymentTransaction();
                tx.setOrderId(orderId);
                tx.setUserId(order.getUserId());
                tx.setProvider(request.getProvider());
                tx.setProviderOrderId(request.getProviderOrderId());
                tx.setRequestId(request.getRequestId());
                tx.setAmount(order.getTotalAmount());
                tx.setCouponId(request.getCouponId());
                tx.setDiscountReserved(request.isDiscountReserved());
                tx.setStatus("PENDING");
                tx.setProviderMessage("Dang cho thanh toan.");

                int transactionId = paymentTransactionDAO.create(conn, tx);
                if (transactionId <= 0) {
                    conn.rollback();
                    return ReservationResult.failure("Khong tao duoc giao dich thanh toan.");
                }

                for (OrderItem item : request.getOrderItems()) {
                    if (!request.getProductStockConsumer().reserve(conn, item.getProductId(), item.getQuantity())) {
                        conn.rollback();
                        return ReservationResult.failure("San pham trong gio hang da het hang trong luc tao giao dich.");
                    }
                }

                conn.commit();
                return ReservationResult.success(orderId, transactionId);
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return ReservationResult.failure("Da co loi khi khoi tao giao dich thanh toan.");
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ReservationResult.failure("Khong mo duoc ket noi CSDL de tao giao dich.");
    }

    public FinalizeResult confirmSuccess(String providerOrderId, String paymentToken,
                                         String providerTransactionId, String responseCode,
                                         String providerMessage, String rawPayload) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PaymentTransaction tx = paymentTransactionDAO.getByProviderOrderIdForUpdate(conn, providerOrderId);
                if (tx == null) {
                    conn.rollback();
                    return FinalizeResult.failure("Khong tim thay giao dich thanh toan.", null, null);
                }

                if ("SUCCESS".equalsIgnoreCase(tx.getStatus())) {
                    conn.rollback();
                    return FinalizeResult.success(tx.getOrderId(), tx.getUserId(), true);
                }

                if (!"PENDING".equalsIgnoreCase(tx.getStatus())) {
                    conn.rollback();
                    return FinalizeResult.failure("Giao dich khong con o trang thai cho thanh toan.", tx.getOrderId(), tx.getUserId());
                }

                if (!orderDAO.updatePaymentDetails(
                        conn,
                        tx.getOrderId(),
                        true,
                        paymentToken,
                        providerTransactionId,
                        providerMessage
                )) {
                    conn.rollback();
                    return FinalizeResult.failure("Khong cap nhat duoc thong tin thanh toan cho don hang.", tx.getOrderId(), tx.getUserId());
                }

                if (!paymentTransactionDAO.updateStatus(
                        conn,
                        tx.getId(),
                        "SUCCESS",
                        paymentToken,
                        providerTransactionId,
                        responseCode,
                        providerMessage,
                        tx.getRedirectUrl(),
                        rawPayload,
                        Timestamp.valueOf(LocalDateTime.now())
                )) {
                    conn.rollback();
                    return FinalizeResult.failure("Khong cap nhat duoc ket qua giao dich thanh toan.", tx.getOrderId(), tx.getUserId());
                }

                List<OrderItem> orderItems = orderDAO.getOrderItems(conn, tx.getOrderId());
                cartDAO.consumeCartItems(conn, tx.getUserId(), orderItems);

                conn.commit();
                return FinalizeResult.success(tx.getOrderId(), tx.getUserId(), false);
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return FinalizeResult.failure("Da co loi khi xac nhan thanh toan thanh cong.", null, null);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return FinalizeResult.failure("Khong mo duoc ket noi CSDL de xac nhan thanh toan.", null, null);
    }

    public FinalizeResult failTransaction(String providerOrderId, String finalStatus,
                                          String responseCode, String providerMessage,
                                          String providerTransactionId, String rawPayload) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PaymentTransaction tx = paymentTransactionDAO.getByProviderOrderIdForUpdate(conn, providerOrderId);
                if (tx == null) {
                    conn.rollback();
                    return FinalizeResult.failure("Khong tim thay giao dich thanh toan.", null, null);
                }

                if (!"PENDING".equalsIgnoreCase(tx.getStatus())) {
                    conn.rollback();
                    return FinalizeResult.success(tx.getOrderId(), tx.getUserId(), true);
                }

                FinalizeResult result = cancelLockedTransaction(
                        conn,
                        tx,
                        finalStatus,
                        responseCode,
                        providerMessage,
                        providerTransactionId,
                        rawPayload
                );
                if (!result.isSuccess()) {
                    conn.rollback();
                    return result;
                }

                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return FinalizeResult.failure("Da co loi khi huy giao dich thanh toan.", null, null);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return FinalizeResult.failure("Khong mo duoc ket noi CSDL de huy giao dich.", null, null);
    }

    public boolean updateGatewayInit(String providerOrderId, PaymentGatewayInitResult initResult) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PaymentTransaction tx = paymentTransactionDAO.getByProviderOrderIdForUpdate(conn, providerOrderId);
                if (tx == null) {
                    conn.rollback();
                    return false;
                }

                boolean updated = paymentTransactionDAO.updateGatewayInit(
                        conn,
                        tx.getId(),
                        initResult.getRequestId(),
                        initResult.getRedirectUrl(),
                        initResult.getResponseCode(),
                        initResult.getMessage(),
                        initResult.getRawResponse()
                );
                if (!updated) {
                    conn.rollback();
                    return false;
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void releaseExpiredPendingTransactions(int userId) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Timestamp expiredBefore = Timestamp.valueOf(LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES));
                List<PaymentTransaction> expiredTransactions = paymentTransactionDAO.getExpiredPendingTransactions(conn, userId, expiredBefore);
                for (PaymentTransaction tx : expiredTransactions) {
                    FinalizeResult result = cancelLockedTransaction(
                            conn,
                            tx,
                            "EXPIRED",
                            "TIMEOUT",
                            "Giao dich thanh toan da het han.",
                            tx.getProviderTransactionId(),
                            tx.getRawPayload()
                    );
                    if (!result.isSuccess()) {
                        conn.rollback();
                        return;
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FinalizeResult cancelLockedTransaction(Connection conn, PaymentTransaction tx, String finalStatus,
                                                   String responseCode, String providerMessage,
                                                   String providerTransactionId, String rawPayload) throws Exception {
        if (!orderDAO.updateStatus(conn, tx.getOrderId(), "Cancelled")) {
            return FinalizeResult.failure("Khong cap nhat duoc trang thai don hang.", tx.getOrderId(), tx.getUserId());
        }

        if (tx.isDiscountReserved()) {
            if (tx.getCouponId() != null && !couponDao.decreaseUsed(conn, tx.getCouponId())) {
                return FinalizeResult.failure("Khong hoan lai duoc so luot ma giam gia.", tx.getOrderId(), tx.getUserId());
            }
            if (!userDAO.unmarkDiscountAsUsed(conn, tx.getUserId())) {
                return FinalizeResult.failure("Khong hoan lai duoc trang thai su dung ma giam gia.", tx.getOrderId(), tx.getUserId());
            }
        }

        if (!orderDAO.updatePaymentDetails(
                conn,
                tx.getOrderId(),
                false,
                null,
                providerTransactionId,
                providerMessage
        )) {
            return FinalizeResult.failure("Khong cap nhat duoc thong tin thanh toan that bai.", tx.getOrderId(), tx.getUserId());
        }

        if (!paymentTransactionDAO.updateStatus(
                conn,
                tx.getId(),
                finalStatus,
                null,
                providerTransactionId,
                responseCode,
                providerMessage,
                tx.getRedirectUrl(),
                rawPayload,
                Timestamp.valueOf(LocalDateTime.now())
        )) {
            return FinalizeResult.failure("Khong cap nhat duoc ket qua giao dich.", tx.getOrderId(), tx.getUserId());
        }

        return FinalizeResult.success(tx.getOrderId(), tx.getUserId(), false);
    }

    public static final class ReservationRequest {
        private final Order order;
        private final List<OrderItem> orderItems;
        private final String provider;
        private final String providerOrderId;
        private final String requestId;
        private final Integer couponId;
        private final boolean discountReserved;
        private final ProductStockConsumer productStockConsumer;

        public ReservationRequest(Order order, List<OrderItem> orderItems, String provider,
                                  String providerOrderId, String requestId, Integer couponId,
                                  boolean discountReserved, ProductStockConsumer productStockConsumer) {
            this.order = order;
            this.orderItems = orderItems;
            this.provider = provider;
            this.providerOrderId = providerOrderId;
            this.requestId = requestId;
            this.couponId = couponId;
            this.discountReserved = discountReserved;
            this.productStockConsumer = productStockConsumer;
        }

        public Order getOrder() {
            return order;
        }

        public List<OrderItem> getOrderItems() {
            return orderItems;
        }

        public String getProvider() {
            return provider;
        }

        public String getProviderOrderId() {
            return providerOrderId;
        }

        public String getRequestId() {
            return requestId;
        }

        public Integer getCouponId() {
            return couponId;
        }

        public boolean isDiscountReserved() {
            return discountReserved;
        }

        public ProductStockConsumer getProductStockConsumer() {
            return productStockConsumer;
        }
    }

    public interface ProductStockConsumer {
        boolean reserve(Connection conn, int productId, int quantity) throws Exception;
    }

    public static final class ReservationResult {
        private final boolean success;
        private final Integer orderId;
        private final Integer transactionId;
        private final String message;

        private ReservationResult(boolean success, Integer orderId, Integer transactionId, String message) {
            this.success = success;
            this.orderId = orderId;
            this.transactionId = transactionId;
            this.message = message;
        }

        public static ReservationResult success(int orderId, int transactionId) {
            return new ReservationResult(true, orderId, transactionId, null);
        }

        public static ReservationResult failure(String message) {
            return new ReservationResult(false, null, null, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public Integer getOrderId() {
            return orderId;
        }

        public Integer getTransactionId() {
            return transactionId;
        }

        public String getMessage() {
            return message;
        }
    }

    public static final class FinalizeResult {
        private final boolean success;
        private final Integer orderId;
        private final Integer userId;
        private final boolean alreadyProcessed;
        private final String message;

        private FinalizeResult(boolean success, Integer orderId, Integer userId,
                               boolean alreadyProcessed, String message) {
            this.success = success;
            this.orderId = orderId;
            this.userId = userId;
            this.alreadyProcessed = alreadyProcessed;
            this.message = message;
        }

        public static FinalizeResult success(Integer orderId, Integer userId, boolean alreadyProcessed) {
            return new FinalizeResult(true, orderId, userId, alreadyProcessed, null);
        }

        public static FinalizeResult failure(String message, Integer orderId, Integer userId) {
            return new FinalizeResult(false, orderId, userId, false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public Integer getOrderId() {
            return orderId;
        }

        public Integer getUserId() {
            return userId;
        }

        public boolean isAlreadyProcessed() {
            return alreadyProcessed;
        }

        public String getMessage() {
            return message;
        }
    }
}
