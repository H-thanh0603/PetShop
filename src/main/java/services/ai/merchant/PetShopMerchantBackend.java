package services.ai.merchant;

import DAO.OrderDAO;
import DAO.ProductDAO;
import DAO.PromotionDAO;
import DAO.ReportDAO;
import Model.Order;
import Model.Product;
import Model.Promotion;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
    private final ChangeLedger ledger = new ChangeLedger();
    private final MerchantChangeDAO changeDAO = new MerchantChangeDAO();

    public ChangeLedger ledger() { return ledger; }

    // ---- Performance ----
    public JsonObject businessSnapshot() {
        JsonObject o = new JsonObject();
        try {
            o.addProperty("totalRevenueVnd", str(reportDAO.getTotalRevenue()));
            o.addProperty("monthRevenueVnd", str(reportDAO.getCurrentMonthRevenue()));
            o.addProperty("completedOrders", reportDAO.getCompletedOrdersCount());
            JsonObject byStatus = new JsonObject();
            for (Map<String, Object> row : reportDAO.getOrdersByStatus()) {
                byStatus.addProperty(String.valueOf(row.get("status")),
                        ((Number) row.get("count")).intValue());
            }
            o.add("ordersByStatus", byStatus);
            JsonArray top = new JsonArray();
            for (Map<String, Object> row : reportDAO.getTopSellingProducts(5)) {
                top.add(row.get("product") + " (đã bán " + row.get("count") + ")");
            }
            o.add("topSellers", top);
        } catch (Exception e) {
            o.addProperty("error", "Snapshot temporarily unavailable");
        }
        o.addProperty("note", "Email channel reports no revenue (limitation)");
        return o;
    }

    public JsonObject queryMetrics(String metric, String segment) {
        JsonObject o = new JsonObject();
        o.addProperty("metric", Fence.sanitize(metric));
        try {
            switch (metric.toLowerCase()) {
                case "revenue_by_month" -> {
                    int year = java.time.LocalDate.now().getYear();
                    JsonArray points = new JsonArray();
                    for (Map<String, Object> row : reportDAO.getRevenueByMonth(year)) {
                        JsonObject p = new JsonObject();
                        p.addProperty("month", String.valueOf(row.get("month")));
                        p.addProperty("revenueVnd", String.valueOf(row.get("revenue")));
                        points.add(p);
                    }
                    o.add("points", points);
                }
                case "top_sellers" -> {
                    JsonArray points = new JsonArray();
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
                        JsonObject pt = new JsonObject();
                        pt.addProperty("product", Fence.sanitize(name));
                        pt.addProperty("sold", ((Number) row.get("count")).intValue());
                        points.add(pt);
                    }
                    o.add("points", points);
                }
                default -> o.addProperty("note", "Metric '" + Fence.sanitize(metric)
                        + "' is not supplied by this store (supported: revenue_by_month, top_sellers)");
            }
        } catch (Exception e) {
            o.addProperty("error", "Metrics temporarily unavailable");
        }
        return o;
    }

    public JsonArray campaignPerformance() {
        JsonArray arr = new JsonArray();
        try {
            for (Promotion promo : promotionDAO.getAllPromotions()) {
                JsonObject o = new JsonObject();
                o.addProperty("id", promo.getId());
                o.addProperty("name", Fence.sanitize(promo.getName()));
                o.addProperty("type", Fence.sanitize(promo.getPromotionType()));
                o.addProperty("status", Fence.sanitize(promo.getStatus()));
                o.addProperty("discount", promo.getDiscountValue() == null ? ""
                        : promo.getDiscountValue().toPlainString() + " " + promo.getDiscountType());
                o.add("spend", null);
                o.add("revenue", null);
                arr.add(o);
            }
        } catch (Exception e) {
            log.warn("campaign read failed", e);
        }
        return arr;
    }

    // ---- Catalog ----
    public JsonArray searchListings(String query, int limit) {
        JsonArray arr = new JsonArray();
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

    public JsonObject getListing(int productId) {
        try {
            Product p = productDAO.getProductById(productId);
            if (p == null) return null;
            JsonObject o = listingSummary(p);
            o.addProperty("description", Fence.sanitize(p.getDescription()));
            o.addProperty("weight", p.getWeight());
            o.addProperty("reserved", p.getReservedQuantity());
            o.addProperty("sold", p.getSoldQuantity());
            o.addProperty("rating", p.getAverageRating());
            return o;
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Inventory & order health ----
    public JsonArray inventoryAlerts() {
        JsonArray arr = new JsonArray();
        try {
            for (Product p : reportDAO.getLowStockProducts(5, 20)) {
                JsonObject o = new JsonObject();
                o.addProperty("type", p.getStock() == 0 ? "out_of_stock" : "low_stock");
                o.addProperty("productId", p.getId());
                o.addProperty("name", Fence.sanitize(p.getName()));
                o.addProperty("stock", p.getStock());
                arr.add(o);
            }
        } catch (Exception e) {
            log.warn("inventory alerts failed", e);
        }
        return arr;
    }

    public JsonArray orderIssues() {
        JsonArray arr = new JsonArray();
        try {
            List<Order> recent = reportDAO.getRecentOrders(50);
            for (Order o : recent) {
                String st = o.getStatus();
                if ("PENDING".equalsIgnoreCase(st) || "FAILED".equalsIgnoreCase(st)
                        || "CANCELLED".equalsIgnoreCase(st)) {
                    JsonObject issue = new JsonObject();
                    issue.addProperty("type", "PENDING".equalsIgnoreCase(st) ? "awaiting_action" : "exception");
                    issue.addProperty("orderId", o.getId());
                    issue.addProperty("status", Fence.sanitize(st));
                    issue.addProperty("paid", o.getPayment_status());
                    arr.add(issue);
                    if (arr.size() >= 20) break;
                }
            }
        } catch (Exception e) {
            log.warn("order issues failed", e);
        }
        return arr;
    }

    public JsonObject pricingContext(int productId) {
        try {
            Product p = productDAO.getProductById(productId);
            if (p == null) return null;
            JsonObject o = new JsonObject();
            o.addProperty("productId", p.getId());
            o.addProperty("currentPriceVnd", p.getPrice() == null ? "0" : p.getPrice().toPlainString());
            o.addProperty("effectivePriceVnd",
                    p.getEffectivePrice() == null ? "0" : p.getEffectivePrice().toPlainString());
            o.addProperty("discountPercent", p.getDisplayDiscountPercent());
            o.addProperty("minPriceBasis", "store rule: never below cost; floor checked at apply");
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
    public JsonObject analysisQuery(String sql) {
        JsonObject o = new JsonObject();
        String normalized = sql == null ? "" : sql.trim();
        if (!normalized.regionMatches(true, 0, "SELECT", 0, 6)
                || normalized.contains(";") || normalized.toLowerCase().contains("/*")) {
            o.addProperty("error", "Only a single SELECT statement without comments is allowed");
            return o;
        }
        JsonArray rows = new JsonArray();
        StringBuilder text = new StringBuilder();
        try (java.sql.Connection c = Context.DBContext.getConnection();
             java.sql.Statement st = c.createStatement()) {
            st.setQueryTimeout(10);
            st.setMaxRows(200);
            try (java.sql.ResultSet rs = st.executeQuery(normalized)) {
                java.sql.ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                int count = 0;
                while (rs.next() && text.length() < 8000) {
                    JsonObject row = new JsonObject();
                    for (int i = 1; i <= cols && text.length() < 8000; i++) {
                        String v = rs.getString(i);
                        row.addProperty(md.getColumnLabel(i), v == null ? "" : v);
                        text.append(v).append("|");
                    }
                    rows.add(row);
                    count++;
                }
                o.addProperty("rowCount", count);
            }
        } catch (Exception e) {
            o.addProperty("error", "Query failed: " + e.getMessage());
            return o;
        }
        o.add("rows", rows);
        return o;
    }

    private static JsonObject listingSummary(Product p) {
        JsonObject o = new JsonObject();
        o.addProperty("id", p.getId());
        o.addProperty("name", Fence.sanitize(p.getName()));
        o.addProperty("priceVnd", p.getPrice() == null ? "0" : p.getPrice().toPlainString());
        o.addProperty("discountPercent", p.getDisplayDiscountPercent());
        o.addProperty("category", Fence.sanitize(p.getCategory()));
        o.addProperty("brand", Fence.sanitize(p.getBrand()));
        o.addProperty("stock", p.getStock());
        o.addProperty("active", p.isActive());
        return o;
    }

    private static String str(BigDecimal v) {
        return v == null ? "0" : v.toPlainString();
    }
}
