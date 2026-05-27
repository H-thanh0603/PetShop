package Model;

public class CustomerRepurchaseSuggestion {
    private int orderId;
    private int productId;
    private String productName;
    private int quantity;
    private int daysSincePurchase;

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getDaysSincePurchase() {
        return daysSincePurchase;
    }

    public void setDaysSincePurchase(int daysSincePurchase) {
        this.daysSincePurchase = daysSincePurchase;
    }

    public String getMessage() {
        return "Bạn đã mua " + productName + " " + daysSincePurchase + " ngày trước. Có muốn mua lại không?";
    }
}
