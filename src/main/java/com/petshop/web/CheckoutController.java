package com.petshop.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import Context.DBContext;
import DAO.AddressDao;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.InventoryBatchDAO;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
import Model.Address;
import Model.CartItem;
import Model.Coupon;
import Model.CouponValidationResult;
import Model.Order;
import Model.PaymentTransaction;
import Model.Product;
import Model.User;
import Util.AppConfig;
import Util.ValidationUtil;
import Util.VnpayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import services.CheckoutResult;
import services.CheckoutService;
import services.InventoryService;
import services.OrderEmailService;
import services.ShippingService;
import services.payment.BankTransferDetails;
import services.payment.PaymentProvider;
import services.payment.PaymentRegistry;
import services.payment.PaymentResult;

/**
 * Replaces CheckoutServlet (/checkout) 1:1 — same render logic, same
 * applyCoupon flow, same placeOrder JSON contract (incl. top-level
 * Throwable safety net that never returns an empty body).
 */
@Controller
public class CheckoutController {
    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);
    private static final int DEFAULT_PRODUCT_WEIGHT = 200;
    private static final int DEFAULT_SHIPPING_FEE = 30000;
    private static final int DEFAULT_PRICE = 500000;
    private static final String BANK_TRANSFER_REFERENCE_SESSION_KEY = "bankTransferReference";

    private final CouponDao couponDao;
    private final AddressDao addressDAO;
    private final InventoryService inventoryService;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;
    private final PaymentTransactionDAO paymentTransactionDAO;
    private final UserDAO userDAO;
    private final OrderEmailService orderEmailService;
    private final InventoryBatchDAO inventoryBatchDAO;
    private final Gson gson = new Gson();

    public CheckoutController() {
        this(new CouponDao(), new AddressDao(), new InventoryService(), new CartDAO(),
                new ProductDAO(), new OrderDAO(), new PaymentTransactionDAO(),
                new UserDAO(), new OrderEmailService(), new InventoryBatchDAO());
    }

    CheckoutController(CouponDao couponDao, AddressDao addressDAO, InventoryService inventoryService,
                       CartDAO cartDAO, ProductDAO productDAO, OrderDAO orderDAO,
                       PaymentTransactionDAO paymentTransactionDAO, UserDAO userDAO,
                       OrderEmailService orderEmailService, InventoryBatchDAO inventoryBatchDAO) {
        this.couponDao = couponDao;
        this.addressDAO = addressDAO;
        this.inventoryService = inventoryService;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
        this.paymentTransactionDAO = paymentTransactionDAO;
        this.userDAO = userDAO;
        this.orderEmailService = orderEmailService;
        this.inventoryBatchDAO = inventoryBatchDAO;
    }

    @GetMapping("/checkout")
    public String checkoutPage(
            @RequestParam(value = "buyNow", required = false) String buyNowParam,
            @RequestParam(value = "id", required = false) String idParam,
            @RequestParam(value = "quantity", required = false) String qtyParam,
            Model model,
            HttpServletRequest request,
            HttpSession session) {
        User userSession = (User) session.getAttribute("user");
        if (userSession == null) {
            return "redirect:" + request.getContextPath() + "/login";
        }
        return renderCheckout(request, model, session, userSession,
                "true".equals(buyNowParam), idParam, qtyParam);
    }

    @PostMapping(value = "/checkout", params = "action=applyCoupon")
    public String applyCoupon(
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "couponCode", required = false) String couponCode,
            @RequestParam(value = "buyNow", required = false) String buyNowParam,
            HttpServletRequest request,
            HttpSession session) {
        User userSession = (User) session.getAttribute("user");
        if (userSession == null) {
            return "redirect:" + request.getContextPath() + "/login";
        }
        User user = refreshUserSession(session, userSession.getId());
        if (user == null) {
            return "redirect:" + request.getContextPath() + "/login";
        }

        session.setAttribute("checkoutNote", trimToEmpty(note));

        CouponValidationResult validation = validateCouponForUser(couponCode, user);

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

        boolean isBuyNow = "true".equals(buyNowParam)
                || (sessionBuyNowCart != null && !sessionBuyNowCart.isEmpty());

        return "redirect:" + request.getContextPath() + "/checkout" + (isBuyNow ? "?buyNow=true" : "");
    }

    @PostMapping(value = "/checkout", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String placeOrder(HttpServletRequest request, HttpSession session,
                             jakarta.servlet.http.HttpServletResponse response) throws IOException {
        User userSession = (User) session.getAttribute("user");
        if (userSession == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        try {
            return placeOrderWithStockCheck(request, session, userSession);
        } catch (Exception e) {
            logger.error("Top-level unhandled exception in placeOrder for user id={}", userSession.getId(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", resolveCheckoutErrorMessage(e));
            return gson.toJson(errorResult);
        }
    }

    private String renderCheckout(HttpServletRequest request, Model model,
                                  HttpSession session, User userSession,
                                  boolean buyNowParam, String idParam, String qtyParam) {
        User user = refreshUserSession(session, userSession.getId());
        if (user == null) {
            return "redirect:" + request.getContextPath() + "/login";
        }

        @SuppressWarnings("unchecked")
        Map<Integer, CartItem> sessionBuyNowCart =
                (Map<Integer, CartItem>) session.getAttribute("buyNowCart");

        boolean isBuyNow = buyNowParam
                || (sessionBuyNowCart != null && !sessionBuyNowCart.isEmpty());
        Map<Integer, CartItem> checkoutCart = isBuyNow
                ? loadBuyNowCart(session, request, idParam, qtyParam)
                : loadLatestCartForUser(session, user);

        if (checkoutCart == null || checkoutCart.isEmpty()) {
            return "redirect:" + request.getContextPath() + "/shop";
        }

        List<String> stockErrors = inventoryService.validateCartForCheckout(checkoutCart);
        if (!stockErrors.isEmpty()) {
            session.setAttribute("toastMessage", stockErrors.get(0));
            session.setAttribute("toastType", "warning");
            return "redirect:" + request.getContextPath() + (isBuyNow ? "/shop" : "/cart");
        }

        List<Address> addressList = addressDAO.getAddressesByUserId(user.getId());
        Address defaultAddress = resolvePrimaryAddress(user.getId(), addressList);
        CouponValidationResult couponState = resolveAppliedCouponFromSession(session, user);
        CheckoutSummary summary = buildCheckoutSummary(checkoutCart, defaultAddress, couponState.getCoupon());

        model.addAttribute("addressList", addressList);
        model.addAttribute("cartItems", new ArrayList<>(checkoutCart.values()));
        model.addAttribute("user", user);
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("defaultShippingAddress", defaultAddress != null ? formatFullAddress(defaultAddress) : "");
        model.addAttribute("selectedAddressId", defaultAddress != null ? defaultAddress.getId() : null);
        model.addAttribute("totalAmount", summary.getTotalAmount());
        model.addAttribute("shippingFee", summary.getShippingFee());
        model.addAttribute("shippingMessage", summary.getShippingMessage());
        model.addAttribute("discount", summary.getDiscount());
        model.addAttribute("finalTotal", summary.getFinalTotal());
        model.addAttribute("appliedCouponCode",
                couponState.getCoupon() != null ? couponState.getCoupon().getCode() : "");
        model.addAttribute("isBuyNow", isBuyNow);

        BankTransferDetails bankTransferDetails = BankTransferDetails.fromConfig();
        String bankTransferReference = ensureBankTransferReference(session, user.getId(), bankTransferDetails);
        model.addAttribute("bankDisplayName", bankTransferDetails.getDisplayName());
        model.addAttribute("bankId", bankTransferDetails.getBankId());
        model.addAttribute("bankAccountNumber", bankTransferDetails.getAccountNumber());
        model.addAttribute("bankAccountName", bankTransferDetails.getAccountName());
        model.addAttribute("bankTransferPrefix", bankTransferDetails.getTransferPrefix());
        model.addAttribute("bankTransferReference", bankTransferReference);
        model.addAttribute("bankPaymentTtlSeconds", AppConfig.getInt("payment.bank.pending-minutes", 10) * 60);
        model.addAttribute("provincesApiBaseUrl",
                AppConfig.getOrDefault("api.provinces.base-url", "https://provinces.open-api.vn/api/v1"));

        String couponMessage = (String) session.getAttribute("couponMessage");
        if (couponMessage != null) {
            model.addAttribute("couponMessage", couponMessage);
            session.removeAttribute("couponMessage");
        } else if (couponState.getMessage() != null) {
            model.addAttribute("couponMessage", couponState.getMessage());
        }

        return "pages/shop/checkout";
    }

    private String placeOrderWithStockCheck(HttpServletRequest request,
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
                return gson.toJson(result);
            }
            @SuppressWarnings("unchecked")
            Map<Integer, CartItem> sessionBuyNowCart =
                    (Map<Integer, CartItem>) session.getAttribute("buyNowCart");

            boolean isBuyNow = "true".equals(request.getParameter("buyNow"))
                    || (sessionBuyNowCart != null && !sessionBuyNowCart.isEmpty());

            Map<Integer, CartItem> checkoutCart = isBuyNow
                    ? loadBuyNowCart(session, request, null, null)
                    : loadLatestCartForUser(session, user);

            if (checkoutCart == null || checkoutCart.isEmpty()) {
                result.put("success", false);
                result.put("message", "Giỏ hàng đang trống.");
                return gson.toJson(result);
            }

            List<Address> addressList = addressDAO.getAddressesByUserId(user.getId());
            Address defaultAddress = resolvePrimaryAddress(user.getId(), addressList);
            if (defaultAddress == null) {
                result.put("success", false);
                result.put("message", "Bạn chưa có địa chỉ mặc định.");
                return gson.toJson(result);
            }

            String addressDetailError = ValidationUtil.validateAddressDetail(defaultAddress.getAddress());
            if (addressDetailError != null) {
                result.put("success", false);
                result.put("message", "Địa chỉ giao hàng hiện tại không hợp lệ. Vui lòng cập nhật lại.");
                return gson.toJson(result);
            }

            Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
            CouponValidationResult couponState = appliedCoupon == null
                    ? CouponValidationResult.empty()
                    : validateCouponForUser(appliedCoupon.getCode(), user);

            if (appliedCoupon != null && !couponState.isValid()) {
                session.removeAttribute("appliedCoupon");
                result.put("success", false);
                result.put("message", couponState.getMessage());
                return gson.toJson(result);
            }

            CheckoutSummary baseSummary = buildCheckoutSummary(checkoutCart, defaultAddress, null);
            String note = trimToEmpty(request.getParameter("note"));

            if (!ValidationUtil.validateMaxLength(note, 500)) {
                result.put("success", false);
                result.put("message", "Ghi chú không được vượt quá 500 ký tự.");
                return gson.toJson(result);
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
                return gson.toJson(result);
            }

            String recipientPhoneError = ValidationUtil.validateRecipientPhone(recipientPhone);
            if (recipientPhoneError != null) {
                result.put("success", false);
                result.put("message", recipientPhoneError);
                return gson.toJson(result);
            }

            if (isBlank(shippingAddress)) {
                result.put("success", false);
                result.put("message", "Địa chỉ giao hàng không được để trống.");
                return gson.toJson(result);
            }
            if (!ValidationUtil.validateMaxLength(shippingAddress, 500)) {
                result.put("success", false);
                result.put("message", "Địa chỉ giao hàng không được vượt quá 500 ký tự.");
                return gson.toJson(result);
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
                return gson.toJson(result);
            }

            completedPaymentMethod = isVnpay ? "VNPAY" : checkoutResult.getPaymentMethodDb();
            completedPaymentTransaction = checkoutResult.getPaymentTransaction();
            completedOrderId = checkoutResult.getOrderId();

            // Queue an app event so the next commerce-agent turn knows the order
            // completed outside the conversation (upstream: host-queued app events).
            try {
                services.ai.common.AppEventBus.publish("user:" + user.getId(), "order_completed",
                        "orderId=" + completedOrderId + " payment=" + completedPaymentMethod);
            } catch (Exception ignored) {}

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

                // Lưu thông tin chữ ký vào session để dùng ở trang order-success
                session.setAttribute("orderHash", checkoutResult.getOrderHash());
                session.setAttribute("privateKeyBase64", checkoutResult.getPrivateKeyBase64());
                session.setAttribute("toolUrl", checkoutResult.getToolUrl());
                session.setAttribute("showSignatureModal", true);

                result.put("success", true);
                result.put("redirectUrl", vnpayUrl);
                // Với VNPay, không trả về showSignatureModal trong JSON để tránh hiện modal ngay lập tức
                return gson.toJson(result);
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
                result.put("orderHash", checkoutResult.getOrderHash());
                result.put("privateKeyBase64", checkoutResult.getPrivateKeyBase64());
                result.put("toolUrl", checkoutResult.getToolUrl());

                // Modal signature cho bank_transfer: dùng button "Tải chữ ký" thay vì popup
                result.put("showSignatureModal", true);
                return gson.toJson(result);
            }
            session.setAttribute("successOrderId", completedOrderId);
            session.setAttribute("successUser", user);
            session.setAttribute("successTotalAmount", checkoutResult.getTotalAmount());
            session.setAttribute("successShippingFee", checkoutResult.getShippingFee());
            session.setAttribute("successDiscount", checkoutResult.getDiscount());
            session.setAttribute("successFinalTotal", checkoutResult.getFinalTotal());
            session.setAttribute("successShippingAddress", shippingAddress);
            session.setAttribute("successOrderNote", note);
            session.setAttribute("successOrderItems", new ArrayList<>(checkoutCart.values()));
            session.setAttribute("orderHash", checkoutResult.getOrderHash());
            session.setAttribute("privateKeyBase64", checkoutResult.getPrivateKeyBase64());
            session.setAttribute("toolUrl", checkoutResult.getToolUrl());
            session.setAttribute("showSignatureModal", true);
            result.put("success", true);
            result.put("message", "Đặt hàng thành công!");
            result.put("orderId", completedOrderId);
            result.put("redirectUrl", request.getContextPath() + "/order-success");
            logger.info("DEBUG COD redirectUrl=" + result.get("redirectUrl"));
            return gson.toJson(result);

        } catch (Throwable t) {
            logger.error("Unexpected error during checkout for user id={}", userSession.getId(), t);
            result.put("success", false);
            result.put("message", resolveCheckoutErrorMessage(t));
            return gson.toJson(result);
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

    private Map<Integer, CartItem> loadBuyNowCart(HttpSession session, HttpServletRequest request,
                                                 String idParam, String qtyParam) {
        @SuppressWarnings("unchecked")
        Map<Integer, CartItem> buyNowCart = (Map<Integer, CartItem>) session.getAttribute("buyNowCart");

        if (buyNowCart == null) {
            String idP = idParam != null ? idParam : request.getParameter("id");
            String qtyP = qtyParam != null ? qtyParam : request.getParameter("quantity");
            if (idP != null && qtyP != null) {
                try {
                    int productId = Integer.parseInt(idP);
                    int quantity  = Math.max(1, Integer.parseInt(qtyP));
                    Product product = productDAO.getProductById(productId);
                    if (product != null) {
                        CartItem item = new CartItem(product, quantity);
                        buyNowCart = new HashMap<>();
                        buyNowCart.put(productId, item);
                        session.setAttribute("buyNowCart", buyNowCart);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid buyNow params: id={}, quantity={}", idP, qtyP);
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
