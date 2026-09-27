package services.ai.merchant;

import DAO.OrderDAO;
import DAO.ProductDAO;
import DAO.PromotionDAO;
import DAO.ReportDAO;
import Model.Order;
import Model.Product;
import Model.Promotion;
import Util.Json;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ai.common.Fence;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PetShop's MerchantBackend (port of merchant_agent/backend.py): every method
 * calls the shop's own systems server-side; the model sees only results as
 * fenced data. Reads are free; stage_* records proposals without touching
 * live state; only apply (after host approval) mutates anything.
 */
public class PetShopMerchantBackend {
    private static final Logger log = LoggerFactory.getLogger(PetShopMerchantBackend.class);

    private final ReportDAO reportDAO = new ReportDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();
    /**
     * Shared ledger: every backend instance (agent turns, MCP calls, approval
     * servlet) stages into and applies from the same lifecycle. Hydrated once
     * from durable storage so staged work and id sequencing survive restarts.
     */
    private static final ChangeLedger SHARED_LEDGER = new ChangeLedger();
    private static volatile boolean hydrated;
    private final ChangeLedger ledger = SHARED_LEDGER;
    private final MerchantChangeDAO changeDAO = new MerchantChangeDAO();

    public PetShopMerchantBackend() {
        if (!hydrated) {
            synchronized (PetShopMerchantBackend.class) {
                if (!hydrated) {
                    for (StagedChange c : changeDAO.loadStaged()) SHARED_LEDGER.reattach(c);
                    hydrated = true;
                }
            }
        }
    }

    public ChangeLedger ledger() { return ledger; }

    // ---- Performance ----
    public ObjectNode businessSnapshot() {
        ObjectNode o = Json.MAPPER.createObjectNode();
        try {
            o.put("totalRevenueVnd", str(reportDAO.getTotalRevenue()));
            o.put("monthRevenueVnd", str(reportDAO.getCurrentMonthRevenue()));
            o.put("completedOrders", reportDAO.getCompletedOrdersCount());
            ObjectNode byStatus = Json.MAPPER.createObjectNode();
            for (Map<String, Object> row : reportDAO.getOrdersByStatus()) {
                byStatus.put(String.valueOf(row.get("status")),
                        ((Number) row.get("count")).intValue());
            }
            o.set("ordersByStatus", byStatus);
            ArrayNode top = Json.MAPPER.createArrayNode();
            for (Map<String, Object> row : reportDAO.getTopSellingProducts(5)) {
                top.add(row.get("product") + " (đã bán " + row.get("count") + ")");
            }
            o.set("topSellers", top);
        } catch (Exception e) {
            o.put("error", "Snapshot temporarily unavailable");
        }
        o.put("note", "Email channel reports no revenue (limitation)");
        return o;
    }

    public ObjectNode queryMetrics(String metric, String segment) {
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("metric", Fence.sanitize(metric));
        try {
            switch (metric.toLowerCase()) {
                case "revenue_by_month" -> {
                    int year = java.time.LocalDate.now().getYear();
                    ArrayNode points = Json.MAPPER.createArrayNode();
                    for (Map<String, Object> row : reportDAO.getRevenueByMonth(year)) {
                        ObjectNode p = Json.MAPPER.createObjectNode();
                        p.put("month", String.valueOf(row.get("month")));
                        p.put("revenueVnd", String.valueOf(row.get("revenue")));
                        points.add(p);
                    }
                    o.set("points", points);
                }
                case "top_sellers" -> {
                    ArrayNode points = Json.MAPPER.createArrayNode();
                    for (Map<String, Object> row : reportDAO.getTopSellingProducts(10)) {
                        String name = String.valueOf(row.get("product"));
                        if (segment != null && !segment.isBlank()) {
                            try {
                                Product p = productDAO.getProductById(
                                        ((Number) row.get("productId")).intValue());
                                if (p == null || !p.getCategory().toLowerCase()
                                        .contains(segment.toLowerCase())) continue;
                            } catch (Exception ignored) {}
                        }
                        ObjectNode pt = Json.MAPPER.createObjectNode();
                        pt.put("product", Fence.sanitize(name));
                        pt.put("sold", ((Number) row.get("count")).intValue());
                        points.add(pt);
                    }
                    o.set("points", points);
                }
                default -> o.put("note", "Metric '" + Fence.sanitize(metric)
                        + "' is not supplied by this store (supported: revenue_by_month, top_sellers)");
            }
        } catch (Exception e) {
            o.put("error", "Metrics temporarily unavailable");
        }
        return o;
    }

    public ArrayNode campaignPerformance() {
        ArrayNode arr = Json.MAPPER.createArrayNode();
        try {
            for (Promotion promo : promotionDAO.getAllPromotions()) {
                ObjectNode o = Json.MAPPER.createObjectNode();
                o.put("id", promo.getId());
                o.put("name", Fence.sanitize(promo.getName()));
                o.put("type", Fence.sanitize(promo.getPromotionType()));
                o.put("status", Fence.sanitize(promo.getStatus()));
                o.put("discount", promo.getDiscountValue() == null ? ""
                        : promo.getDiscountValue().toPlainString() + " " + promo.getDiscountType());
                o.putNull("spend");
                o.putNull("revenue");
                arr.add(o);
            }
        } catch (Exception e) {
            log.warn("campaign read failed", e);
        }
        return arr;
    }

    // ---- Catalog ----
    public ArrayNode searchListings(String query, int limit) {
        ArrayNode arr = Json.MAPPER.createArrayNode();
        int safe = Math.max(1, Math.min(limit, 10));
        try {
            List<Product> products = (query == null || query.isBlank())
                    ? productDAO.getProductsByPage(0, safe)
                    : productDAO.searchProductsLimit(query.trim(), safe);
            for (Product p : products) arr.add(listingSummary(p));
        } catch (Exception e) {
            log.warn("listing search failed", e);
        }
        return arr;
    }

    public ObjectNode getListing(int productId) {
        try {
            Product p = productDAO.getProductById(productId);
            if (p == null) return null;
            ObjectNode o = listingSummary(p);
            o.put("description", Fence.sanitize(p.getDescription()));
            o.put("weight", p.getWeight());
            o.put("reserved", p.getReservedQuantity());
            o.put("sold", p.getSoldQuantity());
            o.put("rating", p.getAverageRating());
            return o;
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Inventory & order health ----
    public ArrayNode inventoryAlerts() {
        ArrayNode arr = Json.MAPPER.createArrayNode();
        try {
            for (Product p : reportDAO.getLowStockProducts(5, 20)) {
                ObjectNode o = Json.MAPPER.createObjectNode();
                o.put("type", p.getStock() == 0 ? "out_of_stock" : "low_stock");
                o.put("productId", p.getId());
                o.put("name", Fence.sanitize(p.getName()));
                o.put("stock", p.getStock());
                arr.add(o);
            }
        } catch (Exception e) {
            log.warn("inventory alerts failed", e);
        }
        return arr;
    }

    public ArrayNode orderIssues() {
        ArrayNode arr = Json.MAPPER.createArrayNode();
        try {
            List<Order> recent = reportDAO.getRecentOrders(50);
            for (Order o : recent) {
                String st = o.getStatus();
                if ("PENDING".equalsIgnoreCase(st) || "FAILED".equalsIgnoreCase(st)
                        || "CANCELLED".equalsIgnoreCase(st)) {
                    ObjectNode issue = Json.MAPPER.createObjectNode();
                    issue.put("type", "PENDING".equalsIgnoreCase(st) ? "awaiting_action" : "exception");
                    issue.put("orderId", o.getId());
                    issue.put("status", Fence.sanitize(st));
                    issue.put("paid", o.getPayment_status());
                    arr.add(issue);
                    if (arr.size() >= 20) break;
                }
            }
        } catch (Exception e) {
            log.warn("order issues failed", e);
        }
        return arr;
    }

    public ObjectNode pricingContext(int productId) {
        try {
            Product p = productDAO.getProductById(productId);
            if (p == null) return null;
            ObjectNode o = Json.MAPPER.createObjectNode();
            o.put("productId", p.getId());
            o.put("currentPriceVnd", p.getPrice() == null ? "0" : p.getPrice().toPlainString());
            o.put("effectivePriceVnd",
                    p.getEffectivePrice() == null ? "0" : p.getEffectivePrice().toPlainString());
            o.put("discountPercent", p.getDisplayDiscountPercent());
            o.put("minPriceBasis", "store rule: never below cost; floor checked at apply");
            return o;
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Staged writes ----
    public StagedChange stage(StagedChange.Kind kind, String summary,
                              List<StagedChange.Item> items, String actor, List<String> notes) {
        StagedChange change = ledger.stage(kind, summary, items, actor, notes);
        changeDAO.save(change);
        return change;
    }

    /**
     * Applies an approved, still-staged change to the live system. Refuses
     * anything not staged, re-checks guardrails, and requires the host
     * approval mark when configured. This method is the platform write.
     */
    public StagedChange apply(String changeId, String actor, boolean hostApproved) {
        StagedChange change = ledger.requireStaged(changeId, "apply");
        List<String> violations = ChangeLedger.checkGuardrails(change.getKind(), change.getItems());
        if (!violations.isEmpty()) throw new ChangeLedger.GuardrailViolation(violations);
        if (MerchantConfig.requireHostApproval() && !hostApproved && !changeDAO.isApproved(changeId)) {
            throw new ChangeLedger.ChangeNotApplicable("change " + changeId
                    + " is staged but not approved on " + MerchantConfig.approvalSurface()
                    + " — approving it there is what applies it");
        }
        writeToLiveSystem(change);
        ledger.apply(changeId, actor);
        changeDAO.save(change);
        return change;
    }

    public StagedChange discard(String changeId, String actor) {
        StagedChange change = ledger.discard(changeId, actor);
        changeDAO.save(change);
        return change;
    }

    /** Executes the staged items against products/promotions tables. */
    private void writeToLiveSystem(StagedChange change) {
        for (StagedChange.Item item : change.getItems()) {
            int productId;
            try {
                productId = Integer.parseInt(item.target());
            } catch (NumberFormatException e) {
                throw new ChangeLedger.ChangeNotApplicable("target '" + item.target() + "' is not a product id");
            }
            try {
                switch (change.getKind()) {
                    case PRICE_UPDATE, PROMOTION -> {
                        Product p = productDAO.getProductById(productId);
                        if (p == null) throw new ChangeLedger.ChangeNotApplicable("product " + productId + " unknown");
                        BigDecimal newPrice = new BigDecimal(item.after().trim());
                        boolean ok = productDAO.updateProduct(p.getId(), p.getName(), p.getImage(),
                                newPrice, p.getDiscount(), p.getDescription(), p.getStock(),
                                p.getWeight(), p.getCategory(), p.getPet_type_id());
                        if (!ok) throw new RuntimeException("price write failed for " + productId);
                    }
                    case INVENTORY_ACTION -> {
                        int newStock = Integer.parseInt(item.after().trim());
                        if (!productDAO.updateStock(productId, newStock)) {
                            throw new RuntimeException("stock write failed for " + productId);
                        }
                    }
                    case LISTING_UPDATE -> {
                        Product p = productDAO.getProductById(productId);
                        if (p == null) throw new ChangeLedger.ChangeNotApplicable("product " + productId + " unknown");
                        String name = p.getName(), desc = p.getDescription(), cat = p.getCategory();
                        if ("name".equalsIgnoreCase(item.field())) name = item.after();
                        else if ("description".equalsIgnoreCase(item.field())) desc = item.after();
                        else if ("category".equalsIgnoreCase(item.field())) cat = item.after();
                        else throw new ChangeLedger.ChangeNotApplicable("field '" + item.field() + "' not editable here");
                        boolean ok = productDAO.updateProduct(p.getId(), name, p.getImage(), p.getPrice(),
                                p.getDiscount(), desc, p.getStock(), p.getWeight(), cat, p.getPet_type_id());
                        if (!ok) throw new RuntimeException("listing write failed for " + productId);
                    }
                    case CAMPAIGN -> throw new ChangeLedger.ChangeNotApplicable(
                            "campaigns are not managed by this store's systems");
                }
            } catch (ChangeLedger.ChangeNotApplicable | ChangeLedger.GuardrailViolation e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("live write failed for target " + item.target(), e);
            }
        }
    }

    /** Read-only analysis query: single SELECT, capped rows/chars, timeout. */
    public ObjectNode analysisQuery(String sql) {
        ObjectNode o = Json.MAPPER.createObjectNode();
        String normalized = sql == null ? "" : sql.trim();
        if (!normalized.regionMatches(true, 0, "SELECT", 0, 6)
                || normalized.contains(";") || normalized.toLowerCase().contains("/*")) {
            o.put("error", "Only a single SELECT statement without comments is allowed");
            return o;
        }
        ArrayNode rows = Json.MAPPER.createArrayNode();
        StringBuilder text = new StringBuilder();
        try (java.sql.Connection c = com.petshop.context.DBContext.getConnection();
             java.sql.Statement st = c.createStatement()) {
            st.setQueryTimeout(10);
            st.setMaxRows(200);
            try (java.sql.ResultSet rs = st.executeQuery(normalized)) {
                java.sql.ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                int count = 0;
                while (rs.next() && text.length() < 8000) {
                    ObjectNode row = Json.MAPPER.createObjectNode();
                    for (int i = 1; i <= cols && text.length() < 8000; i++) {
                        String v = rs.getString(i);
                        row.put(md.getColumnLabel(i), v == null ? "" : v);
                        text.append(v).append("|");
                    }
                    rows.add(row);
                    count++;
                }
                o.put("rowCount", count);
            }
        } catch (Exception e) {
            o.put("error", "Query failed: " + e.getMessage());
            return o;
        }
        o.set("rows", rows);
        return o;
    }

    private static ObjectNode listingSummary(Product p) {
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("id", p.getId());
        o.put("name", Fence.sanitize(p.getName()));
        o.put("priceVnd", p.getPrice() == null ? "0" : p.getPrice().toPlainString());
        o.put("discountPercent", p.getDisplayDiscountPercent());
        o.put("category", Fence.sanitize(p.getCategory()));
        o.put("brand", Fence.sanitize(p.getBrand()));
        o.put("stock", p.getStock());
        o.put("active", p.isActive());
        return o;
    }

    private static String str(BigDecimal v) {
        return v == null ? "0" : v.toPlainString();
    }
}
