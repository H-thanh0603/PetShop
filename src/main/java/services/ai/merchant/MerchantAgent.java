package services.ai.merchant;

import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ai.AiConfig;
import services.ai.AiMessage;
import services.ai.AiProviderFactory;
import services.ai.ChatRequest;
import services.ai.ChatResponse;
import services.ai.SkillLoader;
import services.ai.ToolCall;
import services.ai.common.AppEventBus;
import services.ai.common.AuditLog;
import services.ai.common.Cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Merchant agent runtime (port of merchant_agent executor + prompt): the
 * operator's back-office assistant. Reads are free; writes are staged by the
 * model and applied only through host approval. Provider-agnostic — runs on
 * any configured {@code AiProvider}.
 */
public class MerchantAgent {
    private static final Logger log = LoggerFactory.getLogger(MerchantAgent.class);

    private final PetShopMerchantBackend backend = new PetShopMerchantBackend();

    public record MerchantResult(String answer, String usedProvider, String usedModel,
                                 String requestId, long latencyMs, JsonArray cards) {}

    public MerchantResult run(String operatorMessage, List<String> history, String operator) {
        MerchantTools tools = new MerchantTools(backend, operator == null ? "operator" : operator);
        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.system(buildSystemPrompt()));
        if (history != null) {
            for (String h : history) messages.add(AiMessage.user(h));
        }
        messages.add(AiMessage.user(operatorMessage == null ? "" : operatorMessage.trim()));

        ChatRequest request = ChatRequest.builder(messages)
                .tools(tools.definitions()).temperature(0.1).maxTokens(2000).build();

        int maxSteps = AiConfig.maxToolSteps();
        String usedProvider = AiConfig.provider(), usedModel = AiConfig.model();
        String requestId = "-";
        long totalLatency = 0;
        String finalContent = null;
        JsonArray cards = new JsonArray();

        try {
            for (int step = 0; step < maxSteps; step++) {
                AiProviderFactory.FallbackResult fr = AiProviderFactory.completeWithFallback(request);
                usedProvider = fr.usedProvider();
                usedModel = fr.usedModel();
                ChatResponse resp = fr.response();
                requestId = resp.getRequestId();
                totalLatency += resp.getLatencyMs();
                log.info("merchant-agent step={} provider={} model={} requestId={} latencyMs={} toolCalls={}",
                        step, fr.usedProvider(), fr.usedModel(), resp.getRequestId(),
                        resp.getLatencyMs(), resp.getToolCalls().size());
                if (!resp.hasToolCalls()) {
                    finalContent = resp.getContent();
                    break;
                }
                request.getMessages().add(AiMessage.assistantWithTools(resp.getContent(), resp.getToolCalls()));
                for (ToolCall tc : resp.getToolCalls()) {
                    long t0 = System.currentTimeMillis();
                    String result = tools.execute(tc.getName(), tc.getArgumentsJson());
                    long toolMs = System.currentTimeMillis() - t0;
                    AuditLog.record("merchant", "tool:" + tc.getName(), operator, null,
                            result.length() > 500 ? result.substring(0, 500) : result,
                            fr.usedProvider(), fr.usedModel(), requestId, toolMs);
                    request.getMessages().add(AiMessage.toolResult(tc.getId(), tc.getName(), result));
                    // Attach change preview cards for staged changes.
                    try {
                        var parsed = com.google.gson.JsonParser.parseString(result).getAsJsonObject();
                        if (parsed.has("changeId") && parsed.has("kind")) {
                            cards.add(Cards.changePreviewCard(
                                    parsed.get("changeId").getAsString(),
                                    parsed.get("kind").getAsString(),
                                    parsed.has("summary") ? parsed.get("summary").getAsString() : "",
                                    parsed.has("items") ? parsed.getAsJsonArray("items") : new JsonArray(),
                                    List.of("Staged only — approve on "
                                            + MerchantConfig.approvalSurface())));
                        }
                    } catch (Exception ignored) {}
                }
                if (step == maxSteps - 1) {
                    ChatRequest finalReq = ChatRequest.builder(request.getMessages())
                            .temperature(0.1).maxTokens(1000).build();
                    finalContent = AiProviderFactory.completeWithFallback(finalReq).response().getContent();
                }
            }
        } catch (Exception e) {
            log.error("merchant-agent provider failure", e);
            return new MerchantResult(
                    "Trợ lý merchant hiện chưa kết nối được với dịch vụ AI. Vui lòng thử lại sau.",
                    usedProvider, usedModel, requestId, totalLatency, cards);
        }
        if (finalContent == null || finalContent.isBlank()) {
            finalContent = "Tôi chưa thể xử lý yêu cầu này. Vui lòng thử lại hoặc diễn đạt khác.";
        }
        AuditLog.record("merchant", "turn_complete", operator, null,
                "answerChars=" + finalContent.length(), usedProvider, usedModel, requestId, totalLatency);
        return new MerchantResult(finalContent, usedProvider, usedModel, requestId, totalLatency, cards);
    }

    /**
     * Morning digest port (managed-agents/scheduled-digest): snapshot +
     * alerts + issues + pending changes, no model call, safe to schedule.
     */
    public String digest() {
        StringBuilder sb = new StringBuilder("=== Merchant digest ===\n");
        try {
            var snap = backend.businessSnapshot();
            sb.append("Doanh thu tổng: ").append(snap.get("totalRevenueVnd").getAsString()).append(" VND\n");
            sb.append("Doanh thu tháng: ").append(snap.get("monthRevenueVnd").getAsString()).append(" VND\n");
            sb.append("Đơn hoàn tất: ").append(snap.get("completedOrders").getAsInt()).append("\n");
            var alerts = backend.inventoryAlerts();
            sb.append("Cảnh báo tồn kho: ").append(alerts.size()).append("\n");
            for (int i = 0; i < Math.min(alerts.size(), 5); i++) {
                var a = alerts.get(i).getAsJsonObject();
                sb.append("- ").append(a.get("name").getAsString())
                        .append(" (tồn ").append(a.get("stock").getAsInt()).append(")\n");
            }
            var issues = backend.orderIssues();
            sb.append("Đơn cần chú ý: ").append(issues.size()).append("\n");
            sb.append("Thay đổi đang chờ duyệt: ").append(backend.ledger().pending().size()).append("\n");
            var escalations = AppEventBus.peek("merchant:queue");
            sb.append("Escalation từ shopping agent: ").append(escalations.size()).append("\n");
            for (int i = 0; i < Math.min(escalations.size(), 5); i++) {
                sb.append("- ").append(escalations.get(i).payload()).append("\n");
            }
        } catch (Exception e) {
            sb.append("(digest partially unavailable: ").append(e.getMessage()).append(")\n");
        }
        return sb.toString();
    }

    private String buildSystemPrompt() {
        return "Bạn là trợ lý merchant cho quản trị viên PetShop. Trả lời tiếng Việt, "
                + "ngắn gọn, số liệu trước.\n\n"
                + SkillLoader.renderForPrompt(SkillLoader.loadRole("merchant"))
                + "QUY TẮC BẮT BUỘC:\n"
                + "1. Số liệu chỉ từ kết quả tool trong phiên này. Thiếu số liệu thì nêu rõ giới hạn, không ước đoán.\n"
                + "2. Mọi thay đổi (giá, tồn kho, nội dung, khuyến mãi) chỉ được STAGE qua tool stage_* — "
                + "staging không áp dụng gì cả. Áp dụng chỉ xảy ra khi operator duyệt trên "
                + MerchantConfig.approvalSurface() + ".\n"
                + "3. Chỉ dùng id do tool trả về trong phiên. Đọc đầy đủ (get_listing) trước khi sửa nội dung.\n"
                + "4. Giá/tồn kho của sản phẩm có biến thể được xử lý theo từng biến thể.\n"
                + "5. Trả lời dạng text, sau đó liệt kê các changeId đã stage (nếu có) để operator duyệt.\n";
    }
}
