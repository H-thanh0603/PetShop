package services.payment;

public class BankWebhookReconciliationResult {
    public enum Status {
        VERIFIED,
        AMOUNT_MISMATCH,
        UNMATCHED,
        DUPLICATE,
        EXPIRED,
        INVALID
    }

    private final Status status;
    private final String message;
    private final Integer orderId;
    private final Integer paymentTransactionId;

    public BankWebhookReconciliationResult(Status status, String message,
                                           Integer orderId, Integer paymentTransactionId) {
        this.status = status;
        this.message = message;
        this.orderId = orderId;
        this.paymentTransactionId = paymentTransactionId;
    }

    public static BankWebhookReconciliationResult of(Status status, String message,
                                                     Integer orderId, Integer paymentTransactionId) {
        return new BankWebhookReconciliationResult(status, message, orderId, paymentTransactionId);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public Integer getPaymentTransactionId() {
        return paymentTransactionId;
    }
}
