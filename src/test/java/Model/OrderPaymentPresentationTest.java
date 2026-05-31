package Model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderPaymentPresentationTest {

    @Test
    @DisplayName("pending bank transfer orders should expose review-friendly labels")
    void pendingBankTransferOrdersShouldExposeReviewFriendlyLabels() {
        Order order = new Order();
        order.setPayment_method("bank_transfer");
        order.setPayment_status(false);
        order.setPaymentTransactionStatus("PENDING_VERIFICATION");
        order.setPaymentVerificationStatus("PENDING");

        assertEquals("Chờ đối soát chuyển khoản", order.getPaymentFlowLabel());
        assertEquals("Chờ đối soát", order.getPaymentVerificationLabel());
        assertEquals("payment-pending", order.getPaymentVerificationCssClass());
        assertTrue(order.isAwaitingPaymentReview());
        assertTrue(order.isBankTransferPayment());
    }

    @Test
    @DisplayName("verified bank transfer orders should expose paid labels")
    void verifiedBankTransferOrdersShouldExposePaidLabels() {
        Order order = new Order();
        order.setPayment_method("bank_transfer");
        order.setPayment_status(true);
        order.setPaymentTransactionStatus("VERIFIED");
        order.setPaymentVerificationStatus("VERIFIED");

        assertEquals("Đã xác nhận thanh toán", order.getPaymentFlowLabel());
        assertEquals("Đã xác nhận", order.getPaymentVerificationLabel());
        assertEquals("payment-verified", order.getPaymentVerificationCssClass());
        assertFalse(order.isAwaitingPaymentReview());
    }
}
