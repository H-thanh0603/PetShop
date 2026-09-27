package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "order_signatures")
public class OrderSignature {

    public enum VerifyStatus {
        pending,
        verified,
        failed
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "order_id", nullable = false)
    private int orderId;
    @Column(name = "user_id", nullable = false)
    private int userId;
    @Column(name = "signature", nullable = false, columnDefinition = "TEXT")
    private String signature;
    @Enumerated(EnumType.STRING)
    @Column(name = "verify_status", nullable = false, length = 20)
    private VerifyStatus verifyStatus;
    @Column(name = "verify_message", columnDefinition = "TEXT")
    private String verifyMessage;
    @Column(name = "verified_at")
    private Timestamp verifiedAt;
    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;

    public OrderSignature() {
    }

    public OrderSignature(int id,
                          int orderId,
                          int userId,
                          String signature,
                          VerifyStatus verifyStatus,
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public VerifyStatus getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(VerifyStatus verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public String getVerifyMessage() {
        return verifyMessage;
    }

    public void setVerifyMessage(String verifyMessage) {
        this.verifyMessage = verifyMessage;
    }

    public Timestamp getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Timestamp verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
