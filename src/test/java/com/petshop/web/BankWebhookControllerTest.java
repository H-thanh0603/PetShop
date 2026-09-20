package com.petshop.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import services.payment.BankWebhookPayload;

import java.lang.reflect.Method;
import java.math.BigDecimal;

class BankWebhookControllerTest {

    @Test
    void sepayAuthorizationApiKeyHeaderIsAccepted() throws Exception {
        System.setProperty("payment.bank.webhook-secret", "sepay-secret");
        try {
            BankWebhookController controller = new BankWebhookController();
            assertTrue(controller.isAuthorized(null, null, "Apikey sepay-secret"));
            assertTrue(controller.isAuthorized("sepay-secret", null, null));
            assertFalse(controller.isAuthorized(null, null, null));
            assertFalse(controller.isAuthorized("wrong", null, null));
        } finally {
            System.clearProperty("payment.bank.webhook-secret");
        }
    }

    @Test
    void blankConfiguredSecretRejectsAll() {
        System.clearProperty("payment.bank.webhook-secret");
        BankWebhookController controller = new BankWebhookController();
        assertFalse(controller.isAuthorized("anything", null, null));
    }

    @Test
    void sepayPayloadParsesTransferAmountContentAndReferenceCode() throws Exception {
        String rawPayload = "{"
                + "\"id\":12345,"
                + "\"gateway\":\"VPBank\","
                + "\"transactionDate\":\"2026-06-14 18:05:39\","
                + "\"accountNumber\":\"0000000000\","
                + "\"transferType\":\"in\","
                + "\"transferAmount\":258000,"
                + "\"content\":\"Thanh toan PETSHOP-U9-654321\","
                + "\"referenceCode\":\"SEPAY987\""
                + "}";

        Method parsePayload = BankWebhookController.class.getDeclaredMethod(
                "parsePayload",
                String.class
        );
        parsePayload.setAccessible(true);

        BankWebhookPayload payload = (BankWebhookPayload) parsePayload.invoke(
                new BankWebhookController(),
                rawPayload
        );

        assertEquals("SEPAY987", payload.getTransactionId());
        assertEquals(new BigDecimal("258000"), payload.getAmount());
        assertEquals("Thanh toan PETSHOP-U9-654321", payload.getContent());
        assertEquals("0000000000", payload.getBankAccount());
    }
}
