package controller.shop;

import DAO.AiChatMessageDAO;
import DAO.AiChatSessionDAO;
import DAO.AiSupportSettingDAO;
import Model.AiChatMessage;
import Model.AiChatSession;
import Model.User;
import services.DeepSeekService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet({"/ai-support/chat", "/ai-support/history", "/ai-support/messages", "/ai-support/unread-count"})
public class UserAiSupportServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(UserAiSupportServlet.class);
    private final AiChatSessionDAO sessionDAO = new AiChatSessionDAO();
    private final AiChatMessageDAO messageDAO = new AiChatMessageDAO();
    private final AiSupportSettingDAO settingDAO = new AiSupportSettingDAO();
    private final DeepSeekService deepSeekService = new DeepSeekService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        response.setContentType("application/json;charset=UTF-8");

        HttpSession httpSession = request.getSession();
        User user = (User) httpSession.getAttribute("user");

        if ("/ai-support/history".equals(path)) {
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
            response.getWriter().write(gson.toJson(result));
            
        } else if ("/ai-support/messages".equals(path)) {
            String sessIdStr = request.getParameter("sessionId");
            if (sessIdStr == null || sessIdStr.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Missing sessionId\"}");
                return;
            }

            try {
                int sessionId = Integer.parseInt(sessIdStr);
                AiChatSession chatSession = sessionDAO.getById(sessionId);

                if (chatSession == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\":\"Session not found\"}");
                    return;
                }

                // Security Check: Verify ownership of session
                if (user != null) {
                    if (chatSession.getUserId() == null || chatSession.getUserId() != user.getId()) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"Forbidden. This session does not belong to you.\"}");
                        return;
                    }
                } else {
                    Integer guestSessionId = (Integer) httpSession.getAttribute("guest_chat_session_id");
                    if (guestSessionId == null || guestSessionId != sessionId) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"Forbidden. Guest session mismatch.\"}");
                        return;
                    }
                }

                messageDAO.markMessagesAsRead(sessionId, "ADMIN");
                messageDAO.markMessagesAsRead(sessionId, "AI");
                List<AiChatMessage> messages = messageDAO.getMessagesBySessionId(sessionId);
                response.getWriter().write(gson.toJson(messages));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid sessionId format\"}");
            }
        } else if ("/ai-support/unread-count".equals(path)) {
            String sessIdStr = request.getParameter("sessionId");
            int unreadCount = 0;
            if (sessIdStr != null && !sessIdStr.isEmpty()) {
                try {
                    int sessionId = Integer.parseInt(sessIdStr);
                    AiChatSession chatSession = sessionDAO.getById(sessionId);
                    if (chatSession != null) {
                        // Security Check: Verify ownership
                        if (user != null) {
                            if (chatSession.getUserId() == null || chatSession.getUserId() != user.getId()) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.getWriter().write("{\"error\":\"Forbidden. This session does not belong to you.\"}");
                                return;
                            }
                        } else {
                            Integer guestSessionId = (Integer) httpSession.getAttribute("guest_chat_session_id");
                            if (guestSessionId == null || guestSessionId != sessionId) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.getWriter().write("{\"error\":\"Forbidden. Guest session mismatch.\"}");
                                return;
                            }
                        }
                        unreadCount = messageDAO.getUnreadCountBySessionId(sessionId, "ADMIN");
                    }
                } catch (NumberFormatException ignored) {}
            }
            JsonObject result = new JsonObject();
            result.addProperty("unreadCount", unreadCount);
            response.getWriter().write(gson.toJson(result));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        response.setContentType("application/json;charset=UTF-8");

        if ("/ai-support/chat".equals(path)) {
            HttpSession httpSession = request.getSession();
            User user = (User) httpSession.getAttribute("user");

            // Parse request body or parameters
            int sessionId = 0;
            String message = "";

            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                try (BufferedReader reader = request.getReader()) {
                    JsonObject reqJson = new JsonParser().parse(reader).getAsJsonObject();
                    if (reqJson.has("sessionId") && !reqJson.get("sessionId").isJsonNull()) {
                        sessionId = reqJson.get("sessionId").getAsInt();
                    }
                    if (reqJson.has("message")) {
                        message = reqJson.get("message").getAsString();
                    }
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Invalid JSON payload\"}");
                    return;
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
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Message content is required\"}");
                return;
            }

            // Verify message length limit
            int maxLength = Integer.parseInt(settingDAO.getSetting("MAX_MESSAGE_LENGTH", "1000"));
            if (message.length() > maxLength) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Message is too long. Limit: " + maxLength + " characters.\"}");
                return;
            }

            // Get or create session
            AiChatSession chatSession = null;
            if (sessionId > 0) {
                chatSession = sessionDAO.getById(sessionId);
                // Security Check: Verify ownership
                if (chatSession != null) {
                    if (user != null) {
                        if (chatSession.getUserId() == null || chatSession.getUserId() != user.getId()) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"error\":\"Forbidden. This session does not belong to you.\"}");
                            return;
                        }
                    } else {
                        Integer guestSessionId = (Integer) httpSession.getAttribute("guest_chat_session_id");
                        if (guestSessionId == null || guestSessionId != sessionId) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"error\":\"Forbidden. Guest session mismatch.\"}");
                            return;
                        }
                    }
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

            // Call DeepSeek Service
            DeepSeekService.AiResponse aiRes = deepSeekService.getChatResponse(message, history, user);

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

            // Write JSON response
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("sessionId", sessionId);
            responseJson.addProperty("answer", aiRes.getAnswer());
            responseJson.addProperty("intent", aiRes.getIntent());
            responseJson.addProperty("needAdminSupport", escalate);
            
            // Attach related details if present
            if (aiRes.getRelatedProducts() != null) {
                responseJson.add("relatedProducts", gson.toJsonTree(aiRes.getRelatedProducts()));
            } else {
                responseJson.add("relatedProducts", gson.toJsonTree(new ArrayList<>()));
            }
            
            if (aiRes.getRelatedOrder() != null) {
                responseJson.add("relatedOrder", gson.toJsonTree(aiRes.getRelatedOrder()));
            } else {
                responseJson.addProperty("relatedOrder", (String) null);
            }

            response.getWriter().write(gson.toJson(responseJson));
        }
    }
}
