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

        int productId;
        try {
            productId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            session.setAttribute("toastMessage", "S\u1ea3n ph\u1ea9m kh\u00f4ng t\u1ed3n t\u1ea1i!");
            session.setAttribute("toastType", "error");
            response.sendRedirect(redirectUrl);
            return;
        }

        QuantityInputValidation quantityValidation = validateRequestedQuantity(request.getParameter("quantity"));
        if (!quantityValidation.isValid()) {
            session.setAttribute("toastMessage", quantityValidation.getMessage());
            session.setAttribute("toastType", "warning");
            response.sendRedirect(redirectUrl);
            return;
        }

        int quantity = quantityValidation.getQuantity();

        try {

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

            session.setAttribute("toastMessage", "Đã thêm " + product.getName() + " vào giỏ hàng!");
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

    private QuantityInputValidation validateRequestedQuantity(String rawQuantity) {
        String normalizedQuantity = rawQuantity == null ? "" : rawQuantity.trim();

        if (normalizedQuantity.isEmpty()) {
            return QuantityInputValidation.invalid("S\u1ed1 l\u01b0\u1ee3ng kh\u00f4ng h\u1ee3p l\u1ec7.");
        }

        if (normalizedQuantity.startsWith("-")) {
            return QuantityInputValidation.invalid("Kh\u00f4ng \u0111\u01b0\u1ee3c nh\u1eadp s\u1ed1 \u00e2m.");
        }

        if (normalizedQuantity.contains(".") || normalizedQuantity.contains(",")) {
            return QuantityInputValidation.invalid("Kh\u00f4ng \u0111\u01b0\u1ee3c nh\u1eadp s\u1ed1 th\u1eadp ph\u00e2n.");
        }

        if (!normalizedQuantity.matches("\\d+")) {
            return QuantityInputValidation.invalid("S\u1ed1 l\u01b0\u1ee3ng kh\u00f4ng h\u1ee3p l\u1ec7.");
        }

        long parsedQuantity;
        try {
            parsedQuantity = Long.parseLong(normalizedQuantity);
        } catch (NumberFormatException e) {
            return QuantityInputValidation.invalid("S\u1ed1 l\u01b0\u1ee3ng kh\u00f4ng h\u1ee3p l\u1ec7.");
        }

        if (parsedQuantity == 0) {
            return QuantityInputValidation.invalid("S\u1ed1 l\u01b0\u1ee3ng ph\u1ea3i l\u1edbn h\u01a1n 0.");
        }

        if (parsedQuantity > Integer.MAX_VALUE) {
            return QuantityInputValidation.invalid("S\u1ed1 l\u01b0\u1ee3ng kh\u00f4ng h\u1ee3p l\u1ec7.");
        }

        return QuantityInputValidation.valid((int) parsedQuantity);
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

    private static final class QuantityInputValidation {
        private final boolean valid;
        private final int quantity;
        private final String message;

        private QuantityInputValidation(boolean valid, int quantity, String message) {
            this.valid = valid;
            this.quantity = quantity;
            this.message = message;
        }

        private static QuantityInputValidation valid(int quantity) {
            return new QuantityInputValidation(true, quantity, null);
        }

        private static QuantityInputValidation invalid(String message) {
            return new QuantityInputValidation(false, 0, message);
        }

        private boolean isValid() {
            return valid;
        }

        private int getQuantity() {
            return quantity;
        }

        private String getMessage() {
            return message;
        }
    }
}
