package com.petshop.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import Model.User;
import com.petshop.util.Json;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import services.ai.CommerceTools;
import services.ai.PetShopCommerceBackend;
import services.ai.ToolDefinition;
import services.ai.common.AuditLog;
import services.ai.merchant.MerchantTools;
import services.ai.merchant.PetShopMerchantBackend;

/**
 * Replaces McpServlet (/mcp) 1:1 — same JSON-RPC 2.0 tools/list + tools/call
 * contract, same admin gate for stage/merchant tools. Bind behind auth in
 * production — never expose this without the host's authentication.
 */
@Controller
public class McpController {

    private static final Logger log = LoggerFactory.getLogger(McpController.class);

    @PostMapping(value = "/mcp", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String mcp(@RequestBody(required = false) String rawBody, HttpServletRequest request) {
        ObjectNode req = readJson(rawBody);
        JsonNode id = req.has("id") ? req.get("id") : null;
        String method = req.has("method") ? req.path("method").asString() : "";

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        boolean isAdmin = user != null && "admin".equals(user.getRole());

        try {
            switch (method) {
                case "tools/list":
                    return writeResult(id, toolsList(isAdmin));
                case "tools/call": {
                    ObjectNode params = req.has("params") ? (ObjectNode) req.path("params")
                            : Json.MAPPER.createObjectNode();
                    String name = params.has("name") ? params.path("name").asString() : "";
                    String args = params.has("arguments")
                            ? Json.MAPPER.writeValueAsString(params.get("arguments")) : "{}";
                    return writeResult(id, toolsCall(name, args, user, isAdmin));
                }
                default:
                    return writeError(id, -32601, "Method not found: " + method);
            }
        } catch (Exception e) {
            log.warn("mcp call failed", e);
            return writeError(id, -32603, "Internal error");
        }
    }

    private ObjectNode toolsList(boolean isAdmin) {
        ArrayNode tools = Json.MAPPER.createArrayNode();
        var shopping = new CommerceTools(new PetShopCommerceBackend(),
                PetShopCommerceBackend.SessionContext.of(null));
        for (ToolDefinition d : shopping.definitions()) tools.add(toolJson(d));
        if (isAdmin) {
            var merchant = new MerchantTools(new PetShopMerchantBackend(), "mcp");
            for (ToolDefinition d : merchant.definitions()) tools.add(toolJson(d));
        }
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.set("tools", tools);
        return o;
    }

    private ObjectNode toolsCall(String name, String args, User user, boolean isAdmin) {
        boolean merchantTool = name.startsWith("stage_") || name.startsWith("get_pending")
                || name.equals("get_business_snapshot") || name.equals("query_metrics")
                || name.equals("get_campaign_performance") || name.equals("search_listings")
                || name.equals("get_listing") || name.equals("get_inventory_alerts")
                || name.equals("get_order_issues") || name.equals("get_pricing_context");
        ObjectNode o = Json.MAPPER.createObjectNode();
        if (merchantTool && !isAdmin) {
            o.put("error", "Admin session required for '" + name + "'");
            return o;
        }
        String result;
        if (merchantTool) {
            String operator = "mcp:" + (user == null ? "?" : user.getId());
            result = new MerchantTools(new PetShopMerchantBackend(), operator).execute(name, args);
        } else {
            result = new CommerceTools(new PetShopCommerceBackend(),
                    PetShopCommerceBackend.SessionContext.of(user)).execute(name, args);
        }
        AuditLog.record("mcp", "tools/call:" + name,
                user == null ? "guest" : "user:" + user.getId(), null,
                result.length() > 500 ? result.substring(0, 500) : result, "", "", "", 0);
        try {
            o.set("result", Json.MAPPER.readTree(result));
        } catch (Exception e) {
            o.put("result", result);
        }
        return o;
    }

    private static ObjectNode toolJson(ToolDefinition d) {
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("name", d.getName());
        o.put("description", d.getDescription());
        try {
            o.set("inputSchema", Json.MAPPER.readTree(d.getParametersSchemaJson()));
        } catch (Exception e) {
            ObjectNode s = Json.MAPPER.createObjectNode();
            s.put("type", "object");
            o.set("inputSchema", s);
        }
        return o;
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

    private String writeResult(JsonNode id, ObjectNode result) {
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("jsonrpc", "2.0");
        if (id != null) o.set("id", id);
        o.set("result", result);
        return Json.MAPPER.writeValueAsString(o);
    }

    private String writeError(JsonNode id, int code, String message) {
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("jsonrpc", "2.0");
        if (id != null) o.set("id", id);
        ObjectNode e = Json.MAPPER.createObjectNode();
        e.put("code", code);
        e.put("message", message);
        o.set("error", e);
        return Json.MAPPER.writeValueAsString(o);
    }
}
