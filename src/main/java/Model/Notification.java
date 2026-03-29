package Model;

import java.sql.Timestamp;

/**
 * Model cho Push Notification
 */
public class Notification {
    private int id;
    private int userId;
    private String title;
    private String message;
    private String type; // order, promo, system, review
    private String link;
    private boolean isRead;
    private Timestamp createdAt;
    
    public Notification() {}
    
    public Notification(int userId, String title, String message, String type, String link) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.link = link;
        this.isRead = false;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    // Helper: icon theo type
    public String getIcon() {
        switch (type != null ? type : "") {
            case "order": return "bx-package";
            case "promo": return "bx-gift";
            case "review": return "bx-star";
            case "system": return "bx-bell";
            default: return "bx-bell";
        }
    }
}
