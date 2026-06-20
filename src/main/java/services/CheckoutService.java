package services;

import Context.DBContext;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.InventoryBatchDAO;
import DAO.OrderDAO;
import DAO.OrderSignDAO;
import DAO.CertificateDAO;
import DAO.PaymentTransactionDAO;
import DAO.ProductDAO;
import DAO.PromotionDAO;
import DAO.UserDAO;
import Model.CartItem;
import Model.Coupon;
import Model.CouponValidationResult;
import Model.Order;
import Model.OrderItem;
import Model.PaymentTransaction;
import Model.Product;
import Model.User;
import Util.AppConfig;
import Util.CertificateGenerator;
import Util.DigitalSigner;
import Util.RSAKeyGenerator;
import services.payment.BankTransferDetails;
import services.payment.PaymentProvider;
import services.payment.PaymentRegistry;
import services.payment.PaymentResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
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
    private final PromotionDAO promotionDAO;
    private final ProductPricingService pricingService;
    private final OrderSignDAO orderSignDAO;
    private final CertificateDAO certificateDAO;

    public CheckoutService(ProductDAO productDAO, UserDAO userDAO, CouponDao couponDao,
                           OrderDAO orderDAO, PaymentTransactionDAO paymentTransactionDAO,
                           CartDAO cartDAO, OrderEmailService orderEmailService) {
        this(productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, new InventoryBatchDAO(),
                new OrderSignDAO(), new CertificateDAO());
    }

    public CheckoutService(ProductDAO productDAO, UserDAO userDAO, CouponDao couponDao,
                           OrderDAO orderDAO, PaymentTransactionDAO paymentTransactionDAO,
                           CartDAO cartDAO, OrderEmailService orderEmailService,
                           InventoryBatchDAO inventoryBatchDAO) {
        this(productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, inventoryBatchDAO,
                new OrderSignDAO(), new CertificateDAO());
    }

    public CheckoutService(ProductDAO productDAO, UserDAO userDAO, CouponDao couponDao,
                           OrderDAO orderDAO, PaymentTransactionDAO paymentTransactionDAO,
                           CartDAO cartDAO, OrderEmailService orderEmailService,
                           InventoryBatchDAO inventoryBatchDAO,
                           OrderSignDAO orderSignDAO, CertificateDAO certificateDAO) {
        this.productDAO = productDAO;
        this.userDAO = userDAO;
        this.couponDao = couponDao;
        this.orderDAO = orderDAO;
        this.paymentTransactionDAO = paymentTransactionDAO;
        this.cartDAO = cartDAO;
        this.orderEmailService = orderEmailService;
        this.inventoryBatchDAO = inventoryBatchDAO;
        this.promotionDAO = new PromotionDAO();
        this.pricingService = new ProductPricingService(this.promotionDAO);
        this.orderSignDAO = orderSignDAO;
        this.certificateDAO = certificateDAO;
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
        return processCheckout(user, cart, fullAddress, note, couponState, paymentMethodKey, shippingFee, null);
    }

    public CheckoutResult processCheckout(User user, Map<Integer, CartItem> cart,
                                          String fullAddress, String note,
                                          CouponValidationResult couponState,
                                          String paymentMethodKey,
                                          int shippingFee,
                                          String reservedTransferReference) throws Exception {
        return processCheckout(
                user,
                cart,
                user.getFullname(),
                user.getPhone(),
                fullAddress,
                note,
                couponState,
                paymentMethodKey,
                shippingFee,
                reservedTransferReference
        );
    }

    public CheckoutResult processCheckout(User user, Map<Integer, CartItem> cart,
                                          String recipientFullname, String recipientPhone,
                                          String shippingAddress, String note,
                                          CouponValidationResult couponState,
                                          String paymentMethodKey,
                                          int shippingFee,
                                          String reservedTransferReference) throws Exception {
        BigDecimal totalAmount = calculateCartTotal(cart);

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (CartItem item : cart.values()) {
                    Product latestProduct = productDAO.getProductByIdForUpdate(conn, item.getProduct().getId());
                    if (latestProduct == null) {
                        conn.rollback();
                        return new CheckoutResult(false, "Có sản phẩm không còn tồn tại.");
                    }
                    if (latestProduct.getAvailablePurchaseQuantity() < item.getQuantity()) {
                        conn.rollback();
                        return new CheckoutResult(false, "Sản phẩm \"" + latestProduct.getName() + "\" chỉ còn " + latestProduct.getAvailablePurchaseQuantity() + " sản phẩm có thể mua.");
                    }
                    pricingService.applyPricing(conn, latestProduct, Timestamp.valueOf(LocalDateTime.now()));
                    if (!samePromotion(item.getProduct(), latestProduct)
                            || item.getProduct().getEffectivePrice().compareTo(latestProduct.getEffectivePrice()) != 0) {
                        conn.rollback();
                        return new CheckoutResult(false, "Khuyến mãi của một số sản phẩm đã thay đổi. Vui lòng kiểm tra lại giỏ hàng trước khi đặt hàng.");
                    }
                    item.setProduct(latestProduct);
                }

                totalAmount = calculateCartTotal(cart);

                BigDecimal discount = BigDecimal.ZERO;
                if (couponState != null && couponState.isValid()) {
                    Coupon latestCoupon = couponDao.getValidCouponByCode(conn, couponState.getCoupon().getCode());
                    if (latestCoupon == null) {
                        conn.rollback();
                        return new CheckoutResult(false, "Mã giảm giá không hợp lệ hoặc đã hết hạn.");
                    }
                    if (latestCoupon.getMinOrder() != null && totalAmount.compareTo(latestCoupon.getMinOrder()) < 0) {
                        conn.rollback();
                        return new CheckoutResult(false, "Đơn hàng chưa đạt giá trị tối thiểu để dùng mã giảm giá.");
                    }
                    if (!userDAO.markDiscountAsUsed(conn, user.getId())) {
                        conn.rollback();
                        return new CheckoutResult(false, "Tài khoản này đã sử dụng mã giảm giá trước đó.");
                    }
                    if (!couponDao.increaseUsedIfAvailable(conn, latestCoupon.getId())) {
                        conn.rollback();
                        return new CheckoutResult(false, "Mã giảm giá đã hết lượt sử dụng.");
                    }
                    discount = calculateDiscount(totalAmount, latestCoupon);
                }

                BigDecimal finalTotal = totalAmount.add(BigDecimal.valueOf(shippingFee)).subtract(discount);

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

                Order order = buildOrderSnapshot(
                        user,
                        recipientFullname,
                        recipientPhone,
                        shippingAddress,
                        note,
                        finalTotal,
                        paymentResult.getPaymentMethodDb(),
                        paymentResult.isPaymentStatus()
                );
                order.setSubtotal(totalAmount);
                order.setShippingFee(BigDecimal.valueOf(shippingFee));
                order.setDiscountAmount(discount);

                int orderId = orderDAO.saveOrder(conn, order);
                if (orderId <= 0) {
                    conn.rollback();
                    return new CheckoutResult(false, "Không tạo được đơn hàng.");
                }

                BankTransferDetails bankTransferDetails = BankTransferDetails.fromConfig();
                if ("BANK_TRANSFER".equalsIgnoreCase(paymentResult.getPaymentMethodDb()) && !bankTransferDetails.isConfigured()) {
                    conn.rollback();
                    return new CheckoutResult(false, "Thiếu cấu hình chuyển khoản ngân hàng. Vui lòng liên hệ quản trị viên.");
                }

                for (CartItem ci : cart.values()) {
                    Product product = ci.getProduct();
                    if (product.isFlashSale()) {
                        Integer promotionId = product.getActivePromotionId();
                        Integer remaining = product.getFlashSaleRemainingQuantity();
                        if (promotionId == null || remaining == null || ci.getQuantity() > remaining
                                || !promotionDAO.reserveFlashSaleQuantity(conn, promotionId, product.getId(), ci.getQuantity())) {
                            conn.rollback();
                            return new CheckoutResult(false, "Khuyến mãi của một số sản phẩm đã thay đổi. Vui lòng kiểm tra lại giỏ hàng trước khi đặt hàng.");
                        }
                    }

                    if (!productDAO.reserveStock(conn, product.getId(), ci.getQuantity())) {
                        conn.rollback();
                        return new CheckoutResult(false, "Sản phẩm \"" + product.getName() + "\" đã hết hàng trong lúc thanh toán.");
                    }

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(orderId);
                    orderItem.setProductId(product.getId());
                    orderItem.setQuantity(ci.getQuantity());
                    orderItem.setPrice(product.getEffectivePrice());
                    orderItem.setOriginalPrice(product.getOriginalPrice());
                    orderItem.setFinalPrice(product.getEffectivePrice());
                    orderItem.setDiscountAmount(product.getDiscountAmount());
                    orderItem.setPromotionId(product.getActivePromotionId());
                    orderItem.setPromotionName(product.getActivePromotionName());
                    orderItem.setPromotionType(product.getActivePromotionType());
                    orderItem.setProductNameSnapshot(product.getName());
                    orderItem.setProductImageSnapshot(product.getImage());

                    if (!orderDAO.saveOrderItem(conn, orderItem)) {
                        conn.rollback();
                        return new CheckoutResult(false, "Không lưu được chi tiết đơn hàng.");
                    }
                }

                PaymentTransaction paymentTransaction = buildPaymentTransaction(
                        user, orderId, finalTotal, paymentResult, bankTransferDetails, reservedTransferReference
                );
                int paymentTransactionId = paymentTransactionDAO.save(conn, paymentTransaction);
                if (paymentTransactionId <= 0) {
                    conn.rollback();
                    return new CheckoutResult(false, "Không tạo được giao dịch thanh toán.");
                }

                cartDAO.clearCart(conn, user.getId());

                RSAKeyGenerator rsa = new RSAKeyGenerator();
                PublicKey publicKey = rsa.getPublicKey();
                PrivateKey privateKey = rsa.getPrivateKey();
                String privateKeyBase64 = rsa.encodePrivateKey();
                String publicKeyBase64 = rsa.encodePublicKey();

                String orderData = buildOrderDataForSign(order, orderId, user);
                String orderHash = DigitalSigner.hashOrderData(orderData);

                String toolUrl = "/tools/CryptoToolMVC.exe";

                orderSignDAO.save(orderId, user.getId(), orderData, orderHash, publicKeyBase64);

                java.security.cert.X509Certificate cert = CertificateGenerator.generateX509(publicKey, privateKey, orderId, user.getId());
                String certPem = CertificateGenerator.encodeCertificate(cert);
                certificateDAO.save(orderId, user.getId(), String.valueOf(orderId), certPem, cert.getSubjectX500Principal().getName(), Timestamp.from(cert.getNotAfter().toInstant()));

                conn.commit();

                sendOrderConfirmationAsync(user, cart, orderId, order);

                CheckoutResult successResult = new CheckoutResult(true, "Đặt hàng thành công!");
                successResult.setOrderId(orderId);
                successResult.setPaymentMethodDb(paymentResult.getPaymentMethodDb());
                successResult.setPaymentTransaction(paymentTransaction);
                successResult.setTotalAmount(totalAmount);
                successResult.setShippingFee(shippingFee);
                successResult.setDiscount(discount);
                successResult.setFinalTotal(finalTotal);
                successResult.setPrivateKeyBase64(privateKeyBase64);
                successResult.setOrderHash(orderHash);
                successResult.setToolUrl(toolUrl);
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
                .map(item -> item.getProduct().getEffectivePrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    static Order buildOrderSnapshot(User user, String recipientFullname, String recipientPhone,
                                    String shippingAddress, String note, BigDecimal finalTotal,
                                    String paymentMethodDb, boolean paymentStatus) {
        Order order = new Order();
        order.setUserId(user.getId());
        order.setCustomerFullname(user.getFullname());
        order.setCustomerPhone(user.getPhone());
        order.setRecipientFullname(recipientFullname);
        order.setRecipientPhone(recipientPhone);
        order.setShippingAddress(shippingAddress);
        order.setNote(note);
        order.setTotalAmount(finalTotal);
        order.setStatus(resolveInitialOrderStatus(paymentMethodDb, paymentStatus));
        order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        order.setPayment_method(paymentMethodDb);
        order.setPayment_status(paymentStatus);
        return order;
    }

    private String buildOrderDataForSign(Order order, int orderId, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("orderId=").append(orderId);
        sb.append("&userId=").append(user.getId());
        sb.append("&totalAmount=").append(order.getTotalAmount().toPlainString());
        sb.append("&paymentMethod=").append(order.getPayment_method());
        sb.append("&createdAt=").append(order.getCreatedAt().toString());
        sb.append("&shippingAddress=").append(order.getShippingAddress());
        sb.append("&recipientName=").append(order.getRecipientFullname());
        sb.append("&recipientPhone=").append(order.getRecipientPhone());
        return sb.toString();
    }

    private static String resolveInitialOrderStatus(String paymentMethodDb, boolean paymentStatus) {
        if ("BANK_TRANSFER".equalsIgnoreCase(paymentMethodDb)) {
            return "Awaiting Payment";
        }
        if (paymentStatus && !"COD".equalsIgnoreCase(paymentMethodDb)) {
            return "Paid";
        }
        return "Pending";
    }

    private BigDecimal calculateDiscount(BigDecimal cartTotal, Coupon coupon) {
        if (coupon == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount;
        if ("fixed".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = coupon.getDiscountValue() == null ? BigDecimal.ZERO : coupon.getDiscountValue();
        } else if (coupon.getDiscountPercent() > 0) {
            discount = cartTotal
                    .multiply(BigDecimal.valueOf(coupon.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = BigDecimal.ZERO;
        }
        if (coupon.getMaxDiscount() != null && coupon.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.min(coupon.getMaxDiscount());
        }
        return discount.min(cartTotal);
    }

    private PaymentTransaction buildPaymentTransaction(User user, int orderId, BigDecimal finalTotal,
                                                       PaymentResult paymentResult,
                                                       BankTransferDetails bankTransferDetails,
                                                       String reservedTransferReference) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderId(orderId);
        transaction.setUserId(user.getId());
        transaction.setProviderKey(paymentResult.getPaymentMethodDb());
        transaction.setProviderDisplayName(resolveProviderDisplayName(paymentResult.getPaymentMethodDb()));
        transaction.setAmount(finalTotal);
        transaction.setCurrency(bankTransferDetails.getCurrency());
        transaction.setStatus(paymentResult.getTransactionStatus());
        transaction.setVerificationStatus(isPendingPaymentVerification(paymentResult) ? "PENDING" : "NOT_REQUIRED");
        transaction.setVerificationMessage(paymentResult.getMessage());
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        if ("BANK_TRANSFER".equalsIgnoreCase(paymentResult.getPaymentMethodDb()) && paymentResult.isPendingVerification()) {
            String transferReference = hasText(reservedTransferReference)
                    ? reservedTransferReference.trim()
                    : bankTransferDetails.buildTransferReference(orderId);
            transaction.setTransferReference(transferReference);
            int pendingMinutes = AppConfig.getInt("payment.bank.pending-minutes", 10);
            transaction.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusMinutes(pendingMinutes)));
        }
        return transaction;
    }

    private boolean isPendingPaymentVerification(PaymentResult paymentResult) {
        return paymentResult.isPendingVerification()
                || ("VNPAY".equalsIgnoreCase(paymentResult.getPaymentMethodDb())
                && !paymentResult.isPaymentStatus());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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
            case "VNPAY":
                return "VNPAY";
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
            oi.setPrice(ci.getProduct().getEffectivePrice());
            oi.setOriginalPrice(ci.getProduct().getOriginalPrice());
            oi.setFinalPrice(ci.getProduct().getEffectivePrice());
            oi.setDiscountAmount(ci.getProduct().getDiscountAmount());
            oi.setPromotionId(ci.getProduct().getActivePromotionId());
            oi.setPromotionName(ci.getProduct().getActivePromotionName());
            oi.setPromotionType(ci.getProduct().getActivePromotionType());
            oi.setProductNameSnapshot(ci.getProduct().getName());
            oi.setProductImageSnapshot(ci.getProduct().getImage());
            oi.setProduct(ci.getProduct());
            confirmedItems.add(oi);
        });

        order.setId(orderId);
        String userEmail = user.getEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            orderEmailService.sendOrderConfirmationAsync(userEmail, order, confirmedItems);
        }
    }

    private boolean samePromotion(Product cartProduct, Product latestProduct) {
        Integer cartPromotionId = cartProduct == null ? null : cartProduct.getActivePromotionId();
        Integer latestPromotionId = latestProduct == null ? null : latestProduct.getActivePromotionId();
        if (cartPromotionId == null && latestPromotionId == null) {
            return true;
        }
        return cartPromotionId != null && cartPromotionId.equals(latestPromotionId);
    }
}
