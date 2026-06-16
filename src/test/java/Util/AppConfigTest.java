package Util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigTest {

    @Test
    void envKeyNormalizationReplacesDotsAndHyphensWithUnderscores() throws Exception {
        Method normalizeEnvKey = AppConfig.class.getDeclaredMethod("normalizeEnvKey", String.class);
        normalizeEnvKey.setAccessible(true);

        assertEquals(
                "PAYMENT_BANK_WEBHOOK_SECRET",
                normalizeEnvKey.invoke(null, "payment.bank.webhook-secret")
        );
    }
}
