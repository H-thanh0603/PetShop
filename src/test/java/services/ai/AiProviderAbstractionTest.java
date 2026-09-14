package services.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline tests for the provider abstraction: no network, no DB.
 * Proves agent/tool logic does not depend on any specific provider.
 */
public class AiProviderAbstractionTest {

    @Test
    public void openAiPrefixStrippedForNativeEndpoints() {
        assertEquals("claude-sonnet-4-5-20250929",
                OpenAiCompatibleProvider.stripPrefix("anthropic/claude-sonnet-4-5-20250929"));
        assertEquals("deepseek-chat", OpenAiCompatibleProvider.stripPrefix("deepseek-chat"));
        assertEquals("gpt-4o-mini", OpenAiCompatibleProvider.stripPrefix("openai/gpt-4o-mini"));
    }

    @Test
    public void httpFailuresClassifiedWithoutLeakingKeys() {
        AiException e401 = AiException.fromHttp("openrouter", 401, "invalid api key sk-xyz");
        assertEquals(AiException.Kind.INVALID_KEY, e401.getKind());
        assertFalse(e401.isRetryable());

        AiException e429 = AiException.fromHttp("deepseek", 429, "rate limit");
        assertEquals(AiException.Kind.RATE_LIMIT, e429.getKind());
        assertTrue(e429.isRetryable());

        AiException e404 = AiException.fromHttp("openai", 404, "model not found");
        assertEquals(AiException.Kind.MODEL_NOT_FOUND, e404.getKind());

        AiException e503 = AiException.fromHttp("anthropic", 503, "overloaded");
        assertEquals(AiException.Kind.PROVIDER_UNAVAILABLE, e503.getKind());
        assertTrue(e503.isRetryable());
    }

    @Test
    public void missingKeyFailsFastWithClearMessage() {
        // Unknown provider name => no key configured in this environment.
        AiException ex = assertThrows(AiException.class,
                () -> AiProviderFactory.create("definitely-not-a-provider", "some-model"));
        assertEquals(AiException.Kind.INVALID_KEY, ex.getKind());
        assertFalse(ex.getMessage().contains("sk-"),
                "Error messages must never echo credentials");
    }

    @Test
    public void fallbackListParsedFromConfig() {
        System.setProperty("AI_FALLBACKS", "deepseek:deepseek-chat, openai:gpt-4o-mini");
        try {
            var fallbacks = AiConfig.fallbacks();
            assertEquals(2, fallbacks.size());
            assertEquals("deepseek", fallbacks.get(0).provider());
            assertEquals("deepseek-chat", fallbacks.get(0).model());
            assertEquals("openai", fallbacks.get(1).provider());
        } finally {
            System.clearProperty("AI_FALLBACKS");
        }
    }

    @Test
    public void providersExposeCapabilityFlags() {
        var caps = new OpenAiCompatibleProvider("openrouter", "https://openrouter.ai/api/v1",
                "dummy", "anthropic/claude-sonnet-4-5", 10).getCapabilities();
        assertTrue(caps.supportsChat());
        assertTrue(caps.supportsToolCalling());

        var anthropic = new AnthropicProvider(null, "dummy", "claude-haiku-4-5-20251001", 10);
        assertEquals("anthropic", anthropic.getName());
        assertTrue(anthropic.getCapabilities().supportsStreaming());
    }

    @Test
    public void guestOrderGuardIsProviderIndependent() {
        assertTrue(CommerceAgent.looksLikeOrderInquiry("Đơn hàng của tôi đang ở đâu?"));
        assertTrue(CommerceAgent.looksLikeOrderInquiry("check my order status"));
        assertFalse(CommerceAgent.looksLikeOrderInquiry("Mèo con nên ăn pate gì?"));
    }

    @Test
    public void commerceToolsRejectUnknownToolsAndProtectGuestOrders() {
        PetShopCommerceBackend backend = new PetShopCommerceBackend();
        CommerceTools tools = new CommerceTools(backend,
                new PetShopCommerceBackend.SessionContext(null, true));

        String unknown = tools.execute("deleteAllOrders", "{}");
        assertTrue(unknown.contains("error"));

        String malformed = tools.execute("searchProducts", "not-json{{{");
        assertTrue(malformed.contains("error"));

        // Guest must never reach order data, regardless of provider.
        String guestOrder = tools.execute("getOrderStatus", "{\"orderId\": 1}");
        assertTrue(guestOrder.contains("sign in"));

        // Blank query short-circuits before any DB access.
        String blank = tools.execute("searchProducts", "{\"query\": \"\"}");
        assertEquals("[]", blank);

        // Tool declarations are valid OpenAI-shaped schemas for adapters to convert.
        for (ToolDefinition def : tools.definitions()) {
            assertNotNull(def.getName());
            assertTrue(def.getParametersSchemaJson().contains("\"type\":\"object\""));
        }
    }

    @Test
    public void sanitizerStripsControlCharsAndCapsLength() {
        String dirty = "name\u0000with\u0007control";
        assertFalse(CommerceTools.sanitize(dirty).contains("\u0000"));
        assertEquals("", CommerceTools.sanitize(null));
        assertTrue(CommerceTools.sanitize("x".repeat(5000)).length() <= 2000);
    }
}
