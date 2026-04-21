package controller.shop;

import java.io.IOException;

import DAO.AddressDao;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.OrderDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
import Context.DBContext;

import Model.*;

import Util.ValidationUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import services.ShippingService;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import services.InventoryService;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PRODUCT_WEIGHT = 200;
    private static final int DEFAULT_SHIPPING_FEE = 30000;
    private static final int DEFAULT_PRICE = 500000;

    private final CouponDao couponDao = new CouponDao();
    private final AddressDao addressDAO = new AddressDao();
    private final InventoryService inventoryService = new InventoryService();
    private final CartDAO cartDAO = new CartDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final UserDAO userDAO = new UserDAO();

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

        placeOrderWithStockCheck(request, response, session, userSession);
    }

    private void renderCheckout(HttpServletRequest request, HttpServletResponse response,
                                HttpSession session, User userSession)
            throws ServletException, IOException {
        User user = refreshUserSession(session, userSession.getId());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Map<Integer, CartItem> cart = loadLatestCartForUser(session, user);
        if (cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }

        List<String> stockErrors = inventoryService.validateCartForCheckout(cart);
        if (!stockErrors.isEmpty()) {
            session.setAttribute("toastMessage", stockErrors.get(0));
            session.setAttribute("toastType", "warning");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        List<Address> addressList = addressDAO.getAddressesByUserId(user.getId());
        Address defaultAddress = addressDAO.getDefaultAddressByUserId(user.getId());
        CouponValidationResult couponState = resolveAppliedCouponFromSession(session, user);
        CheckoutSummary summary = buildCheckoutSummary(cart, defaultAddress, couponState.getCoupon());

        request.setAttribute("addressList", addressList);
        request.setAttribute("cartItems", new ArrayList<>(cart.values()));
        request.setAttribute("user", user);
        request.setAttribute("defaultAddress", defaultAddress);
        request.setAttribute("totalAmount", summary.getTotalAmount());
        request.setAttribute("shippingFee", summary.getShippingFee());
        request.setAttribute("shippingMessage", summary.getShippingMessage());
        request.setAttribute("discount", summary.getDiscount());
        request.setAttribute("finalTotal", summary.getFinalTotal());
        request.setAttribute("appliedCouponCode",
                couponState.getCoupon() != null ? couponState.getCoupon().getCode() : "");

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

        response.sendRedirect(request.getContextPath() + "/checkout");
    }

    private void placeOrderWithStockCheck(HttpServletRequest request, HttpServletResponse response,
                                          HttpSession session, User userSession) throws IOException {
        Map<String, Object> result = new HashMap<>();
        User user = refreshUserSession(session, userSession.getId());

        if (user == null) {
            result.put("success", false);
            result.put("message", "Phiên đăng nhập đã hết hạn.");
            write(response, result);
            return;
        }

        Map<Integer, CartItem> cart = loadLatestCartForUser(session, user);
        if (cart.isEmpty()) {
            result.put("success", false);
            result.put("message", "Giỏ hàng đang trống.");
            write(response, result);
            return;
        }

        List<String> stockErrors = inventoryService.validateCartForCheckout(cart);
        if (!stockErrors.isEmpty()) {
            result.put("success", false);
            result.put("message", stockErrors.get(0));
            write(response, result);
            return;
        }

        Address defaultAddress = addressDAO.getDefaultAddressByUserId(user.getId());
        if (isBlank(user.getFullname()) || isBlank(user.getPhone())) {
            result.put("success", false);
            result.put("message", "Thiếu thông tin người nhận.");
            write(response, result);
            return;
        }
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

        CheckoutSummary baseSummary = buildCheckoutSummary(cart, defaultAddress, null);
        String note = trimToEmpty(request.getParameter("note"));
        
        // Validate note max length
        if (!ValidationUtil.validateMaxLength(note, 500)) {
            result.put("success", false);
            result.put("message", "Ghi chú không được vượt quá 500 ký tự.");
            write(response, result);
            return;
        }
        
        String fullAddress = defaultAddress.getAddress() + ", "
                + defaultAddress.getWard() + ", "
                + defaultAddress.getDistrict() + ", "
                + defaultAddress.getProvince();

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try {
                for (CartItem item : cart.values()) {
                    Product latestProduct = productDAO.getProductById(conn, item.getProduct().getId());
                    if (latestProduct == null) {
                        conn.rollback();
                        result.put("success", false);
                        result.put("message", "Có sản phẩm không còn tồn tại.");
                        write(response, result);
                        return;
                    }
                    if (latestProduct.getStock() < item.getQuantity()) {
                        conn.rollback();
                        result.put("success", false);
                        result.put(
                                "message",
                                "Sản phẩm \"" + latestProduct.getName() + "\" chỉ còn "
                                        + latestProduct.getStock() + " sản phẩm."
                        );
                        write(response, result);
                        return;
                    }
                    item.setProduct(latestProduct);
                }

                double discount = 0;
                if (couponState.isValid()) {
                    if (!userDAO.markDiscountAsUsed(conn, user.getId())) {
                        conn.rollback();
                        result.put("success", false);
                        result.put("message", "Tài khoản này đã sử dụng mã giảm giá trước đó.");
                        write(response, result);
                        return;
                    }

                    Coupon latestCoupon = couponDao.getValidCouponByCode(conn, couponState.getCoupon().getCode());
                    if (latestCoupon == null) {
                        conn.rollback();
                        result.put("success", false);
                        result.put("message", "Mã giảm giá không hợp lệ hoặc đã hết hạn.");
                        write(response, result);
                        return;
                    }
                    if (!couponDao.increaseUsedIfAvailable(conn, latestCoupon.getId())) {
                        conn.rollback();
                        result.put("success", false);
                        result.put("message", "Mã giảm giá đã hết lượt sử dụng.");
                        write(response, result);
                        return;
                    }
                    discount = calculateDiscount(baseSummary.getTotalAmount(), latestCoupon);
                }

                double finalTotal = baseSummary.getTotalAmount() + baseSummary.getShippingFee() - discount;
                PaymentSelection paymentSelection = resolvePaymentMethod(
                        request.getParameter("paymentMethod"),
                        finalTotal
                );
                if (!paymentSelection.isValid()) {
                    conn.rollback();
                    result.put("success", false);
                    result.put("message", paymentSelection.getMessage());
                    write(response, result);
                    return;
                }

                Order order = new Order();
                order.setUserId(user.getId());
                order.setFullname(user.getFullname());
                order.setPhone(user.getPhone());
                order.setAddress(fullAddress);
                order.setNote(note);
                order.setTotalAmount(finalTotal);
                order.setStatus("Pending");
                order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                order.setPayment_method(paymentSelection.getPaymentMethodDb());
                order.setPayment_status(paymentSelection.isPaymentStatus());

                int orderId = orderDAO.saveOrder(conn, order);
                if (orderId <= 0) {
                    conn.rollback();
                    result.put("success", false);
                    result.put("message", "Không tạo được đơn hàng.");
                    write(response, result);
                    return;
                }

                for (CartItem ci : cart.values()) {
                    if (!productDAO.decreaseStock(conn, ci.getProduct().getId(), ci.getQuantity())) {
                        conn.rollback();
                        result.put("success", false);
                        result.put(
                                "message",
                                "Sản phẩm \"" + ci.getProduct().getName() + "\" đã hết hàng trong lúc thanh toán."
                        );
                        write(response, result);
                        return;
                    }

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(orderId);
                    orderItem.setProductId(ci.getProduct().getId());
                    orderItem.setQuantity(ci.getQuantity());
                    orderItem.setPrice(ci.getProduct().getPrice());

                    if (!orderDAO.saveOrderItem(conn, orderItem)) {
                        conn.rollback();
                        result.put("success", false);
                        result.put("message", "Không lưu được chi tiết đơn hàng.");
                        write(response, result);
                        return;
                    }
                }

                cartDAO.clearCart(conn, user.getId());
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Đã có lỗi xảy ra. Vui lòng thử lại.");
            write(response, result);
            return;
        }

        session.removeAttribute("cart");
        session.setAttribute("totalQuantity", 0);
        session.removeAttribute("appliedCoupon");
        session.removeAttribute("couponMessage");
        session.removeAttribute("checkoutNote");

        result.put("success", true);
        result.put("message", "Đặt hàng thành công!");
        write(response, result);
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
        double totalAmount = 0;
        int totalWeight = 0;
        for (CartItem item : cart.values()) {
            totalAmount += item.getTotalPrice();

            int productWeight = item.getProduct().getWeight();
            if (productWeight <= 0) {
                productWeight = DEFAULT_PRODUCT_WEIGHT;
            }
            totalWeight += item.getQuantity() * productWeight;
        }
        int shippingFee = (totalAmount >= DEFAULT_PRICE) ? 0 : DEFAULT_SHIPPING_FEE;
        String shippingMessage = null;
        if (defaultAddress != null) {
            try {
                ShippingService shippingService = new ShippingService();
                if(shippingFee != 0){
                shippingFee = shippingService.calculateShippingFee(
                        defaultAddress.getProvince(),
                        defaultAddress.getDistrict(),
                        defaultAddress.getWard(),
                        totalWeight,
                        20,
                        15,
                        10
                );}
            } catch (Exception e) {
                e.printStackTrace();
                shippingMessage = "Không tính được phí ship realtime, tạm dùng phí ship mặc định.";
            }
        } else {
            shippingMessage = "Chưa có địa chỉ mặc định.";
        }

        double discount = calculateDiscount(totalAmount, coupon);
        double finalTotal = totalAmount + shippingFee - discount;
        return new CheckoutSummary(totalAmount, shippingFee, shippingMessage, discount, finalTotal);
    }

    private PaymentSelection resolvePaymentMethod(String paymentMethod, double finalTotal) {
        switch (trimToEmpty(paymentMethod)) {
            case "cod":
                return PaymentSelection.valid("COD", false);
            case "momo":
                if (!callMomoApi(finalTotal)) {
                    return PaymentSelection.invalid("Thanh toán MoMo thất bại.");
                }
                return PaymentSelection.valid("MOMO", true);
            case "bank_transfer":
                if (!callBankApi(finalTotal)) {
                    return PaymentSelection.invalid("Thanh toán ngân hàng thất bại.");
                }
                return PaymentSelection.valid("BANK_TRANSFER", true);
            default:
                return PaymentSelection.invalid("Phương thức thanh toán không hợp lệ.");
        }
    }

    private double calculateDiscount(double totalAmount, Coupon coupon) {
        if (coupon == null || coupon.getDiscountPercent() <= 0) {
            return 0;
        }
        return totalAmount * coupon.getDiscountPercent() / 100.0;
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
        inventoryService.refreshCartProducts(cart);
        session.setAttribute("cart", cart);
        recalculateTotalQuantity(session, cart);
        return cart;
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

    private void write(HttpServletResponse res, Map<String, Object> data) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(new Gson().toJson(data));
    }

    private boolean callMomoApi(double amount) {
        System.out.println("Mock MoMo: " + amount);
        return true;
    }

    private boolean callBankApi(double amount) {
        System.out.println("Mock Bank: " + amount);
        return true;
    }

    private static final class CheckoutSummary {
        private final double totalAmount;
        private final int shippingFee;
        private final String shippingMessage;
        private final double discount;
        private final double finalTotal;

        private CheckoutSummary(double totalAmount, int shippingFee, String shippingMessage,
                                double discount, double finalTotal) {
            this.totalAmount = totalAmount;
            this.shippingFee = shippingFee;
            this.shippingMessage = shippingMessage;
            this.discount = discount;
            this.finalTotal = finalTotal;
        }

        private double getTotalAmount() {
            return totalAmount;
        }

        private int getShippingFee() {
            return shippingFee;
        }

        private String getShippingMessage() {
            return shippingMessage;
        }

        private double getDiscount() {
            return discount;
        }

        private double getFinalTotal() {
            return finalTotal;
        }
    }

    private static final class CouponValidationResult {
        private final boolean valid;
        private final Coupon coupon;
        private final String message;

        private CouponValidationResult(boolean valid, Coupon coupon, String message) {
            this.valid = valid;
            this.coupon = coupon;
            this.message = message;
        }

        private static CouponValidationResult valid(Coupon coupon) {
            return new CouponValidationResult(true, coupon, null);
        }

        private static CouponValidationResult invalid(String message) {
            return new CouponValidationResult(false, null, message);
        }

        private static CouponValidationResult empty() {
            return new CouponValidationResult(false, null, null);
        }

        private boolean isValid() {
            return valid && coupon != null;
        }

        private Coupon getCoupon() {
            return coupon;
        }

        private String getMessage() {
            return message;
        }
    }

    private static final class PaymentSelection {
        private final boolean valid;
        private final String paymentMethodDb;
        private final boolean paymentStatus;
        private final String message;

        private PaymentSelection(boolean valid, String paymentMethodDb, boolean paymentStatus, String message) {
            this.valid = valid;
            this.paymentMethodDb = paymentMethodDb;
            this.paymentStatus = paymentStatus;
            this.message = message;
        }

        private static PaymentSelection valid(String paymentMethodDb, boolean paymentStatus) {
            return new PaymentSelection(true, paymentMethodDb, paymentStatus, null);
        }

        private static PaymentSelection invalid(String message) {
            return new PaymentSelection(false, null, false, message);
        }

        private boolean isValid() {
            return valid;
        }

        private String getPaymentMethodDb() {
            return paymentMethodDb;
        }

        private boolean isPaymentStatus() {
            return paymentStatus;
        }

        private String getMessage() {
            return message;
        }
    }
}
