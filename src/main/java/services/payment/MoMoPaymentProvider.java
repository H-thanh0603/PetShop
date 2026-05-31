package services.payment;

/**
 * MoMo payment provider.
 * Currently uses a mock API call; replace with real MoMo SDK integration.
 */
public class MoMoPaymentProvider implements PaymentProvider {
    @Override
    public PaymentResult process(double amount) {
        boolean apiSuccess = callMomoApi(amount);
        if (apiSuccess) {
            return PaymentResult.success("MOMO", true);
        }
        return PaymentResult.failure("Thanh toán MoMo thất bại.");
    }

    private boolean callMomoApi(double amount) {
        // TODO: integrate real MoMo payment API
        System.out.println("[MoMo] Mock payment: " + amount);
        return true;
    }
}
