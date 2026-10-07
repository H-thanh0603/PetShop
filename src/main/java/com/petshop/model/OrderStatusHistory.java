package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "order_id", nullable = false)
    private int orderId;
    @Column(name = "old_status", nullable = false, length = 50)
    private String oldStatus;
    @Column(name = "new_status", nullable = false, length = 50)
    private String newStatus;
    @Column(name = "changed_by", nullable = false)
    private int changedBy;
    // JOIN-filled display field, not a column.
    @Transient
    private String changedByName;
    @Column(name = "changed_at", insertable = false, updatable = false)
    private Timestamp changedAt;

    public OrderStatusHistory() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public int getChangedBy() { return changedBy; }
    public void setChangedBy(int changedBy) { this.changedBy = changedBy; }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

    public Timestamp getChangedAt() { return changedAt; }
    public void setChangedAt(Timestamp changedAt) { this.changedAt = changedAt; }
}
