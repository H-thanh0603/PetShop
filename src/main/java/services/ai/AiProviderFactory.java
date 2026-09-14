package services.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the primary provider plus the ordered fallback chain from
 * {@link AiConfig}. Agent code calls {@link #completeWithFallback} and never
 * branches on provider.
 */
public final class AiProviderFactory {
    private static final Logger log = LoggerFactory.getLogger(AiProviderFactory.class);

    private AiProviderFactory() {}

    public static AiProvider create(String provider, String model) throws AiException {
        String key = AiConfig.apiKeyFor(provider);
        if (key == null || key.isBlank()) {
            throw new AiException(AiException.Kind.INVALID_KEY, provider,
                    "Missing API key for provider '" + provider + "'. Set the corresponding *_API_KEY env var.");
        }
        int timeout = AiConfig.timeoutSeconds();
        if ("anthropic".equalsIgnoreCase(provider)
                && AiConfig.baseUrlFor(provider).contains("api.anthropic.com")) {
            return new AnthropicProvider(AiConfig.baseUrlFor(provider), key, model, timeout);
        }
        String baseUrl = AiConfig.baseUrlFor(provider);
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new AiException(AiException.Kind.PROVIDER_UNAVAILABLE, provider,
                    "No base URL configured for provider '" + provider + "'");
        }
        return new OpenAiCompatibleProvider(provider.toLowerCase(), baseUrl, key, model, timeout);
    }

    public static AiProvider primary() throws AiException {
        return create(AiConfig.provider(), AiConfig.model());
    }

    public record FallbackResult(ChatResponse response, String usedProvider, String usedModel,
                                 List<String> attempted) {}

    /**
     * Runs the request against the primary, then each configured fallback.
     * No infinite loops: each candidate tried at most once, in order.
     */
    public static FallbackResult completeWithFallback(ChatRequest request) throws AiException {
        List<AiConfig.ProviderRef> chain = new ArrayList<>();
        chain.add(new AiConfig.ProviderRef(AiConfig.provider(), AiConfig.model()));
        chain.addAll(AiConfig.fallbacks());

        List<String> attempted = new ArrayList<>();
        AiException lastError = null;
        for (AiConfig.ProviderRef ref : chain) {
            attempted.add(ref.provider() + ":" + ref.model());
            AiProvider provider;
            try {
                provider = create(ref.provider(), ref.model());
            } catch (AiException e) {
                log.warn("AI provider init failed: {}", AiConfig.describeForLog(ref.provider(), ref.model()));
                lastError = e;
                continue;
            }
            try {
                ChatResponse response = provider.complete(request);
                if (!chain.get(0).provider().equals(ref.provider())) {
                    log.warn("AI fallback engaged: primary={} nowServing={} model={} requestId={}",
                            chain.get(0).provider(), ref.provider(), ref.model(), response.getRequestId());
                }
                return new FallbackResult(response, ref.provider(), ref.model(), attempted);
            } catch (AiException e) {
                log.warn("AI provider '{}' failed ({}): {}", ref.provider(), e.getKind(), e.getMessage());
                lastError = e;
                if (e.getKind() == AiException.Kind.INVALID_KEY
                        || e.getKind() == AiException.Kind.MODEL_NOT_FOUND
                        || e.getKind() == AiException.Kind.BAD_RESPONSE
                        || e.getKind() == AiException.Kind.UNSUPPORTED) {
                    // Non-retryable on this candidate; move to next fallback.
                    continue;
                }
            }
        }
        throw lastError != null ? lastError : new AiException(
                AiException.Kind.PROVIDER_UNAVAILABLE, AiConfig.provider(), "No AI provider configured");
    }
}
