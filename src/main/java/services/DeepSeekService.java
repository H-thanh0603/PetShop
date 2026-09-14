package services;

import DAO.AiSupportSettingDAO;
import Model.*;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ai.CommerceAgent;

import java.util.List;

public class DeepSeekService {
    private static final Logger log = LoggerFactory.getLogger(DeepSeekService.class);
    private final AiSupportSettingDAO settingDAO = new AiSupportSettingDAO();
    private final CommerceAgent commerceAgent = new CommerceAgent();
    private final Gson gson = new Gson();

    public static class AiResponse {
        private String answer;
        private String intent;
        private double confidence;
        private boolean needAdminSupport;
        private String suggestedAdminNote;
        private List<Integer> relatedProductIds;
        private Integer relatedOrderId;
        
        // Formatted products & order data to return to client
        private List<Product> relatedProducts;
        private Order relatedOrder;

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }

        public String getIntent() { return intent; }
        public void setIntent(String intent) { this.intent = intent; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public boolean isNeedAdminSupport() { return needAdminSupport; }
        public void setNeedAdminSupport(boolean needAdminSupport) { this.needAdminSupport = needAdminSupport; }

        public String getSuggestedAdminNote() { return suggestedAdminNote; }
        public void setSuggestedAdminNote(String suggestedAdminNote) { this.suggestedAdminNote = suggestedAdminNote; }

        public List<Integer> getRelatedProductIds() { return relatedProductIds; }
        public void setRelatedProductIds(List<Integer> relatedProductIds) { this.relatedProductIds = relatedProductIds; }

        public Integer getRelatedOrderId() { return relatedOrderId; }
        public void setRelatedOrderId(Integer relatedOrderId) { this.relatedOrderId = relatedOrderId; }

        public List<Product> getRelatedProducts() { return relatedProducts; }
        public void setRelatedProducts(List<Product> relatedProducts) { this.relatedProducts = relatedProducts; }

        public Order getRelatedOrder() { return relatedOrder; }
        public void setRelatedOrder(Order relatedOrder) { this.relatedOrder = relatedOrder; }

        private String cardsJson = "[]";
        public String getCardsJson() { return cardsJson; }
        public void setCardsJson(String cardsJson) { this.cardsJson = cardsJson == null ? "[]" : cardsJson; }

        private String usedProvider = "";
        private String usedModel = "";
        private String requestId = "";
        public String getUsedProvider() { return usedProvider; }
        public void setUsedProvider(String v) { this.usedProvider = v == null ? "" : v; }
        public String getUsedModel() { return usedModel; }
        public void setUsedModel(String v) { this.usedModel = v == null ? "" : v; }
        public String getRequestId() { return requestId; }
        public void setRequestId(String v) { this.requestId = v == null ? "" : v; }
    }

    /**
     * Provider-agnostic entry point (kept for backward compatibility).
     *
     * <p>Delegates to {@link CommerceAgent}, which runs the Commerce Agents-style
     * tool loop through the configured {@code AI_PROVIDER}/{@code AI_MODEL}
     * (see services.ai.AiConfig). No provider SDK is referenced here, so
     * switching providers requires only env/config changes.
     */
    public AiResponse getChatResponse(String userMessage, List<AiChatMessage> history, User user) {
        return getChatResponse(userMessage, history, user, null);
    }

    public AiResponse getChatResponse(String userMessage, List<AiChatMessage> history,
                                      User user, String sessionKey) {
        boolean enabled = Boolean.parseBoolean(settingDAO.getSetting("AI_SUPPORT_ENABLED", "true"));
        if (!enabled) {
            AiResponse fallback = new AiResponse();
            fallback.setAnswer("Trợ lý AI hiện đang tạm ngưng hoạt động. Quý khách vui lòng liên hệ hotline hoặc admin để được hỗ trợ trực tiếp.");
            fallback.setIntent("UNKNOWN");
            fallback.setNeedAdminSupport(true);
            return fallback;
        }
        try {
            CommerceAgent.AgentResult r = commerceAgent.run(userMessage, history, user, sessionKey);
            AiResponse res = new AiResponse();
            res.setAnswer(r.answer());
            res.setIntent(r.intent());
            res.setConfidence(r.confidence());
            res.setNeedAdminSupport(r.needAdminSupport());
            res.setSuggestedAdminNote(r.suggestedAdminNote());
            res.setRelatedProductIds(r.relatedProductIds());
            res.setRelatedOrderId(r.relatedOrderId());
            res.setRelatedProducts(r.relatedProducts());
            res.setRelatedOrder(r.relatedOrder());
            res.setCardsJson(r.cards() == null ? "[]" : r.cards().toString());
            res.setUsedProvider(r.usedProvider());
            res.setUsedModel(r.usedModel());
            res.setRequestId(r.requestId());
            return res;
        } catch (Exception e) {
            log.error("Commerce agent failed", e);
            AiResponse fallback = new AiResponse();
            fallback.setAnswer("Tôi chưa thể kết nối với dịch vụ AI ngay lúc này. Vui lòng liên hệ admin để được hỗ trợ.");
            fallback.setIntent("UNKNOWN");
            fallback.setNeedAdminSupport(true);
            return fallback;
        }
    }
}
