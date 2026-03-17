package controller.shop;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import Model.Order;
import Model.OrderItem;
import Model.CartItem;
import Model.User;
import DAO.OrderDAO;
import java.util.Map;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }

        // 1. Lấy thông tin từ form
        String fullname = request.getParameter("fullname");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String note = request.getParameter("note");
        double totalAmount = Double.parseDouble(request.getParameter("totalAmount"));
        
        // 2. Lưu Order
        Order order = new Order();
        order.setUserId(user.getId());
        order.setFullname(fullname);
        order.setPhone(phone);
        order.setAddress(address);
        order.setNote(note);
        order.setTotalAmount(totalAmount);
        
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
            
            // 4. Xử lý Session
            session.removeAttribute("cart");
            session.removeAttribute("totalQuantity");
            
            // 5. Gửi thông báo thành công
            session.setAttribute("toastMessage", "🎉 Đặt hàng thành công! Cảm ơn bạn đã ủng hộ.");
            session.setAttribute("toastType", "success");
            
            // 6. Chuyển hướng về trang lịch sử đơn hàng
            response.sendRedirect(request.getContextPath() + "/my-orders");
        } else {
            session.setAttribute("toastMessage", "❌ Đã có lỗi xảy ra. Vui lòng thử lại.");
            session.setAttribute("toastType", "error");
            response.sendRedirect(request.getContextPath() + "/pages/shop/cart.jsp");
        }
    }
}