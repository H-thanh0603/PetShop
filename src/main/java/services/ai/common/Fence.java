package services.ai.common;

import java.util.regex.Pattern;

/**
 * Fencing port (commerce-common/fencing.py core): third-party text is
 * sanitized, wrapped in a fixed-label fence, and capped before the model
 * reads it. Fenced text is material to report on, never instructions.
 */
public final class Fence {
    public static final int DEFAULT_MAX_CHARS = 12_000;
    private static final Pattern CONTROL = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");
    private static final Pattern FORGED_MARKER =
            Pattern.compile("(?i)(<\\|?\\s*(system|user|assistant|tool|end).*?\\|?>|```\\s*json)");

    private Fence() {}

    public static String sanitize(String text) {
        if (text == null) return "";
        String t = CONTROL.matcher(text).replaceAll(" ");
        t = FORGED_MARKER.matcher(t).replaceAll("[removed]");
        return t.trim();
    }

    public static String fence(String label, String text) {
        return fence(label, text, DEFAULT_MAX_CHARS);
    }

    public static String fence(String label, String text, int maxChars) {
        String clean = sanitize(text);
        if (clean.length() > maxChars) clean = clean.substring(0, maxChars) + "…[truncated]";
        return "[fenced:" + label + "]\n" + clean + "\n[/fenced:" + label + "]";
    }

    /** Chips/suggestions: sanitized, capped at 4 (presentation.py rule). */
    public static java.util.List<String> sanitizeChips(java.util.List<String> chips) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (chips == null) return out;
        for (String c : chips) {
            String s = sanitize(c);
            if (!s.isEmpty() && s.length() <= 60) out.add(s);
            if (out.size() >= 4) break;
        }
        return out;
    }
}
