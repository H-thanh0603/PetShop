package controller.payment;

import Util.AppConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.payment.BankWebhookPayload;
import services.payment.BankWebhookReconciliationResult;
import services.payment.BankWebhookReconciliationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/payment/bank-webhook")
public class BankWebhookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(BankWebhookServlet.class);
    private final BankWebhookReconciliationService reconciliationService = new BankWebhookReconciliationService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (!isAuthorized(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            write(response, false, "Webhook secret không hợp lệ.", null);
            return;
        }

        String rawPayload = request.getReader().lines()
                .reduce("", (left, right) -> left + right);

        try {
            BankWebhookPayload payload = parsePayload(rawPayload);
            BankWebhookReconciliationResult result = reconciliationService.reconcile(payload);
            logger.info("Bank webhook result={} transactionId={} orderId={} paymentTransactionId={} message={}",
                    result.getStatus(), payload.getTransactionId(), result.getOrderId(),
                    result.getPaymentTransactionId(), result.getMessage());
            response.setStatus(HttpServletResponse.SC_OK);
            writeSuccess(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid bank webhook payload: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            write(response, false, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Failed to process bank webhook", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            write(response, false, "Không xử lý được webhook thanh toán.", null);
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        String configuredSecret = AppConfig.getOrDefault("payment.bank.webhook-secret", "");
        if (configuredSecret.isBlank()) {
            return false;
        }

        String submittedSecret = request.getHeader("X-Bank-Webhook-Secret");
        if (submittedSecret == null || submittedSecret.isBlank()) {
            submittedSecret = request.getHeader("X-Secret-Key");
        }
        if (submittedSecret == null || submittedSecret.isBlank()) {
            submittedSecret = authorizationToken(request.getHeader("Authorization"));
        }
        if (submittedSecret == null || submittedSecret.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                submittedSecret.getBytes(StandardCharsets.UTF_8),
                configuredSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    private BankWebhookPayload parsePayload(String rawPayload) {
        JsonObject json = new JsonParser().parse(rawPayload).getAsJsonObject();
        String transferType = getOptionalString(json, "transferType");
        if (transferType != null && !"in".equalsIgnoreCase(transferType.trim())) {
            throw new IllegalArgumentException("Webhook không phải giao dịch tiền vào.");
        }

        String transactionId = firstRequiredString(json, "referenceCode", "reference_code", "transaction_id",
                "transactionId", "id");
        BigDecimal amount = new BigDecimal(firstRequiredString(json, "transferAmount", "transfer_amount", "amount"));
        String content = firstRequiredString(json, "content", "description", "transactionContent",
                "transaction_content", "bank_content");
        String bankAccount = firstOptionalString(json, "accountNumber", "account_number", "bank_account");
        LocalDateTime paidAt = parseTime(firstOptionalString(json, "transactionDate", "transaction_date", "time"));
        return new BankWebhookPayload(transactionId, amount, content, bankAccount, rawPayload, paidAt);
    }

    private String firstRequiredString(JsonObject json, String... fieldNames) {
        String value = firstOptionalString(json, fieldNames);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Webhook thiếu trường " + String.join("/", fieldNames) + ".");
        }
        return value.trim();
    }

    private String firstOptionalString(JsonObject json, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = getOptionalString(json, fieldName);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String getOptionalString(JsonObject json, String fieldName) {
        if (!json.has(fieldName) || json.get(fieldName).isJsonNull()) {
            return null;
        }
        return json.get(fieldName).getAsString();
    }

    private LocalDateTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String authorizationToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        }
        String[] prefixes = {"Bearer ", "Apikey ", "ApiKey ", "Api-Key "};
        for (String prefix : prefixes) {
            if (authorizationHeader.regionMatches(true, 0, prefix, 0, prefix.length())) {
                return authorizationHeader.substring(prefix.length()).trim();
            }
        }
        return null;
    }

    private void writeSuccess(HttpServletResponse response) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        response.getWriter().write(gson.toJson(body));
    }

    private void write(HttpServletResponse response, boolean success, String message, Map<String, Object> extra)
            throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);
        if (extra != null) {
            body.putAll(extra);
        }
        response.getWriter().write(gson.toJson(body));
    }
}
