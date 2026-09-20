package com.petshop.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import DAO.CartDAO;
import Model.CartItem;
import Model.Product;
import Model.User;
import jakarta.servlet.http.HttpSession;
import services.InventoryService;
import services.InventoryService.StockValidationResult;

/**
 * Replaces CartServlet (/cart) and AddToCartServlet (/add-to-cart) 1:1 —
 * same actions, same session attributes, same JSON bodies, same redirects.
 */
@Controller
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartDAO cartDAO;
    private final InventoryService inventoryService;
    private final Gson gson = new Gson();

    public CartController() {
        this(new CartDAO(), new InventoryService());
    }

    CartController(CartDAO cartDAO, InventoryService inventoryService) {
        this.cartDAO = cartDAO;
        this.inventoryService = inventoryService;
    }

    @GetMapping(value = "/cart", params = "!state")
    public String cart(
            @RequestParam(value = "action", required = false) String action,
            HttpSession session) {
        if ("remove".equals(action)) {
            return "redirect:/cart";
        }
        showCart(session);
        return "pages/shop/cart";
    }

    @GetMapping(value = "/cart", params = "action=state", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String cartState(HttpSession session) {
        return writeCartState(session);
    }

    @PostMapping(value = "/cart", params = "action=remove")
    public String removeFromCart(
            @RequestParam(value = "id", required = false) String idStr,
            HttpSession session) {
        if (idStr == null || idStr.isEmpty()) {
            return "redirect:/cart";
        }

        int productId;
        try {
            productId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            session.setAttribute("toastMessage", "Mã sản phẩm không hợp lệ.");
            session.setAttribute("toastType", "error");
            return "redirect:/cart";
        }
        User user = (User) session.getAttribute("user");

        @SuppressWarnings("unchecked")
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");

        if (cart != null && cart.containsKey(productId)) {
            cart.remove(productId);
            session.setAttribute("cart", cart);
            recalculateTotalQuantity(session, cart);

            if (user != null) {
                cartDAO.removeFromCart(user.getId(), productId);
            }

            session.setAttribute("toastMessage", "Đã xóa sản phẩm khỏi giỏ hàng!");
            session.setAttribute("toastType", "success");
        }

        return "redirect:/cart";
    }

    @PostMapping(value = "/cart", params = "action=update", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String updateCart(
            @RequestParam(value = "id", required = false) String idStr,
            @RequestParam(value = "quantity", required = false) String quantityStr,
            HttpSession session) {
        if (idStr == null || quantityStr == null) {
            return json(false, "Thiếu id hoặc số lượng");
        }

        try {
            int productId = Integer.parseInt(idStr);
            int quantity = Integer.parseInt(quantityStr);
            User user = (User) session.getAttribute("user");

            @SuppressWarnings("unchecked")
            Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");

            if (cart == null || !cart.containsKey(productId)) {
                return json(false, "Không tìm thấy sản phẩm trong giỏ hàng");
            }

            inventoryService.refreshCartProducts(cart);
            session.setAttribute("cart", cart);

            CartItem existingItem = cart.get(productId);
            if (existingItem == null) {
                if (user != null) {
                    cartDAO.removeFromCart(user.getId(), productId);
                }
                cart = reloadCart(session, user, cart);
                recalculateTotalQuantity(session, cart);
                return json(false, "Sản phẩm không còn tồn tại");
            }

            if (quantity <= 0) {
                cart.remove(productId);
                if (user != null) {
                    cartDAO.removeFromCart(user.getId(), productId);
                }
                cart = reloadCart(session, user, cart);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("removed", true);
                result.put("quantity", 0);
                result.put("stock", existingItem.getProduct().getAvailablePurchaseQuantity());
                result.put("totalQuantity", recalculateTotalQuantity(session, cart));
                return gson.toJson(result);
            }

            StockValidationResult validation = inventoryService.validateCartQuantity(cart, productId, quantity);
            Product latestProduct = validation.getProduct();
            if (latestProduct != null) {
                existingItem.setProduct(latestProduct);
            }

            if (!validation.isValid()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", validation.getMessage());
                result.put("stock", latestProduct != null ? latestProduct.getAvailablePurchaseQuantity() : 0);
                result.put("outOfStock", validation.isOutOfStock());

                if (validation.isOutOfStock()) {
                    cart.remove(productId);
                    if (user != null) {
                        cartDAO.removeFromCart(user.getId(), productId);
                    }
                    cart = reloadCart(session, user, cart);
                    result.put("removed", true);
                    result.put("quantity", 0);
                    result.put("totalQuantity", recalculateTotalQuantity(session, cart));
                    return gson.toJson(result);
                }

                existingItem.setQuantity(validation.getSuggestedQuantity());
                if (user != null) {
                    cartDAO.updateCartQuantity(user.getId(), productId, validation.getSuggestedQuantity());
                }
                cart = reloadCart(session, user, cart);
                CartItem syncedItem = cart.get(productId);

                result.put("quantity", syncedItem != null ? syncedItem.getQuantity() : validation.getSuggestedQuantity());
                result.put("totalQuantity", recalculateTotalQuantity(session, cart));
                return gson.toJson(result);
            }

            existingItem.setQuantity(validation.getSuggestedQuantity());
            if (user != null) {
                cartDAO.updateCartQuantity(user.getId(), productId, validation.getSuggestedQuantity());
            }
            cart = reloadCart(session, user, cart);
            CartItem syncedItem = cart.get(productId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("quantity", syncedItem != null ? syncedItem.getQuantity() : validation.getSuggestedQuantity());
            result.put("stock", syncedItem != null ? syncedItem.getProduct().getAvailablePurchaseQuantity()
                    : (latestProduct != null ? latestProduct.getAvailablePurchaseQuantity() : 0));
            result.put("totalQuantity", recalculateTotalQuantity(session, cart));
            return gson.toJson(result);
        } catch (Exception e) {
            logger.error("Error updating cart with stock check for product id={}", idStr, e);
            return json(false, "Loi server");
        }
    }

    @PostMapping(value = "/cart", params = "action=clear")
    public String clearCart(HttpSession session) {
        User user = (User) session.getAttribute("user");

        session.removeAttribute("cart");
        session.setAttribute("totalQuantity", 0);

        if (user != null) {
            cartDAO.clearCart(user.getId());
        }

        session.setAttribute("toastMessage", "Đã xóa toàn bộ giỏ hàng!");
        session.setAttribute("toastType", "success");

        return "redirect:/cart";
    }

    @PostMapping("/add-to-cart")
    public String addToCart(
            @RequestParam(value = "id", required = false) String idRaw,
            @RequestParam(value = "quantity", required = false) String quantityRaw,
            @RequestParam(value = "actionType", required = false) String action,
            HttpSession session,
            jakarta.servlet.http.HttpServletRequest request) {

        boolean isBuyNow = "buy".equals(action);
        String redirectUrl = resolveRedirectUrl(request);

        int productId;
        try {
            productId = Integer.parseInt(idRaw);
        } catch (NumberFormatException e) {
            session.setAttribute("toastMessage", "Sản phẩm không tồn tại!");
            session.setAttribute("toastType", "error");
            return "redirect:" + redirectUrl;
        }

        QuantityInputValidation quantityValidation = validateRequestedQuantity(quantityRaw);
        if (!quantityValidation.isValid()) {
            session.setAttribute("toastMessage", quantityValidation.getMessage());
            session.setAttribute("toastType", "warning");
            return "redirect:" + redirectUrl;
        }

        int quantity = quantityValidation.getQuantity();
        User user = (User) session.getAttribute("user");

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
                    return "redirect:" + redirectUrl;
                }

                int expectedQuantity = validation.getSuggestedQuantity();
                Map<Integer, CartItem> buyNowCart = new HashMap<>();
                buyNowCart.put(productId, new CartItem(product, expectedQuantity));
                session.setAttribute("buyNowCart", buyNowCart);
                return "redirect:" + request.getContextPath() + "/checkout?buyNow=true";

            } catch (Exception e) {
                logger.warn("[BuyNow] Exception for productId=" + productId + ": " + e.getMessage());
                logger.error("Unexpected error", e);
                session.setAttribute("toastMessage", "Không thể mua ngay, vui lòng thử lại!");
                session.setAttribute("toastType", "error");
                return "redirect:" + redirectUrl;
            }
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
                return "redirect:" + redirectUrl;
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

        return "redirect:" + redirectUrl;
    }

    private void showCart(HttpSession session) {
        User user = (User) session.getAttribute("user");

        Map<Integer, CartItem> cart;
        if (user != null) {
            cart = cartDAO.getCartByUserId(user.getId());
        } else {
            @SuppressWarnings("unchecked")
            Map<Integer, CartItem> sessionCart = (Map<Integer, CartItem>) session.getAttribute("cart");
            cart = sessionCart;
        }

        if (cart == null) {
            cart = new HashMap<>();
        }

        List<String> removedNames = inventoryService.refreshCartProductsWithNotification(cart);
        session.setAttribute("cart", cart);
        recalculateTotalQuantity(session, cart);

        if (!removedNames.isEmpty()) {
            session.setAttribute("toastMessage", "Các sản phẩm sau đã bị xóa khỏi giỏ hàng vì không còn hàng: " + String.join(", ", removedNames));
            session.setAttribute("toastType", "warning");
        }
    }

    private String writeCartState(HttpSession session) {
        User user = (User) session.getAttribute("user");

        Map<Integer, CartItem> cart;
        if (user != null) {
            cart = cartDAO.getCartByUserId(user.getId());
        } else {
            @SuppressWarnings("unchecked")
            Map<Integer, CartItem> sessionCart = (Map<Integer, CartItem>) session.getAttribute("cart");
            cart = sessionCart;
            if (cart != null) {
                inventoryService.refreshCartProducts(cart);
            }
        }

        if (cart == null) {
            cart = new HashMap<>();
        }

        session.setAttribute("cart", cart);
        int totalQuantity = recalculateTotalQuantity(session, cart);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalQuantity", totalQuantity);

        List<Map<String, Object>> items = new ArrayList<>();
        for (CartItem item : cart.values()) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("productId", item.getProduct().getId());
            itemData.put("quantity", item.getQuantity());
            itemData.put("stock", item.getProduct().getAvailablePurchaseQuantity());
            items.add(itemData);
        }
        result.put("items", items);

        return gson.toJson(result);
    }

    private Map<Integer, CartItem> reloadCart(HttpSession session, User user, Map<Integer, CartItem> fallbackCart) {
        Map<Integer, CartItem> latestCart = fallbackCart;
        if (user != null) {
            latestCart = cartDAO.getCartByUserId(user.getId());
        }
        if (latestCart == null) {
            latestCart = new HashMap<>();
        }
        session.setAttribute("cart", latestCart);
        return latestCart;
    }

    private int recalculateTotalQuantity(HttpSession session, Map<Integer, CartItem> cart) {
        int totalQuantity = 0;
        for (CartItem item : cart.values()) {
            totalQuantity += item.getQuantity();
        }
        session.setAttribute("totalQuantity", totalQuantity);
        return totalQuantity;
    }

    private String json(boolean success, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        return gson.toJson(result);
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

    private String resolveRedirectUrl(jakarta.servlet.http.HttpServletRequest request) {
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
