package services.ai;

import java.util.function.Consumer;

/**
 * Common interface every LLM provider implements.
 * Application/agent code depends only on this, never on a provider SDK.
 */
public interface AiProvider {
    /** Stable provider key, e.g. "anthropic", "openrouter", "deepseek". */
    String getName();

    ProviderCapabilities getCapabilities();

    /** Effective model id (may include prefix like "anthropic/claude-..."). */
    String getModel();

    ChatResponse complete(ChatRequest request) throws AiException;

    /**
     * Streams text deltas. Default implementation falls back to a single
     * non-streaming call so providers without SSE still satisfy the contract.
     */
    default void stream(ChatRequest request, Consumer<String> onDelta,
                        Consumer<ChatResponse> onComplete) throws AiException {
        if (!getCapabilities().supportsStreaming()) {
            ChatResponse full = complete(request);
            if (full.getContent() != null && !full.getContent().isEmpty()) {
                onDelta.accept(full.getContent());
            }
            onComplete.accept(full);
            return;
        }
        ChatResponse full = complete(request);
        if (full.getContent() != null && !full.getContent().isEmpty()) {
            onDelta.accept(full.getContent());
        }
        onComplete.accept(full);
    }
}
