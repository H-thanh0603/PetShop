package com.petshop.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import DAO.AiChatMessageDAO;
import DAO.AiChatSessionDAO;
import DAO.AiSupportSettingDAO;
import DAO.CustomerSupportKnowledgeDAO;
import DAO.NotificationDAO;
import Model.AiChatMessage;
import Model.AiChatSession;
import Model.CustomerSupportKnowledge;
import Model.User;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces AdminAiSupportServlet (/admin/ai-support*) 1:1 — same dashboard
 * metrics, session list/detail, admin reply/close, knowledge CRUD, settings.
 * POST paths keep the servlet's explicit admin-role check (belt and braces
 * on top of AuthorizationFilter).
 */
@Controller
public class AdminAiSupportController {

    private final AiChatSessionDAO sessionDAO;
    private final AiChatMessageDAO messageDAO;
    private final CustomerSupportKnowledgeDAO knowledgeDAO;
    private final AiSupportSettingDAO settingDAO;
    private final NotificationDAO notificationDAO;
    private final Gson gson = new Gson();

    public AdminAiSupportController() {
        this(new AiChatSessionDAO(), new AiChatMessageDAO(),
                new CustomerSupportKnowledgeDAO(), new AiSupportSettingDAO(), new NotificationDAO());
    }

    AdminAiSupportController(AiChatSessionDAO sessionDAO, AiChatMessageDAO messageDAO,
                             CustomerSupportKnowledgeDAO knowledgeDAO, AiSupportSettingDAO settingDAO,
                             NotificationDAO notificationDAO) {
        this.sessionDAO = sessionDAO;
        this.messageDAO = messageDAO;
        this.knowledgeDAO = knowledgeDAO;
        this.settingDAO = settingDAO;
        this.notificationDAO = notificationDAO;
    }

    @GetMapping("/admin/ai-support")
    public String aiSupportPage() {
        return "pages/admin/ai-support";
    }

    @GetMapping(value = "/admin/ai-support/dashboard", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String dashboard() {
        // Compute dashboard metrics
        List<AiChatSession> allSessions = sessionDAO.getSessionsForAdmin();
        int totalChatsToday = 0;
        int needAdminSupportCount = 0;
        int answeredByAI = 0;

        // Basic today start boundary
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        long todayStart = cal.getTimeInMillis();

        Map<String, Integer> intentCount = new HashMap<>();

        for (AiChatSession s : allSessions) {
            if (s.getCreatedAt().getTime() >= todayStart) {
                totalChatsToday++;
                if (s.isNeedAdminSupport()) {
                    needAdminSupportCount++;
                } else {
                    answeredByAI++;
                }
            }

            // Fetch intents for statistics
            List<AiChatMessage> msgs = messageDAO.getMessagesBySessionId(s.getId());
            for (AiChatMessage m : msgs) {
                if ("AI".equals(m.getSenderType()) && m.getIntent() != null) {
                    intentCount.put(m.getIntent(), intentCount.getOrDefault(m.getIntent(), 0) + 1);
                }
            }
        }

        List<Map<String, Object>> topIntents = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : intentCount.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("intent", entry.getKey());
            map.put("count", entry.getValue());
            topIntents.add(map);
        }
        // Sort by count desc
        topIntents.sort((a, b) -> Integer.compare((Integer) b.get("count"), (Integer) a.get("count")));

        Map<String, Object> data = new HashMap<>();
        data.put("totalChatsToday", totalChatsToday);
        data.put("needAdminSupport", needAdminSupportCount);
        data.put("answeredByAI", answeredByAI);
        data.put("topIntents", topIntents);

        return gson.toJson(data);
    }

    @GetMapping(value = "/admin/ai-support/sessions", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String sessions(@RequestParam(value = "filter", required = false) String filter) {
        List<AiChatSession> sessions;
        if ("waiting".equals(filter)) {
            sessions = sessionDAO.getWaitingAdminSessions();
        } else {
            sessions = sessionDAO.getSessionsForAdmin();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (AiChatSession s : sessions) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("displayName", s.getDisplayName());
            map.put("status", s.getStatus());
            map.put("needAdminSupport", s.isNeedAdminSupport());
            map.put("createdAt", s.getCreatedAt().toString());

            List<AiChatMessage> msgs = messageDAO.getRecentMessagesBySessionId(s.getId(), 1);
            String lastMsg = msgs.isEmpty() ? "" : msgs.get(0).getMessage();
            map.put("lastMessage", lastMsg);
            list.add(map);
        }
        return gson.toJson(list);
    }

    @GetMapping(value = "/admin/ai-support/sessions/detail", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String sessionDetail(@RequestParam(value = "sessionId", required = false) String sessIdStr) {
        if (sessIdStr != null && !sessIdStr.isEmpty()) {
            int sessionId = Integer.parseInt(sessIdStr);
            List<AiChatMessage> messages = messageDAO.getMessagesBySessionId(sessionId);
            return gson.toJson(messages);
        }
        return "[]";
    }

    @GetMapping(value = "/admin/ai-support/knowledge", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String knowledge() {
        List<CustomerSupportKnowledge> list = knowledgeDAO.getAll();
        return gson.toJson(list);
    }

    @GetMapping(value = "/admin/ai-support/settings", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String settings() {
        Map<String, String> map = settingDAO.getAllSettings();
        return gson.toJson(map);
    }

    @PostMapping(value = "/admin/ai-support/sessions/reply", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String reply(
            @RequestParam(value = "sessionId", required = false) String sessionIdRaw,
            @RequestParam(value = "message", required = false) String message,
            HttpSession httpSession,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {
        if (!isAdmin(httpSession)) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            return "{\"error\":\"Forbidden. Access denied.\"}";
        }

        int sessionId = Integer.parseInt(sessionIdRaw);

        if (message == null || message.trim().isEmpty()) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
            return "{\"error\":\"Message is required\"}";
        }

        // Save admin reply
        AiChatMessage msg = new AiChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderType("ADMIN");
        msg.setMessage(message);
        messageDAO.create(msg);

        // Update session status to ANSWERED_BY_ADMIN and turn off needAdminSupport flag
        sessionDAO.updateStatus(sessionId, "ANSWERED_BY_ADMIN", false);

        // Notify user
        AiChatSession chatSession = sessionDAO.getById(sessionId);
        if (chatSession != null && chatSession.getUserId() != null) {
            notificationDAO.create(
                chatSession.getUserId(),
                "Tin nhắn mới từ hỗ trợ viên",
                "Yêu cầu hỗ trợ của bạn đã có phản hồi mới từ quản trị viên.",
                "chat",
                ""
            );
        }

        return "{\"success\":true}";
    }

    @PostMapping(value = "/admin/ai-support/sessions/close", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String close(
            @RequestParam(value = "sessionId", required = false) String sessionIdRaw,
            HttpSession httpSession,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {
        if (!isAdmin(httpSession)) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            return "{\"error\":\"Forbidden. Access denied.\"}";
        }
        int sessionId = Integer.parseInt(sessionIdRaw);
        sessionDAO.updateStatus(sessionId, "CLOSED", false);
        return "{\"success\":true}";
    }

    @PostMapping(value = "/admin/ai-support/knowledge", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String knowledgePost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "id", required = false) String idRaw,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "isActive", required = false) String isActiveRaw,
            HttpSession httpSession,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {
        if (!isAdmin(httpSession)) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            return "{\"error\":\"Forbidden. Access denied.\"}";
        }

        if ("create".equals(action)) {
            boolean isActive = Boolean.parseBoolean(isActiveRaw);

            CustomerSupportKnowledge item = new CustomerSupportKnowledge();
            item.setTitle(title);
            item.setCategory(category);
            item.setContent(content);
            item.setActive(isActive);

            boolean success = knowledgeDAO.create(item);
            return "{\"success\":" + success + "}";

        } else if ("update".equals(action)) {
            int id = Integer.parseInt(idRaw);
            boolean isActive = Boolean.parseBoolean(isActiveRaw);

            CustomerSupportKnowledge item = new CustomerSupportKnowledge();
            item.setId(id);
            item.setTitle(title);
            item.setCategory(category);
            item.setContent(content);
            item.setActive(isActive);

            boolean success = knowledgeDAO.update(item);
            return "{\"success\":" + success + "}";

        } else if ("delete".equals(action)) {
            int id = Integer.parseInt(idRaw);
            boolean success = knowledgeDAO.delete(id);
            return "{\"success\":" + success + "}";
        }
        return "{\"success\":false}";
    }

    @PostMapping(value = "/admin/ai-support/settings", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String settingsPost(
            @RequestParam(value = "settingKey", required = false) String key,
            @RequestParam(value = "settingValue", required = false) String val,
            HttpSession httpSession,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {
        if (!isAdmin(httpSession)) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            return "{\"error\":\"Forbidden. Access denied.\"}";
        }
        // Update settings
        boolean success = settingDAO.updateSetting(key, val);
        return "{\"success\":" + success + "}";
    }

    private boolean isAdmin(HttpSession httpSession) {
        User adminUser = (User) httpSession.getAttribute("user");
        return adminUser != null && "admin".equals(adminUser.getRole());
    }
}
