package Model;

import java.sql.Timestamp;

public class Certificate {

    private int id;
    private int orderId;
    private int userId;
    private String orderCode;
    private String certificateData;
    private String certSubject;
    private Timestamp expiresAt;
    private Timestamp createdAt;

    public Certificate() {
    }

    public Certificate(int id,
                       int orderId,
                       int userId,
                       String orderCode,
                       String certificateData,
                       String certSubject,
                       Timestamp expiresAt,
                       Timestamp createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.orderCode = orderCode;
        this.certificateData = certificateData;
        this.certSubject = certSubject;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getCertificateData() {
        return certificateData;
    }

    public void setCertificateData(String certificateData) {
        this.certificateData = certificateData;
    }

    public String getCertSubject() {
        return certSubject;
    }

    public void setCertSubject(String certSubject) {
        this.certSubject = certSubject;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
