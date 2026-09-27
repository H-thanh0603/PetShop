package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "wishlist")
public class WishlistEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "user_id", nullable = false)
    private int userId;
    @Column(name = "product_id", nullable = false)
    private int productId;
    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;

    public WishlistEntry() {
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public Timestamp getCreatedAt() { return createdAt; }
}
