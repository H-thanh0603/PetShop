package com.petshop.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import com.petshop.context.DBContext;
import DAO.OrderDAO;
import com.petshop.model.Order;
import com.petshop.util.AppConfig;
import com.petshop.util.Json;
import services.ShippingService;

/**
 * Replaces GhnWebhookServlet (POST /api/ghn/webhook?secret=...) 1:1 — same
 * fail-closed secret auth (header or query param), same order lookup by GHN
 * code, same local-status mapping. CsrfFilter already exempts this
 * server-to-server path.
 */
@Controller
public class GhnWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GhnWebhookController.class);
    private static final String QUERY_PARAM_SECRET = "secret";
    private static final String HEADER_SECRET = "X-GHN-Webhook-Secret";

    private final OrderDAO orderDAO;

    public GhnWebhookController() {
        this(new OrderDAO());
    }

    GhnWebhookController(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    @PostMapping(value = "/api/ghn/webhook", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> ghnWebhook(
            @RequestBody(required = false) String rawBody,
            @RequestHeader(value = "X-GHN-Webhook-Secret", required = false) String headerSecret,
            @RequestParam(value = "secret", required = false) String querySecret,
            jakarta.servlet.http.HttpServletRequest request) throws IOException {
        if (!isAuthorized(headerSecret, querySecret)) {
            log.warn("Rejected unauthorized GHN webhook call from {}", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"Unauthorized\"}");
        }

        try {
            if (rawBody == null || rawBody.isBlank()) {
                return sendError(400, "Empty payload");
            }
            JsonNode parsed = Json.MAPPER.readTree(rawBody);
            if (parsed == null || !parsed.isObject()) {
                throw new IllegalStateException("GHN webhook payload không phải JSON object.");
            }
            ObjectNode payload = (ObjectNode) parsed;

            String orderCode = payload.has("order_code") ? payload.path("order_code").asString() : null;
            String ghnStatus = payload.has("status") ? payload.path("status").asString() : null;
            String trackingCode = payload.has("tracking_code") ? payload.path("tracking_code").asString() : null;

            if (orderCode == null || ghnStatus == null) {
                return sendError(400, "Missing order_code or status");
            }

            // Find local order by GHN order code
            Order order = orderByGhnCode(orderCode);
            if (order == null) {
                log.warn("GHN webhook: order not found for code {}", orderCode);
                return sendError(404, "Order not found");
            }

            // Update GHN status
            orderDAO.updateGhnStatus(order.getId(), ghnStatus, trackingCode);

            // Map GHN status to local status and update if needed
            String localStatus = ShippingService.mapGhnStatusToLocal(ghnStatus);
            if (localStatus != null && !localStatus.equals(order.getStatus())) {
                orderDAO.updateStatus(order.getId(), localStatus, 0); // system update
                log.info("GHN webhook: order {} status changed to {}", order.getId(), localStatus);
            } else {
                log.info("GHN webhook: order {} GHN status updated to {}", order.getId(), ghnStatus);
            }

            return ResponseEntity.ok("{\"success\": true}");

        } catch (Exception e) {
            log.error("GHN webhook error", e);
            return sendError(500, "Internal error");
        }
    }

    boolean isAuthorized(String headerSecret, String querySecret) {
        String configuredSecret = AppConfig.getOrDefault("payment.ghn.webhook-secret", "");
        if (configuredSecret.isBlank()) {
            return false;
        }

        String submittedSecret = headerSecret;
        if (submittedSecret == null || submittedSecret.isBlank()) {
            submittedSecret = querySecret;
        }
        if (submittedSecret == null || submittedSecret.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                submittedSecret.getBytes(StandardCharsets.UTF_8),
                configuredSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    private Order orderByGhnCode(String ghnOrderCode) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM orders WHERE ghn_order_id = ? LIMIT 1")) {
            ps.setString(1, ghnOrderCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int orderId = rs.getInt("id");
                    return orderDAO.getOrderById(orderId);
                }
            }
        } catch (Exception e) {
            log.warn("orderByGhnCode error: {}", e.getMessage());
        }
        return null;
    }

    private ResponseEntity<String> sendError(int code, String message) {
        return ResponseEntity.status(code).body("{\"error\": \"" + message + "\"}");
    }
}
