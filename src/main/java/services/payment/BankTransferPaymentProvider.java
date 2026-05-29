package services.payment;

/**
 * Bank transfer payment provider.
 * Currently uses a mock API call; replace with real bank API integration.
 */
public class BankTransferPaymentProvider implements PaymentProvider {
    @Override
    public PaymentResult process(double amount) {
        return PaymentResult.pendingVerification(
                "BANK_TRANSFER",
                "Đơn hàng đã được tạo. Vui lòng hoàn tất chuyển khoản để hệ thống xác nhận thanh toán."
        );
    }
}
