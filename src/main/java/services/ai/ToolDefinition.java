package services.ai;

/**
 * Declares one commerce tool. The JSON schema follows OpenAI function-calling
 * shape ({@code {"type":"object","properties":{...},"required":[...]}}); each
 * provider adapter converts it to the provider's native tool format.
 */
public final class ToolDefinition {
    private final String name;
    private final String description;
    private final String parametersSchemaJson;

    public ToolDefinition(String name, String description, String parametersSchemaJson) {
        this.name = name;
        this.description = description;
        this.parametersSchemaJson = parametersSchemaJson;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getParametersSchemaJson() { return parametersSchemaJson; }
}
