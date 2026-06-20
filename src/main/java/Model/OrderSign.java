package Model;

import java.sql.Timestamp;

public class OrderSign {

    private int id;
    private int orderId;
    private int userId;
    private String orderData;
    private String orderHash;
    private String publicKey;
    private Timestamp createdAt;

    public OrderSign() {
    }

    public OrderSign(int id, int orderId, int userId,
                     String orderData, String orderHash,
                     String publicKey, Timestamp createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.orderData = orderData;
        this.orderHash = orderHash;
        this.publicKey = publicKey;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOrderData() { return orderData; }
    public void setOrderData(String orderData) { this.orderData = orderData; }

    public String getOrderHash() { return orderHash; }
    public void setOrderHash(String orderHash) { this.orderHash = orderHash; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
