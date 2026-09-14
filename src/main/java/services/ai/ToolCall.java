package services.ai;

/** A model-requested tool invocation (provider-neutral). */
public final class ToolCall {
    private final String id;
    private final String name;
    private final String argumentsJson;

    public ToolCall(String id, String name, String argumentsJson) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name;
        this.argumentsJson = argumentsJson == null ? "{}" : argumentsJson;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getArgumentsJson() { return argumentsJson; }
}
