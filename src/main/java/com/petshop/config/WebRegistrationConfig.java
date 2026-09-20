package com.petshop.config;

import controller.admin.AdminAiSupportServlet;
import controller.admin.AdminMerchantAgentServlet;
import controller.admin.CategoryServlet;
import controller.admin.DashboardServlet;
import controller.admin.FileUploadServlet;
import controller.admin.InventoryServlet;
import controller.admin.ManageOrderServlet;
import controller.admin.NotificationServlet;
import controller.admin.PetTypeServlet;
import controller.admin.ProductServlet;
import controller.admin.PromotionServlet;
import controller.admin.ReportServlet;
import controller.admin.ReviewModerationServlet;
import controller.admin.StatisticsServlet;
import controller.admin.UserManageServlet;
import controller.payment.BankWebhookServlet;
import controller.payment.GhnWebhookServlet;
import controller.payment.VnpayIpnServlet;
import controller.shop.AddReviewServlet;
import controller.shop.CheckoutServlet;
import controller.shop.SearchAutocompleteServlet;
import controller.shop.UserAiSupportServlet;
import controller.shop.UserNotificationServlet;
import controller.shop.VnpayReturnServlet;
import controller.updateinformation.AddressServlet;
import controller.updateinformation.MyAccountServlet;
import controller.updateinformation.UpdateProfileCheckoutServlet;
import controller.user.DownloadPrivateKeyServlet;
import controller.user.UploadSignatureServlet;
import jakarta.servlet.MultipartConfigElement;
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
            register(servletContext, "CategoryServlet", controller.admin.CategoryServlet::new, "/pages/admin/categories");
            register(servletContext, "DashboardServlet", controller.admin.DashboardServlet::new, "/pages/admin/dashboard");
            register(servletContext, "InventoryServlet", controller.admin.InventoryServlet::new, "/admin/inventory");
            register(servletContext, "ManageOrderServlet", controller.admin.ManageOrderServlet::new, "/admin/orders");
            register(servletContext, "NotificationServlet", controller.admin.NotificationServlet::new, "/admin/notifications");
            register(servletContext, "PetTypeServlet", controller.admin.PetTypeServlet::new, "/pages/admin/pet-types");
            register(servletContext, "ProductServlet", controller.admin.ProductServlet::new, "/pages/admin/products");
            register(servletContext, "PromotionServlet", controller.admin.PromotionServlet::new, "/admin/promotions");
            register(servletContext, "ReportServlet", controller.admin.ReportServlet::new, "/admin/reports");
            register(servletContext, "ReviewModerationServlet", controller.admin.ReviewModerationServlet::new, "/pages/admin/reviews");
            register(servletContext, "StatisticsServlet", controller.admin.StatisticsServlet::new, "/admin/statistics");
            register(servletContext, "UserManageServlet", controller.admin.UserManageServlet::new, "/admin/users", "/admin/users/api");
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
            register(servletContext, "AddReviewServlet", controller.shop.AddReviewServlet::new, "/add-review");
            // /add-to-cart served by CartController (addToCart).
            // /cart served by CartController.
            register(servletContext, "CheckoutServlet", controller.shop.CheckoutServlet::new, "/checkout");
            // /my-orders served by MyOrdersController.
            // /product-detail + /wishlist served by CatalogController.
            register(servletContext, "SearchAutocompleteServlet", controller.shop.SearchAutocompleteServlet::new, "/api/search-autocomplete");
            // /shop served by ShopController.
            // /toggle-wishlist served by CatalogController.
            register(servletContext, "UserAiSupportServlet", controller.shop.UserAiSupportServlet::new, "/ai-support/chat", "/ai-support/history", "/ai-support/messages", "/ai-support/unread-count", "/ai-support/stream");
            register(servletContext, "McpServlet", controller.shop.McpServlet::new, "/mcp");
            register(servletContext, "UserNotificationServlet", controller.shop.UserNotificationServlet::new, "/notifications/unread-count", "/notifications/list", "/notifications/mark-read");
            register(servletContext, "VnpayReturnServlet", controller.shop.VnpayReturnServlet::new, "/vnpay-return");
            // /wishlist served by CatalogController.
            // /order-success served by OrderResultController.
            register(servletContext, "AddressServlet", controller.updateinformation.AddressServlet::new, "/addresses");
            register(servletContext, "MyAccountServlet", controller.updateinformation.MyAccountServlet::new, "/my-account");
            register(servletContext, "UpdateProfileCheckoutServlet", controller.updateinformation.UpdateProfileCheckoutServlet::new, "/update-profile-checkout");
            register(servletContext, "DownloadPrivateKeyServlet", controller.user.DownloadPrivateKeyServlet::new, "/user/download-private-key");
            register(servletContext, "UploadSignatureServlet", controller.user.UploadSignatureServlet::new, "/user/upload-signature");

            // FileUploadServlet needs an explicit multipart config: programmatic
            // registration does not process the (removed) @MultipartConfig.
            ServletRegistration.Dynamic upload = register(servletContext, "FileUploadServlet",
                    FileUploadServlet::new, "/admin/upload");
            upload.setMultipartConfig(new MultipartConfigElement("",
                    5L * 1024 * 1024, 10L * 1024 * 1024, 1024 * 1024));
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
