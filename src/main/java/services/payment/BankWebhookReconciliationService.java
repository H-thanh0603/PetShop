package services.payment;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import com.petshop.repository.BankWebhookEventRepository;
import com.petshop.repository.OrderLogRepository;
import com.petshop.repository.OrderRepository;
import com.petshop.repository.PaymentTransactionRepository;
import com.petshop.model.BankWebhookEvent;
import com.petshop.model.PaymentTransaction;

import org.springframework.beans.factory.annotation.Autowired;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class BankWebhookReconciliationService {
    private final BankWebhookEventRepository eventDAO;
    private final PaymentTransactionRepository transactionDAO;
    private final OrderRepository orderDAO;
    private final OrderLogRepository orderLogDAO;

    @Autowired
    public BankWebhookReconciliationService(BankWebhookEventRepository eventDAO,
                                            PaymentTransactionRepository transactionDAO,
                                            OrderRepository orderDAO,
                                            OrderLogRepository orderLogDAO) {
        this.eventDAO = eventDAO;
        this.transactionDAO = transactionDAO;
        this.orderDAO = orderDAO;
        this.orderLogDAO = orderLogDAO;
    }

    @Transactional
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

        try {
                BankWebhookEvent existing = eventDAO.findByProviderTransactionId(
                        payload.getTransactionId()
                );
                if (existing != null) {
                    return BankWebhookReconciliationResult.of(
                            BankWebhookReconciliationResult.Status.DUPLICATE,
                            "Webhook đã được xử lý trước đó.",
                            null,
                            existing.getPaymentTransactionId()
                    );
                }

                PaymentTransaction transaction = transactionDAO.findPendingByTransferReferenceInContentForUpdate(
                        payload.getContent()
                );

                if (transaction == null) {
                    eventDAO.save(
                            BankWebhookEvent.Status.UNMATCHED,
                            payload.getTransactionId(),
                            payload.getAmount(),
                            payload.getContent(),
                            payload.getBankAccount(),
                            null,
                            payload.getRawPayload()
                    );
                    return BankWebhookReconciliationResult.of(
                            BankWebhookReconciliationResult.Status.UNMATCHED,
                            "Không tìm thấy đơn chờ thanh toán khớp nội dung chuyển khoản.",
                            null,
                            null
                    );
                }

                if (isExpired(transaction)) {
                    eventDAO.save(
                            BankWebhookEvent.Status.EXPIRED,
                            payload.getTransactionId(),
                            payload.getAmount(),
                            payload.getContent(),
                            payload.getBankAccount(),
                            transaction.getId(),
                            payload.getRawPayload()
                    );
                    if (!transactionDAO.applyWebhookResultTx(
                            transaction.getId(),
                            payload.getTransactionId(),
                            payload.getAmount(),
                            payload.getContent(),
                            payload.getRawPayload(),
                            "EXPIRED",
                            "EXPIRED",
                            "Giao dịch đến sau thời hạn giữ thanh toán.",
                            null
                    )) {
                        rollbackOnly();
                        throw new IllegalStateException("applyWebhookResult failed");
                    }
                    if (!orderDAO.updatePaymentStatus( transaction.getOrderId(), false)
                            || !orderDAO.releaseReservedStockForOrder( transaction.getOrderId())
                            || !orderLogDAO.insert( transaction.getOrderId(), "WEBHOOK", null,
                            "BANK_WEBHOOK_EXPIRED", transaction.getStatus(), "EXPIRED",
                            "Webhook đến sau thời hạn giữ thanh toán.")) {
                        rollbackOnly();
                        return BankWebhookReconciliationResult.of(
                                BankWebhookReconciliationResult.Status.EXPIRED,
                                "KhÃ´ng thá»ƒ tráº£ láº¡i tá»“n kho cho giao dá»‹ch háº¿t háº¡n.",
                                transaction.getOrderId(),
                                transaction.getId()
                        );
                    }
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
                            BankWebhookEvent.Status.AMOUNT_MISMATCH,
                            payload.getTransactionId(),
                            payload.getAmount(),
                            payload.getContent(),
                            payload.getBankAccount(),
                            transaction.getId(),
                            payload.getRawPayload()
                    );
                    if (!transactionDAO.applyWebhookResultTx(
                            transaction.getId(),
                            payload.getTransactionId(),
                            payload.getAmount(),
                            payload.getContent(),
                            payload.getRawPayload(),
                            "FAILED",
                            "MISMATCH",
                            "Số tiền chuyển khoản không khớp đơn hàng.",
                            null
                    )) {
                        rollbackOnly();
                        throw new IllegalStateException("applyWebhookResult failed");
                    }
                    if (!orderDAO.updatePaymentStatus( transaction.getOrderId(), false)
                            || !orderDAO.releaseReservedStockForOrder( transaction.getOrderId())
                            || !orderLogDAO.insert( transaction.getOrderId(), "WEBHOOK", null,
                            "BANK_WEBHOOK_AMOUNT_MISMATCH", transaction.getStatus(), "MISMATCH",
                            "Webhook thanh toán gửi số tiền không khớp.")) {
                        rollbackOnly();
                        return BankWebhookReconciliationResult.of(
                                BankWebhookReconciliationResult.Status.AMOUNT_MISMATCH,
                                "KhÃ´ng thá»ƒ tráº£ láº¡i tá»“n kho cho giao dá»‹ch chÆ°a khá»›p.",
                                transaction.getOrderId(),
                                transaction.getId()
                        );
                    }
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
                        BankWebhookEvent.Status.MATCHED,
                        payload.getTransactionId(),
                        payload.getAmount(),
                        payload.getContent(),
                        payload.getBankAccount(),
                        transaction.getId(),
                        payload.getRawPayload()
                );
                if (!transactionDAO.applyWebhookResultTx(
                        transaction.getId(),
                        payload.getTransactionId(),
                        payload.getAmount(),
                        payload.getContent(),
                        payload.getRawPayload(),
                        "VERIFIED",
                        "VERIFIED",
                        "Webhook ngân hàng đã khớp mã thanh toán và số tiền.",
                        verifiedAt
                )) {
                    rollbackOnly();
                    throw new IllegalStateException("applyWebhookResult failed");
                }
                if (!orderDAO.updatePaymentStatus( transaction.getOrderId(), true)
                        || !orderDAO.markAwaitingPaymentOrderPaid( transaction.getOrderId())
                        || !orderDAO.finalizeReservedStockForOrder( transaction.getOrderId())
                        || !orderLogDAO.insert( transaction.getOrderId(), "WEBHOOK", null,
                        "BANK_WEBHOOK_VERIFIED", transaction.getStatus(), "VERIFIED",
                        "Webhook ngân hàng đã khớp thanh toán.")) {
                    rollbackOnly();
                    return BankWebhookReconciliationResult.of(
                            BankWebhookReconciliationResult.Status.VERIFIED,
                            "KhÃ´ng thá»ƒ chá»‘t tá»“n kho cho giao dá»‹ch Ä‘Ã£ xÃ¡c nháº­n.",
                            transaction.getOrderId(),
                            transaction.getId()
                    );
                }
                return BankWebhookReconciliationResult.of(
                        BankWebhookReconciliationResult.Status.VERIFIED,
                        "Đã xác nhận thanh toán tự động.",
                        transaction.getOrderId(),
                        transaction.getId()
                );
            } catch (Exception e) {
                rollbackOnly();
                throw e;
            }
    }

    private static void rollbackOnly() {
        try {
            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
        } catch (Exception ignored) {
            // No ambient transaction (should not happen here).
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
