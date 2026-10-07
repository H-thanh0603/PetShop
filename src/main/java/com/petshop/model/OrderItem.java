package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "order_id", nullable = false)
    private int orderId;
    @Column(name = "product_id", nullable = false)
    private int productId;
    @Column(name = "quantity", nullable = false)
    private int quantity;
    @Column(name = "price", precision = 18, scale = 0)
    private BigDecimal price;
    @Column(name = "original_price", precision = 18, scale = 0)
    private BigDecimal originalPrice;
    @Column(name = "final_price", precision = 18, scale = 0)
    private BigDecimal finalPrice;
    @Column(name = "discount_amount", precision = 18, scale = 0)
    private BigDecimal discountAmount;
    @Column(name = "promotion_id")
    private Integer promotionId;
    @Column(name = "promotion_name")
    private String promotionName;
    @Column(name = "promotion_type", length = 50)
    private String promotionType;
    @Column(name = "product_name_snapshot")
    private String productNameSnapshot;
    @Column(name = "product_image_snapshot")
    private String productImageSnapshot;
    @Transient
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
