package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "fullname", length = 100)
    private String fullname;
    @Column(name = "email", length = 100)
    private String email;
    @Column(name = "phone", length = 20)
    private String phone;
    @Column(name = "address")
    private String address;
    @Column(name = "role", length = 20)
    private String role;
    // VARCHAR(20) on schema; converter reproduces legacy getBoolean+catch semantics.
    @Convert(converter = StatusBooleanConverter.class)
    @Column(name = "status", length = 20)
    private boolean status; // active, inactive, locked
    @Column(name = "has_used_discount", nullable = false)
    private boolean discountUsed;
    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;

    // Brute-force protection
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;
    @Column(name = "locked_until")
    private java.sql.Timestamp lockedUntil;

    @Column(name = "reset_token")
    private String resetToken;
    @Column(name = "reset_token_expiry")
    private Timestamp resetTokenExpiry;
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;
    @Column(name = "verification_token")
    private String verificationToken;
    @Column(name = "verification_token_expiry")
    private Timestamp verificationTokenExpiry;

    // Thống kê (computed, not columns)
    @Transient
    private int orderCount;
    @Transient
    private BigDecimal totalSpent = BigDecimal.ZERO;

    public User() {}

    public User(int id, String username, String password, String fullname, String email, String role, String phone, String address) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
        this.status = true;
        this.discountUsed = false;
        this.phone = phone;
        this.address = address;
    }
    
    // Constructor đầy đủ
    public User(int id, String username, String password, String fullname, String email, 
                String phone, String address, String role, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.createdAt = createdAt;
        this.status = true;
        this.discountUsed = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean getStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public boolean isDiscountUsed() { return discountUsed; }
    public void setDiscountUsed(boolean discountUsed) { this.discountUsed = discountUsed; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    
    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent != null ? totalSpent : BigDecimal.ZERO; }
    
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    
    public java.sql.Timestamp getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(java.sql.Timestamp lockedUntil) { this.lockedUntil = lockedUntil; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public Timestamp getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(Timestamp resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }
    public Timestamp getVerificationTokenExpiry() { return verificationTokenExpiry; }
    public void setVerificationTokenExpiry(Timestamp verificationTokenExpiry) { this.verificationTokenExpiry = verificationTokenExpiry; }
}
