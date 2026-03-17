package Model;

import java.sql.Date; // Hoặc Timestamp tùy bạn

public class Review {
    private int id;
    private int productId;
    private int userId;
    private String userName; // Tên người dùng (Lấy từ bảng Users)
    private int rating;
    private String comment;
    private Date createdAt;

    public Review() {
    }

    public Review(int id, int productId, int userId, String userName, int rating, String comment, Date createdAt) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}