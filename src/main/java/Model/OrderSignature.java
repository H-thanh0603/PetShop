package Model;

import java.sql.Timestamp;

public class OrderSignature {

    private int id;
    private int orderId;
    private int userId;
    private String signature;
    private String verifyStatus;
    private String verifyMessage;
    private Timestamp verifiedAt;
    private Timestamp createdAt;

    public OrderSignature() {
    }

    public OrderSignature(int id, int orderId, int userId,
                          String signature, String verifyStatus,
                          String verifyMessage,
                          Timestamp verifiedAt,
                          Timestamp createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.signature = signature;
        this.verifyStatus = verifyStatus;
        this.verifyMessage = verifyMessage;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(String verifyStatus) { this.verifyStatus = verifyStatus; }

    public String getVerifyMessage() { return verifyMessage; }
    public void setVerifyMessage(String verifyMessage) { this.verifyMessage = verifyMessage; }

    public Timestamp getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Timestamp verifiedAt) { this.verifiedAt = verifiedAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
