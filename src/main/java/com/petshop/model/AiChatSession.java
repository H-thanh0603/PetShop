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
@Table(name = "ai_chat_sessions")
public class AiChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "guest_name")
    private String guestName;
    @Column(name = "guest_email")
    private String guestEmail;
    @Column(name = "status", length = 50)
    private String status; // OPEN, WAITING_ADMIN, ANSWERED_BY_ADMIN, CLOSED
    @Column(name = "need_admin_support")
    private boolean needAdminSupport;
    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Timestamp updatedAt;

    // Additional user fields for admin dashboard convenience (JOIN-filled, not columns)
    @Transient
    private String userFullname;
    @Transient
    private String userEmail;

    public AiChatSession() {}

    public AiChatSession(int id, Integer userId, String guestName, String guestEmail, String status, boolean needAdminSupport, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.userId = userId;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.status = status;
        this.needAdminSupport = needAdminSupport;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isNeedAdminSupport() { return needAdminSupport; }
    public void setNeedAdminSupport(boolean needAdminSupport) { this.needAdminSupport = needAdminSupport; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getUserFullname() { return userFullname; }
    public void setUserFullname(String userFullname) { this.userFullname = userFullname; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getDisplayName() {
        if (userId != null) {
            return userFullname != null ? userFullname : "User #" + userId;
        }
        if (guestName != null && !guestName.trim().isEmpty()) {
            return guestName + " (Guest)";
        }
        if (guestEmail != null && !guestEmail.trim().isEmpty()) {
            return guestEmail + " (Guest)";
        }
        return "Khách vãng lai";
    }
}
