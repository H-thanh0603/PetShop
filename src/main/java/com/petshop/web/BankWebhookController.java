package com.petshop.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Util.AppConfig;
import jakarta.servlet.http.HttpServletRequest;
import services.payment.BankWebhookPayload;
import services.payment.BankWebhookReconciliationResult;
import services.payment.BankWebhookReconciliationService;

/**
 * Replaces BankWebhookServlet (/api/payment/bank-webhook) 1:1 — same secret
 * auth (X-Bank-Webhook-Secret / X-Secret-Key / Authorization Bearer+Apikey),
 * same SePay-style payload parsing, same reconcile flow. CsrfFilter already
 * exempts this server-to-server path.
 */
@Controller
public class BankWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(BankWebhookController.class);

    private final BankWebhookReconciliationService reconciliationService;
    private final Gson gson = new Gson();

    public BankWebhookController() {
        this(new BankWebhookReconciliationService());
    }

    BankWebhookController(BankWebhookReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping(value = "/api/payment/bank-webhook", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> bankWebhook(
            @RequestBody(required = false) String rawPayload,
            @RequestHeader(value = "X-Bank-Webhook-Secret", required = false) String bankSecret,
            @RequestHeader(value = "X-Secret-Key", required = false) String secretKey,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) throws IOException {
        if (!isAuthorized(bankSecret, secretKey, authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(write(false, "Webhook secret không hợp lệ.", null));
        }

        try {
            BankWebhookPayload payload = parsePayload(rawPayload == null ? "" : rawPayload);
            BankWebhookReconciliationResult result = reconciliationService.reconcile(payload);
            logger.info("Bank webhook result={} transactionId={} orderId={} paymentTransactionId={} message={}",
                    result.getStatus(), payload.getTransactionId(), result.getOrderId(),
                    result.getPaymentTransactionId(), result.getMessage());
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            return ResponseEntity.ok(gson.toJson(body));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid bank webhook payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(write(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Failed to process bank webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(write(false, "Không xử lý được webhook thanh toán.", null));
        }
    }

    boolean isAuthorized(String bankSecret, String secretKey, String authorization) {
        String configuredSecret = AppConfig.getOrDefault("payment.bank.webhook-secret", "");
        if (configuredSecret.isBlank()) {
            return false;
        }

        String submittedSecret = bankSecret;
        if (submittedSecret == null || submittedSecret.isBlank()) {
            submittedSecret = secretKey;
        }
        if (submittedSecret == null || submittedSecret.isBlank()) {
            submittedSecret = authorizationToken(authorization);
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

    private String write(boolean success, String message, Map<String, Object> extra) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);
        if (extra != null) {
            body.putAll(extra);
        }
        return gson.toJson(body);
    }
}
