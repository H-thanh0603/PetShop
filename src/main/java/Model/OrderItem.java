package Model;

import java.math.BigDecimal;

public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal finalPrice;
    private BigDecimal discountAmount;
    private Integer promotionId;
    private String promotionName;
    private String promotionType;
    private String productNameSnapshot;
    private String productImageSnapshot;
    private Product product;

    public OrderItem() {
        this.price = BigDecimal.ZERO;
    }

    public OrderItem(int id, int orderId, int productId, int quantity, BigDecimal price) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price != null ? price : BigDecimal.ZERO; }
    public BigDecimal getOriginalPrice() { return originalPrice != null ? originalPrice : getPrice(); }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public BigDecimal getFinalPrice() { return finalPrice != null ? finalPrice : getPrice(); }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    public BigDecimal getDiscountAmount() { return discountAmount != null ? discountAmount : BigDecimal.ZERO; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public Integer getPromotionId() { return promotionId; }
    public void setPromotionId(Integer promotionId) { this.promotionId = promotionId; }
    public String getPromotionName() { return promotionName; }
    public void setPromotionName(String promotionName) { this.promotionName = promotionName; }
    public String getPromotionType() { return promotionType; }
    public void setPromotionType(String promotionType) { this.promotionType = promotionType; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }
    public String getProductImageSnapshot() { return productImageSnapshot; }
    public void setProductImageSnapshot(String productImageSnapshot) { this.productImageSnapshot = productImageSnapshot; }

    public String getProductName() {
        if (productNameSnapshot != null && !productNameSnapshot.isEmpty()) {
            return productNameSnapshot;
        }
        if (product != null) {
            return product.getName();
        }
        return "Sản phẩm";
    }

    public String getProductImage() {
        if (productImageSnapshot != null && !productImageSnapshot.isEmpty()) {
            return productImageSnapshot;
        }
        if (product != null) {
            return product.getImage();
        }
        return "";
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getSubtotal() {
        return getFinalPrice().multiply(BigDecimal.valueOf(quantity));
    }

    // Alias used by the order-success view (mirrors CartItem.getTotalPrice()).
    public BigDecimal getTotalPrice() {
        return getSubtotal();
    }
}
