package Model;

import java.sql.Timestamp;

public class UserAddress {
    private int id;
    private int userId;
    private String fullname;
    private String address;
    private boolean isDefault;
    private Timestamp createAt;
    public UserAddress(){}

    public UserAddress(int id, int userId, String fullname, String address, boolean isDefault, Timestamp createAt) {
        this.id = id;
        this.userId = userId;
        this.fullname = fullname;
        this.address = address;
        this.isDefault = isDefault;
        this.createAt = createAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

}
