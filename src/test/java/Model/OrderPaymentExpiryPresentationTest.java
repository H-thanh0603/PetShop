package Model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderPaymentExpiryPresentationTest {

    @Test
    @DisplayName("expired bank transfer orders should expose overdue payment labels")
    void expiredBankTransferOrdersShouldExposeOverduePaymentLabels() {
        Order order = new Order();
        order.setPayment_method("bank_transfer");
        order.setPayment_status(false);
        order.setPaymentTransactionStatus("EXPIRED");
        order.setPaymentVerificationStatus("EXPIRED");

        assertEquals("Quá hạn thanh toán", order.getPaymentFlowLabel());
        assertEquals("Quá hạn", order.getPaymentVerificationLabel());
        assertEquals("payment-expired", order.getPaymentVerificationCssClass());
    }
}
