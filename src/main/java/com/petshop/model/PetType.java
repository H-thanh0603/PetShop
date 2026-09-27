package com.petshop.model;

/**
 * Model đại diện cho loại thú cưng (Chó, Mèo, Cá, Chim, ...)
 * Thiết kế để dễ dàng mở rộng thêm loại thú cưng mới
 */
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pet_types")
public class PetType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "code", nullable = false, length = 20)
    private String code;      // dog, cat, fish, bird, ...
    @Column(name = "name", nullable = false, length = 100)
    private String name;      // Chó, Mèo, Cá, Chim, ...
    @Column(name = "icon", length = 50)
    private String icon;      // bxs-dog, bxs-cat, ...
    @Column(name = "display_order")
    private int displayOrder;
    @Column(name = "is_active")
    private boolean isActive;

    public PetType() {}

    public PetType(int id, String code, String name, String icon, int displayOrder, boolean isActive) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.icon = icon;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
