package Model;

import java.sql.Timestamp;

public class Address {
    private int id;
    private int userId;
    private boolean defaultt;
    private String address;
    private Timestamp createAt;
    private String province;
    private String district;
    private String ward;
    public Address(){}

    public Address(int id, int userId, boolean defaultt, Timestamp createAt, String address, String province, String district, String ward) {
        this.id = id;
        this.userId = userId;
        this.defaultt = defaultt;
        this.address = address;
        this.createAt = createAt;
        this.province = province;
        this.district = district;
        this.ward = ward;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public String getProvince() {
        return province;
    }

    public boolean isDefaultt() {
        return defaultt;
    }

    public void setDefaultt(boolean defaultt) {
        this.defaultt = defaultt;
    }
    public boolean getDefaultt() {
        return defaultt;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }
}
