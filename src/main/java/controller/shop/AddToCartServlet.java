package controller.shop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import DAO.CartDAO;
import DAO.ProductDAO;
import Model.CartItem;
import Model.Product;
import Model.User;

@WebServlet("/add-to-cart")
public class AddToCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");

        int productId = Integer.parseInt(request.getParameter("id"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new HashMap<>();

        ProductDAO dao = new ProductDAO();
        Product product = dao.getProductById(productId); 
        
        if (product != null) {
            // Cập nhật session cart
            if (cart.containsKey(productId)) {
                CartItem existingItem = cart.get(productId);
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
            } else {
                cart.put(productId, new CartItem(product, quantity));
            }
            
            session.setAttribute("cart", cart);
            // Nếu user đã đăng nhập, lưu vào database
            if (user != null) {
                CartDAO cartDAO = new CartDAO();
                cartDAO.addToCart(user.getId(), productId, quantity);
            }
            int totalQuantity = 0;
            for (CartItem item : cart.values()) {
                totalQuantity += item.getQuantity();
            }
            session.setAttribute("totalQuantity", totalQuantity);

            session.setAttribute("toastMessage", "Đã thêm <b>" + product.getName() + "</b> vào giỏ hàng!");
            session.setAttribute("toastType", "success");
        } else {
            session.setAttribute("toastMessage", "Sản phẩm không tồn tại!");
            session.setAttribute("toastType", "error");
        }
        
        // Xử lý điều hướng
        String action = request.getParameter("actionType");
        
        if ("buy".equals(action)) {
            response.sendRedirect(request.getContextPath() + "/pages/shop/cart.jsp");
        } else {
            String referer = request.getHeader("referer");
            response.sendRedirect(referer);
        }
    }
}