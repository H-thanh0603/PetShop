package services;

import DAO.ProductDAO;
import Model.CartItem;
import Model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryService {
    public static final int LOW_STOCK_THRESHOLD = 10;

    private final ProductDAO productDAO = new ProductDAO();

    public void refreshCartProducts(Map<Integer, CartItem> cart) {
        if (cart == null || cart.isEmpty()) {
            return;
        }

        List<Integer> removedProductIds = new ArrayList<>();
        for (Map.Entry<Integer, CartItem> entry : cart.entrySet()) {
            Product latestProduct = productDAO.getProductById(entry.getKey());

            // Nếu sản phẩm đã bị xóa khỏi DB thì bỏ khỏi cart session để tránh thao tác lỗi.
            if (latestProduct == null) {
                removedProductIds.add(entry.getKey());
                continue;
            }

            entry.getValue().setProduct(latestProduct);
        }

        for (Integer removedProductId : removedProductIds) {
            cart.remove(removedProductId);
        }
    }

    /**
     * Refresh cart products and return names of removed products for notification.
     */
    public List<String> refreshCartProductsWithNotification(Map<Integer, CartItem> cart) {
        List<String> removedNames = new ArrayList<>();
        if (cart == null || cart.isEmpty()) {
            return removedNames;
        }

        List<Integer> removedProductIds = new ArrayList<>();
        for (Map.Entry<Integer, CartItem> entry : cart.entrySet()) {
            Product latestProduct = productDAO.getProductById(entry.getKey());

            if (latestProduct == null || latestProduct.getStock() <= 0) {
                String name = entry.getValue().getProduct() != null
                        ? entry.getValue().getProduct().getName()
                        : "Sản phẩm #" + entry.getKey();
                removedNames.add(name);
                removedProductIds.add(entry.getKey());
                continue;
            }

            entry.getValue().setProduct(latestProduct);
        }

        for (Integer removedProductId : removedProductIds) {
            cart.remove(removedProductId);
        }
        return removedNames;
    }

    public StockValidationResult validateAddToCart(Map<Integer, CartItem> cart, int productId, int quantityToAdd) {
        Product latestProduct = productDAO.getProductById(productId);
        if (latestProduct == null) {
            return StockValidationResult.invalid("Sản phẩm không tồn tại.", null, 0, false);
        }

        if (quantityToAdd < 1) {
            return StockValidationResult.invalid("Số lượng sản phẩm không hợp lệ.", latestProduct, 0, latestProduct.getStock() <= 0);
        }

        if (latestProduct.getStock() <= 0) {
            return StockValidationResult.invalid(
                    "Sản phẩm \"" + latestProduct.getName() + "\" đã hết hàng.",
                    latestProduct,
                    0,
                    true
            );
        }

        int currentQuantityInCart = 0;
        if (cart != null && cart.containsKey(productId)) {
            currentQuantityInCart = cart.get(productId).getQuantity();
        }

        int requestedTotalQuantity = currentQuantityInCart + quantityToAdd;
        if (requestedTotalQuantity > latestProduct.getStock()) {
            return StockValidationResult.invalid(
                    "Sản phẩm \"" + latestProduct.getName() + "\" chỉ còn " + latestProduct.getStock() + " sản phẩm.",
                    latestProduct,
                    latestProduct.getStock(),
                    false
            );
        }

        return StockValidationResult.valid(latestProduct, requestedTotalQuantity);
    }

    public StockValidationResult validateCartQuantity(Map<Integer, CartItem> cart, int productId, int requestedQuantity) {
        Product latestProduct = productDAO.getProductById(productId);
        if (latestProduct == null) {
            return StockValidationResult.invalid("Sản phẩm không còn tồn tại.", null, 0, false);
        }

        int currentQuantity = 0;
        if (cart != null && cart.containsKey(productId)) {
            currentQuantity = cart.get(productId).getQuantity();
        }

        if (requestedQuantity <= 0) {
            return StockValidationResult.valid(latestProduct, 0);
        }

        if (latestProduct.getStock() <= 0) {
            return StockValidationResult.invalid(
                    "Sản phẩm \"" + latestProduct.getName() + "\" đã hết hàng.",
                    latestProduct,
                    currentQuantity,
                    true
            );
        }

        if (requestedQuantity > latestProduct.getStock()) {
            return StockValidationResult.invalid(
                    "Sản phẩm \"" + latestProduct.getName() + "\" chỉ còn " + latestProduct.getStock() + " sản phẩm.",
                    latestProduct,
                    latestProduct.getStock(),
                    false
            );
        }

        return StockValidationResult.valid(latestProduct, requestedQuantity);
    }

    public List<String> validateCartForCheckout(Map<Integer, CartItem> cart) {
        List<String> stockErrors = new ArrayList<>();
        if (cart == null || cart.isEmpty()) {
            return stockErrors;
        }

        for (CartItem item : cart.values()) {
            Product latestProduct = productDAO.getProductById(item.getProduct().getId());
            if (latestProduct == null) {
                stockErrors.add("Một sản phẩm trong giỏ hàng không còn tồn tại.");
                continue;
            }

            item.setProduct(latestProduct);

            // Chặn checkout nếu sản phẩm đã hết hàng hoặc số lượng trong cart vượt stock hiện tại.
            if (latestProduct.getStock() <= 0) {
                stockErrors.add("Sản phẩm \"" + latestProduct.getName() + "\" đã hết hàng.");
            } else if (item.getQuantity() > latestProduct.getStock()) {
                stockErrors.add("Sản phẩm \"" + latestProduct.getName() + "\" chỉ còn " + latestProduct.getStock() + " sản phẩm.");
            }
        }

        return stockErrors;
    }

    public static class StockValidationResult {
        private final boolean valid;
        private final String message;
        private final Product product;
        private final int suggestedQuantity;
        private final boolean outOfStock;

        private StockValidationResult(boolean valid, String message, Product product, int suggestedQuantity, boolean outOfStock) {
            this.valid = valid;
            this.message = message;
            this.product = product;
            this.suggestedQuantity = suggestedQuantity;
            this.outOfStock = outOfStock;
        }

        public static StockValidationResult valid(Product product, int suggestedQuantity) {
            return new StockValidationResult(true, null, product, suggestedQuantity, false);
        }

        public static StockValidationResult invalid(String message, Product product, int suggestedQuantity, boolean outOfStock) {
            return new StockValidationResult(false, message, product, suggestedQuantity, outOfStock);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public Product getProduct() {
            return product;
        }

        public int getSuggestedQuantity() {
            return suggestedQuantity;
        }

        public boolean isOutOfStock() {
            return outOfStock;
        }
    }
}
