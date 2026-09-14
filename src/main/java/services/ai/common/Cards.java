package services.ai.common;

import Model.Order;
import Model.Product;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import services.ai.CommerceTools;

import java.util.List;

/**
 * Presentation port (commerce-common/presentation.py + role enrichment):
 * UI payloads are built server-side from server records only. Ids without
 * provenance are dropped by the caller before reaching here.
 */
public final class Cards {
    private Cards() {}

    public static JsonObject productCard(Product p) {
        JsonObject card = new JsonObject();
        card.addProperty("type", "product");
        card.addProperty("id", p.getId());
        card.addProperty("name", Fence.sanitize(p.getName()));
        card.addProperty("priceVnd",
                p.getEffectivePrice() == null ? "0" : p.getEffectivePrice().toPlainString());
        card.addProperty("discountPercent", p.getDisplayDiscountPercent());
        card.addProperty("inStock", p.getStock() > 0);
        card.addProperty("url", "/product-detail?id=" + p.getId());
        return card;
    }

    public static JsonObject comparisonCard(List<Product> products) {
        JsonObject card = new JsonObject();
        card.addProperty("type", "comparison");
        JsonArray rows = new JsonArray();
        for (Product p : products) rows.add(productCard(p));
        card.add("rows", rows);
        return card;
    }

    /** Checkout card: links to the host checkout route; no order is placed. */
    public static JsonObject checkoutCard(int itemCount, String totalVnd, String checkoutUrl) {
        JsonObject card = new JsonObject();
        card.addProperty("type", "checkout");
        card.addProperty("itemCount", itemCount);
        card.addProperty("totalVnd", totalVnd);
        String url = checkoutUrl != null && checkoutUrl.startsWith("https://") ? checkoutUrl : "/cart";
        card.addProperty("url", url);
        card.addProperty("note", "Thanh toán được thực hiện tại trang checkout của shop. AI không đặt hàng hay thu tiền.");
        return card;
    }

    public static JsonObject orderCard(Order o) {
        JsonObject card = new JsonObject();
        card.addProperty("type", "order");
        card.addProperty("id", o.getId());
        card.addProperty("status", Fence.sanitize(o.getStatus()));
        card.addProperty("statusLabel", Fence.sanitize(o.getStatusLabel()));
        card.addProperty("totalVnd", o.getTotalAmount() == null ? "0" : o.getTotalAmount().toPlainString());
        card.addProperty("url", "/my-orders");
        return card;
    }

    public static JsonObject changePreviewCard(String changeId, String kind, String summary,
                                               JsonArray items, List<String> notes) {
        JsonObject card = new JsonObject();
        card.addProperty("type", "change_preview");
        card.addProperty("changeId", changeId);
        card.addProperty("kind", kind);
        card.addProperty("summary", Fence.sanitize(summary));
        card.add("items", items);
        JsonArray n = new JsonArray();
        for (String note : Fence.sanitizeChips(notes)) n.add(note);
        card.add("guardrailNotes", n);
        card.addProperty("stagedOnly", true);
        return card;
    }
}
