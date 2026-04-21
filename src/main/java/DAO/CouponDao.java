package DAO;

import Context.DBContext;
import Model.Coupon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CouponDao {
    public Coupon getValidCouponByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = DBContext.getConnection()) {
            return getValidCouponByCode(conn, code.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Coupon getValidCouponByCode(Connection conn, String code) throws Exception {
        String sql = """
            SELECT * 
            FROM coupons
            WHERE code = ?
              AND is_active = true
              AND quantity > 0
              AND (start_date IS NULL OR start_date <= NOW())
              AND (end_date IS NULL OR end_date >= NOW())
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Coupon c = new Coupon();
                    c.setId(rs.getInt("id"));
                    c.setCode(rs.getString("code"));
                    c.setDiscountPercent(rs.getInt("discount_percent"));
                    c.setActive(rs.getBoolean("is_active"));
                    c.setQuantity(rs.getInt("quantity"));
                    c.setStartDate(rs.getTimestamp("start_date"));
                    c.setEndDate(rs.getTimestamp("end_date"));
                    c.setUsed(rs.getInt("used"));
                    return c;
                }
            }
        }
        return null;
    }

    public boolean increaseUsedIfAvailable(Connection conn, int couponId) throws Exception {
        String sql = """
        UPDATE coupons
        SET used = used + 1
        WHERE id = ? AND used < quantity
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            return ps.executeUpdate() > 0;
        }
    }

    public Coupon getCouponByIdForUpdate(Connection conn, int couponId) throws Exception {
        String sql = "SELECT * FROM coupons WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Coupon c = new Coupon();
                    c.setId(rs.getInt("id"));
                    c.setCode(rs.getString("code"));
                    c.setDiscountPercent(rs.getInt("discount_percent"));
                    c.setActive(rs.getBoolean("is_active"));
                    c.setQuantity(rs.getInt("quantity"));
                    c.setStartDate(rs.getTimestamp("start_date"));
                    c.setEndDate(rs.getTimestamp("end_date"));
                    c.setUsed(rs.getInt("used"));
                    return c;
                }
            }
        }
        return null;
    }
}
