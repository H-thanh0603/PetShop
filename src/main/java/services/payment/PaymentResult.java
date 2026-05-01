package services.payment;

/**
 * Result returned by a PaymentProvider after processing a payment.
 */
public class PaymentResult {
    private final boolean success;
    private final String paymentMethodDb;   // e.g. "COD", "MOMO", "BANK_TRANSFER"
    private final boolean paymentStatus;    // true = paid, false = unpaid at order time
    private final String message;
    private final String transactionStatus;

    private PaymentResult(boolean success, String paymentMethodDb, boolean paymentStatus, String message,
                          String transactionStatus) {
        this.success = success;
        this.paymentMethodDb = paymentMethodDb;
        this.paymentStatus = paymentStatus;
        this.message = message;
        this.transactionStatus = transactionStatus;
    }

    public static PaymentResult success(String paymentMethodDb, boolean paymentStatus) {
        return new PaymentResult(true, paymentMethodDb, paymentStatus, null, paymentStatus ? "CAPTURED" : "CREATED");
    }

    public static PaymentResult pendingVerification(String paymentMethodDb, String message) {
        return new PaymentResult(true, paymentMethodDb, false, message, "PENDING_VERIFICATION");
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, false, message, "FAILED");
    }

    public boolean isSuccess()           { return success; }
    public String  getPaymentMethodDb()  { return paymentMethodDb; }
    public boolean isPaymentStatus()     { return paymentStatus; }
    public String  getMessage()          { return message; }
    public String  getTransactionStatus() { return transactionStatus; }
    public boolean isPendingVerification() {
        return "PENDING_VERIFICATION".equalsIgnoreCase(transactionStatus);
    }
}
