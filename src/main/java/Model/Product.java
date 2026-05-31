package Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    
    public Product() {
        this.price = BigDecimal.ZERO;
    }

    // Constructor 6 tham số
    public Product(int id, String name, String image, BigDecimal price, int discount, String description) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.discount = discount;
        this.description = description;
    }

    // Constructor 7 tham số (có category)
    public Product(int id, String name, String image, BigDecimal price, int discount, String description, String category) {
        this(id, name, image, price, discount, description);
        this.category = category;
    }

    // Constructor 7 tham số (có weight - từ main)
    public Product(int id, String name, String image, BigDecimal price, int discount, String description, int weight) {
        this(id, name, image, price, discount, description);
        this.weight = weight;
    }

    // Getters & Setters
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

    public String getFormattedPrice() {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(price).replace(',', '.') + "đ";
    }
    
    public BigDecimal getOldPrice() {
        if (discount > 0 && discount < 100) {
            return price.divide(BigDecimal.ONE.subtract(BigDecimal.valueOf(discount).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)), 0, RoundingMode.HALF_UP);
        }
        return price;
    }
    
    public String getFormattedOldPrice() {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(getOldPrice()).replace(',', '.') + "đ";
    }

    public BigDecimal getDiscountAmount() {
        BigDecimal diff = getOldPrice().subtract(price);
        return diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;
    }

    public String getFormattedDiscountAmount() {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(getDiscountAmount()).replace(',', '.') + "đ";
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getPet_type_id() {
        return pet_type_id;
    }

    public void setPet_type_id(int pet_type_id) {
        this.pet_type_id = pet_type_id;
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getFormattedAverageRating() {
        return String.format(Locale.US, "%.1f", Math.max(0, averageRating));
    }

    public boolean isWishlisted() {
        return wishlisted;
    }

    public void setWishlisted(boolean wishlisted) {
        this.wishlisted = wishlisted;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
