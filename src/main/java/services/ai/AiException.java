package services.ai;

/** Typed failure for provider calls; drives fallback + user-facing messages. */
public class AiException extends Exception {
    public enum Kind {
        INVALID_KEY,
        RATE_LIMIT,
        TIMEOUT,
        MODEL_NOT_FOUND,
        PROVIDER_UNAVAILABLE,
        UNSUPPORTED,
        BAD_RESPONSE,
        TOOL_ERROR
    }

    private final Kind kind;
    private final String provider;
    private final boolean retryable;

    public AiException(Kind kind, String provider, String message) {
        this(kind, provider, message, null);
    }

    public AiException(Kind kind, String provider, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.provider = provider;
        this.retryable = kind == Kind.RATE_LIMIT || kind == Kind.TIMEOUT
                || kind == Kind.PROVIDER_UNAVAILABLE;
    }

    public Kind getKind() { return kind; }
    public String getProvider() { return provider; }
    public boolean isRetryable() { return retryable; }

    /** Never include keys/tokens: classify by HTTP status + message patterns. */
    public static AiException fromHttp(String provider, int status, String bodySnippet) {
        String snippet = bodySnippet == null ? "" :
                bodySnippet.substring(0, Math.min(bodySnippet.length(), 300));
        return switch (status) {
            case 401, 403 -> new AiException(Kind.INVALID_KEY, provider,
                    "Provider '" + provider + "' rejected credentials (HTTP " + status + "): " + snippet);
            case 404 -> new AiException(Kind.MODEL_NOT_FOUND, provider,
                    "Model not available on '" + provider + "' (HTTP 404): " + snippet);
            case 429 -> new AiException(Kind.RATE_LIMIT, provider,
                    "Rate limited by '" + provider + "' (HTTP 429): " + snippet);
            case 400 -> new AiException(Kind.BAD_RESPONSE, provider,
                    "Bad request to '" + provider + "' (HTTP 400): " + snippet);
            case 500, 502, 503, 504 -> new AiException(Kind.PROVIDER_UNAVAILABLE, provider,
                    "Provider '" + provider + "' unavailable (HTTP " + status + "): " + snippet);
            default -> new AiException(Kind.PROVIDER_UNAVAILABLE, provider,
                    "Provider '" + provider + "' error (HTTP " + status + "): " + snippet);
        };
    }
}
