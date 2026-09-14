package services.ai;

import DAO.AiSupportSettingDAO;
import Model.AiChatMessage;
import Model.Order;
import Model.Product;
import Model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Commerce agent runtime (Commerce Agents "executor + skills + gates", ported).
 *
 * <p>Flow: build system prompt (skills) + fenced policy context → agentic tool
 * loop (max {@code AI_MAX_TOOL_STEPS}) through the provider-neutral
 * {@link AiProvider} → parse structured JSON answer → provenance gate
 * (related ids must have been returned by tools this session) → attach rich
 * product/order details. The model never touches DAOs directly.
 */
public class CommerceAgent {
    private static final Logger log = LoggerFactory.getLogger(CommerceAgent.class);
    private static final Gson GSON = new Gson();

    private final AiSupportSettingDAO settingDAO = new AiSupportSettingDAO();
    private final PetShopCommerceBackend backend = new PetShopCommerceBackend();

    public record AgentResult(String answer, String intent, double confidence,
                              boolean needAdminSupport, String suggestedAdminNote,
                              List<Integer> relatedProductIds, Integer relatedOrderId,
                              List<Product> relatedProducts, Order relatedOrder,
                              String usedProvider, String usedModel, String requestId,
                              long latencyMs, List<String> attemptedProviders) {}

    public AgentResult run(String userMessage, List<AiChatMessage> history, User user) {
        PetShopCommerceBackend.SessionContext session = PetShopCommerceBackend.SessionContext.of(user);
        CommerceTools tools = new CommerceTools(backend, session);

        // Gate 1 (deterministic, model-independent): guests asking about orders.
        if (session.guest() && looksLikeOrderInquiry(userMessage)) {
            return guestOrderRefusal();
        }

        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.system(buildSystemPrompt()));
        for (AiChatMessage h : history) {
            if (h == null || h.getMessage() == null) continue;
            if ("USER".equals(h.getSenderType())) {
                messages.add(AiMessage.user(CommerceTools.sanitize(h.getMessage())));
            } else {
                JsonObject past = new JsonObject();
                past.addProperty("answer", h.getMessage());
                past.addProperty("intent", h.getIntent() == null ? "UNKNOWN" : h.getIntent());
                messages.add(AiMessage.user("[Previous assistant answer as JSON] " + GSON.toJson(past)));
            }
        }
        messages.add(AiMessage.user(CommerceTools.sanitize(userMessage)));

        ChatRequest request = ChatRequest.builder(messages)
                .tools(tools.definitions())
                .temperature(0.2).maxTokens(1500).jsonMode(false).build();

        int maxSteps = AiConfig.maxToolSteps();
        List<String> attempted = new ArrayList<>();
        String usedProvider = AiConfig.provider(), usedModel = AiConfig.model();
        String requestId = "-";
        long totalLatency = 0;
        String finalContent = null;

        try {
            for (int step = 0; step < maxSteps; step++) {
                AiProviderFactory.FallbackResult fr = AiProviderFactory.completeWithFallback(request);
                attempted = fr.attempted();
                usedProvider = fr.usedProvider();
                usedModel = fr.usedModel();
                ChatResponse resp = fr.response();
                requestId = resp.getRequestId();
                totalLatency += resp.getLatencyMs();
                log.info("commerce-agent step={} provider={} model={} requestId={} latencyMs={} promptTokens={} completionTokens={} toolCalls={}",
                        step, fr.usedProvider(), fr.usedModel(), resp.getRequestId(), resp.getLatencyMs(),
                        resp.getPromptTokens(), resp.getCompletionTokens(), resp.getToolCalls().size());

                if (!resp.hasToolCalls()) {
                    finalContent = resp.getContent();
                    break;
                }
                // Append assistant tool request + execute each tool server-side.
                request.getMessages().add(AiMessage.assistantWithTools(resp.getContent(), resp.getToolCalls()));
                for (ToolCall tc : resp.getToolCalls()) {
                    long t0 = System.currentTimeMillis();
                    String result = tools.execute(tc.getName(), tc.getArgumentsJson());
                    log.info("commerce-agent tool={} latencyMs={} requestId={}", tc.getName(),
                            System.currentTimeMillis() - t0, requestId);
                    request.getMessages().add(AiMessage.toolResult(tc.getId(), tc.getName(), result));
                }
                if (step == maxSteps - 1) {
                    // Budget exhausted: force a final answer without tools.
                    ChatRequest finalReq = ChatRequest.builder(request.getMessages())
                            .temperature(0.2).maxTokens(800).jsonMode(true).build();
                    AiProviderFactory.FallbackResult fr2 = AiProviderFactory.completeWithFallback(finalReq);
                    finalContent = fr2.response().getContent();
                }
            }
        } catch (AiException e) {
            log.error("commerce-agent provider failure kind={} provider={}", e.getKind(), e.getProvider(), e);
            return fallback("Tôi chưa thể kết nối với dịch vụ AI ngay lúc này. Vui lòng thử lại sau hoặc liên hệ admin.",
                    usedProvider, usedModel, attempted);
        }

        if (finalContent == null || finalContent.isBlank()) {
            return fallback("Tôi chưa thể xử lý câu hỏi này. Tôi đã ghi nhận và sẽ chuyển cho quản trị viên.",
                    usedProvider, usedModel, attempted);
        }
        AgentResult parsed = parseStructured(finalContent, tools, user, usedProvider, usedModel,
                requestId, totalLatency, attempted);
        log.info("commerce-agent done intent={} confidence={} needAdmin={} provider={} model={} requestId={} latencyMs={}",
                parsed.intent(), parsed.confidence(), parsed.needAdminSupport(),
                usedProvider, usedModel, requestId, totalLatency);
        return parsed;
    }

    // ---- system prompt: skills as prompt sections ----
    private String buildSystemPrompt() {
        StringBuilder policies = new StringBuilder();
        try {
            for (var k : backend.allPolicies()) {
                policies.append("[").append(nullSafe(k.getCategory())).append("] ")
                        .append(nullSafe(k.getTitle())).append(": ")
                        .append(nullSafe(k.getContent())).append("\n");
            }
        } catch (Exception ignored) {}
        if (policies.length() > 6000) policies.setLength(6000);

        return "Bạn là trợ lý AI chăm sóc khách hàng cho website bán hàng thú cưng (PetShop). "
                + "Trả lời tiếng Việt, lịch sự, ngắn gọn.\n\n"
                + "KỸ NĂNG (skills):\n"
                + "- product_discovery: dùng searchProducts để tìm sản phẩm theo nhu cầu.\n"
                + "- product_details: dùng getProductDetails khi khách hỏi chi tiết 1 sản phẩm.\n"
                + "- comparison: dùng compareProducts (2-4 id) khi khách muốn so sánh.\n"
                + "- recommendations: dùng recommendProducts kèm lý do 'why'.\n"
                + "- order_support: dùng getOrderStatus cho đơn của chính khách đã đăng nhập.\n"
                + "- policy_faq: dùng searchPolicies cho đổi trả/hoàn tiền/vận chuyển/thanh toán.\n\n"
                + "QUY TẮC BẮT BUỘC:\n"
                + "1. Chỉ nêu sản phẩm/giá/tồn kho có trong kết quả tool. Không bịa.\n"
                + "2. Không xác nhận thanh toán/hoàn tiền/hủy đơn. Cần admin thì nêu rõ sẽ chuyển cho quản trị viên.\n"
                + "3. Không yêu cầu mật khẩu/OTP/token. Không tiết lộ đơn của người khác.\n"
                + "4. Guest hỏi đơn hàng: hướng dẫn đăng nhập, không gọi getOrderStatus.\n"
                + "5. Nếu thiếu dữ liệu, nói rõ chưa đủ thông tin.\n"
                + "6. Luôn trả về JSON hợp lệ duy nhất, không thêm chữ ngoài JSON:\n"
                + "{\"answer\":\"...\",\"intent\":\"PRODUCT_ADVICE|ORDER_STATUS|PAYMENT|SHIPPING|RETURN_REFUND|WARRANTY|ACCOUNT|FAQ|COMPLAINT|UNKNOWN\","
                + "\"confidence\":0.0,\"needAdminSupport\":false,\"suggestedAdminNote\":\"\",\"relatedProductIds\":[],\"relatedOrderId\":null}\n\n"
                + "=== CHÍNH SÁCH & FAQ (fenced, chỉ đọc) ===\n" + policies;
    }

    private AgentResult parseStructured(String content, CommerceTools tools, User user,
                                        String usedProvider, String usedModel, String requestId,
                                        long latencyMs, List<String> attempted) {
        String c = content.trim();
        if (c.startsWith("```")) {
            c = c.replaceFirst("(?s)^```(?:json)?", "").replaceFirst("```\\s*$", "").trim();
        }
        int brace = c.indexOf('{');
        if (brace > 0) c = c.substring(brace);
        try {
            JsonObject o = JsonParser.parseString(c).getAsJsonObject();
            String answer = o.has("answer") && !o.get("answer").isJsonNull()
                    ? o.get("answer").getAsString() : null;
            if (answer == null || answer.isBlank()) throw new IllegalArgumentException("missing answer");
            String intent = o.has("intent") ? o.get("intent").getAsString() : "UNKNOWN";
            double conf = o.has("confidence") ? o.get("confidence").getAsDouble() : 0.7;
            boolean needAdmin = o.has("needAdminSupport") && o.get("needAdminSupport").getAsBoolean();
            String note = o.has("suggestedAdminNote") && !o.get("suggestedAdminNote").isJsonNull()
                    ? o.get("suggestedAdminNote").getAsString() : "";
            List<Integer> pids = new ArrayList<>();
            if (o.has("relatedProductIds") && o.get("relatedProductIds").isJsonArray()) {
                JsonArray arr = o.getAsJsonArray("relatedProductIds");
                // Provenance gate: only ids returned by tools this session are honored.
                for (var el : arr) {
                    try {
                        int id = el.getAsInt();
                        if (tools.getSeenProductIds().contains(id)) pids.add(id);
                    } catch (Exception ignored) {}
                }
            }
            Integer oid = null;
            if (o.has("relatedOrderId") && !o.get("relatedOrderId").isJsonNull()) {
                try {
                    int id = o.get("relatedOrderId").getAsInt();
                    if (tools.getSeenOrderId() != null && tools.getSeenOrderId() == id) oid = id;
                } catch (Exception ignored) {}
            }
            List<Product> products = new ArrayList<>();
            for (int id : pids) {
                Product p = backend.getProductDetails(id);
                if (p != null && p.isActive() && p.getStock() > 0) products.add(p);
            }
            Order order = null;
            if (oid != null && user != null) {
                order = backend.getOrder(PetShopCommerceBackend.SessionContext.of(user), oid);
                if (order == null) oid = null;
            }
            return new AgentResult(answer, intent, conf, needAdmin, note, pids, oid,
                    products, order, usedProvider, usedModel, requestId, latencyMs, attempted);
        } catch (Exception e) {
            log.error("AI_RESPONSE_PARSE_ERROR: {}", content.length() > 500 ? content.substring(0, 500) : content, e);
            return fallback("Tôi chưa thể xử lý câu hỏi này ngay lúc này. Tôi đã ghi nhận yêu cầu và sẽ chuyển cho quản trị viên hỗ trợ thêm.",
                    usedProvider, usedModel, attempted);
        }
    }

    private AgentResult fallback(String msg, String provider, String model, List<String> attempted) {
        return new AgentResult(msg, "UNKNOWN", 0.0, true, "AI error / fallback",
                List.of(), null, List.of(), null, provider, model, "-", 0, attempted);
    }

    private AgentResult guestOrderRefusal() {
        return new AgentResult(
                "Để kiểm tra đơn hàng, bạn vui lòng đăng nhập vào tài khoản đã dùng để đặt hàng. "
                        + "Sau khi đăng nhập, tôi có thể hỗ trợ kiểm tra trạng thái đơn hàng của bạn.",
                "ORDER_STATUS", 1.0, false, "", List.of(), null, List.of(), null,
                AiConfig.provider(), AiConfig.model(), "local-guard", 0, List.of());
    }

    static boolean looksLikeOrderInquiry(String message) {
        if (message == null) return false;
        String n = message.toLowerCase(Locale.ROOT);
        return n.contains("đơn hàng") || n.contains("don hang") || n.contains("kiểm tra đơn")
                || n.contains("order") || n.contains("vận đơn");
    }

    public String aiSupportEnabled() {
        try {
            return settingDAO.getSetting("AI_SUPPORT_ENABLED", "true");
        } catch (Exception e) {
            return "true";
        }
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}
