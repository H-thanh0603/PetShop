package services;

import Context.DBContext;
import DAO.*;
import Model.*;
import services.payment.BankTransferDetails;
import services.payment.PaymentProvider;
import services.payment.PaymentRegistry;
import services.payment.PaymentResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckoutService {

    private final ProductDAO productDAO;
    private final UserDAO userDAO;
    private final CouponDao couponDao;
    private final OrderDAO orderDAO;
    private final PaymentTransactionDAO paymentTransactionDAO;
    private final CartDAO cartDAO;
    private final OrderEmailService orderEmailService;
    private final InventoryBatchDAO inventoryBatchDAO;

    public CheckoutService(ProductDAO productDAO, UserDAO userDAO, CouponDao couponDao,
                           OrderDAO orderDAO, PaymentTransactionDAO paymentTransactionDAO,
                           CartDAO cartDAO, OrderEmailService orderEmailService) {
        this(productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, new InventoryBatchDAO());
    }

    public CheckoutService(ProductDAO productDAO, UserDAO userDAO, CouponDao couponDao,
                           OrderDAO orderDAO, PaymentTransactionDAO paymentTransactionDAO,
                           CartDAO cartDAO, OrderEmailService orderEmailService,
                           InventoryBatchDAO inventoryBatchDAO) {
        this.productDAO = productDAO;
        this.userDAO = userDAO;
        this.couponDao = couponDao;
        this.orderDAO = orderDAO;
        this.paymentTransactionDAO = paymentTransactionDAO;
        this.cartDAO = cartDAO;
        this.orderEmailService = orderEmailService;
        this.inventoryBatchDAO = inventoryBatchDAO;
    }

    public CheckoutResult processCheckout(User user, Map<Integer, CartItem> cart,
                                          String fullAddress, String note,
                                          CouponValidationResult couponState,
                                          String paymentMethodKey) throws Exception {
        return processCheckout(user, cart, fullAddress, note, couponState, paymentMethodKey, 30000);
    }

    public CheckoutResult processCheckout(User user, Map<Integer, CartItem> cart,
                                          String fullAddress, String note,
                                          CouponValidationResult couponState,
                                          String paymentMethodKey,
                                          int shippingFee) throws Exception {
        BigDecimal totalAmount = calculateCartTotal(cart);

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Check stock
                for (CartItem item : cart.values()) {
                    Product latestProduct = productDAO.getProductByIdForUpdate(conn, item.getProduct().getId());
                    if (latestProduct == null) {
                        conn.rollback();
                        return new CheckoutResult(false, "Có sản phẩm không còn tồn tại.");
                    }
                    if (latestProduct.getStock() < item.getQuantity()) {
                        conn.rollback();
                        return new CheckoutResult(false, "Sản phẩm \"" + latestProduct.getName() + "\" chỉ còn " + latestProduct.getStock() + " sản phẩm.");
                    }
                    item.setProduct(latestProduct);
                }

                // 2. Process Coupon
                BigDecimal discount = BigDecimal.ZERO;
                if (couponState != null && couponState.isValid()) {
                    if (!userDAO.markDiscountAsUsed(conn, user.getId())) {
                        conn.rollback();
                        return new CheckoutResult(false, "Tài khoản này đã sử dụng mã giảm giá trước đó.");
                    }

                    Coupon latestCoupon = couponDao.getValidCouponByCode(conn, couponState.getCoupon().getCode());
                    if (latestCoupon == null) {
                        conn.rollback();
                        return new CheckoutResult(false, "Mã giảm giá không hợp lệ hoặc đã hết hạn.");
                    }
                    if (!couponDao.increaseUsedIfAvailable(conn, latestCoupon.getId())) {
                        conn.rollback();
                        return new CheckoutResult(false, "Mã giảm giá đã hết lượt sử dụng.");
                    }
                    discount = calculateDiscount(totalAmount, latestCoupon);
                }

                BigDecimal finalTotal = totalAmount.add(BigDecimal.valueOf(shippingFee)).subtract(discount);

                // 3. Resolve Payment
                PaymentProvider provider = PaymentRegistry.getInstance().get(paymentMethodKey);
                if (provider == null) {
                    conn.rollback();
                    return new CheckoutResult(false, "Phương thức thanh toán không hợp lệ.");
                }
                PaymentResult paymentResult = provider.process(finalTotal.doubleValue());
                if (!paymentResult.isSuccess()) {
                    conn.rollback();
                    return new CheckoutResult(false, paymentResult.getMessage());
                }

                // 4. Create Order
                Order order = new Order();
                order.setUserId(user.getId());
                order.setFullname(user.getFullname());
                order.setPhone(user.getPhone());
                order.setAddress(fullAddress);
                order.setNote(note);
                order.setTotalAmount(finalTotal);
                order.setStatus("Pending");
                order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                order.setPayment_method(paymentResult.getPaymentMethodDb());
                order.setPayment_status(paymentResult.isPaymentStatus());

                int orderId = orderDAO.saveOrder(conn, order);
                if (orderId <= 0) {
                    conn.rollback();
                    return new CheckoutResult(false, "Không tạo được đơn hàng.");
                }

                BankTransferDetails bankTransferDetails = BankTransferDetails.fromConfig();
                if ("BANK_TRANSFER".equalsIgnoreCase(paymentResult.getPaymentMethodDb())
                        && !bankTransferDetails.isConfigured()) {
                    conn.rollback();
                    return new CheckoutResult(false, "Thiếu cấu hình chuyển khoản ngân hàng. Vui lòng liên hệ quản trị viên.");
                }

                // 5. Update Stock and save Order Items
                for (CartItem ci : cart.values()) {
                    boolean hasTrackedBatches = inventoryBatchDAO.hasTrackedBatchesForProduct(
                            conn, ci.getProduct().getId()
                    );
                    if (hasTrackedBatches && !inventoryBatchDAO.consumeProductStock(
                            conn,
                            ci.getProduct().getId(),
                            ci.getQuantity(),
                            orderId,
                            user.getId(),
                            "Checkout order #" + orderId
                    )) {
                        conn.rollback();
                        return new CheckoutResult(false, "Sản phẩm \"" + ci.getProduct().getName() + "\" không còn lô hàng hợp lệ để bán.");
                    }

                    if (!productDAO.decreaseStock(conn, ci.getProduct().getId(), ci.getQuantity())) {
                        conn.rollback();
                        return new CheckoutResult(false, "Sản phẩm \"" + ci.getProduct().getName() + "\" đã hết hàng trong lúc thanh toán.");
                    }

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(orderId);
                    orderItem.setProductId(ci.getProduct().getId());
                    orderItem.setQuantity(ci.getQuantity());
                    orderItem.setPrice(ci.getProduct().getPrice());

                    if (!orderDAO.saveOrderItem(conn, orderItem)) {
                        conn.rollback();
                        return new CheckoutResult(false, "Không lưu được chi tiết đơn hàng.");
                    }
                }

                // 6. Save Payment Transaction
                PaymentTransaction paymentTransaction = buildPaymentTransaction(
                        user, orderId, finalTotal, paymentResult, bankTransferDetails
                );
                int paymentTransactionId = paymentTransactionDAO.save(conn, paymentTransaction);
                if (paymentTransactionId <= 0) {
                    conn.rollback();
                    return new CheckoutResult(false, "Không tạo được giao dịch thanh toán.");
                }

                // 7. Commit Transaction
                cartDAO.clearCart(conn, user.getId());
                conn.commit();

                // 8. Fire async tasks
                sendOrderConfirmationAsync(user, cart, orderId, order);

                CheckoutResult successResult = new CheckoutResult(true, "Đặt hàng thành công!");
                successResult.setOrderId(orderId);
                successResult.setPaymentMethodDb(paymentResult.getPaymentMethodDb());
                successResult.setPaymentTransaction(paymentTransaction);
                return successResult;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private BigDecimal calculateCartTotal(Map<Integer, CartItem> cart) {
        return cart.values().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal cartTotal, Coupon coupon) {
        if (coupon == null || coupon.getDiscountPercent() <= 0) {
            return BigDecimal.ZERO;
        }
        return cartTotal
                .multiply(BigDecimal.valueOf(coupon.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private PaymentTransaction buildPaymentTransaction(User user, int orderId, BigDecimal finalTotal,
                                                       PaymentResult paymentResult,
                                                       BankTransferDetails bankTransferDetails) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderId(orderId);
        transaction.setUserId(user.getId());
        transaction.setProviderKey(paymentResult.getPaymentMethodDb());
        transaction.setProviderDisplayName(resolveProviderDisplayName(paymentResult.getPaymentMethodDb()));
        transaction.setAmount(finalTotal);
        transaction.setCurrency(bankTransferDetails.getCurrency());
        transaction.setStatus(paymentResult.getTransactionStatus());
        transaction.setVerificationStatus(paymentResult.isPendingVerification() ? "PENDING" : "NOT_REQUIRED");
        transaction.setVerificationMessage(paymentResult.getMessage());
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        if (paymentResult.isPendingVerification()) {
            transaction.setTransferReference(bankTransferDetails.buildTransferReference(orderId));
            int pendingHours = 2; // Default or from AppConfig
            transaction.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusHours(pendingHours)));
        }
        return transaction;
    }

    private String resolveProviderDisplayName(String paymentMethodDb) {
        if (paymentMethodDb == null) {
            return "Unknown";
        }
        switch (paymentMethodDb.toUpperCase()) {
            case "COD":
                return "Cash On Delivery";
            case "MOMO":
                return "MoMo";
            case "BANK_TRANSFER":
                return "Bank Transfer";
            default:
                return paymentMethodDb;
        }
    }

    private void sendOrderConfirmationAsync(User user, Map<Integer, CartItem> cart, int orderId, Order order) {
        final List<OrderItem> confirmedItems = new ArrayList<>();
        cart.values().forEach(ci -> {
            OrderItem oi = new OrderItem();
            oi.setOrderId(orderId);
            oi.setProductId(ci.getProduct().getId());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getProduct().getPrice());
            oi.setProduct(ci.getProduct());
            confirmedItems.add(oi);
        });

        order.setId(orderId);
        String userEmail = user.getEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            orderEmailService.sendOrderConfirmationAsync(userEmail, order, confirmedItems);
        }
    }
}
