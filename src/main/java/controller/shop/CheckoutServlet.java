package controller.shop;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DAO.AddressDao;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.InventoryBatchDAO;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
import Context.DBContext;
import Util.VnpayUtil;

import Model.*;

import Util.ValidationUtil;
import Util.AppConfig;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import services.ShippingService;
import services.OrderEmailService;
import services.payment.BankTransferDetails;
import services.payment.PaymentProvider;
import services.payment.PaymentRegistry;
import services.payment.PaymentResult;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import services.InventoryService;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(CheckoutServlet.class);
    private static final int DEFAULT_PRODUCT_WEIGHT = 200;
    private static final int DEFAULT_SHIPPING_FEE = 30000;
    private static final int DEFAULT_PRICE = 500000;
    private static final String BANK_TRANSFER_REFERENCE_SESSION_KEY = "bankTransferReference";

    private final CouponDao couponDao = new CouponDao();
    private final AddressDao addressDAO = new AddressDao();
    private final InventoryService inventoryService = new InventoryService();
    private final CartDAO cartDAO = new CartDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final OrderEmailService orderEmailService = new OrderEmailService();
    private final InventoryBatchDAO inventoryBatchDAO = new InventoryBatchDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User userSession = (User) session.getAttribute("user");

        if (userSession == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        renderCheckout(request, response, session, userSession);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        User userSession = (User) session.getAttribute("user");

        if (userSession == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = trimToEmpty(request.getParameter("action"));
        if ("applyCoupon".equals(action)) {
            handleApplyCoupon(request, response, session, userSession);
            return;
        }

        try {
            placeOrderWithStockCheck(request, response, session, userSession);
        } catch (Exception e) {
            logger.error("Top-level unhandled exception in doPost for user id={}", userSession.getId(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", resolveCheckoutErrorMessage(e));
            write(response, errorResult);
        }
    }

    private void renderCheckout(HttpServletRequest request, HttpServletResponse response,
                                HttpSession session, User userSession)
            throws ServletException, IOException {
        User user = refreshUserSession(session, userSession.getId());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<Integer, CartItem> sessionBuyNowCart =
                (Map<Integer, CartItem>) session.getAttribute("buyNowCart");

        boolean isBuyNow = "true".equals(request.getParameter("buyNow"))
                || (sessionBuyNowCart != null && !sessionBuyNowCart.isEmpty());
        Map<Integer, CartItem> checkoutCart = isBuyNow
                ? loadBuyNowCart(session, request)
                : loadLatestCartForUser(session, user);

        if (checkoutCart == null || checkoutCart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }

        List<String> stockErrors = inventoryService.validateCartForCheckout(checkoutCart);
        if (!stockErrors.isEmpty()) {
            session.setAttribute("toastMessage", stockErrors.get(0));
            session.setAttribute("toastType", "warning");
            response.sendRedirect(request.getContextPath() + (isBuyNow ? "/shop" : "/cart"));
            return;
        }

        List<Address> addressList = addressDAO.getAddressesByUserId(user.getId());
        Address defaultAddress = resolvePrimaryAddress(user.getId(), addressList);
        CouponValidationResult couponState = resolveAppliedCouponFromSession(session, user);
        CheckoutSummary summary = buildCheckoutSummary(checkoutCart, defaultAddress, couponState.getCoupon());

        request.setAttribute("addressList", addressList);
        request.setAttribute("cartItems", new ArrayList<>(checkoutCart.values()));
        request.setAttribute("user", user);
        request.setAttribute("defaultAddress", defaultAddress);
        request.setAttribute("defaultShippingAddress", defaultAddress != null ? formatFullAddress(defaultAddress) : "");
        request.setAttribute("selectedAddressId", defaultAddress != null ? defaultAddress.getId() : null);
        request.setAttribute("totalAmount", summary.getTotalAmount());
        request.setAttribute("shippingFee", summary.getShippingFee());
        request.setAttribute("shippingMessage", summary.getShippingMessage());
        request.setAttribute("discount", summary.getDiscount());
        request.setAttribute("finalTotal", summary.getFinalTotal());
        request.setAttribute("appliedCouponCode",
                couponState.getCoupon() != null ? couponState.getCoupon().getCode() : "");
        request.setAttribute("isBuyNow", isBuyNow);

        BankTransferDetails bankTransferDetails = BankTransferDetails.fromConfig();
        String bankTransferReference = ensureBankTransferReference(session, user.getId(), bankTransferDetails);
        request.setAttribute("bankDisplayName", bankTransferDetails.getDisplayName());
        request.setAttribute("bankId", bankTransferDetails.getBankId());
        request.setAttribute("bankAccountNumber", bankTransferDetails.getAccountNumber());
        request.setAttribute("bankAccountName", bankTransferDetails.getAccountName());
        request.setAttribute("bankTransferPrefix", bankTransferDetails.getTransferPrefix());
        request.setAttribute("bankTransferReference", bankTransferReference);
        request.setAttribute("bankPaymentTtlSeconds", AppConfig.getInt("payment.bank.pending-minutes", 10) * 60);
        request.setAttribute("provincesApiBaseUrl",
                AppConfig.getOrDefault("api.provinces.base-url", "https://provinces.open-api.vn/api/v1"));

        String couponMessage = (String) session.getAttribute("couponMessage");
        if (couponMessage != null) {
            request.setAttribute("couponMessage", couponMessage);
            session.removeAttribute("couponMessage");
        } else if (couponState.getMessage() != null) {
            request.setAttribute("couponMessage", couponState.getMessage());
        }

        request.getRequestDispatcher("/pages/shop/checkout.jsp").forward(request, response);
    }

    private void handleApplyCoupon(HttpServletRequest request, HttpServletResponse response,
                                   HttpSession session, User userSession) throws IOException {
        User user = refreshUserSession(session, userSession.getId());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        session.setAttribute("checkoutNote", trimToEmpty(request.getParameter("note")));

        CouponValidationResult validation = validateCouponForUser(
                request.getParameter("couponCode"),
                user
        );

        if (validation.isValid()) {
            session.setAttribute("appliedCoupon", validation.getCoupon());
            session.setAttribute(
                    "couponMessage",
                    "Áp dụng mã thành công: giảm " + validation.getCoupon().getDiscountPercent() + "%"
            );
        } else {
            session.removeAttribute("appliedCoupon");
            session.setAttribute("couponMessage", validation.getMessage());
        }

        @SuppressWarnings("unchecked")
        Map<Integer, CartItem> sessionBuyNowCart =
                (Map<Integer, CartItem>) session.getAttribute("buyNowCart");

        boolean isBuyNow = "true".equals(request.getParameter("buyNow"))
                || (sessionBuyNowCart != null && !sessionBuyNowCart.isEmpty());

        response.sendRedirect(request.getContextPath() + "/checkout" + (isBuyNow ? "?buyNow=true" : ""));
    }

    private void placeOrderWithStockCheck(HttpServletRequest request, HttpServletResponse response,
                                          HttpSession session, User userSession) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String completedPaymentMethod = null;
        PaymentTransaction completedPaymentTransaction = null;
        Integer completedOrderId = null;
        try {
            User user = refreshUserSession(session, userSession.getId());

            if (user == null) {
                result.put("success", false);
                result.put("message", "Phiên đăng nhập đã hết hạn.");
                write(response, result);
                return;
            }
            @SuppressWarnings("unchecked")
            Map<Integer, CartItem> sessionBuyNowCart =
                    (Map<Integer, CartItem>) session.getAttribute("buyNowCart");

            boolean isBuyNow = "true".equals(request.getParameter("buyNow"))
                    || (sessionBuyNowCart != null && !sessionBuyNowCart.isEmpty());

            Map<Integer, CartItem> checkoutCart = isBuyNow
                    ? loadBuyNowCart(session, request)
                    : loadLatestCartForUser(session, user);

            if (checkoutCart == null || checkoutCart.isEmpty()) {
                result.put("success", false);
                result.put("message", "Giỏ hàng đang trống.");
                write(response, result);
                return;
            }

            List<Address> addressList = addressDAO.getAddressesByUserId(user.getId());
            Address defaultAddress = resolvePrimaryAddress(user.getId(), addressList);
            if (defaultAddress == null) {
                result.put("success", false);
                result.put("message", "Bạn chưa có địa chỉ mặc định.");
                write(response, result);
                return;
            }

            String addressDetailError = ValidationUtil.validateAddressDetail(defaultAddress.getAddress());
            if (addressDetailError != null) {
                result.put("success", false);
                result.put("message", "Địa chỉ giao hàng hiện tại không hợp lệ. Vui lòng cập nhật lại.");
                write(response, result);
                return;
            }

            Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
            CouponValidationResult couponState = appliedCoupon == null
                    ? CouponValidationResult.empty()
                    : validateCouponForUser(appliedCoupon.getCode(), user);

            if (appliedCoupon != null && !couponState.isValid()) {
                session.removeAttribute("appliedCoupon");
                result.put("success", false);
                result.put("message", couponState.getMessage());
                write(response, result);
                return;
            }

            CheckoutSummary baseSummary = buildCheckoutSummary(checkoutCart, defaultAddress, null);
            String note = trimToEmpty(request.getParameter("note"));

            if (!ValidationUtil.validateMaxLength(note, 500)) {
                result.put("success", false);
                result.put("message", "Ghi chú không được vượt quá 500 ký tự.");
                write(response, result);
                return;
            }

            String fullAddress = formatFullAddress(defaultAddress);
            String recipientFullname = normalizeSpaces(resolveParameterOrFallback(
                    request, "recipientFullname", user.getFullname()));
            String recipientPhone = resolveParameterOrFallback(
                    request, "recipientPhone", user.getPhone());
            String shippingAddress = normalizeSpaces(resolveParameterOrFallback(
                    request, "shippingAddress", fullAddress));

            String recipientNameError = ValidationUtil.validateRecipientName(recipientFullname);
            if (recipientNameError != null) {
                result.put("success", false);
                result.put("message", recipientNameError);
                write(response, result);
                return;
            }

            String recipientPhoneError = ValidationUtil.validateRecipientPhone(recipientPhone);
            if (recipientPhoneError != null) {
                result.put("success", false);
                result.put("message", recipientPhoneError);
                write(response, result);
                return;
            }

            if (isBlank(shippingAddress)) {
                result.put("success", false);
                result.put("message", "Địa chỉ giao hàng không được để trống.");
                write(response, result);
                return;
            }
            if (!ValidationUtil.validateMaxLength(shippingAddress, 500)) {
                result.put("success", false);
                result.put("message", "Địa chỉ giao hàng không được vượt quá 500 ký tự.");
                write(response, result);
                return;
            }

            String paymentMethodKey = resolvePaymentMethodKey(request);
            boolean isVnpay = "vnpay".equalsIgnoreCase(paymentMethodKey);

            String servicePaymentMethodKey = isVnpay ? "cod" : paymentMethodKey;

            String reservedTransferReference = null;
            if ("bank_transfer".equalsIgnoreCase(servicePaymentMethodKey)) {
                reservedTransferReference = ensureBankTransferReference(
                        session,
                        user.getId(),
                        BankTransferDetails.fromConfig()
                );
            }

            services.CheckoutResult checkoutResult = buildCheckoutService().processCheckout(
                    user, checkoutCart, recipientFullname, recipientPhone, shippingAddress, note, couponState, servicePaymentMethodKey,
                    baseSummary.getShippingFee(), reservedTransferReference
            );

            if (!checkoutResult.isSuccess()) {
                result.put("success", false);
                result.put("message", checkoutResult.getMessage());
                write(response, result);
                return;
            }

            completedPaymentMethod = isVnpay ? "VNPAY" : checkoutResult.getPaymentMethodDb();
            completedPaymentTransaction = checkoutResult.getPaymentTransaction();
            completedOrderId = checkoutResult.getOrderId();

            // Create notification for user
            try {
                new DAO.NotificationDAO().create(
                    user.getId(),
                    "Đặt hàng thành công",
                    "Đơn hàng #" + completedOrderId + " đã được đặt thành công. Chúng tôi sẽ sớm xử lý.",
                    "order",
                    request.getContextPath() + "/my-orders?action=view&id=" + completedOrderId
                );
            } catch (Exception e) {
                logger.error("Error creating order notification", e);
            }

            // Xóa đúng cart theo mode
            if (isBuyNow) {
                session.removeAttribute("buyNowCart");
            } else {
                session.removeAttribute("cart");
                session.setAttribute("totalQuantity", 0);
            }
            session.removeAttribute("appliedCoupon");
            session.removeAttribute("couponMessage");
            session.removeAttribute("checkoutNote");
            session.removeAttribute(BANK_TRANSFER_REFERENCE_SESSION_KEY);

            // Set shared success data for orderSuccess.jsp
            session.setAttribute("successOrderId", completedOrderId);
            session.setAttribute("successUser", user);
            session.setAttribute("successTotalAmount", checkoutResult.getTotalAmount());
            session.setAttribute("successShippingFee", checkoutResult.getShippingFee());
            session.setAttribute("successDiscount", checkoutResult.getDiscount());
            session.setAttribute("successFinalTotal", checkoutResult.getFinalTotal());
            session.setAttribute("successShippingAddress", fullAddress);
            session.setAttribute("successOrderNote", note);
            session.setAttribute("successOrderItems", new ArrayList<>(checkoutCart.values()));
            session.setAttribute("paymentMethod", completedPaymentMethod);

            if ("VNPAY".equalsIgnoreCase(completedPaymentMethod)) {
                orderDAO.markOnlinePaymentAwaiting(completedOrderId, "VNPAY");
                session.setAttribute("paymentStatus", 0);

                String vnpayUrl = VnpayUtil.createPaymentUrl(
                        request,
                        completedOrderId,
                        checkoutResult.getFinalTotal()
                );

                result.put("success", true);
                result.put("redirectUrl", vnpayUrl);
                write(response, result);
                return;
            }

            if ("BANK_TRANSFER".equalsIgnoreCase(completedPaymentMethod) && completedPaymentTransaction != null) {
                BankTransferDetails bankTransferDetails = BankTransferDetails.fromConfig();

                // Extra data for orderSuccess.jsp if user navigates there
                session.setAttribute("pendingVerification", true);
                session.setAttribute("transferReference", completedPaymentTransaction.getTransferReference());
                session.setAttribute("paymentExpiresAt", completedPaymentTransaction.getExpiresAt());
                session.setAttribute("bankId", bankTransferDetails.getBankId());
                session.setAttribute("bankDisplayName", bankTransferDetails.getDisplayName());
                session.setAttribute("bankAccountNumber", bankTransferDetails.getAccountNumber());
                session.setAttribute("bankAccountName", bankTransferDetails.getAccountName());
                session.setAttribute("paymentTtlSeconds", AppConfig.getInt("payment.bank.pending-minutes", 10) * 60);

                // JSON response for immediate display on checkout page
                result.put("success", true);
                result.put("pendingVerification", true);
                result.put("orderId", completedOrderId);
                result.put("bankDisplayName", bankTransferDetails.getDisplayName());
                result.put("bankId", bankTransferDetails.getBankId());
                result.put("bankAccountNumber", bankTransferDetails.getAccountNumber());
                result.put("bankAccountName", bankTransferDetails.getAccountName());
                result.put("transferReference", completedPaymentTransaction.getTransferReference());
                result.put("paymentExpiresAt", completedPaymentTransaction.getExpiresAt());
                result.put("paymentTtlSeconds", AppConfig.getInt("payment.bank.pending-minutes", 10) * 60);
                write(response, result);
                return;
            } else {
                session.setAttribute("successOrderId", completedOrderId);
                session.setAttribute("successUser", user);
                session.setAttribute("successTotalAmount", checkoutResult.getTotalAmount());
                session.setAttribute("successShippingFee", checkoutResult.getShippingFee());
                session.setAttribute("successDiscount", checkoutResult.getDiscount());
                session.setAttribute("successFinalTotal", checkoutResult.getFinalTotal());
                session.setAttribute("successShippingAddress", shippingAddress);
                session.setAttribute("successOrderNote", note);
                session.setAttribute("successOrderItems", new ArrayList<>(checkoutCart.values()));
                result.put("success", true);
                result.put("message", "Đặt hàng thành công!");
                result.put("orderId", completedOrderId);
                result.put("orderHash", checkoutResult.getOrderHash());
                result.put("privateKeyBase64", checkoutResult.getPrivateKeyBase64());
                result.put("toolUrl", checkoutResult.getToolUrl());
                result.put("redirectUrl", request.getContextPath() + "/order-success");
                write(response, result);
                return;
            }

        } catch (Throwable t) {
            logger.error("Unexpected error during checkout for user id={}", userSession.getId(), t);
            result.put("success", false);
            result.put("message", resolveCheckoutErrorMessage(t));
            write(response, result);
        }
    }

    private CouponValidationResult resolveAppliedCouponFromSession(HttpSession session, User user) {
        Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
        if (appliedCoupon == null) {
            return CouponValidationResult.empty();
        }

        CouponValidationResult validation = validateCouponForUser(appliedCoupon.getCode(), user);
        if (!validation.isValid()) {
            session.removeAttribute("appliedCoupon");
            return validation;
        }

        session.setAttribute("appliedCoupon", validation.getCoupon());
        return validation;
    }

    private CouponValidationResult validateCouponForUser(String couponCode, User user) {
        String normalizedCode = trimToEmpty(couponCode);
        if (normalizedCode.isEmpty()) {
            return CouponValidationResult.invalid("Vui lòng nhập mã giảm giá.");
        }
        if (user == null) {
            return CouponValidationResult.invalid("Vui lòng đăng nhập để sử dụng mã giảm giá.");
        }
        if (user.isDiscountUsed()) {
            return CouponValidationResult.invalid("Tài khoản này đã sử dụng mã giảm giá trước đó.");
        }

        Coupon coupon = couponDao.getValidCouponByCode(normalizedCode);
        if (coupon == null) {
            return CouponValidationResult.invalid("Mã giảm giá không hợp lệ hoặc đã hết hạn.");
        }
        if (coupon.getUsed() >= coupon.getQuantity()) {
            return CouponValidationResult.invalid("Mã giảm giá đã hết lượt sử dụng.");
        }

        return CouponValidationResult.valid(coupon);
    }

    private CheckoutSummary buildCheckoutSummary(Map<Integer, CartItem> cart, Address defaultAddress, Coupon coupon) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalWeight = 0;
        for (CartItem item : cart.values()) {
            totalAmount = totalAmount.add(item.getTotalPrice());

            int productWeight = item.getProduct().getWeight();
            if (productWeight <= 0) {
                productWeight = DEFAULT_PRODUCT_WEIGHT;
            }
            totalWeight += item.getQuantity() * productWeight;
        }
        int shippingFee = (totalAmount.compareTo(BigDecimal.valueOf(DEFAULT_PRICE)) >= 0) ? 0 : DEFAULT_SHIPPING_FEE;
        String shippingMessage = null;
        if (defaultAddress != null) {
            try {
                ShippingService shippingService = new ShippingService();
                if (shippingFee != 0) {
                    shippingFee = shippingService.calculateShippingFee(
                            defaultAddress.getProvince(),
                            defaultAddress.getDistrict(),
                            defaultAddress.getWard(),
                            totalWeight,
                            20,
                            15,
                            10
                    );
                }
            } catch (Exception e) {
                shippingFee = DEFAULT_SHIPPING_FEE;
                logger.warn("Failed to calculate shipping fee from GHN API, using default fee of {} VND", DEFAULT_SHIPPING_FEE, e);
                shippingMessage = "Không tính được phí ship realtime, tạm dùng phí ship mặc định.";
            }
        } else {
            shippingMessage = "Chưa có địa chỉ mặc định.";
        }

        BigDecimal discount = calculateDiscount(totalAmount, coupon);
        BigDecimal finalTotal = totalAmount.add(BigDecimal.valueOf(shippingFee)).subtract(discount).setScale(0, RoundingMode.HALF_UP);
        return new CheckoutSummary(totalAmount, shippingFee, shippingMessage, discount, finalTotal);
    }

    private BigDecimal calculateDiscount(BigDecimal totalAmount, Coupon coupon) {
        if (coupon == null || coupon.getDiscountPercent() <= 0) {
            return BigDecimal.ZERO;
        }
        return totalAmount
                .multiply(BigDecimal.valueOf(coupon.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    private User refreshUserSession(HttpSession session, int userId) {
        User user = userDAO.getUserById(userId);
        if (user != null) {
            session.setAttribute("user", user);
        }
        return user;
    }

    private Map<Integer, CartItem> loadLatestCartForUser(HttpSession session, User user) {
        Map<Integer, CartItem> cart = cartDAO.getCartByUserId(user.getId());
        if (cart == null) {
            cart = new HashMap<>();
        }
        List<String> removedNames = inventoryService.refreshCartProductsWithNotification(cart);
        session.setAttribute("cart", cart);
        recalculateTotalQuantity(session, cart);

        if (!removedNames.isEmpty()) {
            session.setAttribute("toastMessage", "Các sản phẩm sau đã bị xóa khỏi giỏ hàng vì không còn hàng: "
                    + String.join(", ", removedNames));
            session.setAttribute("toastType", "warning");
        }
        return cart;
    }

    private Map<Integer, CartItem> loadBuyNowCart(HttpSession session, HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Map<Integer, CartItem> buyNowCart = (Map<Integer, CartItem>) session.getAttribute("buyNowCart");

        if (buyNowCart == null) {
            String idParam = request.getParameter("id");
            String qtyParam = request.getParameter("quantity");
            if (idParam != null && qtyParam != null) {
                try {
                    int productId = Integer.parseInt(idParam);
                    int quantity  = Math.max(1, Integer.parseInt(qtyParam));
                    Product product = productDAO.getProductById(productId);
                    if (product != null) {
                        CartItem item = new CartItem(product, quantity);
                        buyNowCart = new HashMap<>();
                        buyNowCart.put(productId, item);
                        session.setAttribute("buyNowCart", buyNowCart);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid buyNow params: id={}, quantity={}", idParam, qtyParam);
                }
            }
        }

        if (buyNowCart == null) return new HashMap<>();
        inventoryService.refreshCartProducts(buyNowCart);
        return buyNowCart;
    }

    private int recalculateTotalQuantity(HttpSession session, Map<Integer, CartItem> cart) {
        int totalQuantity = 0;
        for (CartItem item : cart.values()) {
            totalQuantity += item.getQuantity();
        }
        session.setAttribute("totalQuantity", totalQuantity);
        return totalQuantity;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String resolveParameterOrFallback(HttpServletRequest request, String parameterName, String fallback) {
        String value = request.getParameter(parameterName);
        return value == null ? trimToEmpty(fallback) : trimToEmpty(value);
    }

    private String normalizeSpaces(String value) {
        return trimToEmpty(value).replaceAll("\\s+", " ");
    }

    private String resolveCheckoutErrorMessage(Throwable t) {
        String message = t == null ? "" : String.valueOf(t.getMessage()).toLowerCase();
        if (message.contains("recipient_fullname")
                || message.contains("recipient_phone")
                || message.contains("shipping_address")) {
            return "Database đơn hàng chưa được cập nhật thông tin người nhận. Vui lòng chạy migration 16_order_recipient_snapshot.sql rồi khởi động lại server.";
        }
        return "Đã có lỗi xảy ra. Vui lòng thử lại.";
    }

    private String formatFullAddress(Address address) {
        if (address == null) {
            return "";
        }
        return trimToEmpty(address.getAddress()) + ", "
                + trimToEmpty(address.getWard()) + ", "
                + trimToEmpty(address.getDistrict()) + ", "
                + trimToEmpty(address.getProvince());
    }

    private String resolvePaymentMethodKey(HttpServletRequest request) {
        String paymentMethodKey = trimToEmpty(request.getParameter("paymentMethod"));
        if (!paymentMethodKey.isEmpty()) {
            return paymentMethodKey;
        }
        return trimToEmpty(request.getParameter("payment"));
    }

    private String ensureBankTransferReference(HttpSession session, int userId, BankTransferDetails bankTransferDetails) {
        Object existing = session.getAttribute(BANK_TRANSFER_REFERENCE_SESSION_KEY);
        if (existing instanceof String && !((String) existing).trim().isEmpty()) {
            return ((String) existing).trim();
        }
        String reference = bankTransferDetails.buildReservedTransferReference(userId);
        session.setAttribute(BANK_TRANSFER_REFERENCE_SESSION_KEY, reference);
        return reference;
    }

    private Address resolvePrimaryAddress(int userId, List<Address> addressList) {
        Address defaultAddress = addressDAO.getDefaultAddressByUserId(userId);
        if (defaultAddress != null) {
            return defaultAddress;
        }
        if (addressList != null && !addressList.isEmpty()) {
            return addressList.get(0);
        }
        return null;
    }

    private services.CheckoutService buildCheckoutService() {
        return new services.CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO, cartDAO,
                orderEmailService, inventoryBatchDAO
        );
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
            int pendingMinutes = AppConfig.getInt("payment.bank.pending-minutes", 10);
            transaction.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().plus(pendingMinutes, ChronoUnit.MINUTES)));
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
            case "VNPAY":
                return "VNPAY";
            case "BANK_TRANSFER":
                return "Bank Transfer";
            default:
                return paymentMethodDb;
        }
    }

    private void write(HttpServletResponse res, Map<String, Object> data) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(new Gson().toJson(data));
    }

    private static final class CheckoutSummary {
        private final BigDecimal totalAmount;
        private final int shippingFee;
        private final String shippingMessage;
        private final BigDecimal discount;
        private final BigDecimal finalTotal;

        private CheckoutSummary(BigDecimal totalAmount, int shippingFee, String shippingMessage,
                                BigDecimal discount, BigDecimal finalTotal) {
            this.totalAmount = totalAmount;
            this.shippingFee = shippingFee;
            this.shippingMessage = shippingMessage;
            this.discount = discount;
            this.finalTotal = finalTotal;
        }

        private BigDecimal getTotalAmount() { return totalAmount; }
        private int getShippingFee() { return shippingFee; }
        private String getShippingMessage() { return shippingMessage; }
        private BigDecimal getDiscount() { return discount; }
        private BigDecimal getFinalTotal() { return finalTotal; }
    }
}
