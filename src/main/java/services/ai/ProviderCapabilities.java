package services.ai;

/** Capability flags so callers degrade gracefully when a provider lacks a feature. */
public final class ProviderCapabilities {
    private final boolean chat;
    private final boolean streaming;
    private final boolean toolCalling;
    private final boolean structuredOutput;

    public ProviderCapabilities(boolean chat, boolean streaming,
                                boolean toolCalling, boolean structuredOutput) {
        this.chat = chat;
        this.streaming = streaming;
        this.toolCalling = toolCalling;
        this.structuredOutput = structuredOutput;
    }

    public static ProviderCapabilities full() {
        return new ProviderCapabilities(true, true, true, true);
    }

    public boolean supportsChat() { return chat; }
    public boolean supportsStreaming() { return streaming; }
    public boolean supportsToolCalling() { return toolCalling; }
    public boolean supportsStructuredOutput() { return structuredOutput; }
}
