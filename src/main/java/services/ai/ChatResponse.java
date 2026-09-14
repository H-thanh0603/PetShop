package services.ai;

import java.util.List;

/** Provider-neutral chat response used by the agent runtime. */
public final class ChatResponse {
    private final String content;
    private final List<ToolCall> toolCalls;
    private final String model;
    private final String provider;
    private final String requestId;
    private final long latencyMs;
    private final Integer promptTokens;
    private final Integer completionTokens;

    public ChatResponse(String content, List<ToolCall> toolCalls, String model,
                        String provider, String requestId, long latencyMs,
                        Integer promptTokens, Integer completionTokens) {
        this.content = content == null ? "" : content;
        this.toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
        this.model = model;
        this.provider = provider;
        this.requestId = requestId;
        this.latencyMs = latencyMs;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
    }

    public String getContent() { return content; }
    public List<ToolCall> getToolCalls() { return toolCalls; }
    public boolean hasToolCalls() { return !toolCalls.isEmpty(); }
    public String getModel() { return model; }
    public String getProvider() { return provider; }
    public String getRequestId() { return requestId; }
    public long getLatencyMs() { return latencyMs; }
    public Integer getPromptTokens() { return promptTokens; }
    public Integer getCompletionTokens() { return completionTokens; }
}
