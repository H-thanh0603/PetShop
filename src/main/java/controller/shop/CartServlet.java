package controller.shop;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(CartServlet.class);
    private final CartDAO cartDAO = new CartDAO();
    private final InventoryService inventoryService = new InventoryService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("remove".equals(action)) {
            response.sendRedirect(request.getContextPath() + "/cart");
        } else if ("state".equals(action)) {
            writeCartState(request, response);
        } else {
            showCart(request, response);
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
            updateCartWithStockCheck(request, response);
        } else if ("clear".equals(action)) {
            clearCart(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/cart");
        }
    }

    private void showCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

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

        request.getRequestDispatcher("/pages/shop/cart.jsp").forward(request, response);
    }

    private void writeCartState(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
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

        java.util.List<Map<String, Object>> items = new java.util.ArrayList<>();
        for (CartItem item : cart.values()) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("productId", item.getProduct().getId());
            itemData.put("quantity", item.getQuantity());
            itemData.put("stock", item.getProduct().getStock());
            items.add(itemData);
        }
        result.put("items", items);

        response.getWriter().write(gson.toJson(result));
    }
    
    private void removeFromCart(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession();
        String idStr = request.getParameter("id");
        
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        
        int productId;
        try {
            productId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            session.setAttribute("toastMessage", "Mã sản phẩm không hợp lệ.");
            session.setAttribute("toastType", "error");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        User user = (User) session.getAttribute("user");
        
        @SuppressWarnings("unchecked")
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        
        if (cart != null && cart.containsKey(productId)) {
            // Xóa khỏi session cart
            cart.remove(productId);
            session.setAttribute("cart", cart);
            
            // Cập nhật tổng số lượng
            recalculateTotalQuantity(session, cart);
            
            // Nếu user đã đăng nhập, xóa khỏi database
            if (user != null) {
                cartDAO.removeFromCart(user.getId(), productId);
            }
            
            session.setAttribute("toastMessage", "Đã xóa sản phẩm khỏi giỏ hàng!");
            session.setAttribute("toastType", "success");
        }
        
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private void updateCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String idStr = request.getParameter("id");
        String quantityStr = request.getParameter("quantity");

        if (idStr == null || quantityStr == null) {
            writeJson(response, false, "Thiếu id hoặc quantity");
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

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("quantity", quantity);
                result.put("totalQuantity", totalQuantity);
                writeJson(response, result);
                return;
            }

            writeJson(response, false, "Không tìm thấy sản phẩm trong cart");

        } catch (Exception e) {
            logger.error("Error updating cart for product id={}", request.getParameter("id"), e);
            writeJson(response, false, "Lỗi server");
        }
    }

    private void updateCartWithStockCheck(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String idStr = request.getParameter("id");
        String quantityStr = request.getParameter("quantity");

        if (idStr == null || quantityStr == null) {
            writeJson(response, false, "Thiếu id hoặc số lượng");
            return;
        }

        try {
            int productId = Integer.parseInt(idStr);
            int quantity = Integer.parseInt(quantityStr);
            User user = (User) session.getAttribute("user");

            @SuppressWarnings("unchecked")
            Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");

            if (cart == null || !cart.containsKey(productId)) {
                writeJson(response, false, "Không tìm thấy sản phẩm trong giỏ hàng");
                return;
            }

            // Refresh stock from DB on every +/- action so the cart cannot exceed live inventory.
            inventoryService.refreshCartProducts(cart);
            session.setAttribute("cart", cart);

            CartItem existingItem = cart.get(productId);
            if (existingItem == null) {
                if (user != null) {
                    cartDAO.removeFromCart(user.getId(), productId);
                }
                cart = reloadCart(session, user, cart);
                recalculateTotalQuantity(session, cart);
                writeJson(response, false, "Sản phẩm không còn tồn tại");
                return;
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
                result.put("stock", existingItem.getProduct().getStock());
                result.put("totalQuantity", recalculateTotalQuantity(session, cart));
                writeJson(response, result);
                return;
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
                result.put("stock", latestProduct != null ? latestProduct.getStock() : 0);
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
                    writeJson(response, result);
                    return;
                }

                existingItem.setQuantity(validation.getSuggestedQuantity());
                if (user != null) {
                    cartDAO.updateCartQuantity(user.getId(), productId, validation.getSuggestedQuantity());
                }
                cart = reloadCart(session, user, cart);
                CartItem syncedItem = cart.get(productId);

                result.put("quantity", syncedItem != null ? syncedItem.getQuantity() : validation.getSuggestedQuantity());
                result.put("totalQuantity", recalculateTotalQuantity(session, cart));
                writeJson(response, result);
                return;
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
            result.put("stock", syncedItem != null ? syncedItem.getProduct().getStock() : (latestProduct != null ? latestProduct.getStock() : 0));
            result.put("totalQuantity", recalculateTotalQuantity(session, cart));
            writeJson(response, result);
        } catch (Exception e) {
            logger.error("Error updating cart with stock check for product id={}", request.getParameter("id"), e);
            writeJson(response, false, "Loi server");
        }
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

    private void writeJson(HttpServletResponse response, boolean success, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        writeJson(response, result);
    }

    private void writeJson(HttpServletResponse response, Map<String, Object> result) throws IOException {
        response.getWriter().write(gson.toJson(result));
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
            cartDAO.clearCart(user.getId());
        }
        
        session.setAttribute("toastMessage", "Đã xóa toàn bộ giỏ hàng!");
        session.setAttribute("toastType", "success");
        
        response.sendRedirect(request.getContextPath() + "/cart");
    }
}
