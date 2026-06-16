package DAO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentTransactionDAOTest {

    @Test
    void transferReferenceNormalizationIgnoresSeparatorsAndCase() {
        assertEquals(
                "PETSHOPU3622107",
                PaymentTransactionDAO.normalizeTransferReferenceToken("PETSHOP-U3-622107")
        );
        assertEquals(
                "PETSHOPU3622107",
                PaymentTransactionDAO.normalizeTransferReferenceToken("petshopu3622107")
        );
        assertEquals(
                "THANHTOANPETSHOPU3622107",
                PaymentTransactionDAO.normalizeTransferReferenceToken("Thanh toan PETSHOP U3 622107")
        );
    }
}
