package controller.admin;

import DAO.AiSupportSettingDAO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import Model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ai.common.AppEventBus;
import services.ai.common.AuditLog;
import services.ai.common.DbMemoryStore;
import services.ai.common.MemoryService;
import services.ai.merchant.MerchantAgent;
import services.ai.merchant.MerchantChangeDAO;
import services.ai.merchant.MerchantConfig;
import services.ai.merchant.PetShopMerchantBackend;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Merchant back-office agent surface (port of the merchant portal +
 * approval surface): conversational assistant, staged-change approval queue,
 * morning digest, and memory lifecycle. Every write applies only after host
 * approval here — a chat approval sets nothing.
 */
public class AdminMerchantAgentServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AdminMerchantAgentServlet.class);
    private final MerchantAgent agent = new MerchantAgent();
    private final PetShopMerchantBackend backend = new PetShopMerchantBackend();
    private final MerchantChangeDAO changeDAO = new MerchantChangeDAO();
    private final MemoryService memory = new MemoryService(new DbMemoryStore());
    private final AiSupportSettingDAO settingDAO = new AiSupportSettingDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();
        response.setContentType("application/json;charset=UTF-8");
        if ("/admin/ai-merchant".equals(path)) {
            try {
                request.getRequestDispatcher("/pages/admin/ai-merchant.jsp").forward(request, response);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }
        switch (path) {
            case "/admin/ai-merchant/pending" -> response.getWriter().write(gson.toJson(changeDAO.pendingJson()));
            case "/admin/ai-merchant/digest" -> {
                JsonObject o = new JsonObject();
                o.addProperty("digest", agent.digest());
                response.getWriter().write(gson.toJson(o));
            }
            case "/admin/ai-merchant/escalations" -> {
                JsonArray arr = new JsonArray();
                for (AppEventBus.AppEvent ev : AppEventBus.peek("merchant:queue")) {
                    JsonObject e = new JsonObject();
                    e.addProperty("type", ev.type());
                    e.addProperty("payload", ev.payload());
                    e.addProperty("timestamp", ev.timestamp());
                    arr.add(e);
                }
                JsonObject o = new JsonObject();
                o.add("escalations", arr);
                response.getWriter().write(gson.toJson(o));
            }
            case "/admin/ai-merchant/memory" -> {
                String subject = request.getParameter("subject");
                if (subject == null || subject.isBlank()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Missing subject (e.g. user:123)\"}");
                    return;
                }
                JsonObject o = new JsonObject();
                o.add("facts", memory.factsJson(subject));
                response.getWriter().write(gson.toJson(o));
            }
            default -> {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Unknown endpoint\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();
        response.setContentType("application/json;charset=UTF-8");
        HttpSession session = request.getSession();
        User admin = (User) session.getAttribute("user");
        String operator = admin == null ? "admin" : ("admin:" + admin.getId());

        JsonObject body = readJson(request);
        switch (path) {
            case "/admin/ai-merchant/chat" -> {
                String message = body.has("message") ? body.get("message").getAsString() : "";
                if (message.isBlank()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Message is required\"}");
                    return;
                }
                @SuppressWarnings("unchecked")
                List<String> history = (List<String>) session.getAttribute("merchant_chat_history");
                if (history == null) history = new ArrayList<>();
                MerchantAgent.MerchantResult result = agent.run(message, last(history, 6), operator);
                history.add("Operator: " + message);
                history.add("Assistant: " + result.answer());
                while (history.size() > 12) history.remove(0);
                session.setAttribute("merchant_chat_history", history);
                JsonObject o = new JsonObject();
                o.addProperty("answer", result.answer());
                o.addProperty("provider", result.usedProvider());
                o.addProperty("model", result.usedModel());
                o.add("cards", result.cards());
                response.getWriter().write(gson.toJson(o));
            }
            case "/admin/ai-merchant/approve" -> {
                String changeId = str(body, "changeId");
                if (changeId.isEmpty()) return;
                changeDAO.markApproved(changeId, operator);
                AuditLog.record("merchant", "approve", operator, null, changeId, "", "", "", 0);
                log.info("merchant change approved id={} by={}", changeId, operator);
                JsonObject o = new JsonObject();
                o.addProperty("changeId", changeId);
                o.addProperty("approved", true);
                response.getWriter().write(gson.toJson(o));
            }
            case "/admin/ai-merchant/apply" -> {
                String changeId = str(body, "changeId");
                if (changeId.isEmpty()) return;
                try {
                    backend.apply(changeId, operator, true);
                    AuditLog.record("merchant", "apply", operator, null, changeId, "", "", "", 0);
                    JsonObject o = new JsonObject();
                    o.addProperty("changeId", changeId);
                    o.addProperty("applied", true);
                    response.getWriter().write(gson.toJson(o));
                } catch (Exception e) {
                    log.warn("merchant apply refused id={}", changeId, e);
                    response.setStatus(422); // Unprocessable Entity (guardrail/approval refusal)
                    JsonObject o = new JsonObject();
                    o.addProperty("error", e.getMessage());
                    response.getWriter().write(gson.toJson(o));
                }
            }
            case "/admin/ai-merchant/discard" -> {
                String changeId = str(body, "changeId");
                if (changeId.isEmpty()) return;
                backend.discard(changeId, operator);
                AuditLog.record("merchant", "discard", operator, null, changeId, "", "", "", 0);
                JsonObject o = new JsonObject();
                o.addProperty("changeId", changeId);
                o.addProperty("discarded", true);
                response.getWriter().write(gson.toJson(o));
            }
            default -> {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Unknown endpoint\"}");
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        if ("/admin/ai-merchant/memory".equals(request.getServletPath())) {
            String subject = request.getParameter("subject");
            String key = request.getParameter("key");
            DbMemoryStore store = new DbMemoryStore();
            JsonObject o = new JsonObject();
            if (key != null && !key.isBlank()) {
                o.addProperty("deleted", store.deleteFact(subject, key));
            } else {
                store.clear(subject);
                o.addProperty("purged", true);
            }
            response.getWriter().write(gson.toJson(o));
            return;
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private JsonObject readJson(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            StringBuilder raw = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) raw.append(line);
            if (!raw.toString().isBlank()) return JsonParser.parseString(raw.toString()).getAsJsonObject();
        } catch (Exception ignored) {}
        return new JsonObject();
    }

    private String str(JsonObject body, String key) {
        try {
            return body.has(key) && !body.get(key).isJsonNull() ? body.get(key).getAsString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static List<String> last(List<String> history, int n) {
        if (history.size() <= n) return new ArrayList<>(history);
        return new ArrayList<>(history.subList(history.size() - n, history.size()));
    }
}
