package com.petshop.repository;

import com.petshop.model.Coupon;
import jakarta.persistence.LockModeType;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    @Query(value = "SELECT * FROM coupons WHERE code = :code AND is_active = 1 "
            + "AND quantity > 0 AND used < quantity "
            + "AND (start_date IS NULL OR DATE(start_date) <= CURRENT_DATE()) "
            + "AND (end_date IS NULL OR DATE(end_date) >= CURRENT_DATE())",
            nativeQuery = true)
    Coupon findValidByCode(@Param("code") String code);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE coupons SET used = used + 1, "
            + "is_active = CASE WHEN used + 1 >= quantity THEN 0 ELSE is_active END "
            + "WHERE id = :id AND is_active = 1 AND used < quantity "
            + "AND (start_date IS NULL OR DATE(start_date) <= CURRENT_DATE()) "
            + "AND (end_date IS NULL OR DATE(end_date) >= CURRENT_DATE())",
            nativeQuery = true)
    int increaseUsedIfAvailable(@Param("id") int couponId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Coupon findByIdForUpdate(@Param("id") int couponId);

    @Transactional
    default Coupon getValidCouponByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return findValidByCode(code.trim());
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(CouponRepository.class)
                    .error("Error fetching valid coupon by code={}", code, e);
            return null;
        }
    }

    @Transactional
    default boolean increaseUsedIfAvailableTx(int couponId) {
        try {
            return increaseUsedIfAvailable(couponId) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(CouponRepository.class)
                    .error("DB error", e);
            return false;
        }
    }

    @Transactional
    default Coupon getCouponByIdForUpdate(int couponId) {
        try {
            return findByIdForUpdate(couponId);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(CouponRepository.class)
                    .error("DB error", e);
            return null;
        }
    }
}
