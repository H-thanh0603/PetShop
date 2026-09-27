package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "daily_sales_summary")
public class DailySalesSummary {
    @Id
    @Column(name = "sale_date")
    private Date saleDate;
    @Column(name = "total_orders", nullable = false)
    private int totalOrders;
    @Column(name = "pending_orders", nullable = false)
    private int pendingOrders;
    @Column(name = "completed_orders", nullable = false)
    private int completedOrders;
    @Column(name = "cancelled_orders", nullable = false)
    private int cancelledOrders;
    @Column(name = "revenue", nullable = false, precision = 18, scale = 0)
    private BigDecimal revenue = BigDecimal.ZERO;
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Timestamp updatedAt;

    public DailySalesSummary() {
    }

    public Date getSaleDate() { return saleDate; }
    public void setSaleDate(Date saleDate) { this.saleDate = saleDate; }
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    public int getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }
    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }
    public int getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(int cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    public Timestamp getUpdatedAt() { return updatedAt; }
}
