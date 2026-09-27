package com.petshop.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "user_id", nullable = false)
    private int userId;
    @Column(name = "is_default", nullable = false)
    private boolean defaultt;
    @Column(name = "address", nullable = false)
    private String address;
    @Column(name = "created_at", nullable = false)
    private Timestamp createAt;
    @Column(name = "province", nullable = false, length = 100)
    private String province;
    @Column(name = "district", nullable = false, length = 100)
    private String district;
    @Column(name = "ward", nullable = false, length = 100)
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
