package DAO;

import Context.DBContext;
import Model.Coupon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CouponDao {
    private static final Logger logger = LoggerFactory.getLogger(CouponDao.class);

    public Coupon getValidCouponByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = DBContext.getConnection()) {
            return getValidCouponByCode(conn, code.trim());
        } catch (Exception e) {
            logger.error("Error fetching valid coupon by code={}", code, e);
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
              AND used < quantity
              AND COALESCE(status, 'available') = 'available'
              AND (start_date IS NULL OR start_date <= NOW())
              AND (end_date IS NULL OR end_date >= NOW())
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCoupon(rs);
                }
            }
        }
        return null;
    }

    public boolean increaseUsedIfAvailable(Connection conn, int couponId) throws Exception {
        String sql = """
        UPDATE coupons
        SET used = used + 1,
            status = CASE WHEN used + 1 >= quantity THEN 'used' ELSE COALESCE(status, 'available') END
        WHERE id = ? AND used < quantity
          AND COALESCE(status, 'available') = 'available'
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
                    return mapCoupon(rs);
                }
            }
        }
        return null;
    }

    private Coupon mapCoupon(ResultSet rs) throws Exception {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDiscountType(rs.getString("discount_type"));
        c.setDiscountValue(rs.getBigDecimal("discount_value"));
        c.setDiscountPercent(rs.getInt("discount_percent"));
        c.setMinOrder(rs.getBigDecimal("min_order"));
        c.setMaxDiscount(rs.getBigDecimal("max_discount"));
        c.setActive(rs.getBoolean("is_active"));
        c.setQuantity(rs.getInt("quantity"));
        c.setStartDate(rs.getTimestamp("start_date"));
        c.setEndDate(rs.getTimestamp("end_date"));
        c.setUsed(rs.getInt("used"));
        try {
            c.setStatus(rs.getString("status"));
        } catch (Exception ignored) {
            c.setStatus("available");
        }
        return c;
    }
}
