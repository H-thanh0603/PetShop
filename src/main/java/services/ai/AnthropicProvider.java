package services.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    private final Gson gson = new Gson();

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
        JsonObject payload = buildPayload(request);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.min(timeoutSeconds, 15))).build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
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

    private JsonObject buildPayload(ChatRequest request) {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", OpenAiCompatibleProvider.stripPrefix(model));
        payload.addProperty("max_tokens", Math.max(request.getMaxTokens(), 256));
        payload.addProperty("temperature", request.getTemperature());
        StringBuilder system = new StringBuilder();
        JsonArray messages = new JsonArray();
        for (AiMessage m : request.getMessages()) {
            switch (m.getRole()) {
                case SYSTEM -> {
                    if (system.length() > 0) system.append("\n\n");
                    system.append(m.getContent());
                }
                case USER -> {
                    JsonObject o = new JsonObject();
                    o.addProperty("role", "user");
                    o.addProperty("content", m.getContent());
                    messages.add(o);
                }
                case ASSISTANT -> {
                    JsonObject o = new JsonObject();
                    o.addProperty("role", "assistant");
                    JsonArray content = new JsonArray();
                    if (m.getContent() != null && !m.getContent().isEmpty()) {
                        JsonObject t = new JsonObject();
                        t.addProperty("type", "text");
                        t.addProperty("text", m.getContent());
                        content.add(t);
                    }
                    for (ToolCall tc : m.getToolCalls()) {
                        JsonObject use = new JsonObject();
                        use.addProperty("type", "tool_use");
                        use.addProperty("id", tc.getId());
                        use.addProperty("name", tc.getName());
                        try {
                            use.add("input", JsonParser.parseString(tc.getArgumentsJson()));
                        } catch (Exception e) {
                            use.add("input", new JsonObject());
                        }
                        content.add(use);
                    }
                    o.add("content", content);
                    messages.add(o);
                }
                case TOOL -> {
                    JsonObject o = new JsonObject();
                    o.addProperty("role", "user");
                    JsonArray content = new JsonArray();
                    JsonObject r = new JsonObject();
                    r.addProperty("type", "tool_result");
                    r.addProperty("tool_use_id", m.getToolCallId());
                    r.addProperty("content", m.getContent());
                    content.add(r);
                    o.add("content", content);
                    messages.add(o);
                }
            }
        }
        if (system.length() > 0) payload.addProperty("system", system.toString());
        payload.add("messages", messages);
        if (!request.getTools().isEmpty()) {
            JsonArray tools = new JsonArray();
            for (ToolDefinition t : request.getTools()) {
                JsonObject tool = new JsonObject();
                tool.addProperty("name", t.getName());
                tool.addProperty("description", t.getDescription());
                try {
                    tool.add("input_schema", JsonParser.parseString(t.getParametersSchemaJson()));
                } catch (Exception e) {
                    JsonObject schema = new JsonObject();
                    schema.addProperty("type", "object");
                    tool.add("input_schema", schema);
                }
                tools.add(tool);
            }
            payload.add("tools", tools);
        }
        return payload;
    }

    private ChatResponse parseResponse(String body, String requestId, long latency) throws AiException {
        try {
            JsonObject res = JsonParser.parseString(body).getAsJsonObject();
            StringBuilder text = new StringBuilder();
            List<ToolCall> calls = new ArrayList<>();
            JsonArray content = res.getAsJsonArray("content");
            if (content != null) {
                for (JsonElement el : content) {
                    JsonObject block = el.getAsJsonObject();
                    String type = block.has("type") ? block.get("type").getAsString() : "";
                    if ("text".equals(type) && block.has("text")) {
                        text.append(block.get("text").getAsString());
                    } else if ("tool_use".equals(type)) {
                        calls.add(new ToolCall(
                                block.get("id").getAsString(),
                                block.get("name").getAsString(),
                                gson.toJson(block.get("input"))));
                    }
                }
            }
            Integer in = null, out = null;
            if (res.has("usage") && res.get("usage").isJsonObject()) {
                JsonObject u = res.getAsJsonObject("usage");
                if (u.has("input_tokens")) in = u.get("input_tokens").getAsInt();
                if (u.has("output_tokens")) out = u.get("output_tokens").getAsInt();
            }
            return new ChatResponse(text.toString(), calls, model, "anthropic",
                    requestId, latency, in, out);
        } catch (Exception e) {
            throw new AiException(AiException.Kind.BAD_RESPONSE, "anthropic",
                    "Unparsable Anthropic response", e);
        }
    }
}
