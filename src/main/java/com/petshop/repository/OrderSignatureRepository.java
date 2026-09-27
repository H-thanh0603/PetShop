package com.petshop.repository;

import com.petshop.model.OrderSignature;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderSignatureRepository extends JpaRepository<OrderSignature, Integer> {

    OrderSignature findByOrderId(int orderId);

    List<OrderSignature> findByUserId(int userId);

    @Transactional
    default boolean save(int orderId, int userId, String signatureBase64) {
        try {
            OrderSignature signature = new OrderSignature();
            signature.setOrderId(orderId);
            signature.setUserId(userId);
            signature.setSignature(signatureBase64);
            signature.setVerifyStatus(OrderSignature.VerifyStatus.pending);
            save(signature);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(OrderSignatureRepository.class)
                    .error("DB error", e);
            return false;
        }
    }

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE OrderSignature s SET s.verifyStatus = :status, s.verifyMessage = :message, s.verifiedAt = CURRENT_TIMESTAMP WHERE s.orderId = :orderId")
    int updateVerifyStatus(@Param("orderId") int orderId,
                           @Param("status") OrderSignature.VerifyStatus status,
                           @Param("message") String message);

    @Transactional
    default boolean updateVerifyStatusBool(int orderId, OrderSignature.VerifyStatus status, String message) {
        try {
            return updateVerifyStatus(orderId, status, message) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(OrderSignatureRepository.class)
                    .error("DB error", e);
            return false;
        }
    }
}
