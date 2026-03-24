package controller.shop;

import java.io.IOException;

import DAO.AddressDao;
import DAO.UserDAO;
import Model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.OrderDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
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
        AddressDao addressDAO = new AddressDao();
        List<Address> addressList = addressDAO.getAddressesByUser(user.getId());
        List<CartItem> cartItems = new ArrayList<>(cart.values());
        double totalAmount = 0;
        for (CartItem item : cart.values()) {
            totalAmount += item.getTotalPrice();
        }
        request.setAttribute("addressList", addressList);
        request.setAttribute("totalAmount", totalAmount);
        request.setAttribute("cartItems", cartItems);
        request.setAttribute("user", user);
        request.getRequestDispatcher("/pages/shop/checkout.jsp").forward(request, response);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        User userSession = (User) session.getAttribute("user");

        if (userSession == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        UserDAO userdao = new UserDAO();
        User user = userdao.getUserById(userSession.getId());

        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }
        List<CartItem> cartItems = new ArrayList<>(cart.values());
        double totalAmount = 0;
        for (CartItem item : cart.values()) {
            totalAmount += item.getTotalPrice();
        }

        // 1. Lấy thông tin từ form
        String fullname = user.getFullname();
        String phone = user.getPhone();
        String address = user.getAddress();
        String note = request.getParameter("note");

        // 2. Lưu Order
        Order order = new Order();
        order.setUserId(user.getId());
        order.setFullname(fullname);
        order.setPhone(phone);
        order.setAddress(address);
        order.setNote(note);
        order.setTotalAmount(totalAmount);
        order.setStatus("Pending");


        //lấy ra danh sách các sản phẩm đã tới phần thanh toán
        OrderDAO orderDAO = new OrderDAO();
        int orderId = orderDAO.saveOrder(order);

        if (orderId > 0) {
            // 3. Lưu Order Items
            for (CartItem ci : cart.values()) {
                OrderItem oi = new OrderItem();
                oi.setOrderId(orderId);
                oi.setProductId(ci.getProduct().getId());
                oi.setQuantity(ci.getQuantity());
                oi.setPrice(ci.getProduct().getPrice());
                orderDAO.saveOrderItem(oi);
            }

//             4. Xử lý Session
            session.removeAttribute("cart");
            session.removeAttribute("totalQuantity");
//
//            // 5. Gửi thông báo thành công
            session.setAttribute("toastMessage", "🎉 Đặt hàng thành công! Cảm ơn bạn đã ủng hộ.");
            session.setAttribute("toastType", "success");
//            // 6. Chuyển hướng về trang lịch sử đơn hàng
            response.sendRedirect(request.getContextPath() + "/my-orders");
        } else {
            session.setAttribute("toastMessage", "❌ Đã có lỗi xảy ra. Vui lòng thử lại.");
            session.setAttribute("toastType", "error");
                response.sendRedirect(request.getContextPath() + "/cart");
        }
    }
}