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
import Model.CartItem;
import Model.Product;
import Model.User;
import services.InventoryService;
import services.InventoryService.StockValidationResult;

@WebServlet("/add-to-cart")
public class AddToCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final InventoryService inventoryService = new InventoryService();
    private final CartDAO cartDAO = new CartDAO();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        String action = request.getParameter("actionType");
        String redirectUrl = resolveRedirectUrl(request, action);

        try {
            int productId = Integer.parseInt(request.getParameter("id"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));

            @SuppressWarnings("unchecked")
            Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
            if (cart == null) {
                cart = new HashMap<>();
            }

            // Always validate add-to-cart against the latest stock in DB before changing the cart.
            inventoryService.refreshCartProducts(cart);
            StockValidationResult validation = inventoryService.validateAddToCart(cart, productId, quantity);
            Product product = validation.getProduct();

            if (!validation.isValid()) {
                if (product != null && cart.containsKey(productId)) {
                    cart.get(productId).setProduct(product);
                }
                session.setAttribute("cart", cart);
                recalculateTotalQuantity(session, cart);
                session.setAttribute("toastMessage", validation.getMessage());
                session.setAttribute("toastType", validation.isOutOfStock() ? "error" : "warning");
                response.sendRedirect(redirectUrl);
                return;
            }

            int expectedQuantity = validation.getSuggestedQuantity();

            if (cart.containsKey(productId)) {
            // Cập nhật session cart
                CartItem existingItem = cart.get(productId);
                existingItem.setProduct(product);
                existingItem.setQuantity(expectedQuantity);
            } else {
                cart.put(productId, new CartItem(product, expectedQuantity));
            }
            
            session.setAttribute("cart", cart);
            // Nếu user đã đăng nhập, lưu vào database
            if (user != null) {
                cartDAO.addToCart(user.getId(), productId, quantity);
                cart = cartDAO.getCartByUserId(user.getId());
            }
            session.setAttribute("cart", cart);
            recalculateTotalQuantity(session, cart);

            session.setAttribute("toastMessage", "Đã thêm <b>" + product.getName() + "</b> vào giỏ hàng!");
            session.setAttribute("toastType", "success");
        } catch (Exception e) {
            session.setAttribute("toastMessage", "Sản phẩm không tồn tại!");
            session.setAttribute("toastType", "error");
        }
        
        // Xử lý điều hướng
        response.sendRedirect(redirectUrl);
    }

    private void recalculateTotalQuantity(HttpSession session, Map<Integer, CartItem> cart) {
        int totalQuantity = 0;
        for (CartItem item : cart.values()) {
            totalQuantity += item.getQuantity();
        }
        session.setAttribute("totalQuantity", totalQuantity);
    }

    private String resolveRedirectUrl(HttpServletRequest request, String action) {
        if ("buy".equals(action)) {
            return request.getContextPath() + "/cart";
        }

        String referer = request.getHeader("referer");
        if (referer == null || referer.isBlank()) {
            return request.getContextPath() + "/shop";
        }
        return referer;
    }
}
