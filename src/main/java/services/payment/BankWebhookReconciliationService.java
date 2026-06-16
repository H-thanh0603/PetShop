package services.payment;

import Context.DBContext;
import DAO.BankWebhookEventDAO;
import DAO.OrderLogDAO;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import Model.BankWebhookEvent;
import Model.PaymentTransaction;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class BankWebhookReconciliationService {
    private final BankWebhookEventDAO eventDAO;
    private final PaymentTransactionDAO transactionDAO;
    private final OrderDAO orderDAO;
    private final OrderLogDAO orderLogDAO;

    public BankWebhookReconciliationService() {
        this(new BankWebhookEventDAO(), new PaymentTransactionDAO(), new OrderDAO(), new OrderLogDAO());
    }

    public BankWebhookReconciliationService(BankWebhookEventDAO eventDAO,
                                            PaymentTransactionDAO transactionDAO,
                                            OrderDAO orderDAO) {
        this(eventDAO, transactionDAO, orderDAO, new OrderLogDAO());
    }

    public BankWebhookReconciliationService(BankWebhookEventDAO eventDAO,
                                            PaymentTransactionDAO transactionDAO,
                                            OrderDAO orderDAO,
                                            OrderLogDAO orderLogDAO) {
        this.eventDAO = eventDAO;
        this.transactionDAO = transactionDAO;
        this.orderDAO = orderDAO;
        this.orderLogDAO = orderLogDAO;
    }

    public BankWebhookReconciliationResult reconcile(BankWebhookPayload payload) throws Exception {
        if (payload == null || isBlank(payload.getTransactionId())
                || payload.getAmount() == null || isBlank(payload.getContent())) {
            return BankWebhookReconciliationResult.of(
                    BankWebhookReconciliationResult.Status.INVALID,
                    "Webhook thiếu transaction_id, amount hoặc content.",
                    null,
                    null
            );
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BankWebhookEvent existing = eventDAO.findByProviderTransactionId(
                        conn,
                        payload.getTransactionId()
                );
                if (existing != null) {
                    conn.commit();
                    return BankWebhookReconciliationResult.of(
                            BankWebhookReconciliationResult.Status.DUPLICATE,
                            "Webhook đã được xử lý trước đó.",
                            null,
                            existing.getPaymentTransactionId()
                    );
                }

                PaymentTransaction transaction = transactionDAO.findPendingByTransferReferenceInContentForUpdate(
                        conn,
                        payload.getContent()
                );

                if (transaction == null) {
                    eventDAO.save(
                            conn,
                            BankWebhookEvent.Status.UNMATCHED,
                            payload.getTransactionId(),
                            payload.getAmount(),
                            payload.getContent(),
                            payload.getBankAccount(),
                            null,
                            payload.getRawPayload()
                    );
                    conn.commit();
                    return BankWebhookReconciliationResult.of(
                            BankWebhookReconciliationResult.Status.UNMATCHED,
                            "Không tìm thấy đơn chờ thanh toán khớp nội dung chuyển khoản.",
                            null,
                            null
                    );
                }

                if (isExpired(transaction)) {
                    eventDAO.save(
                            conn,
                            BankWebhookEvent.Status.EXPIRED,
                            payload.getTransactionId(),
                            payload.getAmount(),
                            payload.getContent(),
                            payload.getBankAccount(),
                            transaction.getId(),
                            payload.getRawPayload()
                    );
                    transactionDAO.applyWebhookResult(
                            conn,
                            transaction.getId(),
                            payload.getTransactionId(),
                            payload.getAmount(),
                            payload.getContent(),
                            payload.getRawPayload(),
                            "EXPIRED",
                            "EXPIRED",
                            "Giao dịch đến sau thời hạn giữ thanh toán.",
                            null
                    );
                    if (!orderDAO.updatePaymentStatus(conn, transaction.getOrderId(), false)
                            || !orderDAO.releaseReservedStockForOrder(conn, transaction.getOrderId())
                            || !orderLogDAO.insert(conn, transaction.getOrderId(), "WEBHOOK", null,
                            "BANK_WEBHOOK_EXPIRED", transaction.getStatus(), "EXPIRED",
                            "Webhook đến sau thời hạn giữ thanh toán.")) {
                        conn.rollback();
                        return BankWebhookReconciliationResult.of(
                                BankWebhookReconciliationResult.Status.EXPIRED,
                                "KhÃ´ng thá»ƒ tráº£ láº¡i tá»“n kho cho giao dá»‹ch háº¿t háº¡n.",
                                transaction.getOrderId(),
                                transaction.getId()
                        );
                    }
                    conn.commit();
                    return BankWebhookReconciliationResult.of(
                            BankWebhookReconciliationResult.Status.EXPIRED,
                            "Giao dịch đến sau thời hạn giữ thanh toán.",
                            transaction.getOrderId(),
                            transaction.getId()
                    );
                }

                boolean amountMatches = payload.getAmount().compareTo(transaction.getAmount()) == 0;
                if (!amountMatches) {
                    eventDAO.save(
                            conn,
                            BankWebhookEvent.Status.AMOUNT_MISMATCH,
                            payload.getTransactionId(),
                            payload.getAmount(),
                            payload.getContent(),
                            payload.getBankAccount(),
                            transaction.getId(),
                            payload.getRawPayload()
                    );
                    transactionDAO.applyWebhookResult(
                            conn,
                            transaction.getId(),
                            payload.getTransactionId(),
                            payload.getAmount(),
                            payload.getContent(),
                            payload.getRawPayload(),
                            "FAILED",
                            "MISMATCH",
                            "Số tiền chuyển khoản không khớp đơn hàng.",
                            null
                    );
                    if (!orderDAO.updatePaymentStatus(conn, transaction.getOrderId(), false)
                            || !orderDAO.releaseReservedStockForOrder(conn, transaction.getOrderId())
                            || !orderLogDAO.insert(conn, transaction.getOrderId(), "WEBHOOK", null,
                            "BANK_WEBHOOK_AMOUNT_MISMATCH", transaction.getStatus(), "MISMATCH",
                            "Webhook thanh toán gửi số tiền không khớp.")) {
                        conn.rollback();
                        return BankWebhookReconciliationResult.of(
                                BankWebhookReconciliationResult.Status.AMOUNT_MISMATCH,
                                "KhÃ´ng thá»ƒ tráº£ láº¡i tá»“n kho cho giao dá»‹ch chÆ°a khá»›p.",
                                transaction.getOrderId(),
                                transaction.getId()
                        );
                    }
                    conn.commit();
                    return BankWebhookReconciliationResult.of(
                            BankWebhookReconciliationResult.Status.AMOUNT_MISMATCH,
                            "Số tiền chuyển khoản không khớp đơn hàng.",
                            transaction.getOrderId(),
                            transaction.getId()
                    );
                }

                Timestamp verifiedAt = payload.getPaidAt() == null
                        ? Timestamp.valueOf(LocalDateTime.now())
                        : Timestamp.valueOf(payload.getPaidAt());
                eventDAO.save(
                        conn,
                        BankWebhookEvent.Status.MATCHED,
                        payload.getTransactionId(),
                        payload.getAmount(),
                        payload.getContent(),
                        payload.getBankAccount(),
                        transaction.getId(),
                        payload.getRawPayload()
                );
                transactionDAO.applyWebhookResult(
                        conn,
                        transaction.getId(),
                        payload.getTransactionId(),
                        payload.getAmount(),
                        payload.getContent(),
                        payload.getRawPayload(),
                        "VERIFIED",
                        "VERIFIED",
                        "Webhook ngân hàng đã khớp mã thanh toán và số tiền.",
                        verifiedAt
                );
                if (!orderDAO.updatePaymentStatus(conn, transaction.getOrderId(), true)
                        || !orderDAO.markAwaitingPaymentOrderPaid(conn, transaction.getOrderId())
                        || !orderDAO.finalizeReservedStockForOrder(conn, transaction.getOrderId())
                        || !orderLogDAO.insert(conn, transaction.getOrderId(), "WEBHOOK", null,
                        "BANK_WEBHOOK_VERIFIED", transaction.getStatus(), "VERIFIED",
                        "Webhook ngân hàng đã khớp thanh toán.")) {
                    conn.rollback();
                    return BankWebhookReconciliationResult.of(
                            BankWebhookReconciliationResult.Status.VERIFIED,
                            "KhÃ´ng thá»ƒ chá»‘t tá»“n kho cho giao dá»‹ch Ä‘Ã£ xÃ¡c nháº­n.",
                            transaction.getOrderId(),
                            transaction.getId()
                    );
                }
                conn.commit();
                return BankWebhookReconciliationResult.of(
                        BankWebhookReconciliationResult.Status.VERIFIED,
                        "Đã xác nhận thanh toán tự động.",
                        transaction.getOrderId(),
                        transaction.getId()
                );
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private boolean isExpired(PaymentTransaction transaction) {
        return transaction.getExpiresAt() != null
                && transaction.getExpiresAt().before(Timestamp.valueOf(LocalDateTime.now()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
