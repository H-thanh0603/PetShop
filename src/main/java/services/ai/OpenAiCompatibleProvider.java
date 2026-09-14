package services.ai;

import Util.AppConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    private final Gson gson = new Gson();

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
        JsonObject payload = buildPayload(request);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.min(timeoutSeconds, 15))).build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", AppConfig.getOrDefault("app.base-url", "http://localhost:8080/PetShop"))
                .header("X-Title", "PetShop Commerce Agent")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
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
        JsonObject payload = buildPayload(request);
        payload.addProperty("stream", true);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.min(timeoutSeconds, 15))).build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
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
                        JsonObject chunk = JsonParser.parseString(data).getAsJsonObject();
                        JsonArray choices = chunk.getAsJsonArray("choices");
                        if (choices == null || choices.isEmpty()) continue;
                        JsonObject delta = choices.get(0).getAsJsonObject().getAsJsonObject("delta");
                        if (delta == null) continue;
                        if (delta.has("content") && !delta.get("content").isJsonNull()) {
                            String piece = delta.get("content").getAsString();
                            full.append(piece);
                            onDelta.accept(piece);
                        }
                        // Streaming tool calls: accumulate per index (OpenAI chunk shape).
                        if (delta.has("tool_calls") && delta.get("tool_calls").isJsonArray()) {
                            mergeStreamedToolCalls(toolCalls, delta.getAsJsonArray("tool_calls"));
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

    private void mergeStreamedToolCalls(List<ToolCall> acc, JsonArray deltas) {
        for (JsonElement el : deltas) {
            JsonObject tc = el.getAsJsonObject();
            int index = tc.has("index") ? tc.get("index").getAsInt() : acc.size();
            while (acc.size() <= index) acc.add(new ToolCall("stream-" + acc.size(), "", "{}"));
            String id = tc.has("id") && !tc.get("id").isJsonNull() ? tc.get("id").getAsString() : acc.get(index).getId();
            String fname = acc.get(index).getName();
            String fargs = acc.get(index).getArgumentsJson();
            if (tc.has("function") && tc.get("function").isJsonObject()) {
                JsonObject fn = tc.getAsJsonObject("function");
                if (fn.has("name") && !fn.get("name").isJsonNull()) fname = fname + fn.get("name").getAsString();
                if (fn.has("arguments") && !fn.get("arguments").isJsonNull()) fargs = fargs + fn.get("arguments").getAsString();
            }
            acc.set(index, new ToolCall(id, fname, fargs));
        }
    }

    private JsonObject buildPayload(ChatRequest request) {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", stripPrefix(model));
        payload.addProperty("temperature", request.getTemperature());
        payload.addProperty("max_tokens", request.getMaxTokens());
        JsonArray messages = new JsonArray();
        for (AiMessage m : request.getMessages()) {
            messages.add(toWireMessage(m));
        }
        payload.add("messages", messages);
        if (!request.getTools().isEmpty()) {
            JsonArray tools = new JsonArray();
            for (ToolDefinition t : request.getTools()) {
                JsonObject tool = new JsonObject();
                tool.addProperty("type", "function");
                JsonObject fn = new JsonObject();
                fn.addProperty("name", t.getName());
                fn.addProperty("description", t.getDescription());
                try {
                    fn.add("parameters", JsonParser.parseString(t.getParametersSchemaJson()));
                } catch (Exception e) {
                    JsonObject fallback = new JsonObject();
                    fallback.addProperty("type", "object");
                    fn.add("parameters", fallback);
                }
                tool.add("function", fn);
                tools.add(tool);
            }
            payload.add("tools", tools);
            payload.addProperty("tool_choice", "auto");
        }
        if (request.isJsonMode()) {
            JsonObject fmt = new JsonObject();
            fmt.addProperty("type", "json_object");
            payload.add("response_format", fmt);
        }
        return payload;
    }

    private JsonObject toWireMessage(AiMessage m) {
        JsonObject o = new JsonObject();
        switch (m.getRole()) {
            case SYSTEM -> o.addProperty("role", "system");
            case USER -> o.addProperty("role", "user");
            case TOOL -> {
                o.addProperty("role", "tool");
                o.addProperty("tool_call_id", m.getToolCallId());
                o.addProperty("content", m.getContent());
                return o;
            }
            case ASSISTANT -> {
                o.addProperty("role", "assistant");
                if (!m.getToolCalls().isEmpty()) {
                    o.addProperty("content", m.getContent() == null ? "" : m.getContent());
                    JsonArray tcs = new JsonArray();
                    for (ToolCall tc : m.getToolCalls()) {
                        JsonObject t = new JsonObject();
                        t.addProperty("id", tc.getId());
                        t.addProperty("type", "function");
                        JsonObject fn = new JsonObject();
                        fn.addProperty("name", tc.getName());
                        fn.addProperty("arguments", tc.getArgumentsJson());
                        t.add("function", fn);
                        tcs.add(t);
                    }
                    o.add("tool_calls", tcs);
                    return o;
                }
            }
        }
        o.addProperty("content", m.getContent() == null ? "" : m.getContent());
        return o;
    }

    private ChatResponse parseResponse(String body, String requestId, long latency) throws AiException {
        try {
            JsonObject res = JsonParser.parseString(body).getAsJsonObject();
            JsonObject choice = res.getAsJsonArray("choices").get(0).getAsJsonObject();
            JsonObject msg = choice.getAsJsonObject("message");
            String content = msg.has("content") && !msg.get("content").isJsonNull()
                    ? msg.get("content").getAsString() : "";
            List<ToolCall> calls = new ArrayList<>();
            if (msg.has("tool_calls") && msg.get("tool_calls").isJsonArray()) {
                for (JsonElement el : msg.getAsJsonArray("tool_calls")) {
                    JsonObject t = el.getAsJsonObject();
                    JsonObject fn = t.getAsJsonObject("function");
                    calls.add(new ToolCall(
                            t.has("id") ? t.get("id").getAsString() : UUID.randomUUID().toString(),
                            fn.get("name").getAsString(),
                            fn.has("arguments") && !fn.get("arguments").isJsonNull()
                                    ? fn.get("arguments").getAsString() : "{}"));
                }
            }
            Integer promptTokens = null, completionTokens = null;
            if (res.has("usage") && res.get("usage").isJsonObject()) {
                JsonObject u = res.getAsJsonObject("usage");
                if (u.has("prompt_tokens")) promptTokens = u.get("prompt_tokens").getAsInt();
                if (u.has("completion_tokens")) completionTokens = u.get("completion_tokens").getAsInt();
            }
            String respModel = res.has("model") ? res.get("model").getAsString() : model;
            return new ChatResponse(content, calls, respModel, name, requestId, latency,
                    promptTokens, completionTokens);
        } catch (Exception e) {
            throw new AiException(AiException.Kind.BAD_RESPONSE, name,
                    "Unparsable response from '" + name + "'", e);
        }
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
