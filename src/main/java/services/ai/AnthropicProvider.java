package services.ai;

import Util.Json;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Native Anthropic Messages API adapter.
 * Accepts the same provider-neutral {@link ChatRequest} (including OpenAI-shaped
 * tool definitions) and converts to Anthropic's {@code tools} / {@code tool_use}
 * format so agent logic never branches on provider.
 */
public class AnthropicProvider implements AiProvider {
    private static final String DEFAULT_BASE = "https://api.anthropic.com";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;

    public AnthropicProvider(String baseUrl, String apiKey, String model, int timeoutSeconds) {
        this.baseUrl = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE : baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override public String getName() { return "anthropic"; }
    @Override public String getModel() { return model; }
    @Override public ProviderCapabilities getCapabilities() { return ProviderCapabilities.full(); }

    @Override
    public ChatResponse complete(ChatRequest request) throws AiException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long start = System.currentTimeMillis();
        ObjectNode payload = buildPayload(request);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.min(timeoutSeconds, 15))).build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(payload)))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        try {
            HttpResponse<String> response =
                    client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - start;
            if (response.statusCode() != 200) {
                throw AiException.fromHttp("anthropic", response.statusCode(), response.body());
            }
            return parseResponse(response.body(), requestId, latency);
        } catch (IOException e) {
            boolean timeout = e instanceof java.net.http.HttpTimeoutException;
            throw new AiException(timeout ? AiException.Kind.TIMEOUT : AiException.Kind.PROVIDER_UNAVAILABLE,
                    "anthropic", "Anthropic request failed: " + e.getClass().getSimpleName(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException(AiException.Kind.PROVIDER_UNAVAILABLE, "anthropic", "Interrupted", e);
        }
    }

    private ObjectNode buildPayload(ChatRequest request) {
        ObjectNode payload = Json.MAPPER.createObjectNode();
        payload.put("model", OpenAiCompatibleProvider.stripPrefix(model));
        payload.put("max_tokens", Math.max(request.getMaxTokens(), 256));
        payload.put("temperature", request.getTemperature());
        StringBuilder system = new StringBuilder();
        ArrayNode messages = Json.MAPPER.createArrayNode();
        for (AiMessage m : request.getMessages()) {
            switch (m.getRole()) {
                case SYSTEM -> {
                    if (system.length() > 0) system.append("\n\n");
                    system.append(m.getContent());
                }
                case USER -> {
                    ObjectNode o = Json.MAPPER.createObjectNode();
                    o.put("role", "user");
                    o.put("content", m.getContent());
                    messages.add(o);
                }
                case ASSISTANT -> {
                    ObjectNode o = Json.MAPPER.createObjectNode();
                    o.put("role", "assistant");
                    ArrayNode content = Json.MAPPER.createArrayNode();
                    if (m.getContent() != null && !m.getContent().isEmpty()) {
                        ObjectNode t = Json.MAPPER.createObjectNode();
                        t.put("type", "text");
                        t.put("text", m.getContent());
                        content.add(t);
                    }
                    for (ToolCall tc : m.getToolCalls()) {
                        ObjectNode use = Json.MAPPER.createObjectNode();
                        use.put("type", "tool_use");
                        use.put("id", tc.getId());
                        use.put("name", tc.getName());
                        try {
                            use.set("input", Json.MAPPER.readTree(tc.getArgumentsJson()));
                        } catch (Exception e) {
                            use.set("input", Json.MAPPER.createObjectNode());
                        }
                        content.add(use);
                    }
                    o.set("content", content);
                    messages.add(o);
                }
                case TOOL -> {
                    ObjectNode o = Json.MAPPER.createObjectNode();
                    o.put("role", "user");
                    ArrayNode content = Json.MAPPER.createArrayNode();
                    ObjectNode r = Json.MAPPER.createObjectNode();
                    r.put("type", "tool_result");
                    r.put("tool_use_id", m.getToolCallId());
                    r.put("content", m.getContent());
                    content.add(r);
                    o.set("content", content);
                    messages.add(o);
                }
            }
        }
        if (system.length() > 0) payload.put("system", system.toString());
        payload.set("messages", messages);
        if (!request.getTools().isEmpty()) {
            ArrayNode tools = Json.MAPPER.createArrayNode();
            for (ToolDefinition t : request.getTools()) {
                ObjectNode tool = Json.MAPPER.createObjectNode();
                tool.put("name", t.getName());
                tool.put("description", t.getDescription());
                try {
                    tool.set("input_schema", Json.MAPPER.readTree(t.getParametersSchemaJson()));
                } catch (Exception e) {
                    ObjectNode schema = Json.MAPPER.createObjectNode();
                    schema.put("type", "object");
                    tool.set("input_schema", schema);
                }
                tools.add(tool);
            }
            payload.set("tools", tools);
        }
        return payload;
    }

    private ChatResponse parseResponse(String body, String requestId, long latency) throws AiException {
        try {
            JsonNode res = Json.MAPPER.readTree(body);
            // Giữ hành vi cũ: body không phải object (Gson getAsJsonObject ném) →
            // BAD_RESPONSE. Jackson readTree trả node thay vì ném nên kiểm tra tường minh.
            if (!res.isObject()) {
                throw new AiException(AiException.Kind.BAD_RESPONSE, "anthropic",
                        "Unparsable Anthropic response", null);
            }
            StringBuilder text = new StringBuilder();
            List<ToolCall> calls = new ArrayList<>();
            JsonNode content = res.path("content");
            if (content.isArray()) {
                for (JsonNode block : content) {
                    String type = block.has("type") ? block.path("type").asString() : "";
                    if ("text".equals(type) && block.has("text") && !block.path("text").isNull()) {
                        text.append(block.path("text").asString());
                    } else if ("tool_use".equals(type)) {
                        calls.add(new ToolCall(
                                block.path("id").asString(),
                                block.path("name").asString(),
                                Json.MAPPER.writeValueAsString(block.path("input"))));
                    }
                }
            }
            Integer in = null, out = null;
            if (res.has("usage") && res.path("usage").isObject()) {
                JsonNode u = res.path("usage");
                if (u.has("input_tokens")) in = u.path("input_tokens").asInt();
                if (u.has("output_tokens")) out = u.path("output_tokens").asInt();
            }
            return new ChatResponse(text.toString(), calls, model, "anthropic",
                    requestId, latency, in, out);
        } catch (AiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AiException(AiException.Kind.BAD_RESPONSE, "anthropic",
                    "Unparsable Anthropic response", e);
        }
    }
}
