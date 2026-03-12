package Model;

import java.text.DecimalFormat;

public class Product {
    private int id;
    private String name;
    private String image;
    private double price;    
    private int discount;     
    private String description; // Giữ lại thuộc tính mới này
    
    // 1. Constructor rỗng (Bắt buộc)
    public Product() {
    }

    // 2. Constructor đầy đủ 6 tham số (Dùng cho DAO mới)
    public Product(int id, String name, String image, double price, int discount, String description) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.price = price;
        this.discount = discount;
        this.description = description;
    }

    // --- GETTERS & SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }
    
    // QUAN TRỌNG: Giữ Getter/Setter cho Description
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // (Đã XÓA setOldPrice vì không dùng nữa)

    // Hàm tiện ích format giá tiền
    public String getFormattedPrice() {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(price) + "đ";
    }
    
    // Tính giá gốc từ giá bán và % giảm giá (để hiển thị trong JSP)
    public double getOldPrice() {
        if (discount > 0 && discount < 100) {
            return price / (1 - discount / 100.0);
        }
        return price;
    }
    
    public String getFormattedOldPrice() {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(getOldPrice()) + "đ";
    }
}