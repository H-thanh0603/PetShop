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
 * Replaces CartServlet (/cart) 1:1 — same actions, same session attributes,
 * same JSON bodies, same redirects.
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
}
