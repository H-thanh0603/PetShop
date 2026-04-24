package services.payment;

/**
 * Bank transfer payment provider.
 * Currently uses a mock API call; replace with real bank API integration.
 */
public class BankTransferPaymentProvider implements PaymentProvider {
    @Override
    public PaymentResult process(double amount) {
        boolean apiSuccess = callBankApi(amount);
        if (apiSuccess) {
            return PaymentResult.success("BANK_TRANSFER", true);
        }
        return PaymentResult.failure("Thanh toán ngân hàng thất bại.");
    }

    private boolean callBankApi(double amount) {
        // TODO: integrate real bank transfer API
        System.out.println("[BankTransfer] Mock payment: " + amount);
        return true;
    }
}
