package com.petshop.repository;

import com.petshop.model.BankWebhookEvent;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface BankWebhookEventRepository extends JpaRepository<BankWebhookEvent, Integer> {

    BankWebhookEvent findFirstByProviderTransactionId(String providerTransactionId);

    @Transactional
    default BankWebhookEvent findByProviderTransactionId(String providerTransactionId) {
        // Preserved: the old DAO threw (callers treat only null as not-found).
        return findFirstByProviderTransactionId(providerTransactionId);
    }

    @Transactional
    default int save(BankWebhookEvent.Status status, String providerTransactionId,
                     java.math.BigDecimal amount, String bankContent, String bankAccount,
                     Integer paymentTransactionId, String rawPayload) {
        // Preserved: the old DAO threw on SQL failure (callers never checked
        // the id). Propagate so @Transactional callers roll back identically.
        BankWebhookEvent event = new BankWebhookEvent();
        event.setProviderTransactionId(providerTransactionId);
        event.setAmount(amount);
        event.setBankContent(bankContent);
        event.setBankAccount(bankAccount);
        event.setPaymentTransactionId(paymentTransactionId);
        event.setStatus(status.name());
        event.setRawPayload(rawPayload);
        event.setReceivedAt(Timestamp.valueOf(LocalDateTime.now()));
        return save(event).getId();
    }
}
