package Model;

import java.math.BigDecimal;

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem() {
    }

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Getters & Setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    // Hàm tính tổng tiền của riêng món này (Giá * Số lượng)
    public BigDecimal getTotalPrice() {
        return product.getEffectivePrice().multiply(BigDecimal.valueOf(quantity));
    }
}
