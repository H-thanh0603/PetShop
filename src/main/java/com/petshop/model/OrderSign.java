package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "order_signs")
public class OrderSign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "order_id", nullable = false)
    private int orderId;
    @Column(name = "user_id", nullable = false)
    private int userId;
    @Column(name = "order_data", nullable = false, columnDefinition = "TEXT")
    private String orderData;
    @Column(name = "order_hash", nullable = false, length = 64)
    private String orderHash;
    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;
    @Column(name = "private_key", columnDefinition = "TEXT")
    private String privateKey;
    @Column(name = "created_at", insertable = false, updatable = false)
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

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
