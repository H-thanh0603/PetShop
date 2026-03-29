package controller.shop;

import java.io.IOException;

import DAO.AddressDao;
import DAO.CouponDao;
import DAO.UserDAO;
import DAO.OrderDAO;

import Model.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import services.ShippingService;

import java.util.ArrayList;
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

        Order order = new Order();
        order.setUserId(user.getId());
        order.setFullname(fullname);
        order.setPhone(phone);
        order.setAddress(address);
        order.setNote(note);
        order.setTotalAmount(finalTotal); // lưu số tiền sau giảm giá
        order.setStatus("Pending");

        // nếu Order có field discount/coupon thì set thêm
        // order.setDiscountAmount(discount);
        // order.setCouponCode(couponCode);

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

            session.setAttribute("toastMessage", "🎉 Đặt hàng thành công! Cảm ơn bạn đã ủng hộ.");
            session.setAttribute("toastType", "success");

            response.sendRedirect(request.getContextPath() + "/my-orders");
        } else {
            session.setAttribute("toastMessage", "❌ Đã có lỗi xảy ra. Vui lòng thử lại.");
            session.setAttribute("toastType", "error");
            response.sendRedirect(request.getContextPath() + "/cart");
        }
    }
}