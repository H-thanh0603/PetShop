package Model;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private String fullname;
    private String phone;
    private String address;
    private String note;
    private double totalAmount;
    private String status; // Pending, Confirmed, Shipping, Completed, Cancelled
    private Timestamp createdAt;
    private List<OrderItem> items;

    public Order() {}

    public Order(int id, int userId, String fullname, String phone, String address, String note, double totalAmount, String status, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.fullname = fullname;
        this.phone = phone;
        this.address = address;
        this.note = note;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getFormattedTotalAmount() {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(totalAmount) + "đ";
    }
}
