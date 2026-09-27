package services.ai.common;

import Model.Order;
import Model.Product;
import com.petshop.util.Json;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import services.ai.CommerceTools;

import java.util.List;

/**
 * Presentation port (commerce-common/presentation.py + role enrichment):
 * UI payloads are built server-side from server records only. Ids without
 * provenance are dropped by the caller before reaching here.
 */
public final class Cards {
    private Cards() {}

    public static ObjectNode productCard(Product p) {
        ObjectNode card = Json.MAPPER.createObjectNode();
        card.put("type", "product");
        card.put("id", p.getId());
        card.put("name", Fence.sanitize(p.getName()));
        card.put("priceVnd",
                p.getEffectivePrice() == null ? "0" : p.getEffectivePrice().toPlainString());
        card.put("discountPercent", p.getDisplayDiscountPercent());
        card.put("inStock", p.getStock() > 0);
        card.put("url", "/product-detail?id=" + p.getId());
        return card;
    }

    public static ObjectNode comparisonCard(List<Product> products) {
        ObjectNode card = Json.MAPPER.createObjectNode();
        card.put("type", "comparison");
        ArrayNode rows = Json.MAPPER.createArrayNode();
        for (Product p : products) rows.add(productCard(p));
        card.set("rows", rows);
        return card;
    }

    /** Checkout card: links to the host checkout route; no order is placed. */
    public static ObjectNode checkoutCard(int itemCount, String totalVnd, String checkoutUrl) {
        ObjectNode card = Json.MAPPER.createObjectNode();
        card.put("type", "checkout");
        card.put("itemCount", itemCount);
        card.put("totalVnd", totalVnd);
        String url = checkoutUrl != null && checkoutUrl.startsWith("https://") ? checkoutUrl : "/cart";
        card.put("url", url);
        card.put("note", "Thanh toán được thực hiện tại trang checkout của shop. AI không đặt hàng hay thu tiền.");
        return card;
    }

    public static ObjectNode orderCard(Order o) {
        ObjectNode card = Json.MAPPER.createObjectNode();
        card.put("type", "order");
        card.put("id", o.getId());
        card.put("status", Fence.sanitize(o.getStatus()));
        card.put("statusLabel", Fence.sanitize(o.getStatusLabel()));
        card.put("totalVnd", o.getTotalAmount() == null ? "0" : o.getTotalAmount().toPlainString());
        card.put("url", "/my-orders");
        return card;
    }

    public static ObjectNode changePreviewCard(String changeId, String kind, String summary,
                                               ArrayNode items, List<String> notes) {
        ObjectNode card = Json.MAPPER.createObjectNode();
        card.put("type", "change_preview");
        card.put("changeId", changeId);
        card.put("kind", kind);
        card.put("summary", Fence.sanitize(summary));
        card.set("items", items);
        ArrayNode n = Json.MAPPER.createArrayNode();
        for (String note : Fence.sanitizeChips(notes)) n.add(note);
        card.set("guardrailNotes", n);
        card.put("stagedOnly", true);
        return card;
    }
}
