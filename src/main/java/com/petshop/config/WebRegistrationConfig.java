package com.petshop.config;

import controller.admin.AdminAiSupportServlet;
import controller.admin.AdminMerchantAgentServlet;
import controller.payment.BankWebhookServlet;
import controller.payment.GhnWebhookServlet;
import controller.payment.VnpayIpnServlet;
import controller.shop.UserAiSupportServlet;
import jakarta.servlet.ServletContext;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServlet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

/**
 * Registers the legacy servlets (previously @WebServlet-annotated) with their
 * original URL patterns. Mappings were extracted 1:1 from the annotations at
 * migration time; behaviour is unchanged.
 */
@Configuration
public class WebRegistrationConfig {

    @Bean
    public ServletContextInitializer legacyServletsInitializer() {
        return (ServletContext servletContext) -> {
            register(servletContext, "AdminAiSupportServlet", controller.admin.AdminAiSupportServlet::new, "/admin/ai-support", "/admin/ai-support/dashboard", "/admin/ai-support/sessions", "/admin/ai-support/sessions/detail", "/admin/ai-support/sessions/reply", "/admin/ai-support/sessions/close", "/admin/ai-support/knowledge", "/admin/ai-support/settings");
            register(servletContext, "AdminMerchantAgentServlet", controller.admin.AdminMerchantAgentServlet::new, "/admin/ai-merchant", "/admin/ai-merchant/chat", "/admin/ai-merchant/pending", "/admin/ai-merchant/approve", "/admin/ai-merchant/apply", "/admin/ai-merchant/discard", "/admin/ai-merchant/digest", "/admin/ai-merchant/memory", "/admin/ai-merchant/escalations");
            // /pages/admin/categories served by AdminReadController.
            // /pages/admin/dashboard served by AdminReadController.
            // /admin/inventory served by AdminInventoryController.
            // /admin/orders served by AdminOrderController.
            // /pages/admin/products served by AdminProductController.
            // /admin/promotions served by AdminPromotionController.
            // /pages/admin/reviews served by AdminReviewController.
            // /admin/users + /admin/users/api served by AdminUserController.
            // /admin/login served by AuthController.
            // /forgot-password, /verify-otp, /reset-password served by AuthController.
            // /LoginByFacebookServlet + /LoginByGoogleServlet served by AuthController
            // (same legacy paths so OAuth provider consoles keep working).
            // /login served by AuthController.
            // /logout served by AuthController.
            // /register served by AuthController.
            // /verify-email served by AuthController.
            // About/Home/Policy served by com.petshop.web.PageController (Spring MVC).
            // HomeServlet, AboutServlet, PolicyServlet removed.
            register(servletContext, "BankWebhookServlet", controller.payment.BankWebhookServlet::new, "/api/payment/bank-webhook");
            register(servletContext, "VnpayIpnServlet", VnpayIpnServlet::new, "/api/payment/vnpay-ipn");
            register(servletContext, "GhnWebhookServlet", controller.payment.GhnWebhookServlet::new, "/api/ghn/webhook");
            // /add-review served by ShopApiController.
            // /add-to-cart served by CartController (addToCart).
            // /cart served by CartController.
            // /checkout served by CheckoutController.
            // /my-orders served by MyOrdersController.
            // /product-detail + /wishlist served by CatalogController.
            // /api/search-autocomplete served by ShopApiController.
            // /shop served by ShopController.
            // /toggle-wishlist served by CatalogController.
            register(servletContext, "UserAiSupportServlet", controller.shop.UserAiSupportServlet::new, "/ai-support/chat", "/ai-support/history", "/ai-support/messages", "/ai-support/unread-count", "/ai-support/stream");
            register(servletContext, "McpServlet", controller.shop.McpServlet::new, "/mcp");
            // /notifications/* served by NotificationController.
            // /vnpay-return served by ShopApiController.
            // /wishlist served by CatalogController.
            // /order-success served by OrderResultController.
            // /addresses + /my-account + /update-profile-checkout served by AccountController.
            // /user/download-private-key + /user/upload-signature served by SignatureController.
            // /admin/upload served by AdminUploadController (multipart via Spring,
            // spring.servlet.multipart limits in application.yml).
        };
    }

    private static ServletRegistration.Dynamic register(ServletContext servletContext, String name,
                                                        Supplier<HttpServlet> factory, String... urlPatterns)
            throws ServletException {
        ServletRegistration.Dynamic registration = servletContext.addServlet(name, factory.get());
        registration.addMapping(urlPatterns);
        return registration;
    }
}
