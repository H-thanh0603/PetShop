package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import Context.DBContext;
import Model.Coupon;

/**
 * DAO cho Coupon - Mã giảm giá
 */
public class CouponDAO {
    
    // Lấy coupon theo code
    public Coupon getCouponByCode(String code) {
        String query = "SELECT * FROM coupons WHERE code = ? AND is_active = 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, code.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapCoupon(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Kiểm tra coupon có hợp lệ không
    public boolean isValidCoupon(String code, double orderTotal) {
        Coupon coupon = getCouponByCode(code);
        if (coupon == null) return false;
        
        // Kiểm tra thời gian
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (coupon.getStartDate() != null && now.before(coupon.getStartDate())) {
            return false;
        }
        if (coupon.getEndDate() != null && now.after(coupon.getEndDate())) {
            return false;
        }
        
        // Kiểm tra số lần sử dụng
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return false;
        }
        
        // Kiểm tra giá trị đơn hàng tối thiểu
        if (orderTotal < coupon.getMinOrder()) {
            return false;
        }
        
        return true;
    }
    
    // Tính giá trị giảm giá
    public double calculateDiscount(String code, double orderTotal) {
        Coupon coupon = getCouponByCode(code);
        if (coupon == null || !isValidCoupon(code, orderTotal)) {
            return 0;
        }
        
        double discount = 0;
        if ("percent".equals(coupon.getDiscountType())) {
            discount = orderTotal * coupon.getDiscountValue() / 100;
            // Áp dụng giới hạn giảm tối đa
            if (coupon.getMaxDiscount() != null && discount > coupon.getMaxDiscount()) {
                discount = coupon.getMaxDiscount();
            }
        } else {
            // Fixed discount
            discount = coupon.getDiscountValue();
        }
        
        // Không giảm quá tổng đơn hàng
        return Math.min(discount, orderTotal);
    }
    
    // Tăng số lần sử dụng coupon
    public boolean incrementUsage(String code) {
        String query = "UPDATE coupons SET used_count = used_count + 1 WHERE code = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, code.toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Map ResultSet to Coupon
    private Coupon mapCoupon(ResultSet rs) throws Exception {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDiscountType(rs.getString("discount_type"));
        c.setDiscountValue(rs.getDouble("discount_value"));
        c.setMinOrder(rs.getDouble("min_order"));
        c.setMaxDiscount(rs.getObject("max_discount") != null ? rs.getDouble("max_discount") : null);
        c.setUsageLimit(rs.getObject("usage_limit") != null ? rs.getInt("usage_limit") : null);
        c.setUsedCount(rs.getInt("used_count"));
        c.setStartDate(rs.getTimestamp("start_date"));
        c.setEndDate(rs.getTimestamp("end_date"));
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }
}
