package controller.shop;

import Model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ai.CommerceTools;
import services.ai.PetShopCommerceBackend;
import services.ai.ToolDefinition;
import services.ai.common.AuditLog;
import services.ai.merchant.MerchantTools;
import services.ai.merchant.PetShopMerchantBackend;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * MCP-style tools endpoint port (commerce-common/mcp_server.py concept):
 * JSON-RPC 2.0 over HTTP exposing the same commerce tools the agents use.
 * Read tools are public (same data as the shop); stage/merchant tools
 * require an admin session. Bind behind auth in production — never expose
 * this without the host's authentication.
 */
public class McpServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(McpServlet.class);
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JsonObject req = readJson(request);
        Object id = req.has("id") ? req.get("id") : null;
        String method = req.has("method") ? req.get("method").getAsString() : "";

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        boolean isAdmin = user != null && "admin".equals(user.getRole());

        try {
            switch (method) {
                case "tools/list" -> writeResult(response, id, toolsList(isAdmin));
                case "tools/call" -> {
                    JsonObject params = req.has("params") ? req.getAsJsonObject("params") : new JsonObject();
                    String name = params.has("name") ? params.get("name").getAsString() : "";
                    String args = params.has("arguments") ? gson.toJson(params.get("arguments")) : "{}";
                    writeResult(response, id, toolsCall(name, args, user, isAdmin));
                }
                default -> writeError(response, id, -32601, "Method not found: " + method);
            }
        } catch (Exception e) {
            log.warn("mcp call failed", e);
            writeError(response, id, -32603, "Internal error");
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

    private JsonObject readJson(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            StringBuilder raw = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) raw.append(line);
            if (!raw.toString().isBlank()) return JsonParser.parseString(raw.toString()).getAsJsonObject();
        } catch (Exception ignored) {}
        return new JsonObject();
    }

    private void writeResult(HttpServletResponse response, Object id, JsonObject result) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("jsonrpc", "2.0");
        if (id != null) o.add("id", (com.google.gson.JsonElement) id);
        o.add("result", result);
        response.getWriter().write(gson.toJson(o));
    }

    private void writeError(HttpServletResponse response, Object id, int code, String message) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("jsonrpc", "2.0");
        if (id != null) o.add("id", (com.google.gson.JsonElement) id);
        JsonObject e = new JsonObject();
        e.addProperty("code", code);
        e.addProperty("message", message);
        o.add("error", e);
        response.getWriter().write(gson.toJson(o));
    }
}
