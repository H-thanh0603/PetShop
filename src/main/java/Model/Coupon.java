package Model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Coupon {
    private int id;
    private String code;
    private String discountType = "percent";
    private BigDecimal discountValue = BigDecimal.ZERO;
    private int discountPercent;
    private BigDecimal minOrder = BigDecimal.ZERO;
    private BigDecimal maxDiscount = BigDecimal.ZERO;
    private boolean active;
    private int quantity;
    private Timestamp startDate;
    private Timestamp endDate;
    private int used;
    private String status = "available";

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
