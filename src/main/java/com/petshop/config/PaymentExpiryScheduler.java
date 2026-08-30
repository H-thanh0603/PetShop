package com.petshop.config;

import DAO.PaymentTransactionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Replaces the ScheduledExecutorService in the old AppLifecycleListener:
 * expires pending bank-transfer payment transactions and releases their
 * stock reservations every 5 minutes, batched with LIMIT 200 FOR UPDATE.
 *
 * Single-node assumption: with more than one app instance this job must be
 * guarded by a distributed lock (DB/Redis) to avoid double-running.
 */
@Component
public class PaymentExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PaymentExpiryScheduler.class);

    private final PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();

    @Scheduled(initialDelay = 60_000, fixedDelay = 300_000)
    public void expirePendingTransactions() {
        try {
            paymentTransactionDAO.expirePendingTransactions();
        } catch (Exception e) {
            logger.error("Payment expiry job failed", e);
        }
    }
}
