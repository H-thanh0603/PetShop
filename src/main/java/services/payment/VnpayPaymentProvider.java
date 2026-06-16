package services.payment;

/**
 * VNPAY starts as an unpaid redirect flow. The return endpoint verifies the
 * signed provider response before marking the order as paid.
 */
public class VnpayPaymentProvider implements PaymentProvider {
    @Override
    public PaymentResult process(double amount) {
        return PaymentResult.success("VNPAY", false);
    }
}
