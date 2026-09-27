package services.ai;

import com.petshop.model.CustomerSupportKnowledge;
import com.petshop.model.Order;
import com.petshop.model.OrderItem;
import com.petshop.model.Product;
import com.petshop.util.Json;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

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

    public record ToolExecutor(ToolDefinition definition,
                               Function<ObjectNode, String> handler) {}

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
                    ArrayNode arr = Json.MAPPER.createArrayNode();
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
                    ArrayNode arr = Json.MAPPER.createArrayNode();
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
                    ArrayNode arr = Json.MAPPER.createArrayNode();
                    for (Product p : backend.recommendProducts(need, limit)) {
                        seenProductIds.add(p.getId());
                        ObjectNode o = toProductJson(p, true);
                        o.put("why", "Matches: " + sanitize(need));
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
                    ArrayNode arr = Json.MAPPER.createArrayNode();
                    for (CustomerSupportKnowledge k : backend.searchPolicies(str(args, "query", ""))) {
                        ObjectNode o = Json.MAPPER.createObjectNode();
                        o.put("category", sanitize(k.getCategory()));
                        o.put("title", sanitize(k.getTitle()));
                        o.put("content", sanitize(k.getContent()));
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
        ObjectNode args;
        try {
            JsonNode parsed = Json.MAPPER.readTree(argumentsJson == null ? "{}" : argumentsJson);
            if (!parsed.isObject()) return error("Invalid tool arguments");
            args = (ObjectNode) parsed;
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
        ObjectNode s = Json.MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = Json.MAPPER.createObjectNode();
        for (String name : props) {
            ObjectNode f = Json.MAPPER.createObjectNode();
            if (name.toLowerCase().contains("id") || name.equals("limit")) f.put("type", "integer");
            else f.put("type", "string");
            p.set(name, f);
        }
        s.set("properties", p);
        ArrayNode r = Json.MAPPER.createArrayNode();
        for (String name : required) r.add(name);
        s.set("required", r);
        return s.toString();
    }

    private static String str(ObjectNode args, String key, String def) {
        try {
            return args.has(key) && !args.path(key).isNull() ? args.path(key).asString() : def;
        } catch (Exception e) { return def; }
    }

    private static int intArg(ObjectNode args, String key, int def) {
        try {
            if (!args.has(key) || args.path(key).isNull()) return def;
            if (args.path(key).isNumber()) {
                return args.path(key).asInt();
            }
            return Integer.parseInt(args.path(key).asString().trim());
        } catch (Exception e) { return def; }
    }

    private static List<Integer> intList(ObjectNode args, String key) {
        List<Integer> out = new ArrayList<>();
        try {
            JsonNode arr = args.path(key);
            if (arr.isArray()) for (JsonNode el : arr) out.add(el.asInt());
        } catch (Exception ignored) {}
        return out;
    }

    private static String error(String msg) {
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("error", msg);
        return o.toString();
    }

    static ObjectNode toProductJson(Product p, boolean compact) {
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("id", p.getId());
        o.put("name", sanitize(p.getName()));
        o.put("priceVnd", p.getEffectivePrice() == null ? "0" : p.getEffectivePrice().toPlainString());
        o.put("discountPercent", p.getDisplayDiscountPercent());
        o.put("category", sanitize(p.getCategory()));
        o.put("brand", sanitize(p.getBrand()));
        o.put("stock", p.getStock());
        o.put("inStock", p.getStock() > 0);
        if (!compact) o.put("description", sanitize(p.getDescription()));
        return o;
    }

    static ObjectNode toOrderJson(Order o) {
        ObjectNode j = Json.MAPPER.createObjectNode();
        j.put("id", o.getId());
        j.put("status", sanitize(o.getStatus()));
        j.put("statusLabel", sanitize(o.getStatusLabel()));
        j.put("paymentMethod", sanitize(o.getPayment_method()));
        j.put("paid", o.getPayment_status());
        j.put("totalVnd", o.getTotalAmount() == null ? "0" : o.getTotalAmount().toPlainString());
        j.put("createdAt", o.getCreatedAt() == null ? "" : o.getCreatedAt().toString());
        ArrayNode items = Json.MAPPER.createArrayNode();
        if (o.getItems() != null) {
            for (OrderItem it : o.getItems()) {
                ObjectNode i = Json.MAPPER.createObjectNode();
                i.put("name", sanitize(it.getProductName()));
                i.put("qty", it.getQuantity());
                i.put("priceVnd", it.getPrice() == null ? "0" : it.getPrice().toPlainString());
                items.add(i);
            }
        }
        j.set("items", items);
        return j;
    }

    /** Sanitize external/product data before it enters prompts or tool results. */
    static String sanitize(String s) {
        if (s == null) return "";
        String t = s.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ").trim();
        return t.length() > 2000 ? t.substring(0, 2000) : t;
    }
}
