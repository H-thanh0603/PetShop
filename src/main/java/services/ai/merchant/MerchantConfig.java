package services.ai.merchant;

import Util.AppConfig;

import java.util.List;
import java.util.Set;

/**
 * Per-deployment merchant settings (port of merchant_agent/config.py).
 * Guardrail defaults are demonstration values — tune for the real store.
 */
public final class MerchantConfig {
    private MerchantConfig() {}

    public static int maxItemsPerChange() { return AppConfig.getInt("MERCHANT_MAX_ITEMS_PER_CHANGE", 25); }
    public static double maxPriceDeltaPct() { return doubleOf("MERCHANT_MAX_PRICE_DELTA_PCT", 20.0); }
    public static double maxPromotionDiscountPct() { return doubleOf("MERCHANT_MAX_PROMOTION_DISCOUNT_PCT", 50.0); }
    public static int maxRestockQuantity() { return AppConfig.getInt("MERCHANT_MAX_RESTOCK_QTY", 500); }
    public static double maxCampaignBudget() { return doubleOf("MERCHANT_MAX_CAMPAIGN_BUDGET", 10_000.0); }

    public static Set<String> protectedFields() {
        return Set.of("id", "listing_id", "currency", "tax_category", "compliance_notes");
    }

    public static Set<String> priceBearingFields() { return Set.of("price"); }

    /** Fields a listing update may not carry (route through price/inventory tools). */
    public static Set<String> listingUpdateBlockedFields() { return Set.of("price", "stock"); }

    public static boolean enableListingEdits() { return AppConfig.getBoolean("MERCHANT_ENABLE_LISTING_EDITS", true); }
    public static boolean enableInventory() { return AppConfig.getBoolean("MERCHANT_ENABLE_INVENTORY", true); }
    public static boolean enablePricing() { return AppConfig.getBoolean("MERCHANT_ENABLE_PRICING", true); }
    public static boolean enableCampaigns() { return AppConfig.getBoolean("MERCHANT_ENABLE_CAMPAIGNS", true); }

    public static boolean stagesChanges() {
        return enableListingEdits() || enableInventory() || enablePricing() || enableCampaigns();
    }

    /** Host approval required before apply (default true — never auto-apply from chat). */
    public static boolean requireHostApproval() {
        return AppConfig.getBoolean("MERCHANT_REQUIRE_HOST_APPROVAL", true);
    }

    public static String approvalSurface() {
        return AppConfig.getOrDefault("MERCHANT_APPROVAL_SURFACE", "trang quản trị (Admin > AI Merchant)");
    }

    public static List<String> limitations() {
        return List.of(
                "campaigns:email: kênh email không báo cáo doanh thu",
                "orders:history: lịch sử phân tích đầy đủ nhất từ bảng orders hiện tại");
    }

    private static double doubleOf(String key, double def) {
        try {
            String v = AppConfig.get(key);
            return v == null ? def : Double.parseDouble(v.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
