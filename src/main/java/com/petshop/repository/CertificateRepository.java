package com.petshop.repository;

import com.petshop.model.Certificate;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CertificateRepository extends JpaRepository<Certificate, Integer> {

    Certificate findByOrderId(int orderId);

    @Transactional
    default boolean save(int orderId, int userId, String orderCode, String certificatePem,
                         String subject, Timestamp expiresAt) {
        try {
            Certificate certificate = new Certificate();
            certificate.setOrderId(orderId);
            certificate.setUserId(userId);
            certificate.setOrderCode(orderCode);
            certificate.setCertificateData(certificatePem);
            certificate.setCertSubject(subject);
            certificate.setExpiresAt(expiresAt);
            save(certificate);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(CertificateRepository.class)
                    .error("DB error", e);
            return false;
        }
    }
}
