package services.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankTransferPaymentProviderTest {

    @Test
    @DisplayName("bank transfer provider should create a pending-verification result instead of paid success")
    void bankTransferProviderShouldReturnPendingVerification() {
        BankTransferPaymentProvider provider = new BankTransferPaymentProvider();

        PaymentResult result = provider.process(250000d);

        assertTrue(result.isSuccess());
        assertEquals("BANK_TRANSFER", result.getPaymentMethodDb());
        assertFalse(result.isPaymentStatus());
        assertTrue(result.isPendingVerification());
        assertEquals("PENDING_VERIFICATION", result.getTransactionStatus());
    }
}
