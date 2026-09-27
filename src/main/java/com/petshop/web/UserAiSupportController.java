package com.petshop.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import DAO.AiChatMessageDAO;
import DAO.AiChatSessionDAO;
import DAO.AiSupportSettingDAO;
import Model.AiChatMessage;
import Model.AiChatSession;
import Model.User;
import Util.Json;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import services.DeepSeekService;

/**
 * Replaces UserAiSupportServlet (/ai-support/*) 1:1 — same session ownership
 * checks, same daily/session quotas, same DeepSeek + commerce-agent flows.
 * /ai-support/stream emits SSE delta/done events like the servlet.
 */
@Controller
public class UserAiSupportController {

    private static final Logger log = LoggerFactory.getLogger(UserAiSupportController.class);

    private final AiChatSessionDAO sessionDAO;
    private final AiChatMessageDAO messageDAO;
    private final AiSupportSettingDAO settingDAO;
    private final DeepSeekService deepSeekService;

    public UserAiSupportController() {
        this(new AiChatSessionDAO(), new AiChatMessageDAO(), new AiSupportSettingDAO(), new DeepSeekService());
    }

    UserAiSupportController(AiChatSessionDAO sessionDAO, AiChatMessageDAO messageDAO,
                            AiSupportSettingDAO settingDAO, DeepSeekService deepSeekService) {
        this.sessionDAO = sessionDAO;
        this.messageDAO = messageDAO;
        this.settingDAO = settingDAO;
        this.deepSeekService = deepSeekService;
    }

    @GetMapping(value = "/ai-support/history", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String history(HttpSession httpSession) {
        User user = (User) httpSession.getAttribute("user");
        List<AiChatSession> sessions = new ArrayList<>();
        if (user != null) {
            sessions = sessionDAO.getSessionsByUserId(user.getId());
        } else {
            Integer guestSessionId = (Integer) httpSession.getAttribute("guest_chat_session_id");
            if (guestSessionId != null) {
                AiChatSession gs = sessionDAO.getById(guestSessionId);
                if (gs != null) {
                    sessions.add(gs);
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (AiChatSession s : sessions) {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", s.getId());
            map.put("status", s.getStatus());
            map.put("needAdminSupport", s.isNeedAdminSupport());
            map.put("createdAt", s.getCreatedAt().toString());

            // Get the last message of this session
            List<AiChatMessage> msgs = messageDAO.getRecentMessagesBySessionId(s.getId(), 1);
            String lastMsg = msgs.isEmpty() ? "Bắt đầu cuộc trò chuyện" : msgs.get(0).getMessage();
            map.put("lastMessage", lastMsg);
            result.add(map);
        }
        return Json.MAPPER.writeValueAsString(result);
    }

    @GetMapping(value = "/ai-support/messages", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String messages(
            @RequestParam(value = "sessionId", required = false) String sessIdStr,
            HttpSession httpSession,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {
        User user = (User) httpSession.getAttribute("user");
        if (sessIdStr == null || sessIdStr.isEmpty()) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
            return "{\"error\":\"Missing sessionId\"}";
        }

        try {
            int sessionId = Integer.parseInt(sessIdStr);
            AiChatSession chatSession = sessionDAO.getById(sessionId);

            if (chatSession == null) {
                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND);
                return "{\"error\":\"Session not found\"}";
            }

            // Security Check: Verify ownership of session
            if (!ownsSession(httpSession, user, chatSession, sessionId)) {
                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                return "{\"error\":\"Forbidden.\"}";
            }

            messageDAO.markMessagesAsRead(sessionId, "ADMIN");
            messageDAO.markMessagesAsRead(sessionId, "AI");
            List<AiChatMessage> messages = messageDAO.getMessagesBySessionId(sessionId);
            return Json.MAPPER.writeValueAsString(messages);
        } catch (NumberFormatException e) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
            return "{\"error\":\"Invalid sessionId format\"}";
        }
    }

    @GetMapping(value = "/ai-support/unread-count", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String unreadCount(
            @RequestParam(value = "sessionId", required = false) String sessIdStr,
            HttpSession httpSession,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {
        User user = (User) httpSession.getAttribute("user");
        int unreadCount = 0;
        if (sessIdStr != null && !sessIdStr.isEmpty()) {
            try {
                int sessionId = Integer.parseInt(sessIdStr);
                AiChatSession chatSession = sessionDAO.getById(sessionId);
                if (chatSession != null) {
                    if (!ownsSession(httpSession, user, chatSession, sessionId)) {
                        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                        return "{\"error\":\"Forbidden.\"}";
                    }
                    unreadCount = messageDAO.getUnreadCountBySessionId(sessionId, "ADMIN");
                }
            } catch (NumberFormatException ignored) {}
        }
        ObjectNode result = Json.MAPPER.createObjectNode();
        result.put("unreadCount", unreadCount);
        return Json.MAPPER.writeValueAsString(result);
    }

    @PostMapping(value = "/ai-support/chat", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String chat(
            @RequestBody(required = false) String rawBody,
            HttpServletRequest request,
            HttpSession httpSession,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {
        User user = (User) httpSession.getAttribute("user");

        // Parse request body or parameters (same contract as the servlet)
        int sessionId = 0;
        String message = "";

        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            try {
                ObjectNode reqJson;
                if (rawBody == null || rawBody.isBlank()) {
                    reqJson = Json.MAPPER.createObjectNode();
                } else {
                    JsonNode parsed = Json.MAPPER.readTree(rawBody);
                    if (parsed == null || !parsed.isObject()) {
                        throw new IllegalArgumentException("Payload không phải JSON object.");
                    }
                    reqJson = (ObjectNode) parsed;
                }
                if (reqJson.has("sessionId") && !reqJson.get("sessionId").isNull()) {
                    sessionId = reqJson.path("sessionId").asInt();
                }
                if (reqJson.has("message")) {
                    message = reqJson.path("message").asString();
                }
            } catch (Exception e) {
                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
                return "{\"error\":\"Invalid JSON payload\"}";
            }
        } else {
            String sessIdStr = request.getParameter("sessionId");
            if (sessIdStr != null && !sessIdStr.isEmpty()) {
                try {
                    sessionId = Integer.parseInt(sessIdStr);
                } catch (NumberFormatException ignored) {}
            }
            message = request.getParameter("message");
        }

        if (message == null || message.trim().isEmpty()) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
            return "{\"error\":\"Message content is required\"}";
        }

        // Verify message length limit
        int maxLength = Integer.parseInt(settingDAO.getSetting("MAX_MESSAGE_LENGTH", "1000"));
        if (message.length() > maxLength) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
            return "{\"error\":\"Message is too long. Limit: " + maxLength + " characters.\"}";
        }

        // Cost control: daily per-user agent turn budget (guests: per-session cap below).
        if (user != null) {
            int dailyCap = Integer.parseInt(settingDAO.getSetting("AI_MAX_TURNS_PER_DAY", "100"));
            try {
                if (messageDAO.countUserMessagesToday(user.getId()) >= dailyCap) {
                    response.setStatus(429); // Too Many Requests (quota guard)
                    return "{\"error\":\"Daily AI quota reached. Please try again tomorrow.\"}";
                }
            } catch (Exception ignored) {}
        }

        // Get or create session
        AiChatSession chatSession = null;
        if (sessionId > 0) {
            chatSession = sessionDAO.getById(sessionId);
            // Security Check: Verify ownership
            if (chatSession != null && !ownsSession(httpSession, user, chatSession, sessionId)) {
                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                return "{\"error\":\"Forbidden.\"}";
            }
        }

        if (chatSession == null) {
            chatSession = new AiChatSession();
            if (user != null) {
                chatSession.setUserId(user.getId());
            } else {
                chatSession.setGuestName("Guest");
            }
            chatSession.setStatus("OPEN");
            chatSession.setNeedAdminSupport(false);
            int newSessionId = sessionDAO.create(chatSession);
            chatSession.setId(newSessionId);
            sessionId = newSessionId;

            // If guest, save in HTTP session
            if (user == null) {
                httpSession.setAttribute("guest_chat_session_id", newSessionId);
            }
        }

        // Save user message to database
        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setSenderType("USER");
        userMsg.setMessage(message);
        messageDAO.create(userMsg);

        // Fetch recent messages for memory context (e.g., last 10 messages)
        List<AiChatMessage> history = messageDAO.getRecentMessagesBySessionId(sessionId, 10);
        // Exclude the last message we just added since it's passed as current message
        if (!history.isEmpty()) {
            history.remove(history.size() - 1);
        }

        // Guest per-session cap (cost control alongside the user daily quota).
        try {
            int sessionCap = Integer.parseInt(settingDAO.getSetting("AI_MAX_MSGS_PER_SESSION", "60"));
            if (messageDAO.countBySession(sessionId) >= sessionCap) {
                response.setStatus(429); // Too Many Requests (quota guard)
                return "{\"error\":\"This chat reached its message limit. Please start a new conversation.\"}";
            }
        } catch (Exception ignored) {}

        // Call DeepSeek Service (sessionKey keeps cross-turn provenance)
        DeepSeekService.AiResponse aiRes = deepSeekService.getChatResponse(
                message, history, user, "chat:" + sessionId);

        // Save AI message to database
        AiChatMessage aiMsg = new AiChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setSenderType("AI");
        aiMsg.setMessage(aiRes.getAnswer());
        aiMsg.setIntent(aiRes.getIntent());
        aiMsg.setConfidence(BigDecimal.valueOf(aiRes.getConfidence()));
        aiMsg.setNeedAdminSupport(aiRes.isNeedAdminSupport());
        aiMsg.setSuggestedAdminNote(aiRes.getSuggestedAdminNote());
        messageDAO.create(aiMsg);

        // Update session status / admin escalation
        String newStatus = chatSession.getStatus();
        boolean escalate = aiRes.isNeedAdminSupport();

        // If escalate is true, and setting AUTO_ESCALATE_TO_ADMIN is true, set status to WAITING_ADMIN
        boolean autoEscalate = Boolean.parseBoolean(settingDAO.getSetting("AUTO_ESCALATE_TO_ADMIN", "true"));
        if (escalate && autoEscalate) {
            newStatus = "WAITING_ADMIN";
        }

        // If chat was in ANSWERED_BY_ADMIN and customer chats again, set back to OPEN or WAITING_ADMIN
        if ("ANSWERED_BY_ADMIN".equals(chatSession.getStatus())) {
            newStatus = escalate && autoEscalate ? "WAITING_ADMIN" : "OPEN";
        }

        sessionDAO.updateStatus(sessionId, newStatus, chatSession.isNeedAdminSupport() || escalate);

        // Agent-to-agent handoff: escalations enter the merchant queue so the
        // merchant agent surfaces them (digest + admin view).
        if (escalate) {
            try {
                String who = user == null ? "guest" : "user:" + user.getId();
                services.ai.common.AppEventBus.publish("merchant:queue", "support_escalation",
                        "session=" + sessionId + " who=" + who + " intent=" + aiRes.getIntent()
                                + " note=" + aiRes.getSuggestedAdminNote());
            } catch (Exception ignored) {}
        }

        // Write JSON response (provider details are non-sensitive metadata only;
        // keys never leave the server — see services.ai.AiConfig)
        ObjectNode responseJson = Json.MAPPER.createObjectNode();
        responseJson.put("sessionId", sessionId);
        responseJson.put("answer", aiRes.getAnswer());
        responseJson.put("intent", aiRes.getIntent());
        responseJson.put("needAdminSupport", escalate);
        responseJson.put("provider", aiRes.getUsedProvider());
        responseJson.put("model", aiRes.getUsedModel());
        responseJson.put("requestId", aiRes.getRequestId());
        JsonNode cards = null;
        if (aiRes.getCardsJson() != null) {
            try {
                cards = Json.MAPPER.readTree(aiRes.getCardsJson());
            } catch (Exception ignored) {}
        }
        responseJson.set("cards", cards != null ? cards : Json.MAPPER.createArrayNode());

        // Attach related details if present
        if (aiRes.getRelatedProducts() != null) {
            responseJson.set("relatedProducts", Json.MAPPER.valueToTree(aiRes.getRelatedProducts()));
        } else {
            responseJson.set("relatedProducts", Json.MAPPER.valueToTree(new ArrayList<>()));
        }

        if (aiRes.getRelatedOrder() != null) {
            responseJson.set("relatedOrder", Json.MAPPER.valueToTree(aiRes.getRelatedOrder()));
        } else {
            responseJson.put("relatedOrder", (String) null);
        }

        return Json.MAPPER.writeValueAsString(responseJson);
    }

    /**
     * SSE endpoint (POST, same auth/session rules as /ai-support/chat).
     * Runs the full commerce-agent tool loop server-side, then emits the final
     * answer as text chunks ({@code event: delta}) followed by {@code event: done}
     * with session/provider metadata.
     */
    @PostMapping(value = "/ai-support/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter streamChat(
            @RequestBody(required = false) String rawBody,
            HttpSession httpSession) {
        SseEmitter emitter = new SseEmitter(120_000L);
        User user = (User) httpSession.getAttribute("user");

        int sessionId = 0;
        String message = "";
        try {
            if (rawBody != null && !rawBody.isBlank()) {
                JsonNode parsed = Json.MAPPER.readTree(rawBody);
                if (parsed == null || !parsed.isObject()) {
                    throw new IllegalArgumentException("Payload không phải JSON object.");
                }
                ObjectNode reqJson = (ObjectNode) parsed;
                if (reqJson.has("sessionId") && !reqJson.get("sessionId").isNull()) {
                    sessionId = reqJson.path("sessionId").asInt();
                }
                if (reqJson.has("message")) message = reqJson.path("message").asString();
            }
        } catch (Exception e) {
            sendSse(emitter, "error", "{\"error\":\"Invalid JSON payload\"}");
            emitter.complete();
            return emitter;
        }
        if (message == null || message.trim().isEmpty()) {
            sendSse(emitter, "error", "{\"error\":\"Message content is required\"}");
            emitter.complete();
            return emitter;
        }

        AiChatSession chatSession = resolveSession(httpSession, user, sessionId);
        if (chatSession == null) {
            sendSse(emitter, "error", "{\"error\":\"Forbidden.\"}");
            emitter.complete();
            return emitter;
        }
        int resolvedSessionId = chatSession.getId();
        String resolvedMessage = message;

        // Same blocking agent work as the servlet, run off the MVC thread.
        new Thread(() -> {
            try {
                AiChatMessage userMsg = new AiChatMessage();
                userMsg.setSessionId(resolvedSessionId);
                userMsg.setSenderType("USER");
                userMsg.setMessage(resolvedMessage);
                messageDAO.create(userMsg);

                List<AiChatMessage> history = messageDAO.getRecentMessagesBySessionId(resolvedSessionId, 10);
                if (!history.isEmpty()) history.remove(history.size() - 1);

                services.ai.CommerceAgent agent = new services.ai.CommerceAgent();
                services.ai.CommerceAgent.AgentResult result;
                try {
                    result = agent.run(resolvedMessage, history, user);
                } catch (Exception e) {
                    log.error("stream commerce agent failed", e);
                    sendSse(emitter, "error", "{\"error\":\"AI service temporarily unavailable\"}");
                    emitter.complete();
                    return;
                }

                AiChatMessage aiMsg = new AiChatMessage();
                aiMsg.setSessionId(resolvedSessionId);
                aiMsg.setSenderType("AI");
                aiMsg.setMessage(result.answer());
                aiMsg.setIntent(result.intent());
                aiMsg.setConfidence(BigDecimal.valueOf(result.confidence()));
                aiMsg.setNeedAdminSupport(result.needAdminSupport());
                aiMsg.setSuggestedAdminNote(result.suggestedAdminNote());
                messageDAO.create(aiMsg);

                boolean autoEscalate = Boolean.parseBoolean(settingDAO.getSetting("AUTO_ESCALATE_TO_ADMIN", "true"));
                String newStatus = chatSession.getStatus();
                if (result.needAdminSupport() && autoEscalate) newStatus = "WAITING_ADMIN";
                if ("ANSWERED_BY_ADMIN".equals(chatSession.getStatus())) {
                    newStatus = result.needAdminSupport() && autoEscalate ? "WAITING_ADMIN" : "OPEN";
                }
                sessionDAO.updateStatus(resolvedSessionId, newStatus,
                        chatSession.isNeedAdminSupport() || result.needAdminSupport());

                String answer = result.answer() == null ? "" : result.answer();
                for (int i = 0; i < answer.length(); i += 60) {
                    ObjectNode delta = Json.MAPPER.createObjectNode();
                    delta.put("text", answer.substring(i, Math.min(answer.length(), i + 60)));
                    sendSse(emitter, "delta", Json.MAPPER.writeValueAsString(delta));
                }
                ObjectNode done = Json.MAPPER.createObjectNode();
                done.put("sessionId", resolvedSessionId);
                done.put("intent", result.intent());
                done.put("needAdminSupport", result.needAdminSupport());
                done.put("provider", result.usedProvider());
                done.put("model", result.usedModel());
                sendSse(emitter, "done", Json.MAPPER.writeValueAsString(done));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    private boolean ownsSession(HttpSession httpSession, User user, AiChatSession chatSession, int sessionId) {
        if (user != null) {
            return chatSession.getUserId() != null && chatSession.getUserId() == user.getId();
        }
        Integer guestSessionId = (Integer) httpSession.getAttribute("guest_chat_session_id");
        return guestSessionId != null && guestSessionId == sessionId;
    }

    private AiChatSession resolveSession(HttpSession httpSession, User user, int sessionId) {
        AiChatSession chatSession = null;
        if (sessionId > 0) {
            chatSession = sessionDAO.getById(sessionId);
            if (chatSession != null && !ownsSession(httpSession, user, chatSession, sessionId)) {
                return null;
            }
        }
        if (chatSession == null) {
            chatSession = new AiChatSession();
            if (user != null) chatSession.setUserId(user.getId());
            else chatSession.setGuestName("Guest");
            chatSession.setStatus("OPEN");
            chatSession.setNeedAdminSupport(false);
            chatSession.setId(sessionDAO.create(chatSession));
            if (user == null) httpSession.setAttribute("guest_chat_session_id", chatSession.getId());
        }
        return chatSession;
    }

    private void sendSse(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (Exception ignored) {}
    }
}
