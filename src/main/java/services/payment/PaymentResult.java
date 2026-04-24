package services.payment;

/**
 * Result returned by a PaymentProvider after processing a payment.
 */
public class PaymentResult {
    private final boolean success;
    private final String paymentMethodDb;   // e.g. "COD", "MOMO", "BANK_TRANSFER"
    private final boolean paymentStatus;    // true = paid, false = unpaid at order time
    private final String message;

    private PaymentResult(boolean success, String paymentMethodDb, boolean paymentStatus, String message) {
        this.success = success;
        this.paymentMethodDb = paymentMethodDb;
        this.paymentStatus = paymentStatus;
        this.message = message;
    }

    public static PaymentResult success(String paymentMethodDb, boolean paymentStatus) {
        return new PaymentResult(true, paymentMethodDb, paymentStatus, null);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, false, message);
    }

    public boolean isSuccess()           { return success; }
    public String  getPaymentMethodDb()  { return paymentMethodDb; }
    public boolean isPaymentStatus()     { return paymentStatus; }
    public String  getMessage()          { return message; }
}
