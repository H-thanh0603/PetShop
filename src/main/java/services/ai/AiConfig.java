package services.ai;

import Util.AppConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Central AI configuration. Everything comes from env / system properties /
 * app.properties (via {@link AppConfig}) — never hard-coded, never frontend.
 *
 * <pre>
 * AI_PROVIDER=openrouter
 * AI_MODEL=anthropic/claude-sonnet-4-5-20250929
 * OPENROUTER_API_KEY=...
 * AI_FALLBACKS=deepseek:deepseek-chat,anthropic:claude-haiku-4-5-20251001
 * </pre>
 */
public final class AiConfig {
    private AiConfig() {}

    public record ProviderRef(String provider, String model) {}

    public static String provider() {
        // Legacy DeepSeek-only keys keep working: if AI_PROVIDER is unset but
        // DEEPSEEK_API_KEY exists, default to deepseek.
        String p = AppConfig.getOrDefault("AI_PROVIDER", null, "AI_PROVIDER");
        if (p == null || p.isBlank()) {
            p = AppConfig.hasValue("DEEPSEEK_API_KEY") ? "deepseek" : "deepseek";
        }
        return p.trim().toLowerCase();
    }

    public static String model() {
        String m = AppConfig.get("AI_MODEL");
        if (m != null && !m.isBlank()) return m.trim();
        // Back-compat with the existing DeepSeek setting + DB override.
        m = AppConfig.getOrDefault("DEEPSEEK_MODEL", "deepseek-chat");
        if ("deepseek".equals(provider()) && m.startsWith("deepseek-v4")) return "deepseek-chat";
        return m.trim();
    }

    public static int timeoutSeconds() {
        return AppConfig.getInt("AI_TIMEOUT_SECONDS",
                AppConfig.getInt("DEEPSEEK_TIMEOUT_SECONDS", 30));
    }

    public static int maxToolSteps() {
        return AppConfig.getInt("AI_MAX_TOOL_STEPS", 6);
    }

    /** Comma-separated "provider:model" list, e.g. "deepseek:deepseek-chat,openai:gpt-4o-mini". */
    public static List<ProviderRef> fallbacks() {
        String raw = AppConfig.getOrDefault("AI_FALLBACKS", "");
        List<ProviderRef> out = new ArrayList<>();
        for (String part : raw.split(",")) {
            part = part.trim();
            if (part.isEmpty()) continue;
            int colon = part.indexOf(':');
            if (colon <= 0) continue;
            out.add(new ProviderRef(part.substring(0, colon).trim().toLowerCase(),
                    part.substring(colon + 1).trim()));
        }
        return out;
    }

    public static String apiKeyFor(String provider) {
        return switch (provider.toLowerCase()) {
            case "anthropic" -> AppConfig.get("ANTHROPIC_API_KEY");
            case "openrouter" -> AppConfig.get("OPENROUTER_API_KEY");
            case "tokenrouter" -> AppConfig.get("TOKENROUTER_API_KEY");
            case "openai" -> AppConfig.get("OPENAI_API_KEY");
            case "gemini", "google" -> AppConfig.get("GOOGLE_API_KEY", "GEMINI_API_KEY");
            case "deepseek" -> AppConfig.get("DEEPSEEK_API_KEY");
            default -> AppConfig.get(provider.toUpperCase() + "_API_KEY");
        };
    }

    public static String baseUrlFor(String provider) {
        return switch (provider.toLowerCase()) {
            case "anthropic" -> AppConfig.getOrDefault("ANTHROPIC_BASE_URL", "https://api.anthropic.com");
            case "openrouter" -> AppConfig.getOrDefault("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1");
            case "tokenrouter" -> AppConfig.getOrDefault("TOKENROUTER_BASE_URL", "https://api.tokenrouter.ai/v1");
            case "openai" -> AppConfig.getOrDefault("OPENAI_BASE_URL", "https://api.openai.com/v1");
            case "gemini", "google" -> AppConfig.getOrDefault("GOOGLE_BASE_URL",
                    "https://generativelanguage.googleapis.com/v1beta/openai");
            case "deepseek" -> AppConfig.getOrDefault("DEEPSEEK_BASE_URL", "https://api.deepseek.com");
            default -> AppConfig.getOrDefault(provider.toUpperCase() + "_BASE_URL", "");
        };
    }

    /** Redacted summary safe for logs (never log keys). */
    public static String describeForLog(String provider, String model) {
        boolean hasKey = apiKeyFor(provider) != null && !apiKeyFor(provider).isBlank();
        return "provider=" + provider + " model=" + model + " keyPresent=" + hasKey;
    }
}
