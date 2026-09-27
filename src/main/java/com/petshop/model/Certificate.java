package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "order_id", nullable = false)
    private int orderId;
    @Column(name = "user_id", nullable = false)
    private int userId;
    @Column(name = "order_code", nullable = false, length = 50)
    private String orderCode;
    @Column(name = "certificate_data", nullable = false, columnDefinition = "TEXT")
    private String certificateData;
    @Column(name = "cert_subject", nullable = false)
    private String certSubject;
    @Column(name = "expires_at")
    private Timestamp expiresAt;
    @Column(name = "created_at", insertable = false, updatable = false)
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
