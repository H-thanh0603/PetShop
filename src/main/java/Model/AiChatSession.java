package Model;

import java.sql.Timestamp;

public class AiChatSession {
    private int id;
    private Integer userId;
    private String guestName;
    private String guestEmail;
    private String status; // OPEN, WAITING_ADMIN, ANSWERED_BY_ADMIN, CLOSED
    private boolean needAdminSupport;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional user fields for admin dashboard convenience
    private String userFullname;
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
