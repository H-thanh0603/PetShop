package Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Locale;

public class Product {
    private int id;
    private String name;
    private String image;
    private BigDecimal price;
    private int discount;
    private String description;
    private String category;
    private int weight;
    private int stock;
    private int pet_type_id;
    private String brand;
    private double averageRating;
    private int reviewCount;
    private boolean wishlisted;
    private boolean isActive = true;
    private BigDecimal promotionFinalPrice;
    private BigDecimal promotionOriginalPrice;
    private BigDecimal promotionDiscountAmount;
    private int promotionDiscountPercent;
    private Integer activePromotionId;
    private String activePromotionName;
    private String activePromotionType;
    private Integer flashSaleRemainingQuantity;
    private Timestamp promotionEndTime;

    public Product() {
        this.price = BigDecimal.ZERO;
    }

    public Product(int id, String name, String image, BigDecimal price, int discount, String description) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.discount = discount;
        this.description = description;
    }

    public Product(int id, String name, String image, BigDecimal price, int discount, String description, String category) {
        this(id, name, image, price, discount, description);
        this.category = category;
    }

    public Product(int id, String name, String image, BigDecimal price, int discount, String description, int weight) {
        this(id, name, image, price, discount, description);
        this.weight = weight;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price != null ? price : BigDecimal.ZERO; }
    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getPet_type_id() { return pet_type_id; }
    public void setPet_type_id(int pet_type_id) { this.pet_type_id = pet_type_id; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public boolean isWishlisted() { return wishlisted; }
    public void setWishlisted(boolean wishlisted) { this.wishlisted = wishlisted; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public BigDecimal getLegacyOriginalPrice() {
        if (discount > 0 && discount < 100) {
            return price.divide(
                    BigDecimal.ONE.subtract(BigDecimal.valueOf(discount).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)),
                    0,
                    RoundingMode.HALF_UP
            );
        }
        return price != null ? price : BigDecimal.ZERO;
    }

    public BigDecimal getOriginalPrice() {
        if (promotionOriginalPrice != null && promotionOriginalPrice.compareTo(BigDecimal.ZERO) > 0) {
            return promotionOriginalPrice;
        }
        return getLegacyOriginalPrice();
    }

    public BigDecimal getEffectivePrice() {
        if (promotionFinalPrice != null && promotionFinalPrice.compareTo(BigDecimal.ZERO) >= 0) {
            return promotionFinalPrice;
        }
        return price != null ? price : BigDecimal.ZERO;
    }

    public String getFormattedPrice() {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(getEffectivePrice()).replace(',', '.') + "đ";
    }

    public BigDecimal getOldPrice() {
        return getOriginalPrice();
    }

    public String getFormattedOldPrice() {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(getOldPrice()).replace(',', '.') + "đ";
    }

    public BigDecimal getDiscountAmount() {
        BigDecimal diff = promotionDiscountAmount != null
                ? promotionDiscountAmount
                : getOriginalPrice().subtract(getEffectivePrice());
        return diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;
    }

    public String getFormattedDiscountAmount() {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(getDiscountAmount()).replace(',', '.') + "đ";
    }

    public String getFormattedAverageRating() {
        return String.format(Locale.US, "%.1f", Math.max(0, averageRating));
    }

    public BigDecimal getPromotionFinalPrice() {
        return promotionFinalPrice;
    }

    public void setPromotionFinalPrice(BigDecimal promotionFinalPrice) {
        this.promotionFinalPrice = promotionFinalPrice;
    }

    public BigDecimal getPromotionOriginalPrice() {
        return promotionOriginalPrice;
    }

    public void setPromotionOriginalPrice(BigDecimal promotionOriginalPrice) {
        this.promotionOriginalPrice = promotionOriginalPrice;
    }

    public BigDecimal getPromotionDiscountAmount() {
        return promotionDiscountAmount;
    }

    public void setPromotionDiscountAmount(BigDecimal promotionDiscountAmount) {
        this.promotionDiscountAmount = promotionDiscountAmount;
    }

    public int getPromotionDiscountPercent() {
        return promotionDiscountPercent;
    }

    public void setPromotionDiscountPercent(int promotionDiscountPercent) {
        this.promotionDiscountPercent = Math.max(0, promotionDiscountPercent);
    }

    public Integer getActivePromotionId() {
        return activePromotionId;
    }

    public void setActivePromotionId(Integer activePromotionId) {
        this.activePromotionId = activePromotionId;
    }

    public String getActivePromotionName() {
        return activePromotionName;
    }

    public void setActivePromotionName(String activePromotionName) {
        this.activePromotionName = activePromotionName;
    }

    public String getActivePromotionType() {
        return activePromotionType;
    }

    public void setActivePromotionType(String activePromotionType) {
        this.activePromotionType = activePromotionType;
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
        return getDiscountAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    // JavaBean getter alias để JSP EL có thể truy cập qua ${product.hasPromotion}.
    // EL chuẩn chỉ nhận getter dạng getXxx()/isXxx(), nên cần getHasPromotion() ở đây.
    public boolean getHasPromotion() {
        return hasPromotion();
    }

    public boolean isFlashSale() {
        return "FLASH_SALE".equalsIgnoreCase(activePromotionType);
    }

    public int getAvailablePurchaseQuantity() {
        int available = Math.max(0, stock);
        if (isFlashSale() && flashSaleRemainingQuantity != null) {
            available = Math.min(available, Math.max(0, flashSaleRemainingQuantity));
        }
        return available;
    }

    public boolean isAvailableForPurchase() {
        return getAvailablePurchaseQuantity() > 0;
    }

    public int getDisplayDiscountPercent() {
        return promotionDiscountPercent > 0 ? promotionDiscountPercent : Math.max(0, discount);
    }

    public void clearPromotionState() {
        this.promotionFinalPrice = null;
        this.promotionOriginalPrice = null;
        this.promotionDiscountAmount = null;
        this.promotionDiscountPercent = 0;
        this.activePromotionId = null;
        this.activePromotionName = null;
        this.activePromotionType = null;
        this.flashSaleRemainingQuantity = null;
        this.promotionEndTime = null;
    }
}
