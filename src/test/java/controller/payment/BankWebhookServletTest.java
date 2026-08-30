package controller.payment;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import services.payment.BankWebhookPayload;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BankWebhookServletTest {

    @Test
    void sepayAuthorizationApiKeyHeaderIsAccepted() throws Exception {
        System.setProperty("payment.bank.webhook-secret", "sepay-secret");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Bank-Webhook-Secret")).thenReturn(null);
        when(request.getHeader("X-Secret-Key")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Apikey sepay-secret");

        Method isAuthorized = BankWebhookServlet.class.getDeclaredMethod(
                "isAuthorized",
                HttpServletRequest.class
        );
        isAuthorized.setAccessible(true);

        try {
            assertTrue((Boolean) isAuthorized.invoke(new BankWebhookServlet(), request));
        } finally {
            System.clearProperty("payment.bank.webhook-secret");
        }
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

        Method parsePayload = BankWebhookServlet.class.getDeclaredMethod(
                "parsePayload",
                String.class
        );
        parsePayload.setAccessible(true);

        BankWebhookPayload payload = (BankWebhookPayload) parsePayload.invoke(
                new BankWebhookServlet(),
                rawPayload
        );

        assertEquals("SEPAY987", payload.getTransactionId());
        assertEquals(new BigDecimal("258000"), payload.getAmount());
        assertEquals("Thanh toan PETSHOP-U9-654321", payload.getContent());
        assertEquals("0000000000", payload.getBankAccount());
    }
}
