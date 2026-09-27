package com.petshop.web;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import com.petshop.model.User;
import com.petshop.util.Json;
import jakarta.servlet.http.HttpSession;
import services.ai.common.AppEventBus;
import services.ai.common.AuditLog;
import services.ai.common.DbMemoryStore;
import services.ai.common.MemoryService;
import services.ai.merchant.MerchantAgent;
import services.ai.merchant.MerchantChangeDAO;
import services.ai.merchant.PetShopMerchantBackend;

/**
 * Replaces AdminMerchantAgentServlet (/admin/ai-merchant*) 1:1 — same chat,
 * pending/digest/escalations/memory reads, same approve/apply/discard split
 * (chat approval never applies). AuthorizationFilter still guards admin paths.
 */
@Controller
public class AdminMerchantAgentController {

    private static final Logger log = LoggerFactory.getLogger(AdminMerchantAgentController.class);

    private final MerchantAgent agent;
    private final PetShopMerchantBackend backend;
    private final MerchantChangeDAO changeDAO;
    private final MemoryService memory;

    public AdminMerchantAgentController() {
        this(new MerchantAgent(), new PetShopMerchantBackend(),
                new MerchantChangeDAO(), new MemoryService(new DbMemoryStore()));
    }

    AdminMerchantAgentController(MerchantAgent agent, PetShopMerchantBackend backend,
                                 MerchantChangeDAO changeDAO, MemoryService memory) {
        this.agent = agent;
        this.backend = backend;
        this.changeDAO = changeDAO;
        this.memory = memory;
    }

    @GetMapping("/admin/ai-merchant")
    public String merchantPage() {
        return "pages/admin/ai-merchant";
    }

    @GetMapping(value = "/admin/ai-merchant/pending", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String pending() {
        return Json.MAPPER.writeValueAsString(changeDAO.pendingJson());
    }

    @GetMapping(value = "/admin/ai-merchant/digest", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String digest() {
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("digest", agent.digest());
        return Json.MAPPER.writeValueAsString(o);
    }

    @GetMapping(value = "/admin/ai-merchant/escalations", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String escalations() {
        ArrayNode arr = Json.MAPPER.createArrayNode();
        for (AppEventBus.AppEvent ev : AppEventBus.peek("merchant:queue")) {
            ObjectNode e = Json.MAPPER.createObjectNode();
            e.put("type", ev.type());
            e.put("payload", ev.payload());
            e.put("timestamp", ev.timestamp());
            arr.add(e);
        }
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.set("escalations", arr);
        return Json.MAPPER.writeValueAsString(o);
    }

    @GetMapping(value = "/admin/ai-merchant/memory", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> memory(
            @RequestParam(value = "subject", required = false) String subject) {
        if (subject == null || subject.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Missing subject (e.g. user:123)\"}");
        }
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.set("facts", memory.factsJson(subject));
        return ResponseEntity.ok(Json.MAPPER.writeValueAsString(o));
    }

    @PostMapping(value = "/admin/ai-merchant/chat", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> chat(
            @RequestBody(required = false) String rawBody,
            HttpSession session) {
        User admin = (User) session.getAttribute("user");
        String operator = admin == null ? "admin" : ("admin:" + admin.getId());

        ObjectNode body = readJson(rawBody);
        String message = body.has("message") ? body.path("message").asString() : "";
        if (message.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Message is required\"}");
        }
        @SuppressWarnings("unchecked")
        List<String> history = (List<String>) session.getAttribute("merchant_chat_history");
        if (history == null) history = new ArrayList<>();
        MerchantAgent.MerchantResult result = agent.run(message, last(history, 6), operator);
        history.add("Operator: " + message);
        history.add("Assistant: " + result.answer());
        while (history.size() > 12) history.remove(0);
        session.setAttribute("merchant_chat_history", history);
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("answer", result.answer());
        o.put("provider", result.usedProvider());
        o.put("model", result.usedModel());
        o.set("cards", result.cards());
        return ResponseEntity.ok(Json.MAPPER.writeValueAsString(o));
    }

    @PostMapping(value = "/admin/ai-merchant/approve", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String approve(@RequestBody(required = false) String rawBody, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        String operator = admin == null ? "admin" : ("admin:" + admin.getId());

        ObjectNode body = readJson(rawBody);
        String changeId = str(body, "changeId");
        if (changeId.isEmpty()) return "{}";
        changeDAO.markApproved(changeId, operator);
        AuditLog.record("merchant", "approve", operator, null, changeId, "", "", "", 0);
        log.info("merchant change approved id={} by={}", changeId, operator);
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("changeId", changeId);
        o.put("approved", true);
        return Json.MAPPER.writeValueAsString(o);
    }

    @PostMapping(value = "/admin/ai-merchant/apply", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> apply(
            @RequestBody(required = false) String rawBody,
            HttpSession session) {
        User admin = (User) session.getAttribute("user");
        String operator = admin == null ? "admin" : ("admin:" + admin.getId());

        ObjectNode body = readJson(rawBody);
        String changeId = str(body, "changeId");
        if (changeId.isEmpty()) return ResponseEntity.ok("{}");
        try {
            backend.apply(changeId, operator, true);
            AuditLog.record("merchant", "apply", operator, null, changeId, "", "", "", 0);
            ObjectNode o = Json.MAPPER.createObjectNode();
            o.put("changeId", changeId);
            o.put("applied", true);
            return ResponseEntity.ok(Json.MAPPER.writeValueAsString(o));
        } catch (Exception e) {
            log.warn("merchant apply refused id={}", changeId, e);
            ObjectNode o = Json.MAPPER.createObjectNode();
            o.put("error", e.getMessage());
            return ResponseEntity.status(422).body(Json.MAPPER.writeValueAsString(o));
        }
    }

    @PostMapping(value = "/admin/ai-merchant/discard", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String discard(@RequestBody(required = false) String rawBody, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        String operator = admin == null ? "admin" : ("admin:" + admin.getId());

        ObjectNode body = readJson(rawBody);
        String changeId = str(body, "changeId");
        if (changeId.isEmpty()) return "{}";
        backend.discard(changeId, operator);
        AuditLog.record("merchant", "discard", operator, null, changeId, "", "", "", 0);
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("changeId", changeId);
        o.put("discarded", true);
        return Json.MAPPER.writeValueAsString(o);
    }

    @DeleteMapping(value = "/admin/ai-merchant/memory", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> deleteMemory(
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "key", required = false) String key) {
        DbMemoryStore store = new DbMemoryStore();
        ObjectNode o = Json.MAPPER.createObjectNode();
        if (key != null && !key.isBlank()) {
            o.put("deleted", store.deleteFact(subject, key));
        } else {
            store.clear(subject);
            o.put("purged", true);
        }
        return ResponseEntity.ok(Json.MAPPER.writeValueAsString(o));
    }

    private ObjectNode readJson(String rawBody) {
        try {
            if (rawBody != null && !rawBody.isBlank()) {
                JsonNode parsed = Json.MAPPER.readTree(rawBody);
                if (parsed != null && parsed.isObject()) {
                    return (ObjectNode) parsed;
                }
            }
        } catch (Exception ignored) {}
        return Json.MAPPER.createObjectNode();
    }

    private String str(ObjectNode body, String key) {
        try {
            return body.has(key) && !body.get(key).isNull() ? body.path(key).asString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static List<String> last(List<String> history, int n) {
        if (history.size() <= n) return new ArrayList<>(history);
        return new ArrayList<>(history.subList(history.size() - n, history.size()));
    }
}
