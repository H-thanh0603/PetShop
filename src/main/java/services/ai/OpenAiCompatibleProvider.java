package services.ai;

import com.petshop.util.AppConfig;
import com.petshop.util.Json;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * One adapter for every OpenAI-compatible {@code /chat/completions} endpoint:
 * OpenAI, DeepSeek, OpenRouter, TokenRouter, and Gemini's OpenAI-compat path.
 * Only {@code baseUrl} / key / model differ per provider (see {@link AiConfig}).
 */
public class OpenAiCompatibleProvider implements AiProvider {
    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleProvider.class);

    private final String name;
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;

    public OpenAiCompatibleProvider(String name, String baseUrl, String apiKey,
                                    String model, int timeoutSeconds) {
        this.name = name;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override public String getName() { return name; }
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
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", AppConfig.getOrDefault("app.base-url", "http://localhost:8080/PetShop"))
                .header("X-Title", "PetShop Commerce Agent")
                .POST(HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(payload)))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        try {
            HttpResponse<String> response =
                    client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - start;
            if (response.statusCode() != 200) {
                throw AiException.fromHttp(name, response.statusCode(), response.body());
            }
            return parseResponse(response.body(), requestId, latency);
        } catch (IOException e) {
            boolean timeout = e instanceof java.net.http.HttpTimeoutException;
            throw new AiException(timeout ? AiException.Kind.TIMEOUT : AiException.Kind.PROVIDER_UNAVAILABLE,
                    name, "Provider '" + name + "' request failed: " + e.getClass().getSimpleName(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException(AiException.Kind.PROVIDER_UNAVAILABLE, name, "Interrupted", e);
        }
    }

    @Override
    public void stream(ChatRequest request, Consumer<String> onDelta,
                       Consumer<ChatResponse> onComplete) throws AiException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long start = System.currentTimeMillis();
        ObjectNode payload = buildPayload(request);
        payload.put("stream", true);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.min(timeoutSeconds, 15))).build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(payload)))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        StringBuilder full = new StringBuilder();
        List<ToolCall> toolCalls = new ArrayList<>();
        try {
            HttpResponse<java.io.InputStream> response =
                    client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                String body;
                try { body = new String(response.body().readAllBytes()); }
                catch (IOException ioe) { body = ""; }
                throw AiException.fromHttp(name, response.statusCode(), body);
            }
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(response.body(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.equals(":")) continue;
                    if (!line.startsWith("data:")) continue;
                    String data = line.substring(5).trim();
                    if ("[DONE]".equals(data)) break;
                    try {
                        JsonNode chunk = Json.MAPPER.readTree(data);
                        JsonNode choices = chunk.path("choices");
                        if (!choices.isArray() || choices.isEmpty()) continue;
                        JsonNode delta = choices.path(0).path("delta");
                        if (!delta.isObject()) continue;
                        if (delta.has("content") && !delta.path("content").isNull()) {
                            String piece = delta.path("content").asString();
                            full.append(piece);
                            onDelta.accept(piece);
                        }
                        // Streaming tool calls: accumulate per index (OpenAI chunk shape).
                        if (delta.path("tool_calls").isArray()) {
                            mergeStreamedToolCalls(toolCalls, (ArrayNode) delta.path("tool_calls"));
                        }
                    } catch (Exception parseEx) {
                        log.debug("[{}] Skipping unparsable SSE chunk", requestId);
                    }
                }
            }
            long latency = System.currentTimeMillis() - start;
            onComplete.accept(new ChatResponse(full.toString(), toolCalls, model,
                    name, requestId, latency, null, null));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new AiException(AiException.Kind.PROVIDER_UNAVAILABLE, name,
                    "Streaming from '" + name + "' failed: " + e.getClass().getSimpleName(), e);
        }
    }

    private void mergeStreamedToolCalls(List<ToolCall> acc, ArrayNode deltas) {
        for (JsonNode el : deltas) {
            JsonNode tc = el;
            if (!tc.isObject()) continue;
            int index = tc.has("index") ? tc.path("index").asInt() : acc.size();
            while (acc.size() <= index) acc.add(new ToolCall("stream-" + acc.size(), "", "{}"));
            String id = tc.has("id") && !tc.path("id").isNull() ? tc.path("id").asString() : acc.get(index).getId();
            String fname = acc.get(index).getName();
            String fargs = acc.get(index).getArgumentsJson();
            if (tc.has("function") && tc.path("function").isObject()) {
                JsonNode fn = tc.path("function");
                if (fn.has("name") && !fn.path("name").isNull()) fname = fname + fn.path("name").asString();
                if (fn.has("arguments") && !fn.path("arguments").isNull()) fargs = fargs + fn.path("arguments").asString();
            }
            acc.set(index, new ToolCall(id, fname, fargs));
        }
    }

    private ObjectNode buildPayload(ChatRequest request) {
        ObjectNode payload = Json.MAPPER.createObjectNode();
        payload.put("model", stripPrefix(model));
        payload.put("temperature", request.getTemperature());
        payload.put("max_tokens", request.getMaxTokens());
        ArrayNode messages = Json.MAPPER.createArrayNode();
        for (AiMessage m : request.getMessages()) {
            messages.add(toWireMessage(m));
        }
        payload.set("messages", messages);
        if (!request.getTools().isEmpty()) {
            ArrayNode tools = Json.MAPPER.createArrayNode();
            for (ToolDefinition t : request.getTools()) {
                ObjectNode tool = Json.MAPPER.createObjectNode();
                tool.put("type", "function");
                ObjectNode fn = Json.MAPPER.createObjectNode();
                fn.put("name", t.getName());
                fn.put("description", t.getDescription());
                try {
                    fn.set("parameters", Json.MAPPER.readTree(t.getParametersSchemaJson()));
                } catch (Exception e) {
                    ObjectNode fallback = Json.MAPPER.createObjectNode();
                    fallback.put("type", "object");
                    fn.set("parameters", fallback);
                }
                tool.set("function", fn);
                tools.add(tool);
            }
            payload.set("tools", tools);
            payload.put("tool_choice", "auto");
        }
        if (request.isJsonMode()) {
            ObjectNode fmt = Json.MAPPER.createObjectNode();
            fmt.put("type", "json_object");
            payload.set("response_format", fmt);
        }
        return payload;
    }

    private ObjectNode toWireMessage(AiMessage m) {
        ObjectNode o = Json.MAPPER.createObjectNode();
        switch (m.getRole()) {
            case SYSTEM -> o.put("role", "system");
            case USER -> o.put("role", "user");
            case TOOL -> {
                o.put("role", "tool");
                o.put("tool_call_id", m.getToolCallId());
                o.put("content", m.getContent());
                return o;
            }
            case ASSISTANT -> {
                o.put("role", "assistant");
                if (!m.getToolCalls().isEmpty()) {
                    o.put("content", m.getContent() == null ? "" : m.getContent());
                    ArrayNode tcs = Json.MAPPER.createArrayNode();
                    for (ToolCall tc : m.getToolCalls()) {
                        ObjectNode t = Json.MAPPER.createObjectNode();
                        t.put("id", tc.getId());
                        t.put("type", "function");
                        ObjectNode fn = Json.MAPPER.createObjectNode();
                        fn.put("name", tc.getName());
                        fn.put("arguments", tc.getArgumentsJson());
                        t.set("function", fn);
                        tcs.add(t);
                    }
                    o.set("tool_calls", tcs);
                    return o;
                }
            }
        }
        o.put("content", m.getContent() == null ? "" : m.getContent());
        return o;
    }

    private ChatResponse parseResponse(String body, String requestId, long latency) throws AiException {
        try {
            JsonNode res = Json.MAPPER.readTree(body);
            // Giữ hành vi cũ: body thiếu "choices"/"message" phải ném AiException
            // (Gson ném IllegalStateException khigetAsJsonObject/get(0) trượt →
            // catch(Exception) gói thành BAD_RESPONSE). Jackson path() trả missing
            // node thay vì ném, nên phải kiểm tra tường minh.
            if (!res.isObject()
                    || !res.path("choices").isArray()
                    || res.path("choices").isEmpty()
                    || !res.path("choices").path(0).isObject()
                    || !res.path("choices").path(0).path("message").isObject()) {
                throw new AiException(AiException.Kind.BAD_RESPONSE, name,
                        "Unparsable response from '" + name + "'", null);
            }
            JsonNode msg = res.path("choices").path(0).path("message");
            String content = msg.has("content") && !msg.path("content").isNull()
                    ? msg.path("content").asString() : "";
            List<ToolCall> calls = new ArrayList<>();
            if (msg.path("tool_calls").isArray()) {
                for (JsonNode t : msg.path("tool_calls")) {
                    // Strict like Gson (review fix): non-object entries or a
                    // non-object/missing function threw (getAsJsonObject / NPE
                    // on get("name")) → BAD_RESPONSE. The brief's guard covers
                    // only choices/message; tool_calls keeps the old outcome.
                    if (!t.isObject() || !t.path("function").isObject()) {
                        throw new AiException(AiException.Kind.BAD_RESPONSE, name,
                                "Unparsable response from '" + name + "'", null);
                    }
                    JsonNode fn = t.path("function");
                    calls.add(new ToolCall(
                            t.has("id") ? gsonString(t.path("id")) : UUID.randomUUID().toString(),
                            gsonString(fn.path("name")),
                            fn.has("arguments") && !fn.path("arguments").isNull()
                                    ? gsonString(fn.path("arguments")) : "{}"));
                }
            }
            Integer promptTokens = null, completionTokens = null;
            if (res.path("usage").isObject()) {
                JsonNode u = res.path("usage");
                if (u.has("prompt_tokens")) promptTokens = u.path("prompt_tokens").asInt();
                if (u.has("completion_tokens")) completionTokens = u.path("completion_tokens").asInt();
            }
            String respModel = res.has("model") ? res.path("model").asString() : model;
            return new ChatResponse(content, calls, respModel, name, requestId, latency,
                    promptTokens, completionTokens);
        } catch (AiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AiException(AiException.Kind.BAD_RESPONSE, name,
                    "Unparsable response from '" + name + "'", e);
        }
    }

    /**
     * Mirrors Gson {@code JsonElement.getAsString}: JSON primitives stringify,
     * but missing / explicit null / objects / arrays throw so the caller wraps
     * them into BAD_RESPONSE exactly like the Gson code did.
     */
    private static String gsonString(JsonNode n) {
        if (n == null || n.isMissingNode() || n.isNull() || n.isObject() || n.isArray()) {
            throw new IllegalArgumentException("not a JSON primitive");
        }
        return n.asString();
    }

    /** OpenRouter-style "provider/model" prefixes must not be sent to native endpoints. */
    static String stripPrefix(String model) {
        if (model == null) return "";
        int slash = model.indexOf('/');
        if (slash > 0 && (model.startsWith("anthropic/") || model.startsWith("openai/")
                || model.startsWith("google/") || model.startsWith("deepseek/"))) {
            return model.substring(slash + 1);
        }
        return model;
    }
}
