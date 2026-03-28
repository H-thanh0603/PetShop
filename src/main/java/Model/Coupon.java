package Model;

import java.sql.Timestamp;

public class Coupon {
    private int id;
    private String code;
    private int discountPercent;
    private boolean active;
    private int quantity;
    private Timestamp startDate;
    private Timestamp endDate;

    public Coupon() {
    }

    public Coupon(int id, String code, int discountPercent, boolean active, int quantity, Timestamp startDate, Timestamp endDate) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.active = active;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
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
}