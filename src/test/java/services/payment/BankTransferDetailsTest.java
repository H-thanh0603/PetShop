package services.payment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankTransferDetailsTest {

    @Test
    void reservedTransferReferenceUsesSePayPaymentCodeFormat() {
        BankTransferDetails details = BankTransferDetails.fromConfig();

        String reference = details.buildReservedTransferReference(3);

        assertTrue(reference.startsWith("PETSH3"));
        assertFalse(reference.contains("-"));
        assertTrue(reference.matches("PETSH\\d{3,10}"));
    }

    @Test
    void orderTransferReferenceUsesSePayPaymentCodeFormat() {
        BankTransferDetails details = BankTransferDetails.fromConfig();

        String reference = details.buildTransferReference(15);

        assertTrue(reference.startsWith("PETSH15"));
        assertFalse(reference.contains("-"));
        assertTrue(reference.matches("PETSH\\d{3,10}"));
    }
}
