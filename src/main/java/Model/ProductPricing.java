package Model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ProductPricing {
    private BigDecimal originalPrice = BigDecimal.ZERO;
    private BigDecimal finalPrice = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private int discountPercent;
    private Integer promotionId;
    private String promotionName;
    private String promotionType;
    private Integer flashSaleRemainingQuantity;
    private Timestamp promotionEndTime;

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice != null ? originalPrice : BigDecimal.ZERO;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice != null ? finalPrice : BigDecimal.ZERO;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = Math.max(0, discountPercent);
    }

    public Integer getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Integer promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public String getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(String promotionType) {
        this.promotionType = promotionType;
    }

    public Integer getFlashSaleRemainingQuantity() {
        return flashSaleRemainingQuantity;
    }

    public void setFlashSaleRemainingQuantity(Integer flashSaleRemainingQuantity) {
        this.flashSaleRemainingQuantity = flashSaleRemainingQuantity;
    }

    public Timestamp getPromotionEndTime() {
        return promotionEndTime;
    }

    public void setPromotionEndTime(Timestamp promotionEndTime) {
        this.promotionEndTime = promotionEndTime;
    }

    public boolean hasPromotion() {
        return promotionId != null || "LEGACY_DISCOUNT".equalsIgnoreCase(promotionType);
    }

    public boolean isFlashSale() {
        return "FLASH_SALE".equalsIgnoreCase(promotionType);
    }
}
