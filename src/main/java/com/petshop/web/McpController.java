package com.petshop.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Model.User;
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

    private final Gson gson = new Gson();

    @PostMapping(value = "/mcp", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String mcp(@RequestBody(required = false) String rawBody, HttpServletRequest request) {
        JsonObject req = readJson(rawBody);
        Object id = req.has("id") ? req.get("id") : null;
        String method = req.has("method") ? req.get("method").getAsString() : "";

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        boolean isAdmin = user != null && "admin".equals(user.getRole());

        try {
            switch (method) {
                case "tools/list":
                    return writeResult(id, toolsList(isAdmin));
                case "tools/call": {
                    JsonObject params = req.has("params") ? req.getAsJsonObject("params") : new JsonObject();
                    String name = params.has("name") ? params.get("name").getAsString() : "";
                    String args = params.has("arguments") ? gson.toJson(params.get("arguments")) : "{}";
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

    private JsonObject toolsList(boolean isAdmin) {
        JsonArray tools = new JsonArray();
        var shopping = new CommerceTools(new PetShopCommerceBackend(),
                PetShopCommerceBackend.SessionContext.of(null));
        for (ToolDefinition d : shopping.definitions()) tools.add(toolJson(d));
        if (isAdmin) {
            var merchant = new MerchantTools(new PetShopMerchantBackend(), "mcp");
            for (ToolDefinition d : merchant.definitions()) tools.add(toolJson(d));
        }
        JsonObject o = new JsonObject();
        o.add("tools", tools);
        return o;
    }

    private JsonObject toolsCall(String name, String args, User user, boolean isAdmin) {
        boolean merchantTool = name.startsWith("stage_") || name.startsWith("get_pending")
                || name.equals("get_business_snapshot") || name.equals("query_metrics")
                || name.equals("get_campaign_performance") || name.equals("search_listings")
                || name.equals("get_listing") || name.equals("get_inventory_alerts")
                || name.equals("get_order_issues") || name.equals("get_pricing_context");
        JsonObject o = new JsonObject();
        if (merchantTool && !isAdmin) {
            o.addProperty("error", "Admin session required for '" + name + "'");
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
            o.add("result", JsonParser.parseString(result));
        } catch (Exception e) {
            o.addProperty("result", result);
        }
        return o;
    }

    private static JsonObject toolJson(ToolDefinition d) {
        JsonObject o = new JsonObject();
        o.addProperty("name", d.getName());
        o.addProperty("description", d.getDescription());
        try {
            o.add("inputSchema", JsonParser.parseString(d.getParametersSchemaJson()));
        } catch (Exception e) {
            JsonObject s = new JsonObject();
            s.addProperty("type", "object");
            o.add("inputSchema", s);
        }
        return o;
    }

    private JsonObject readJson(String rawBody) {
        try {
            if (rawBody != null && !rawBody.isBlank()) {
                return JsonParser.parseString(rawBody).getAsJsonObject();
            }
        } catch (Exception ignored) {}
        return new JsonObject();
    }

    private String writeResult(Object id, JsonObject result) {
        JsonObject o = new JsonObject();
        o.addProperty("jsonrpc", "2.0");
        if (id != null) o.add("id", (com.google.gson.JsonElement) id);
        o.add("result", result);
        return gson.toJson(o);
    }

    private String writeError(Object id, int code, String message) {
        JsonObject o = new JsonObject();
        o.addProperty("jsonrpc", "2.0");
        if (id != null) o.add("id", (com.google.gson.JsonElement) id);
        JsonObject e = new JsonObject();
        e.addProperty("code", code);
        e.addProperty("message", message);
        o.add("error", e);
        return gson.toJson(o);
    }
}
