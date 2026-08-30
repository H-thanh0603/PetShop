package controller.shop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import DAO.CartDAO;
import Model.CartItem;
import Model.Product;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import services.InventoryService;
import services.InventoryService.StockValidationResult;

public class AddToCartServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AddToCartServlet.class);

    private static final long serialVersionUID = 1L;

    private final InventoryService inventoryService = new InventoryService();
    private final CartDAO cartDAO = new CartDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        String action = request.getParameter("actionType");
        boolean isBuyNow = "buy".equals(action);
        String redirectUrl = resolveRedirectUrl(request);

        int productId;
        try {
            productId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            session.setAttribute("toastMessage", "Sản phẩm không tồn tại!");
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

        // ── BUY NOW PATH ─────────────────────────────────────────────────────────
        if (isBuyNow) {
            try {
                // For buy-now we validate against an EMPTY cart (no existing items)
                Map<Integer, CartItem> emptyCart = new HashMap<>();
                StockValidationResult validation = inventoryService.validateAddToCart(emptyCart, productId, quantity);
                Product product = validation.getProduct();

                if (!validation.isValid()) {
                    session.setAttribute("toastMessage", validation.getMessage());
                    session.setAttribute("toastType", validation.isOutOfStock() ? "error" : "warning");
                    response.sendRedirect(redirectUrl);
                    return;
                }

                int expectedQuantity = validation.getSuggestedQuantity();
                Map<Integer, CartItem> buyNowCart = new HashMap<>();
                buyNowCart.put(productId, new CartItem(product, expectedQuantity));
                session.setAttribute("buyNowCart", buyNowCart);
                response.sendRedirect(request.getContextPath() + "/checkout?buyNow=true");

            } catch (Exception e) {
                logger.warn("[BuyNow] Exception for productId=" + productId + ": " + e.getMessage());
                logger.error("Unexpected error", e);
                session.setAttribute("toastMessage", "Không thể mua ngay, vui lòng thử lại!");
                session.setAttribute("toastType", "error");
                response.sendRedirect(redirectUrl);
            }
            return; // always stop here, never fall through to cart logic
        }

        // ── ADD TO CART PATH ─────────────────────────────────────────────────────
        try {
            @SuppressWarnings("unchecked")
            Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
            if (cart == null) {
                cart = new HashMap<>();
            }

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
                CartItem existingItem = cart.get(productId);
                existingItem.setProduct(product);
                existingItem.setQuantity(expectedQuantity);
            } else {
                cart.put(productId, new CartItem(product, expectedQuantity));
            }

            session.setAttribute("cart", cart);

            if (user != null) {
                cartDAO.addToCart(user.getId(), productId, quantity);
                cart = cartDAO.getCartByUserId(user.getId());
                session.setAttribute("cart", cart);
            }

            recalculateTotalQuantity(session, cart);
            session.setAttribute("toastMessage", "Đã thêm " + product.getName() + " vào giỏ hàng!");
            session.setAttribute("toastType", "success");

        } catch (Exception e) {
            logger.warn("[AddToCart] Exception for productId=" + productId + ": " + e.getMessage());
            logger.error("Unexpected error", e);
            session.setAttribute("toastMessage", "Sản phẩm không tồn tại!");
            session.setAttribute("toastType", "error");
        }

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
            return QuantityInputValidation.invalid("Số lượng không hợp lệ.");
        }

        if (normalizedQuantity.startsWith("-")) {
            return QuantityInputValidation.invalid("Không được nhập số âm.");
        }

        if (normalizedQuantity.contains(".") || normalizedQuantity.contains(",")) {
            return QuantityInputValidation.invalid("Không được nhập số thập phân.");
        }

        if (!normalizedQuantity.matches("\\d+")) {
            return QuantityInputValidation.invalid("Số lượng không hợp lệ.");
        }

        long parsedQuantity;
        try {
            parsedQuantity = Long.parseLong(normalizedQuantity);
        } catch (NumberFormatException e) {
            return QuantityInputValidation.invalid("Số lượng không hợp lệ.");
        }

        if (parsedQuantity == 0) {
            return QuantityInputValidation.invalid("Số lượng phải lớn hơn 0.");
        }

        if (parsedQuantity > Integer.MAX_VALUE) {
            return QuantityInputValidation.invalid("Số lượng không hợp lệ.");
        }

        return QuantityInputValidation.valid((int) parsedQuantity);
    }

    private String resolveRedirectUrl(HttpServletRequest request) {
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