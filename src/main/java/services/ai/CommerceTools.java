package services.ai;

import Model.CustomerSupportKnowledge;
import Model.Order;
import Model.OrderItem;
import Model.Product;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Provider-independent commerce tools (Commerce Agents "tool contracts").
 * The agent invokes these by name; each provider adapter converts the
 * declaration to its native function-calling schema, and results always come
 * back as sanitized JSON the model reads as fenced data.
 */
public final class CommerceTools {
    private static final Gson GSON = new Gson();

    public record ToolExecutor(ToolDefinition definition,
                               Function<JsonObject, String> handler) {}

    private final PetShopCommerceBackend backend;
    private final PetShopCommerceBackend.SessionContext session;
    /** Provenance: product ids returned this session (gates cart/advice claims). */
    private final Set<Integer> seenProductIds = new LinkedHashSet<>();
    private Integer seenOrderId = null;

    public CommerceTools(PetShopCommerceBackend backend,
                         PetShopCommerceBackend.SessionContext session) {
        this.backend = backend;
        this.session = session;
    }

    public List<ToolExecutor> all() {
        List<ToolExecutor> tools = new ArrayList<>();
        tools.add(new ToolExecutor(
                new ToolDefinition("searchProducts", "Search the pet shop catalog by keyword.",
                        schema(new String[]{"query"}, new String[]{"query"})),
                args -> {
                    String q = str(args, "query", "");
                    int limit = intArg(args, "limit", 5);
                    List<Product> products = backend.searchProducts(q, limit);
                    JsonArray arr = new JsonArray();
                    for (Product p : products) {
                        seenProductIds.add(p.getId());
                        arr.add(toProductJson(p, true));
                    }
                    return arr.toString();
                }));
        tools.add(new ToolExecutor(
                new ToolDefinition("getProductDetails", "Full details for one product id.",
                        schema(new String[]{"productId"}, new String[]{"productId"})),
                args -> {
                    int id = intArg(args, "productId", -1);
                    if (id <= 0) return error("Invalid productId");
                    Product p = backend.getProductDetails(id);
                    if (p == null) return error("Product not found");
                    seenProductIds.add(p.getId());
                    return toProductJson(p, false).toString();
                }));
        tools.add(new ToolExecutor(
                new ToolDefinition("compareProducts", "Side-by-side facts for 2-4 product ids.",
                        "{\"type\":\"object\",\"properties\":{\"productIds\":{\"type\":\"array\",\"items\":{\"type\":\"integer\"},\"minItems\":2,\"maxItems\":4}},\"required\":[\"productIds\"]}"),
                args -> {
                    List<Integer> ids = intList(args, "productIds");
                    if (ids.size() < 2 || ids.size() > 4) return error("Provide 2-4 productIds");
                    JsonArray arr = new JsonArray();
                    for (int id : ids) {
                        Product p = backend.getProductDetails(id);
                        if (p != null) {
                            seenProductIds.add(p.getId());
                            arr.add(toProductJson(p, false));
                        }
                    }
                    return arr.toString();
                }));
        tools.add(new ToolExecutor(
                new ToolDefinition("recommendProducts", "Recommend products for a need (e.g. 'pate cho meo con').",
                        schema(new String[]{"need"}, new String[]{"need"})),
                args -> {
                    String need = str(args, "need", "");
                    int limit = intArg(args, "limit", 4);
                    JsonArray arr = new JsonArray();
                    for (Product p : backend.recommendProducts(need, limit)) {
                        seenProductIds.add(p.getId());
                        JsonObject o = toProductJson(p, true);
                        o.addProperty("why", "Matches: " + sanitize(need));
                        arr.add(o);
                    }
                    return arr.toString();
                }));
        tools.add(new ToolExecutor(
                new ToolDefinition("getOrderStatus", "Status of one of the customer's own orders.",
                        schema(new String[]{"orderId"}, new String[]{"orderId"})),
                args -> {
                    if (session.guest()) return error("Guest session: ask the customer to sign in first.");
                    int id = intArg(args, "orderId", -1);
                    Order o = backend.getOrder(session, id);
                    if (o == null) return error("Order not found for this account");
                    seenOrderId = o.getId();
                    return toOrderJson(o).toString();
                }));
        tools.add(new ToolExecutor(
                new ToolDefinition("searchPolicies", "Search shop policies/FAQ entries.",
                        schema(new String[]{"query"}, new String[]{"query"})),
                args -> {
                    JsonArray arr = new JsonArray();
                    for (CustomerSupportKnowledge k : backend.searchPolicies(str(args, "query", ""))) {
                        JsonObject o = new JsonObject();
                        o.addProperty("category", sanitize(k.getCategory()));
                        o.addProperty("title", sanitize(k.getTitle()));
                        o.addProperty("content", sanitize(k.getContent()));
                        arr.add(o);
                    }
                    return arr.toString();
                }));
        return tools;
    }

    public List<ToolDefinition> definitions() {
        List<ToolDefinition> defs = new ArrayList<>();
        for (ToolExecutor t : all()) defs.add(t.definition());
        return defs;
    }

    /** Executes a model-requested call; unknown tools and bad input are safe errors, not throws. */
    public String execute(String name, String argumentsJson) {
        JsonObject args;
        try {
            args = JsonParser.parseString(argumentsJson == null ? "{}" : argumentsJson).getAsJsonObject();
        } catch (Exception e) {
            return error("Invalid tool arguments");
        }
        for (ToolExecutor t : all()) {
            if (t.definition().getName().equals(name)) {
                try {
                    String result = t.handler().apply(args);
                    return result.length() > 12000 ? result.substring(0, 12000) : result;
                } catch (Exception e) {
                    return error("Tool temporarily unavailable");
                }
            }
        }
        return error("Unknown tool: " + sanitize(name));
    }

    public Set<Integer> getSeenProductIds() { return Set.copyOf(seenProductIds); }
    public Integer getSeenOrderId() { return seenOrderId; }

    /** Restores cross-turn provenance loaded from the session store. */
    public void seedProvenance(Set<Integer> productIds, Integer orderId) {
        if (productIds != null) seenProductIds.addAll(productIds);
        if (orderId != null) seenOrderId = orderId;
    }

    // ---- helpers ----
    private static String schema(String[] props, String[] required) {
        JsonObject s = new JsonObject();
        s.addProperty("type", "object");
        JsonObject p = new JsonObject();
        for (String name : props) {
            JsonObject f = new JsonObject();
            if (name.toLowerCase().contains("id") || name.equals("limit")) f.addProperty("type", "integer");
            else f.addProperty("type", "string");
            p.add(name, f);
        }
        s.add("properties", p);
        JsonArray r = new JsonArray();
        for (String name : required) r.add(name);
        s.add("required", r);
        return s.toString();
    }

    private static String str(JsonObject args, String key, String def) {
        try {
            return args.has(key) && !args.get(key).isJsonNull() ? args.get(key).getAsString() : def;
        } catch (Exception e) { return def; }
    }

    private static int intArg(JsonObject args, String key, int def) {
        try {
            if (!args.has(key) || args.get(key).isJsonNull()) return def;
            if (args.get(key).isJsonPrimitive() && args.get(key).getAsJsonPrimitive().isNumber()) {
                return args.get(key).getAsInt();
            }
            return Integer.parseInt(args.get(key).getAsString().trim());
        } catch (Exception e) { return def; }
    }

    private static List<Integer> intList(JsonObject args, String key) {
        List<Integer> out = new ArrayList<>();
        try {
            for (var el : args.getAsJsonArray(key)) out.add(el.getAsInt());
        } catch (Exception ignored) {}
        return out;
    }

    private static String error(String msg) {
        JsonObject o = new JsonObject();
        o.addProperty("error", msg);
        return o.toString();
    }

    static JsonObject toProductJson(Product p, boolean compact) {
        JsonObject o = new JsonObject();
        o.addProperty("id", p.getId());
        o.addProperty("name", sanitize(p.getName()));
        o.addProperty("priceVnd", p.getEffectivePrice() == null ? "0" : p.getEffectivePrice().toPlainString());
        o.addProperty("discountPercent", p.getDisplayDiscountPercent());
        o.addProperty("category", sanitize(p.getCategory()));
        o.addProperty("brand", sanitize(p.getBrand()));
        o.addProperty("stock", p.getStock());
        o.addProperty("inStock", p.getStock() > 0);
        if (!compact) o.addProperty("description", sanitize(p.getDescription()));
        return o;
    }

    static JsonObject toOrderJson(Order o) {
        JsonObject j = new JsonObject();
        j.addProperty("id", o.getId());
        j.addProperty("status", sanitize(o.getStatus()));
        j.addProperty("statusLabel", sanitize(o.getStatusLabel()));
        j.addProperty("paymentMethod", sanitize(o.getPayment_method()));
        j.addProperty("paid", o.getPayment_status());
        j.addProperty("totalVnd", o.getTotalAmount() == null ? "0" : o.getTotalAmount().toPlainString());
        j.addProperty("createdAt", o.getCreatedAt() == null ? "" : o.getCreatedAt().toString());
        JsonArray items = new JsonArray();
        if (o.getItems() != null) {
            for (OrderItem it : o.getItems()) {
                JsonObject i = new JsonObject();
                i.addProperty("name", sanitize(it.getProductName()));
                i.addProperty("qty", it.getQuantity());
                i.addProperty("priceVnd", it.getPrice() == null ? "0" : it.getPrice().toPlainString());
                items.add(i);
            }
        }
        j.add("items", items);
        return j;
    }

    /** Sanitize external/product data before it enters prompts or tool results. */
    static String sanitize(String s) {
        if (s == null) return "";
        String t = s.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ").trim();
        return t.length() > 2000 ? t.substring(0, 2000) : t;
    }
}
