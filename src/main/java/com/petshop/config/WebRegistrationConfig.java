package com.petshop.config;

import jakarta.servlet.ServletContext;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Legacy servlet registration seam. Phases 0-13 migrated every servlet to
 * Spring MVC controllers 1:1, so this initializer registers nothing today.
 * It stays so a future servlet has one place to land with its URL patterns.
 *
 * Route map (servlet → controller):
 * - admin pages (dashboard/categories/pet-types/reports/statistics/
 *   notifications) → AdminReadController
 * - /admin/orders → AdminOrderController
 * - /pages/admin/products → AdminProductController
 * - /admin/promotions → AdminPromotionController
 * - /admin/users(+/api) → AdminUserController
 * - /admin/inventory → AdminInventoryController
 * - /pages/admin/reviews → AdminReviewController
 * - /admin/upload → AdminUploadController
 * - /api/payment/bank-webhook → BankWebhookController
 * - /api/payment/vnpay-ipn → VnpayIpnController
 * - /api/ghn/webhook → GhnWebhookController
 * - /ai-support/* → UserAiSupportController
 * - /mcp → McpController
 * - /admin/ai-support* → AdminAiSupportController
 * - /admin/ai-merchant* → AdminMerchantAgentController
 * - auth/shop/cart/checkout/orders/notifications/signature → Auth, Page,
 *   Shop, Catalog, Cart, Checkout, MyOrders, Account, ShopApi,
 *   Notification, OrderResult, Signature controllers.
 */
@Configuration
public class WebRegistrationConfig {

    @Bean
    public ServletContextInitializer legacyServletsInitializer() {
        return (ServletContext servletContext) -> {
            // Intentionally empty: no legacy servlets remain.
        };
    }
}
