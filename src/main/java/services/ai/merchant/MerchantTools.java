package services.ai.merchant;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import services.ai.ToolDefinition;
import services.ai.common.Fence;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Merchant tool contracts port (merchant_agent/tools/registry.py): reads plus
 * stage_* / queue tools. Provenance gates hold writes naming ids no tool
 * returned this session; apply additionally needs the host approval mark.
 * The tool surface is a function of {@link MerchantConfig} switches.
 */
public final class MerchantTools {
    public record ToolExecutor(ToolDefinition definition, Function<JsonObject, String> handler) {}

    private final PetShopMerchantBackend backend;
    private final String operator;
    private final Set<Integer> seenListings = new LinkedHashSet<>();
    private final Set<String> seenChanges = new LinkedHashSet<>();

    public MerchantTools(PetShopMerchantBackend backend, String operator) {
        this.backend = backend;
        this.operator = operator;
    }

    public List<ToolExecutor> all() {
        List<ToolExecutor> tools = new ArrayList<>();
        tools.add(new ToolExecutor(def("get_business_snapshot",
                "Headline revenue/order numbers for the current period.", "{}"),
                args -> backend.businessSnapshot().toString()));
        tools.add(new ToolExecutor(def("query_metrics",
                "One metric over time. metrics: revenue_by_month, top_sellers.",
                req("metric")),
                args -> backend.queryMetrics(str(args, "metric", ""), str(args, "segment", "")).toString()));
        tools.add(new ToolExecutor(def("get_campaign_performance",
                "Promotions/campaigns the store can see (spend/revenue unreported).", "{}"),
                args -> backend.campaignPerformance().toString()));
        tools.add(new ToolExecutor(def("search_listings", "Search product listings by text.",
                req("query")),
                args -> {
                    JsonArray arr = backend.searchListings(str(args, "query", ""), intArg(args, "limit", 8));
                    for (var el : arr) {
                        try { seenListings.add(el.getAsJsonObject().get("id").getAsInt()); }
                        catch (Exception ignored) {}
                    }
                    return arr.toString();
                }));
        tools.add(new ToolExecutor(def("get_listing", "Full record for one listing id.",
                req("productId")),
                args -> {
                    int id = intArg(args, "productId", -1);
                    JsonObject o = backend.getListing(id);
                    if (o == null) return error("Listing not found");
                    seenListings.add(id);
                    return o.toString();
                }));
        if (MerchantConfig.enableInventory()) {
            tools.add(new ToolExecutor(def("get_inventory_alerts",
                    "Low-stock and out-of-stock alerts derived from stock.", "{}"),
                    args -> backend.inventoryAlerts().toString()));
            tools.add(new ToolExecutor(def("get_order_issues", "Open order exceptions.", "{}"),
                    args -> backend.orderIssues().toString()));
        }
        if (MerchantConfig.enablePricing()) {
            tools.add(new ToolExecutor(def("get_pricing_context",
                    "Current grounded price for a listing (needed before any price move).",
                    req("productId")),
                    args -> {
                        JsonObject o = backend.pricingContext(intArg(args, "productId", -1));
                        return o == null ? error("Listing not found") : o.toString();
                    }));
        }
        if (MerchantConfig.enableListingEdits()) {
            tools.add(new ToolExecutor(def("stage_listing_update",
                    "Stage content edits (name/description/category) for one listing.",
                    "{\"type\":\"object\",\"properties\":{\"productId\":{\"type\":\"integer\"},"
                            + "\"fields\":{\"type\":\"object\"},\"note\":{\"type\":\"string\"}},"
                            + "\"required\":[\"productId\",\"fields\"]}"),
                    args -> stage(StagedChange.Kind.LISTING_UPDATE, args, "listing content edit")));
        }
        if (MerchantConfig.enablePricing()) {
            tools.add(new ToolExecutor(def("stage_price_update",
                    "Stage price changes: items [{productId, newPrice}].",
                    "{\"type\":\"object\",\"properties\":{\"items\":{\"type\":\"array\"},"
                            + "\"note\":{\"type\":\"string\"}},\"required\":[\"items\"]}"),
                    args -> {
                        List<StagedChange.Item> items = new ArrayList<>();
                        try {
                            for (var el : args.getAsJsonArray("items")) {
                                JsonObject it = el.getAsJsonObject();
                                int pid = it.get("productId").getAsInt();
                                String gate = provenanceGate(pid);
                                if (gate != null) return gate;
                                JsonObject ctx = backend.pricingContext(pid);
                                String before = ctx == null ? "" : ctx.get("currentPriceVnd").getAsString();
                                items.add(new StagedChange.Item(String.valueOf(pid), "price", before,
                                        it.get("newPrice").getAsString()));
                            }
                        } catch (Exception e) {
                            return error("items must be [{productId, newPrice}]");
                        }
                        return doStage(StagedChange.Kind.PRICE_UPDATE, "price update", items, args);
                    }));
            tools.add(new ToolExecutor(def("stage_promotion",
                    "Stage a discount: {productIds:[...], discountPct, note}.",
                    "{\"type\":\"object\",\"properties\":{\"productIds\":{\"type\":\"array\"},"
                            + "\"discountPct\":{\"type\":\"number\"},\"note\":{\"type\":\"string\"}},"
                            + "\"required\":[\"productIds\",\"discountPct\"]}"),
                    args -> {
                        double pct;
                        try { pct = args.get("discountPct").getAsDouble(); }
                        catch (Exception e) { return error("discountPct must be a number"); }
                        if (Math.abs(pct) > MerchantConfig.maxPromotionDiscountPct()) {
                            return error("A " + pct + "% move exceeds the "
                                    + Math.round(MerchantConfig.maxPromotionDiscountPct())
                                    + "% promotion limit. Propose a shallower move.");
                        }
                        List<StagedChange.Item> items = new ArrayList<>();
                        try {
                            for (var el : args.getAsJsonArray("productIds")) {
                                int pid = el.getAsInt();
                                String gate = provenanceGate(pid);
                                if (gate != null) return gate;
                                JsonObject ctx = backend.pricingContext(pid);
                                if (ctx == null) return error("Listing " + pid + " not found");
                                double before = Double.parseDouble(ctx.get("currentPriceVnd").getAsString());
                                double after = before * (1 - pct / 100.0);
                                items.add(new StagedChange.Item(String.valueOf(pid), "price",
                                        String.valueOf(before), String.valueOf(Math.round(after))));
                            }
                        } catch (Exception e) {
                            return error("productIds must be an id array");
                        }
                        return doStage(StagedChange.Kind.PROMOTION, pct + "% promotion", items, args);
                    }));
        }
        if (MerchantConfig.enableInventory()) {
            tools.add(new ToolExecutor(def("stage_inventory_action",
                    "Stage restocks: items [{productId, newStock}].",
                    "{\"type\":\"object\",\"properties\":{\"items\":{\"type\":\"array\"},"
                            + "\"note\":{\"type\":\"string\"}},\"required\":[\"items\"]}"),
                    args -> {
                        List<StagedChange.Item> items = new ArrayList<>();
                        try {
                            for (var el : args.getAsJsonArray("items")) {
                                JsonObject it = el.getAsJsonObject();
                                int pid = it.get("productId").getAsInt();
                                String gate = provenanceGate(pid);
                                if (gate != null) return gate;
                                JsonObject listing = backend.getListing(pid);
                                String before = listing == null ? "0"
                                        : String.valueOf(listing.get("stock").getAsInt());
                                items.add(new StagedChange.Item(String.valueOf(pid), "stock", before,
                                        it.get("newStock").getAsString()));
                            }
                        } catch (Exception e) {
                            return error("items must be [{productId, newStock}]");
                        }
                        return doStage(StagedChange.Kind.INVENTORY_ACTION, "inventory action", items, args);
                    }));
        }
        if (MerchantConfig.enableCampaigns()) {
            tools.add(new ToolExecutor(def("stage_campaign",
                    "Stage a campaign draft (campaigns are not applied by this store).",
                    "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},"
                            + "\"budget\":{\"type\":\"string\"},\"note\":{\"type\":\"string\"}},"
                            + "\"required\":[\"name\"]}"),
                    args -> error("campaigns are not managed by this store's systems "
                            + "(limitation recorded); the draft was not staged")));
        }
        if (MerchantConfig.stagesChanges()) {
            tools.add(new ToolExecutor(def("get_pending_changes",
                    "Changes staged but not yet applied or discarded.", "{}"),
                    args -> {
                        JsonArray arr = new JsonArray();
                        for (StagedChange c : backend.ledger().pending()) {
                            seenChanges.add(c.getChangeId());
                            arr.add(changeJson(c));
                        }
                        return arr.toString();
                    }));
        }
        return tools;
    }

    public List<ToolDefinition> definitions() {
        List<ToolDefinition> defs = new ArrayList<>();
        for (ToolExecutor t : all()) defs.add(t.definition());
        return defs;
    }

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
                } catch (ChangeLedger.GuardrailViolation gv) {
                    return error("That change exceeds this store's guardrails: "
                            + String.join("; ", gv.getViolations())
                            + ". Explain the block and propose a compliant alternative.");
                } catch (ChangeLedger.ChangeNotApplicable na) {
                    return error(na.getMessage());
                } catch (Exception e) {
                    return error("Tool temporarily unavailable");
                }
            }
        }
        return error("Unknown tool: " + Fence.sanitize(name));
    }

    private String stage(StagedChange.Kind kind, JsonObject args, String summary) {
        int pid = intArg(args, "productId", -1);
        String gate = provenanceGate(pid);
        if (gate != null) return gate;
        if (pid > 0 && backend.getListing(pid) == null) return error("Listing not found");
        JsonObject fields;
        try {
            fields = args.getAsJsonObject("fields");
        } catch (Exception e) {
            return error("fields must be an object like {\"name\": \"...\"}");
        }
        List<StagedChange.Item> items = new ArrayList<>();
        for (String field : fields.keySet()) {
            JsonObject current = backend.getListing(pid);
            String before = current != null && current.has(field) ? current.get(field).getAsString() : "";
            items.add(new StagedChange.Item(String.valueOf(pid), field, before,
                    fields.get(field).getAsString()));
        }
        return doStage(kind, summary + " for listing " + pid, items, args);
    }

    private String doStage(StagedChange.Kind kind, String summary,
                           List<StagedChange.Item> items, JsonObject args) {
        if (items.isEmpty()) return error("Nothing to stage");
        try {
            StagedChange c = backend.stage(kind, summary, items, operator,
                    List.of("Staged only — apply it only after the operator approves on "
                            + MerchantConfig.approvalSurface()));
            seenChanges.add(c.getChangeId());
            JsonObject o = changeJson(c);
            o.addProperty("note", "Staged only — staged, waiting approval on "
                    + MerchantConfig.approvalSurface());
            return o.toString();
        } catch (ChangeLedger.GuardrailViolation gv) {
            return error("That change exceeds this store's guardrails: "
                    + String.join("; ", gv.getViolations()));
        }
    }

    /** Provenance gate: staged writes accept only ids tools returned this session. */
    private String provenanceGate(int productId) {
        if (seenListings.contains(productId)) return null;
        JsonObject o = new JsonObject();
        o.addProperty("error", "listing id " + productId + " was not returned by catalog tools "
                + "in this session. Search or look the listing up first and use ids from the results.");
        o.addProperty("gate", "provenance");
        return o.toString();
    }

    static JsonObject changeJson(StagedChange c) {
        JsonObject o = new JsonObject();
        o.addProperty("changeId", c.getChangeId());
        o.addProperty("kind", c.getKind().name());
        o.addProperty("status", c.getStatus().name());
        o.addProperty("summary", c.getSummary());
        JsonArray arr = new JsonArray();
        for (StagedChange.Item it : c.getItems()) {
            JsonObject i = new JsonObject();
            i.addProperty("target", it.target());
            i.addProperty("field", it.field());
            i.addProperty("before", it.before());
            i.addProperty("after", it.after());
            arr.add(i);
        }
        o.add("items", arr);
        o.addProperty("createdBy", c.getCreatedBy());
        return o;
    }

    private static ToolDefinition def(String name, String desc, String schema) {
        return new ToolDefinition(name, desc, schema);
    }

    private static String req(String prop) {
        return "{\"type\":\"object\",\"properties\":{\"" + prop + "\":{\"type\":\"string\"}},"
                + "\"required\":[\"" + prop + "\"]}";
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

    private static String error(String msg) {
        JsonObject o = new JsonObject();
        o.addProperty("error", msg);
        return o.toString();
    }
}
