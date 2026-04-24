package services.payment;

/**
 * Cash-on-delivery: always succeeds, payment status = false (unpaid at order time).
 */
public class CODPaymentProvider implements PaymentProvider {
    @Override
    public PaymentResult process(double amount) {
        return PaymentResult.success("COD", false);
    }
}
