package com.petshop.repository;

import com.petshop.model.PaymentTransaction;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {

    @Query("SELECT t FROM PaymentTransaction t WHERE t.orderId = :orderId ORDER BY t.createdAt DESC, t.id DESC LIMIT 1")
    PaymentTransaction findLatestByOrderId(@Param("orderId") int orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM PaymentTransaction t WHERE t.orderId = :orderId ORDER BY t.createdAt DESC, t.id DESC LIMIT 1")
    PaymentTransaction findLatestByOrderIdForUpdate(@Param("orderId") int orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM PaymentTransaction t WHERE t.providerKey = 'BANK_TRANSFER' "
            + "AND t.status = 'PENDING_VERIFICATION' AND t.transferReference IS NOT NULL "
            + "AND :content LIKE CONCAT('%', t.transferReference, '%') "
            + "ORDER BY LENGTH(t.transferReference) DESC, t.createdAt ASC, t.id ASC LIMIT 1")
    PaymentTransaction findPendingByExactReferenceForUpdate(@Param("content") String content);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM PaymentTransaction t WHERE t.providerKey = 'BANK_TRANSFER' "
            + "AND t.status = 'PENDING_VERIFICATION' AND t.transferReference IS NOT NULL "
            + "ORDER BY t.createdAt ASC, t.id ASC LIMIT 200")
    List<PaymentTransaction> findPendingCandidatesForUpdate();

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE PaymentTransaction t SET t.status = :status, t.verificationStatus = :verificationStatus, "
            + "t.verificationMessage = :verificationMessage, t.updatedAt = :updatedAt, t.verifiedAt = :verifiedAt "
            + "WHERE t.id = :id")
    int updateVerificationStatus(@Param("id") int transactionId, @Param("status") String transactionStatus,
                                 @Param("verificationStatus") String verificationStatus,
                                 @Param("verificationMessage") String verificationMessage,
                                 @Param("updatedAt") Timestamp updatedAt,
                                 @Param("verifiedAt") Timestamp verifiedAt);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE PaymentTransaction t SET t.providerTransactionId = :providerTransactionId, "
            + "t.amountReceived = :amountReceived, t.bankContent = :bankContent, "
            + "t.providerMetadata = :providerMetadata, t.status = :status, "
            + "t.verificationStatus = :verificationStatus, t.verificationMessage = :verificationMessage, "
            + "t.updatedAt = CURRENT_TIMESTAMP, t.verifiedAt = :verifiedAt WHERE t.id = :id")
    int applyWebhookResult(@Param("id") int transactionId,
                           @Param("providerTransactionId") String providerTransactionId,
                           @Param("amountReceived") BigDecimal amountReceived,
                           @Param("bankContent") String bankContent,
                           @Param("providerMetadata") String providerMetadata,
                           @Param("status") String transactionStatus,
                           @Param("verificationStatus") String verificationStatus,
                           @Param("verificationMessage") String verificationMessage,
                           @Param("verifiedAt") Timestamp verifiedAt);

    @Transactional
    default int saveTx(PaymentTransaction transaction) {
        try {
            return save(transaction).getId();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(PaymentTransactionRepository.class)
                    .error("DB error", e);
            return -1;
        }
    }

    @Transactional
    default PaymentTransaction getLatestByOrderIdTx(int orderId) {
        try {
            return findLatestByOrderId(orderId);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(PaymentTransactionRepository.class)
                    .error("DB error", e);
            return null;
        }
    }

    @Transactional
    default PaymentTransaction findPendingByTransferReferenceInContentForUpdate(String content) {
        try {
            PaymentTransaction exactMatch = findPendingByExactReferenceForUpdate(content);
            if (exactMatch != null) {
                return exactMatch;
            }
            String normalizedContent = normalizeTransferReferenceToken(content);
            if (normalizedContent.isEmpty()) {
                return null;
            }
            PaymentTransaction bestMatch = null;
            int bestLength = -1;
            for (PaymentTransaction candidate : findPendingCandidatesForUpdate()) {
                String normalizedReference = normalizeTransferReferenceToken(candidate.getTransferReference());
                if (!normalizedReference.isEmpty()
                        && normalizedContent.contains(normalizedReference)
                        && normalizedReference.length() > bestLength) {
                    bestMatch = candidate;
                    bestLength = normalizedReference.length();
                }
            }
            return bestMatch;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(PaymentTransactionRepository.class)
                    .error("DB error", e);
            return null;
        }
    }

    static String normalizeTransferReferenceToken(String value) {
        if (value == null) {
            return "";
        }
        return value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    @Transactional
    default boolean updateVerificationStatusTx(int transactionId, String transactionStatus,
                                              String verificationStatus, String verificationMessage,
                                              Timestamp updatedAt, Timestamp verifiedAt) {
        try {
            return updateVerificationStatus(transactionId, transactionStatus, verificationStatus,
                    verificationMessage, updatedAt, verifiedAt) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(PaymentTransactionRepository.class)
                    .error("DB error", e);
            return false;
        }
    }

    @Transactional
    default boolean applyWebhookResultTx(int transactionId, String providerTransactionId,
                                        BigDecimal amountReceived, String bankContent,
                                        String providerMetadata, String transactionStatus,
                                        String verificationStatus, String verificationMessage,
                                        Timestamp verifiedAt) {
        try {
            return applyWebhookResult(transactionId, providerTransactionId, amountReceived, bankContent,
                    providerMetadata, transactionStatus, verificationStatus, verificationMessage, verifiedAt) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(PaymentTransactionRepository.class)
                    .error("DB error", e);
            return false;
        }
    }

    @Transactional
    default boolean updateLatestProviderResultForOrder(int orderId, String providerKey,
                                                      String providerTransactionId,
                                                      BigDecimal amountReceived,
                                                      String providerMetadata,
                                                      String transactionStatus,
                                                      String verificationStatus,
                                                      String verificationMessage) {
        try {
            PaymentTransaction transaction = findLatestByOrderIdForUpdate(orderId);
            if (transaction == null || !providerKey.equalsIgnoreCase(transaction.getProviderKey())) {
                return false;
            }
            Timestamp verifiedAt = "VERIFIED".equalsIgnoreCase(verificationStatus)
                    ? Timestamp.valueOf(LocalDateTime.now())
                    : null;
            return applyWebhookResult(transaction.getId(), providerTransactionId, amountReceived, null,
                    providerMetadata, transactionStatus, verificationStatus, verificationMessage, verifiedAt) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(PaymentTransactionRepository.class)
                    .error("Failed to update provider result for order id={}", orderId, e);
            return false;
        }
    }

    @Deprecated(forRemoval = true)
    default int save(java.sql.Connection conn, PaymentTransaction transaction) throws Exception {
        // TODO(Task 9): OrderDAO/CheckoutService/ShopApiController drop conn once @Transactional.
        return saveTx(transaction);
    }

    @Deprecated(forRemoval = true)
    default PaymentTransaction getLatestByOrderId(java.sql.Connection conn, int orderId) throws Exception {
        // TODO(Task 9): callers drop conn.
        return getLatestByOrderIdTx(orderId);
    }

    @Deprecated(forRemoval = true)
    default PaymentTransaction getLatestByOrderIdForUpdate(java.sql.Connection conn, int orderId) throws Exception {
        // TODO(Task 9): callers drop conn.
        return findLatestByOrderIdForUpdate(orderId);
    }

    @Deprecated(forRemoval = true)
    default PaymentTransaction findPendingByTransferReferenceInContentForUpdate(java.sql.Connection conn,
                                                                               String content) throws Exception {
        // TODO(Task 9): BankWebhookReconciliationService drops conn once @Transactional.
        return findPendingByTransferReferenceInContentForUpdate(content);
    }

    @Deprecated(forRemoval = true)
    default boolean updateVerificationStatus(java.sql.Connection conn, int transactionId, String transactionStatus,
                                             String verificationStatus, String verificationMessage,
                                             Timestamp updatedAt, Timestamp verifiedAt) throws Exception {
        // TODO(Task 9): callers drop conn.
        return updateVerificationStatusTx(transactionId, transactionStatus, verificationStatus,
                verificationMessage, updatedAt, verifiedAt);
    }

    @Deprecated(forRemoval = true)
    default boolean applyWebhookResult(java.sql.Connection conn, int transactionId, String providerTransactionId,
                                       BigDecimal amountReceived, String bankContent,
                                       String providerMetadata, String transactionStatus,
                                       String verificationStatus, String verificationMessage,
                                       Timestamp verifiedAt) throws Exception {
        // TODO(Task 9): BankWebhookReconciliationService drops conn once @Transactional.
        return applyWebhookResultTx(transactionId, providerTransactionId, amountReceived, bankContent,
                providerMetadata, transactionStatus, verificationStatus, verificationMessage, verifiedAt);
    }
}
