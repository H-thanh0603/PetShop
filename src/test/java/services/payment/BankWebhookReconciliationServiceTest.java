package services.payment;


import com.petshop.repository.BankWebhookEventRepository;
import com.petshop.repository.OrderLogRepository;
import com.petshop.repository.OrderRepository;
import com.petshop.repository.PaymentTransactionRepository;
import com.petshop.model.BankWebhookEvent;
import com.petshop.model.PaymentTransaction;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BankWebhookReconciliationServiceTest {

    @Test
    void exactAmountAndReferenceMarksPaymentVerified() throws Exception {
        BankWebhookEventRepository eventDAO = mock(BankWebhookEventRepository.class);
        PaymentTransactionRepository transactionDAO = mock(PaymentTransactionRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        OrderLogRepository orderLogDAO = mock(OrderLogRepository.class);

        PaymentTransaction pending = pendingTransaction();
        when(eventDAO.findByProviderTransactionId("BANK_TXN_987")).thenReturn(null);
        when(transactionDAO.findPendingByTransferReferenceInContentForUpdate("Thanh toan PETSHOP-U7-123456"))
                .thenReturn(pending);
        when(eventDAO.save(BankWebhookEvent.Status.MATCHED, "BANK_TXN_987",
                new BigDecimal("258000"), "Thanh toan PETSHOP-U7-123456", "123456789", 55, "{}"))
                .thenReturn(10);
        when(transactionDAO.applyWebhookResultTx(eq(55), eq("BANK_TXN_987"),
                eq(new BigDecimal("258000")), eq("Thanh toan PETSHOP-U7-123456"), eq("{}"),
                eq("VERIFIED"), eq("VERIFIED"), any(), any())).thenReturn(true);
        when(orderDAO.updatePaymentStatus(456, true)).thenReturn(true);
        when(orderDAO.markAwaitingPaymentOrderPaid(456)).thenReturn(true);
        when(orderDAO.finalizeReservedStockForOrder(456)).thenReturn(true);
        when(orderLogDAO.insert(456, "WEBHOOK", null,
                "BANK_WEBHOOK_VERIFIED", "PENDING_VERIFICATION", "VERIFIED",
                "Webhook ngân hàng đã khớp thanh toán.")).thenReturn(true);

        BankWebhookReconciliationService service = new BankWebhookReconciliationService(
                eventDAO, transactionDAO, orderDAO, orderLogDAO
        );

            BankWebhookReconciliationResult result = service.reconcile(
                    new BankWebhookPayload("BANK_TXN_987", new BigDecimal("258000"),
                            "Thanh toan PETSHOP-U7-123456", "123456789", "{}", null)
            );

            assertEquals(BankWebhookReconciliationResult.Status.VERIFIED, result.getStatus());

        verify(orderDAO).updatePaymentStatus(456, true);
        verify(orderDAO).markAwaitingPaymentOrderPaid(456);
        verify(orderDAO).finalizeReservedStockForOrder(456);
    }

    @Test
    void expiredPaymentReleasesReservedStock() throws Exception {
        BankWebhookEventRepository eventDAO = mock(BankWebhookEventRepository.class);
        PaymentTransactionRepository transactionDAO = mock(PaymentTransactionRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        OrderLogRepository orderLogDAO = mock(OrderLogRepository.class);

        PaymentTransaction pending = pendingTransaction();
        pending.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().minusMinutes(1)));
        when(eventDAO.findByProviderTransactionId("BANK_TXN_990")).thenReturn(null);
        when(transactionDAO.findPendingByTransferReferenceInContentForUpdate("PETSHOP-U7-123456"))
                .thenReturn(pending);
        when(eventDAO.save(BankWebhookEvent.Status.EXPIRED, "BANK_TXN_990",
                new BigDecimal("258000"), "PETSHOP-U7-123456", "123456789", 55, "{}"))
                .thenReturn(13);
        when(transactionDAO.applyWebhookResultTx(eq(55), eq("BANK_TXN_990"),
                eq(new BigDecimal("258000")), eq("PETSHOP-U7-123456"), eq("{}"),
                eq("EXPIRED"), eq("EXPIRED"), any(), eq(null))).thenReturn(true);
        when(orderDAO.updatePaymentStatus(456, false)).thenReturn(true);
        when(orderDAO.releaseReservedStockForOrder(456)).thenReturn(true);
        when(orderLogDAO.insert(456, "WEBHOOK", null,
                "BANK_WEBHOOK_EXPIRED", "PENDING_VERIFICATION", "EXPIRED",
                "Webhook đến sau thời hạn giữ thanh toán.")).thenReturn(true);

        BankWebhookReconciliationService service = new BankWebhookReconciliationService(
                eventDAO, transactionDAO, orderDAO, orderLogDAO
        );

            BankWebhookReconciliationResult result = service.reconcile(
                    new BankWebhookPayload("BANK_TXN_990", new BigDecimal("258000"),
                            "PETSHOP-U7-123456", "123456789", "{}", null)
            );

            assertEquals(BankWebhookReconciliationResult.Status.EXPIRED, result.getStatus());

        verify(orderDAO).releaseReservedStockForOrder(456);
    }

    @Test
    void duplicateProviderTransactionIdIsIgnored() throws Exception {
        BankWebhookEventRepository eventDAO = mock(BankWebhookEventRepository.class);
        PaymentTransactionRepository transactionDAO = mock(PaymentTransactionRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        OrderLogRepository orderLogDAO = mock(OrderLogRepository.class);

        BankWebhookEvent existing = new BankWebhookEvent();
        existing.setProviderTransactionId("BANK_TXN_987");
        existing.setStatus(BankWebhookEvent.Status.MATCHED.name());
        when(eventDAO.findByProviderTransactionId("BANK_TXN_987")).thenReturn(existing);

        BankWebhookReconciliationService service = new BankWebhookReconciliationService(
                eventDAO, transactionDAO, orderDAO, orderLogDAO
        );

            BankWebhookReconciliationResult result = service.reconcile(
                    new BankWebhookPayload("BANK_TXN_987", new BigDecimal("258000"),
                            "Thanh toan PETSHOP-U7-123456", "123456789", "{}", null)
            );

            assertEquals(BankWebhookReconciliationResult.Status.DUPLICATE, result.getStatus());

        verifyNoInteractions(transactionDAO, orderDAO);
    }

    @Test
    void amountMismatchIsStoredForManualReviewAndDoesNotMarkPaid() throws Exception {
        BankWebhookEventRepository eventDAO = mock(BankWebhookEventRepository.class);
        PaymentTransactionRepository transactionDAO = mock(PaymentTransactionRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        OrderLogRepository orderLogDAO = mock(OrderLogRepository.class);

        PaymentTransaction pending = pendingTransaction();
        when(eventDAO.findByProviderTransactionId("BANK_TXN_988")).thenReturn(null);
        when(transactionDAO.findPendingByTransferReferenceInContentForUpdate("PETSHOP-U7-123456"))
                .thenReturn(pending);
        when(eventDAO.save(BankWebhookEvent.Status.AMOUNT_MISMATCH, "BANK_TXN_988",
                new BigDecimal("250000"), "PETSHOP-U7-123456", "123456789", 55, "{}"))
                .thenReturn(11);
        when(transactionDAO.applyWebhookResultTx(eq(55), eq("BANK_TXN_988"),
                eq(new BigDecimal("250000")), eq("PETSHOP-U7-123456"), eq("{}"),
                eq("FAILED"), eq("MISMATCH"), any(), eq(null))).thenReturn(true);
        when(orderDAO.updatePaymentStatus(456, false)).thenReturn(true);
        when(orderDAO.releaseReservedStockForOrder(456)).thenReturn(true);
        when(orderLogDAO.insert(456, "WEBHOOK", null,
                "BANK_WEBHOOK_AMOUNT_MISMATCH", "PENDING_VERIFICATION", "MISMATCH",
                "Webhook thanh toán gửi số tiền không khớp.")).thenReturn(true);

        BankWebhookReconciliationService service = new BankWebhookReconciliationService(
                eventDAO, transactionDAO, orderDAO, orderLogDAO
        );

            BankWebhookReconciliationResult result = service.reconcile(
                    new BankWebhookPayload("BANK_TXN_988", new BigDecimal("250000"),
                            "PETSHOP-U7-123456", "123456789", "{}", null)
            );

            assertEquals(BankWebhookReconciliationResult.Status.AMOUNT_MISMATCH, result.getStatus());

        verify(orderDAO).updatePaymentStatus(456, false);
        verify(orderDAO).releaseReservedStockForOrder(456);
    }

    @Test
    void unknownContentIsStoredAsUnmatched() throws Exception {
        BankWebhookEventRepository eventDAO = mock(BankWebhookEventRepository.class);
        PaymentTransactionRepository transactionDAO = mock(PaymentTransactionRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        OrderLogRepository orderLogDAO = mock(OrderLogRepository.class);

        when(eventDAO.findByProviderTransactionId("BANK_TXN_989")).thenReturn(null);
        when(transactionDAO.findPendingByTransferReferenceInContentForUpdate("thanh toan don hang"))
                .thenReturn(null);
        when(eventDAO.save(BankWebhookEvent.Status.UNMATCHED, "BANK_TXN_989",
                new BigDecimal("258000"), "thanh toan don hang", "123456789", null, "{}"))
                .thenReturn(12);

        BankWebhookReconciliationService service = new BankWebhookReconciliationService(
                eventDAO, transactionDAO, orderDAO, orderLogDAO
        );

            BankWebhookReconciliationResult result = service.reconcile(
                    new BankWebhookPayload("BANK_TXN_989", new BigDecimal("258000"),
                            "thanh toan don hang", "123456789", "{}", null)
            );

            assertEquals(BankWebhookReconciliationResult.Status.UNMATCHED, result.getStatus());

        verifyNoInteractions(orderDAO);
    }

    private PaymentTransaction pendingTransaction() {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(55);
        transaction.setOrderId(456);
        transaction.setAmount(new BigDecimal("258000"));
        transaction.setTransferReference("PETSHOP-U7-123456");
        transaction.setStatus("PENDING_VERIFICATION");
        transaction.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusMinutes(5)));
        return transaction;
    }
}
