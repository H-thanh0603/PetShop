package controller.shop;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import DAO.CartDAO;
import Model.CartItem;
import Model.User;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("remove".equals(action)) {
            removeFromCart(request, response);
        } else {
            // Redirect to cart page
            response.sendRedirect(request.getContextPath() + "/pages/shop/cart.jsp");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        
        if ("remove".equals(action)) {
            removeFromCart(request, response);
        } else if ("update".equals(action)) {
            updateCart(request, response);
        } else if ("clear".equals(action)) {
            clearCart(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/pages/shop/cart.jsp");
        }
    }
    
    private void removeFromCart(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession();
        String idStr = request.getParameter("id");
        
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/pages/shop/cart.jsp");
            return;
        }
        
        int productId = Integer.parseInt(idStr);
        User user = (User) session.getAttribute("user");
        
        @SuppressWarnings("unchecked")
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        
        if (cart != null && cart.containsKey(productId)) {
            // Xóa khỏi session cart
            cart.remove(productId);
            session.setAttribute("cart", cart);
            
            // Cập nhật tổng số lượng
            int totalQuantity = 0;
            for (CartItem item : cart.values()) {
                totalQuantity += item.getQuantity();
            }
            session.setAttribute("totalQuantity", totalQuantity);
            
            // Nếu user đã đăng nhập, xóa khỏi database
            if (user != null) {
                CartDAO cartDAO = new CartDAO();
                cartDAO.removeFromCart(user.getId(), productId);
            }
            
            session.setAttribute("toastMessage", "Đã xóa sản phẩm khỏi giỏ hàng!");
            session.setAttribute("toastType", "success");
        }
        
        response.sendRedirect(request.getContextPath() + "/pages/shop/cart.jsp");
    }

    private void updateCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String idStr = request.getParameter("id");
        String quantityStr = request.getParameter("quantity");

        if (idStr == null || quantityStr == null) {
            response.getWriter().write("{\"success\":false,\"message\":\"Thiếu id hoặc quantity\"}");
            return;
        }

        try {
            int productId = Integer.parseInt(idStr);
            int quantity = Integer.parseInt(quantityStr);
            User user = (User) session.getAttribute("user");

            @SuppressWarnings("unchecked")
            Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");

            if (cart != null && cart.containsKey(productId)) {
                if (quantity <= 0) {
                    cart.remove(productId);
                } else {
                    cart.get(productId).setQuantity(quantity);
                }

                session.setAttribute("cart", cart);

                int totalQuantity = 0;
                for (CartItem item : cart.values()) {
                    totalQuantity += item.getQuantity();
                }
                session.setAttribute("totalQuantity", totalQuantity);

                if (user != null) {
                    CartDAO cartDAO = new CartDAO();
                    if (quantity <= 0) {
                        cartDAO.removeFromCart(user.getId(), productId);
                    } else {
                        cartDAO.updateCartQuantity(user.getId(), productId, quantity);
                    }
                }

                response.getWriter().write(
                        "{\"success\":true," +
                                "\"quantity\":" + quantity + "," +
                                "\"totalQuantity\":" + totalQuantity +
                                "}"
                );
                return;
            }

            response.getWriter().write("{\"success\":false,\"message\":\"Không tìm thấy sản phẩm trong cart\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\":false,\"message\":\"Lỗi server\"}");
        }
    }
    
    private void clearCart(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        // Xóa cart khỏi session
        session.removeAttribute("cart");
        session.setAttribute("totalQuantity", 0);
        
        // Nếu user đã đăng nhập, xóa khỏi database
        if (user != null) {
            CartDAO cartDAO = new CartDAO();
            cartDAO.clearCart(user.getId());
        }
        
        session.setAttribute("toastMessage", "Đã xóa toàn bộ giỏ hàng!");
        session.setAttribute("toastType", "success");
        
        response.sendRedirect(request.getContextPath() + "/pages/shop/cart.jsp");
    }
}
