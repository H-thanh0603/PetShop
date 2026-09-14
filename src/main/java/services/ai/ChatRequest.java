package services.ai;

import java.util.ArrayList;
import java.util.List;

/** Provider-neutral chat request. */
public final class ChatRequest {
    private final List<AiMessage> messages;
    private final List<ToolDefinition> tools;
    private final double temperature;
    private final int maxTokens;
    private final boolean jsonMode;

    private ChatRequest(Builder b) {
        this.messages = List.copyOf(b.messages);
        this.tools = List.copyOf(b.tools);
        this.temperature = b.temperature;
        this.maxTokens = b.maxTokens;
        this.jsonMode = b.jsonMode;
    }

    public List<AiMessage> getMessages() { return messages; }
    public List<ToolDefinition> getTools() { return tools; }
    public double getTemperature() { return temperature; }
    public int getMaxTokens() { return maxTokens; }
    public boolean isJsonMode() { return jsonMode; }

    public static Builder builder(List<AiMessage> messages) {
        return new Builder(messages);
    }

    public static final class Builder {
        private final List<AiMessage> messages;
        private List<ToolDefinition> tools = new ArrayList<>();
        private double temperature = 0.2;
        private int maxTokens = 1500;
        private boolean jsonMode = false;

        Builder(List<AiMessage> messages) {
            this.messages = new ArrayList<>(messages);
        }

        public Builder tools(List<ToolDefinition> tools) {
            this.tools = new ArrayList<>(tools);
            return this;
        }

        public Builder temperature(double t) { this.temperature = t; return this; }
        public Builder maxTokens(int n) { this.maxTokens = n; return this; }
        public Builder jsonMode(boolean j) { this.jsonMode = j; return this; }

        public ChatRequest build() { return new ChatRequest(this); }
    }
}
