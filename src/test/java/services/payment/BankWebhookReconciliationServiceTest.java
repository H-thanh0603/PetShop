package services.payment;

import Context.DBContext;
import DAO.BankWebhookEventDAO;
import DAO.OrderLogDAO;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import Model.BankWebhookEvent;
import Model.PaymentTransaction;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BankWebhookReconciliationServiceTest {

    @Test
    void exactAmountAndReferenceMarksPaymentVerified() throws Exception {
        BankWebhookEventDAO eventDAO = mock(BankWebhookEventDAO.class);
        PaymentTransactionDAO transactionDAO = mock(PaymentTransactionDAO.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        OrderLogDAO orderLogDAO = mock(OrderLogDAO.class);
        Connection conn = mock(Connection.class);

        PaymentTransaction pending = pendingTransaction();
        when(eventDAO.findByProviderTransactionId(conn, "BANK_TXN_987")).thenReturn(null);
        when(transactionDAO.findPendingByTransferReferenceInContentForUpdate(conn, "Thanh toan PETSHOP-U7-123456"))
                .thenReturn(pending);
        when(eventDAO.save(conn, BankWebhookEvent.Status.MATCHED, "BANK_TXN_987",
                new BigDecimal("258000"), "Thanh toan PETSHOP-U7-123456", "123456789", 55, "{}"))
                .thenReturn(10);
        when(transactionDAO.applyWebhookResult(eq(conn), eq(55), eq("BANK_TXN_987"),
                eq(new BigDecimal("258000")), eq("Thanh toan PETSHOP-U7-123456"), eq("{}"),
                eq("VERIFIED"), eq("VERIFIED"), any(), any())).thenReturn(true);
        when(orderDAO.updatePaymentStatus(conn, 456, true)).thenReturn(true);
        when(orderDAO.finalizeReservedStockForOrder(conn, 456)).thenReturn(true);
        when(orderLogDAO.insert(conn, 456, "WEBHOOK", null,
                "BANK_WEBHOOK_VERIFIED", "PENDING_VERIFICATION", "VERIFIED",
                "Webhook ngân hàng đã khớp thanh toán.")).thenReturn(true);

        BankWebhookReconciliationService service = new BankWebhookReconciliationService(
                eventDAO, transactionDAO, orderDAO, orderLogDAO
        );

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);
            BankWebhookReconciliationResult result = service.reconcile(
                    new BankWebhookPayload("BANK_TXN_987", new BigDecimal("258000"),
                            "Thanh toan PETSHOP-U7-123456", "123456789", "{}", null)
            );

            assertEquals(BankWebhookReconciliationResult.Status.VERIFIED, result.getStatus());
        }

        verify(orderDAO).updatePaymentStatus(conn, 456, true);
        verify(orderDAO).finalizeReservedStockForOrder(conn, 456);
        verify(conn).commit();
    }

    @Test
    void expiredPaymentReleasesReservedStock() throws Exception {
        BankWebhookEventDAO eventDAO = mock(BankWebhookEventDAO.class);
        PaymentTransactionDAO transactionDAO = mock(PaymentTransactionDAO.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        OrderLogDAO orderLogDAO = mock(OrderLogDAO.class);
        Connection conn = mock(Connection.class);

        PaymentTransaction pending = pendingTransaction();
        pending.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().minusMinutes(1)));
        when(eventDAO.findByProviderTransactionId(conn, "BANK_TXN_990")).thenReturn(null);
        when(transactionDAO.findPendingByTransferReferenceInContentForUpdate(conn, "PETSHOP-U7-123456"))
                .thenReturn(pending);
        when(eventDAO.save(conn, BankWebhookEvent.Status.EXPIRED, "BANK_TXN_990",
                new BigDecimal("258000"), "PETSHOP-U7-123456", "123456789", 55, "{}"))
                .thenReturn(13);
        when(transactionDAO.applyWebhookResult(eq(conn), eq(55), eq("BANK_TXN_990"),
                eq(new BigDecimal("258000")), eq("PETSHOP-U7-123456"), eq("{}"),
                eq("EXPIRED"), eq("EXPIRED"), any(), eq(null))).thenReturn(true);
        when(orderDAO.updatePaymentStatus(conn, 456, false)).thenReturn(true);
        when(orderDAO.releaseReservedStockForOrder(conn, 456)).thenReturn(true);
        when(orderLogDAO.insert(conn, 456, "WEBHOOK", null,
                "BANK_WEBHOOK_EXPIRED", "PENDING_VERIFICATION", "EXPIRED",
                "Webhook đến sau thời hạn giữ thanh toán.")).thenReturn(true);

        BankWebhookReconciliationService service = new BankWebhookReconciliationService(
                eventDAO, transactionDAO, orderDAO, orderLogDAO
        );

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);
            BankWebhookReconciliationResult result = service.reconcile(
                    new BankWebhookPayload("BANK_TXN_990", new BigDecimal("258000"),
                            "PETSHOP-U7-123456", "123456789", "{}", null)
            );

            assertEquals(BankWebhookReconciliationResult.Status.EXPIRED, result.getStatus());
        }

        verify(orderDAO).releaseReservedStockForOrder(conn, 456);
        verify(conn).commit();
    }

    @Test
    void duplicateProviderTransactionIdIsIgnored() throws Exception {
        BankWebhookEventDAO eventDAO = mock(BankWebhookEventDAO.class);
        PaymentTransactionDAO transactionDAO = mock(PaymentTransactionDAO.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        OrderLogDAO orderLogDAO = mock(OrderLogDAO.class);
        Connection conn = mock(Connection.class);

        BankWebhookEvent existing = new BankWebhookEvent();
        existing.setProviderTransactionId("BANK_TXN_987");
        existing.setStatus(BankWebhookEvent.Status.MATCHED.name());
        when(eventDAO.findByProviderTransactionId(conn, "BANK_TXN_987")).thenReturn(existing);

        BankWebhookReconciliationService service = new BankWebhookReconciliationService(
                eventDAO, transactionDAO, orderDAO, orderLogDAO
        );

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);
            BankWebhookReconciliationResult result = service.reconcile(
                    new BankWebhookPayload("BANK_TXN_987", new BigDecimal("258000"),
                            "Thanh toan PETSHOP-U7-123456", "123456789", "{}", null)
            );

            assertEquals(BankWebhookReconciliationResult.Status.DUPLICATE, result.getStatus());
        }

        verifyNoInteractions(transactionDAO, orderDAO);
        verify(conn).commit();
    }

    @Test
    void amountMismatchIsStoredForManualReviewAndDoesNotMarkPaid() throws Exception {
        BankWebhookEventDAO eventDAO = mock(BankWebhookEventDAO.class);
        PaymentTransactionDAO transactionDAO = mock(PaymentTransactionDAO.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        OrderLogDAO orderLogDAO = mock(OrderLogDAO.class);
        Connection conn = mock(Connection.class);

        PaymentTransaction pending = pendingTransaction();
        when(eventDAO.findByProviderTransactionId(conn, "BANK_TXN_988")).thenReturn(null);
        when(transactionDAO.findPendingByTransferReferenceInContentForUpdate(conn, "PETSHOP-U7-123456"))
                .thenReturn(pending);
        when(eventDAO.save(conn, BankWebhookEvent.Status.AMOUNT_MISMATCH, "BANK_TXN_988",
                new BigDecimal("250000"), "PETSHOP-U7-123456", "123456789", 55, "{}"))
                .thenReturn(11);
        when(transactionDAO.applyWebhookResult(eq(conn), eq(55), eq("BANK_TXN_988"),
                eq(new BigDecimal("250000")), eq("PETSHOP-U7-123456"), eq("{}"),
                eq("FAILED"), eq("MISMATCH"), any(), eq(null))).thenReturn(true);
        when(orderDAO.updatePaymentStatus(conn, 456, false)).thenReturn(true);
        when(orderDAO.releaseReservedStockForOrder(conn, 456)).thenReturn(true);
        when(orderLogDAO.insert(conn, 456, "WEBHOOK", null,
                "BANK_WEBHOOK_AMOUNT_MISMATCH", "PENDING_VERIFICATION", "MISMATCH",
                "Webhook thanh toán gửi số tiền không khớp.")).thenReturn(true);

        BankWebhookReconciliationService service = new BankWebhookReconciliationService(
                eventDAO, transactionDAO, orderDAO, orderLogDAO
        );

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);
            BankWebhookReconciliationResult result = service.reconcile(
                    new BankWebhookPayload("BANK_TXN_988", new BigDecimal("250000"),
                            "PETSHOP-U7-123456", "123456789", "{}", null)
            );

            assertEquals(BankWebhookReconciliationResult.Status.AMOUNT_MISMATCH, result.getStatus());
        }

        verify(orderDAO).updatePaymentStatus(conn, 456, false);
        verify(orderDAO).releaseReservedStockForOrder(conn, 456);
        verify(conn).commit();
    }

    @Test
    void unknownContentIsStoredAsUnmatched() throws Exception {
        BankWebhookEventDAO eventDAO = mock(BankWebhookEventDAO.class);
        PaymentTransactionDAO transactionDAO = mock(PaymentTransactionDAO.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        OrderLogDAO orderLogDAO = mock(OrderLogDAO.class);
        Connection conn = mock(Connection.class);

        when(eventDAO.findByProviderTransactionId(conn, "BANK_TXN_989")).thenReturn(null);
        when(transactionDAO.findPendingByTransferReferenceInContentForUpdate(conn, "thanh toan don hang"))
                .thenReturn(null);
        when(eventDAO.save(conn, BankWebhookEvent.Status.UNMATCHED, "BANK_TXN_989",
                new BigDecimal("258000"), "thanh toan don hang", "123456789", null, "{}"))
                .thenReturn(12);

        BankWebhookReconciliationService service = new BankWebhookReconciliationService(
                eventDAO, transactionDAO, orderDAO, orderLogDAO
        );

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);
            BankWebhookReconciliationResult result = service.reconcile(
                    new BankWebhookPayload("BANK_TXN_989", new BigDecimal("258000"),
                            "thanh toan don hang", "123456789", "{}", null)
            );

            assertEquals(BankWebhookReconciliationResult.Status.UNMATCHED, result.getStatus());
        }

        verifyNoInteractions(orderDAO);
        verify(conn).commit();
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
