package Model;

import java.sql.Timestamp;

/**
 * Model cho Coupon - Mã giảm giá
 */
public class Coupon {
    private int id;
    private String code;
    private String discountType; // "percent" hoặc "fixed"
    private double discountValue;
    private double minOrder;
    private Double maxDiscount;
    private Integer usageLimit;
    private int usedCount;
    private Timestamp startDate;
    private Timestamp endDate;
    private boolean isActive;
    
    public Coupon() {}
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    
    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }
    
    public double getMinOrder() { return minOrder; }
    public void setMinOrder(double minOrder) { this.minOrder = minOrder; }
    
    public Double getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(Double maxDiscount) { this.maxDiscount = maxDiscount; }
    
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    
    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    
    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }
    
    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    // Helper method để hiển thị mô tả coupon
    public String getDescription() {
        if ("percent".equals(discountType)) {
            String desc = "Giảm " + (int)discountValue + "%";
            if (maxDiscount != null) {
                desc += " (tối đa " + String.format("%,.0f", maxDiscount) + "đ)";
            }
            return desc;
        } else {
            return "Giảm " + String.format("%,.0f", discountValue) + "đ";
        }
    }
}
