package controller.payment;

import Context.DBContext;
import DAO.OrderDAO;
import Model.Order;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ShippingService;
import Util.AppConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Webhook endpoint for GHN to push order status updates.
 * GHN calls this when a shipper updates the order status on their system.
 *
 * POST /api/ghn/webhook?secret={webhook-secret}
 * Body: { "order_code": "GHN123", "status": "delivering", "tracking_code": "VN123" }
 *
 * Authentication: the request must present the configured
 * payment.ghn.webhook-secret (via the X-GHN-Webhook-Secret header or the
 * "secret" query parameter, so the secret can be embedded in the webhook URL
 * registered on the GHN seller portal). When no secret is configured the
 * endpoint rejects every request (fail closed).
 */
@WebServlet("/api/ghn/webhook")
public class GhnWebhookServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(GhnWebhookServlet.class);
    private static final String QUERY_PARAM_SECRET = "secret";
    private static final String HEADER_SECRET = "X-GHN-Webhook-Secret";
    private final OrderDAO orderDAO = new OrderDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAuthorized(request)) {
            log.warn("Rejected unauthorized GHN webhook call from {}", request.getRemoteAddr());
            sendError(response, 401, "Unauthorized");
            return;
        }

        // Read request body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        try {
            JsonObject payload = gson.fromJson(sb.toString(), JsonObject.class);
            if (payload == null) {
                sendError(response, 400, "Empty payload");
                return;
            }

            String orderCode = payload.has("order_code") ? payload.get("order_code").getAsString() : null;
            String ghnStatus = payload.has("status") ? payload.get("status").getAsString() : null;
            String trackingCode = payload.has("tracking_code") ? payload.get("tracking_code").getAsString() : null;

            if (orderCode == null || ghnStatus == null) {
                sendError(response, 400, "Missing order_code or status");
                return;
            }

            // Find local order by GHN order code
            Order order = orderByGhnCode(orderCode);
            if (order == null) {
                log.warn("GHN webhook: order not found for code {}", orderCode);
                sendError(response, 404, "Order not found");
                return;
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

            response.setStatus(200);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": true}");

        } catch (Exception e) {
            log.error("GHN webhook error", e);
            sendError(response, 500, "Internal error");
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        String configuredSecret = AppConfig.getOrDefault("payment.ghn.webhook-secret", "");
        if (configuredSecret.isBlank()) {
            return false;
        }

        String submittedSecret = request.getHeader(HEADER_SECRET);
        if (submittedSecret == null || submittedSecret.isBlank()) {
            submittedSecret = request.getParameter(QUERY_PARAM_SECRET);
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

    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
