package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "code", nullable = false, length = 50)
    private String code;
    @Column(name = "discount_type")
    private String discountType = "percent";
    @Column(name = "discount_value", precision = 18, scale = 0)
    private BigDecimal discountValue = BigDecimal.ZERO;
    @Column(name = "discount_percent")
    private int discountPercent;
    @Column(name = "min_order", precision = 18, scale = 0)
    private BigDecimal minOrder = BigDecimal.ZERO;
    @Column(name = "max_discount", precision = 18, scale = 0)
    private BigDecimal maxDiscount = BigDecimal.ZERO;
    @Column(name = "is_active")
    private boolean active;
    @Column(name = "quantity")
    private int quantity;
    @Column(name = "start_date")
    private Timestamp startDate;
    @Column(name = "end_date")
    private Timestamp endDate;
    @Column(name = "used")
    private int used;

    public Coupon() {
    }

    public Coupon(int id, String code, int discountPercent, boolean active, int quantity, Timestamp startDate, Timestamp endDate, int used) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.active = active;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.used = used;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getMinOrder() {
        return minOrder;
    }

    public void setMinOrder(BigDecimal minOrder) {
        this.minOrder = minOrder;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }
}
