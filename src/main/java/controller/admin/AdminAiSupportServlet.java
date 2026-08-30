package controller.admin;

import DAO.AiChatMessageDAO;
import DAO.AiChatSessionDAO;
import DAO.AiSupportSettingDAO;
import DAO.CustomerSupportKnowledgeDAO;
import Model.AiChatMessage;
import Model.AiChatSession;
import Model.CustomerSupportKnowledge;
import Model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
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

public class AdminAiSupportServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AdminAiSupportServlet.class);
    private final AiChatSessionDAO sessionDAO = new AiChatSessionDAO();
    private final AiChatMessageDAO messageDAO = new AiChatMessageDAO();
    private final CustomerSupportKnowledgeDAO knowledgeDAO = new CustomerSupportKnowledgeDAO();
    private final AiSupportSettingDAO settingDAO = new AiSupportSettingDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        // Render view for the main page
        if ("/admin/ai-support".equals(path)) {
            request.getRequestDispatcher("/pages/admin/ai-support.jsp").forward(request, response);
            return;
        }

        response.setContentType("application/json;charset=UTF-8");

        if ("/admin/ai-support/dashboard".equals(path)) {
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

            response.getWriter().write(gson.toJson(data));

        } else if ("/admin/ai-support/sessions".equals(path)) {
            String filter = request.getParameter("filter"); // all or waiting
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
            response.getWriter().write(gson.toJson(list));

        } else if ("/admin/ai-support/sessions/detail".equals(path)) {
            String sessIdStr = request.getParameter("sessionId");
            if (sessIdStr != null && !sessIdStr.isEmpty()) {
                int sessionId = Integer.parseInt(sessIdStr);
                List<AiChatMessage> messages = messageDAO.getMessagesBySessionId(sessionId);
                response.getWriter().write(gson.toJson(messages));
            }

        } else if ("/admin/ai-support/knowledge".equals(path)) {
            List<CustomerSupportKnowledge> list = knowledgeDAO.getAll();
            response.getWriter().write(gson.toJson(list));

        } else if ("/admin/ai-support/settings".equals(path)) {
            Map<String, String> map = settingDAO.getAllSettings();
            response.getWriter().write(gson.toJson(map));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        response.setContentType("application/json;charset=UTF-8");

        HttpSession httpSession = request.getSession();
        User adminUser = (User) httpSession.getAttribute("user");
        
        // Safety check
        if (adminUser == null || !"admin".equals(adminUser.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Forbidden. Access denied.\"}");
            return;
        }

        if ("/admin/ai-support/sessions/reply".equals(path)) {
            int sessionId = Integer.parseInt(request.getParameter("sessionId"));
            String message = request.getParameter("message");

            if (message == null || message.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Message is required\"}");
                return;
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
                new DAO.NotificationDAO().create(
                    chatSession.getUserId(),
                    "Tin nhắn mới từ hỗ trợ viên",
                    "Yêu cầu hỗ trợ của bạn đã có phản hồi mới từ quản trị viên.",
                    "chat",
                    ""
                );
            }

            response.getWriter().write("{\"success\":true}");

        } else if ("/admin/ai-support/sessions/close".equals(path)) {
            int sessionId = Integer.parseInt(request.getParameter("sessionId"));
            sessionDAO.updateStatus(sessionId, "CLOSED", false);
            response.getWriter().write("{\"success\":true}");

        } else if ("/admin/ai-support/knowledge".equals(path)) {
            String action = request.getParameter("action");
            
            if ("create".equals(action)) {
                String title = request.getParameter("title");
                String category = request.getParameter("category");
                String content = request.getParameter("content");
                boolean isActive = Boolean.parseBoolean(request.getParameter("isActive"));

                CustomerSupportKnowledge item = new CustomerSupportKnowledge();
                item.setTitle(title);
                item.setCategory(category);
                item.setContent(content);
                item.setActive(isActive);

                boolean success = knowledgeDAO.create(item);
                response.getWriter().write("{\"success\":" + success + "}");
                
            } else if ("update".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                String title = request.getParameter("title");
                String category = request.getParameter("category");
                String content = request.getParameter("content");
                boolean isActive = Boolean.parseBoolean(request.getParameter("isActive"));

                CustomerSupportKnowledge item = new CustomerSupportKnowledge();
                item.setId(id);
                item.setTitle(title);
                item.setCategory(category);
                item.setContent(content);
                item.setActive(isActive);

                boolean success = knowledgeDAO.update(item);
                response.getWriter().write("{\"success\":" + success + "}");

            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                boolean success = knowledgeDAO.delete(id);
                response.getWriter().write("{\"success\":" + success + "}");
            }

        } else if ("/admin/ai-support/settings".equals(path)) {
            // Update settings
            String key = request.getParameter("settingKey");
            String val = request.getParameter("settingValue");
            
            boolean success = settingDAO.updateSetting(key, val);
            response.getWriter().write("{\"success\":" + success + "}");
        }
    }
}
