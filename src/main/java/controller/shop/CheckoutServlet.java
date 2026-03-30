package controller.shop;

import java.io.IOException;

import DAO.AddressDao;
import DAO.CouponDao;
import DAO.UserDAO;
import DAO.OrderDAO;

import Model.*;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import services.ShippingService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private AddressDao addressDAO = new AddressDao();
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User userSession = (User) session.getAttribute("user");

        if (userSession == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        UserDAO userdao = new UserDAO();
        User user = userdao.getUserById(userSession.getId());
        session.setAttribute("user", user);

        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }

        List<Address> addressList = addressDAO.getAddressesByUserId(user.getId());
        Address defaultAddress = addressDAO.getDefaultAddressByUserId(user.getId());
        List<CartItem> cartItems = new ArrayList<>(cart.values());

        double totalAmount = 0;
        int totalWeight = 0;
        for (CartItem item : cart.values()) {
            totalAmount += item.getTotalPrice();
            int productWeight = item.getProduct().getWeight();
            if (productWeight <= 0) productWeight = 500;
            totalWeight += item.getQuantity() * productWeight;
        }
        int shippingFee = 30000; // fallback
        String shippingMessage = null;

        if (defaultAddress != null) {
            try {
                ShippingService shippingService = new ShippingService();

                shippingFee = shippingService.calculateShippingFee(
                        defaultAddress.getProvince(),
                        defaultAddress.getDistrict(),
                        defaultAddress.getWard(),
                        totalWeight,
                        20,
                        15,
                        10
                );
            } catch (Exception e) {
                e.printStackTrace();
                shippingMessage = "Không tính được phí ship realtime, tạm dùng phí ship mặc định.";
            }
        } else {
            shippingMessage = "Chưa có địa chỉ mặc định.";
        }
        Coupon coupon = (Coupon) session.getAttribute("appliedCoupon");
        double discount = 0;
        if (coupon != null) {
            discount = totalAmount * coupon.getDiscountPercent() / 100.0;
        }

        double finalTotal = totalAmount + shippingFee - discount;
        session.removeAttribute("appliedCoupon");
        request.setAttribute("addressList", addressList);
        request.setAttribute("totalAmount", totalAmount);
        request.setAttribute("cartItems", cartItems);
        request.setAttribute("user", user);
        request.setAttribute("discount", discount);
        request.setAttribute("finalTotal", finalTotal);
        request.setAttribute("shippingFee", shippingFee);
        request.setAttribute("shippingMessage", shippingMessage);
        request.setAttribute("defaultAddress", defaultAddress);

        String couponMessage = (String) session.getAttribute("couponMessage");
        if (couponMessage != null) {
            request.setAttribute("couponMessage", couponMessage);
            session.removeAttribute("couponMessage");
        }

        request.getRequestDispatcher("/pages/shop/checkout.jsp").forward(request, response);
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

        String action = request.getParameter("action");
        if ("applyCoupon".equals(action)) {
            String couponCode = request.getParameter("couponCode");
            String note = request.getParameter("note");
            session.setAttribute("checkoutNote", note);
            if (couponCode == null || couponCode.trim().isEmpty()) {
                session.setAttribute("couponMessage", "Vui lòng nhập mã giảm giá");
                session.removeAttribute("appliedCoupon");
            } else {
                CouponDao couponDAO = new CouponDao();
                Coupon coupon = couponDAO.getValidCouponByCode(couponCode.trim());

                if (coupon != null) {
                    session.setAttribute("appliedCoupon", coupon);
                    session.setAttribute("couponMessage",
                            "Áp dụng mã thành công: giảm " + coupon.getDiscountPercent() + "%");
                } else {
                    session.removeAttribute("appliedCoupon");
                    session.setAttribute("couponMessage", "Mã giảm giá không hợp lệ hoặc đã hết hạn");
                }
            }

            response.sendRedirect(request.getContextPath() + "/checkout");
            return;
        }

        Map<String, Object> result = new HashMap<>();
        // PLACE ORDER
        UserDAO userdao = new UserDAO();
        User user = userdao.getUserById(userSession.getId());

        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }
        Address defaultAddress = addressDAO.getDefaultAddressByUserId(user.getId());
        double totalAmount = 0;
        int totalWeight = 0;
        for (CartItem item : cart.values()) {
            totalAmount += item.getTotalPrice();

            int productWeight = item.getProduct().getWeight();
            if (productWeight <= 0) productWeight = 500;

            totalWeight += item.getQuantity() * productWeight;
        }

        int shippingFee = 30000;
        try {
            if (defaultAddress != null) {
                ShippingService shippingService = new ShippingService();
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
            e.printStackTrace();
        }


        Coupon coupon = (Coupon) session.getAttribute("appliedCoupon");
        double discount = 0;

        if (coupon != null) {
            discount = totalAmount * coupon.getDiscountPercent() / 100.0;
        }

        double finalTotal = totalAmount + shippingFee - discount;

        String fullname = user.getFullname();
        String phone = user.getPhone();
        String address = user.getAddress();
        String note = request.getParameter("note");
        String paymentMethod = request.getParameter("paymentMethod");
        if (user.getFullname() == null || user.getPhone() == null) {
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
        String fullAddress = defaultAddress.getAddress() + ", " +
                defaultAddress.getWard() + ", " +
                defaultAddress.getDistrict() + ", " +
                defaultAddress.getProvince();

//        payment
        String paymentMethodDb;
        boolean paymentStatus;

        switch (paymentMethod) {
            case "cod":
                paymentMethodDb = "COD";
                paymentStatus = false;
                break;

            case "momo":
                if (!callMomoApi(finalTotal)) {
                    result.put("success", false);
                    result.put("message", "Thanh toán MoMo thất bại.");
                    write(response, result);
                    return;
                }
                paymentMethodDb = "MOMO";
                paymentStatus = true;
                break;

            case "bank_transfer":
                if (!callBankApi(finalTotal)) {
                    result.put("success", false);
                    result.put("message", "Thanh toán ngân hàng thất bại.");
                    write(response, result);
                    return;
                }
                paymentMethodDb = "BANK_TRANSFER";
                paymentStatus = true;
                break;

            default:
                result.put("success", false);
                result.put("message", "Phương thức thanh toán không hợp lệ.");
                write(response, result);
                return;
        }


        Order order = new Order();
        order.setUserId(user.getId());
        order.setFullname(fullname);
        order.setPhone(phone);
        order.setAddress(fullAddress);
        order.setNote(note);
        order.setTotalAmount(finalTotal);
        order.setStatus("Pending");
        order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        order.setPayment_method(paymentMethodDb);
        order.setPayment_status(paymentStatus);

        OrderDAO orderDAO = new OrderDAO();
        int orderId = orderDAO.saveOrder(order);

        if (orderId > 0) {
            for (CartItem ci : cart.values()) {
                OrderItem oi = new OrderItem();
                oi.setOrderId(orderId);
                oi.setProductId(ci.getProduct().getId());
                oi.setQuantity(ci.getQuantity());
                oi.setPrice(ci.getProduct().getPrice());
                orderDAO.saveOrderItem(oi);
            }

            session.removeAttribute("cart");
            session.removeAttribute("totalQuantity");
            session.removeAttribute("appliedCoupon");
            session.removeAttribute("couponMessage");
            session.removeAttribute("checkoutNote");

            result.put("success", true);
            result.put("message", "Đặt hàng thành công!");
            write(response, result);
            return;
        } else {
            result.put("success", false);
            result.put("message", "Đã có lỗi xảy ra. Vui lòng thử lại.");
            write(response, result);
            return;
        }
    }
    private void write(HttpServletResponse res, Map<String, Object> data) throws IOException {
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
}