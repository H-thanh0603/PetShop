package services.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A single chat message in provider-neutral form.
 * Commerce Agents parallel: the per-turn message list streamed to the runtime.
 */
public final class AiMessage {
    public enum Role { SYSTEM, USER, ASSISTANT, TOOL }

    private final Role role;
    private final String content;
    private final List<ToolCall> toolCalls;
    private final String toolCallId;
    private final String toolName;

    private AiMessage(Role role, String content, List<ToolCall> toolCalls,
                      String toolCallId, String toolName) {
        this.role = role;
        this.content = content;
        this.toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
        this.toolCallId = toolCallId;
        this.toolName = toolName;
    }

    public static AiMessage system(String content) {
        return new AiMessage(Role.SYSTEM, content, null, null, null);
    }

    public static AiMessage user(String content) {
        return new AiMessage(Role.USER, content, null, null, null);
    }

    public static AiMessage assistant(String content) {
        return new AiMessage(Role.ASSISTANT, content, null, null, null);
    }

    public static AiMessage assistantWithTools(String content, List<ToolCall> toolCalls) {
        return new AiMessage(Role.ASSISTANT, content, toolCalls, null, null);
    }

    public static AiMessage toolResult(String toolCallId, String toolName, String resultJson) {
        return new AiMessage(Role.TOOL, resultJson, null, toolCallId, toolName);
    }

    public Role getRole() { return role; }
    public String getContent() { return content; }
    public List<ToolCall> getToolCalls() { return toolCalls; }
    public String getToolCallId() { return toolCallId; }
    public String getToolName() { return toolName; }

    public static List<AiMessage> copyOf(List<AiMessage> messages) {
        return new ArrayList<>(messages == null ? Collections.emptyList() : messages);
    }
}
