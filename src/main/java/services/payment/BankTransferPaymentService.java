package services.payment;

import Util.PaymentCryptoUtil;
import Util.SecretConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BankTransferPaymentService {
    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public PaymentGatewayInitResult createPayment(PaymentGatewayRequest request) {
        String createUrl = trim(SecretConfig.get("bank_gateway_create_url"));
        String partnerCode = trim(SecretConfig.get("bank_gateway_partner_code"));
        String apiKey = trim(SecretConfig.get("bank_gateway_api_key"));
        String secretKey = trim(SecretConfig.get("bank_gateway_secret_key"));

        if (isBlank(createUrl) || isBlank(partnerCode) || isBlank(apiKey) || isBlank(secretKey)) {
            List<String> missingKeys = SecretConfig.getMissingKeys(
                    "bank_gateway_create_url",
                    "bank_gateway_partner_code",
                    "bank_gateway_api_key",
                    "bank_gateway_secret_key"
            );
            return PaymentGatewayInitResult.failure(
                    "CONFIG_MISSING",
                    "Chua cau hinh bank gateway: " + String.join(", ", missingKeys),
                    null
            );
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("partnerCode", partnerCode);
        payload.addProperty("apiKey", apiKey);
        payload.addProperty("requestId", request.getRequestId());
        payload.addProperty("orderId", request.getProviderOrderId());
        payload.addProperty("amount", request.getAmount());
        payload.addProperty("orderInfo", request.getOrderInfo());
        payload.addProperty("returnUrl", request.getRedirectUrl());
        payload.addProperty("ipnUrl", request.getIpnUrl());
        payload.addProperty("clientIp", request.getClientIp());
        payload.addProperty("customerName", defaultString(request.getCustomerName()));
        payload.addProperty("customerEmail", defaultString(request.getCustomerEmail()));
        payload.addProperty("customerPhone", defaultString(request.getCustomerPhone()));

        TreeMap<String, String> signData = new TreeMap<>();
        signData.put("amount", String.valueOf(request.getAmount()));
        signData.put("ipnUrl", request.getIpnUrl());
        signData.put("orderId", request.getProviderOrderId());
        signData.put("orderInfo", defaultString(request.getOrderInfo()));
        signData.put("partnerCode", partnerCode);
        signData.put("requestId", request.getRequestId());
        signData.put("returnUrl", request.getRedirectUrl());
        payload.addProperty(
                "signature",
                PaymentCryptoUtil.hmacSha256(secretKey, PaymentCryptoUtil.buildSortedQueryString(signData))
        );

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(createUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject body = gson.fromJson(response.body(), JsonObject.class);

            String responseCode = firstNonBlank(
                    getString(body, "code"),
                    getString(body, "resultCode"),
                    getString(body, "statusCode"),
                    String.valueOf(response.statusCode())
            );
            String message = firstNonBlank(
                    getString(body, "message"),
                    getString(body, "resultMessage"),
                    "Khong tao duoc giao dich bank."
            );
            String redirectUrl = firstNonBlank(
                    getString(body, "paymentUrl"),
                    getString(body, "payUrl"),
                    getString(body, "checkoutUrl"),
                    getString(body, "redirectUrl")
            );
            String returnedRequestId = firstNonBlank(getString(body, "requestId"), request.getRequestId());

            if (response.statusCode() >= 200 && response.statusCode() < 300
                    && isSuccessfulCode(responseCode)
                    && !isBlank(redirectUrl)) {
                return PaymentGatewayInitResult.success(
                        redirectUrl,
                        returnedRequestId,
                        responseCode,
                        message,
                        response.body()
                );
            }

            return PaymentGatewayInitResult.failure(responseCode, message, response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentGatewayInitResult.failure("HTTP_ERROR", "Khong ket noi duoc cong thanh toan bank.", e.getMessage());
        } catch (IOException e) {
            return PaymentGatewayInitResult.failure("HTTP_ERROR", "Khong ket noi duoc cong thanh toan bank.", e.getMessage());
        }
    }

    public boolean validateCallbackSignature(Map<String, String> params) {
        String secretKey = trim(SecretConfig.get("bank_gateway_secret_key"));
        if (isBlank(secretKey)) {
            return false;
        }

        String receivedSignature = firstNonBlank(params.get("signature"), params.get("secureHash"), params.get("checksum"));
        if (isBlank(receivedSignature)) {
            return false;
        }

        TreeMap<String, String> signParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if ("signature".equalsIgnoreCase(key) || "secureHash".equalsIgnoreCase(key) || "checksum".equalsIgnoreCase(key)) {
                continue;
            }
            signParams.put(key, defaultString(entry.getValue()));
        }

        String payload = PaymentCryptoUtil.buildSortedQueryString(signParams);
        return PaymentCryptoUtil.hmacSha256(secretKey, payload).equalsIgnoreCase(receivedSignature);
    }

    public boolean isSuccess(Map<String, String> params) {
        String status = firstNonBlank(params.get("status"), params.get("transactionStatus"));
        String code = firstNonBlank(params.get("code"), params.get("resultCode"), params.get("responseCode"));
        return "SUCCESS".equalsIgnoreCase(status)
                || "PAID".equalsIgnoreCase(status)
                || isSuccessfulCode(code);
    }

    public boolean isCancelled(Map<String, String> params) {
        String status = firstNonBlank(params.get("status"), params.get("transactionStatus"));
        String code = firstNonBlank(params.get("code"), params.get("resultCode"), params.get("responseCode"));
        return "CANCELLED".equalsIgnoreCase(status)
                || "CANCELED".equalsIgnoreCase(status)
                || "USER_CANCELLED".equalsIgnoreCase(status)
                || "24".equals(code);
    }

    public String getProviderOrderId(Map<String, String> params) {
        return firstNonBlank(params.get("orderId"), params.get("providerOrderId"), params.get("merchantOrderId"));
    }

    public String getResultCode(Map<String, String> params) {
        return firstNonBlank(params.get("code"), params.get("resultCode"), params.get("responseCode"));
    }

    public String getMessage(Map<String, String> params) {
        String explicit = firstNonBlank(params.get("message"), params.get("resultMessage"));
        if (!isBlank(explicit)) {
            return explicit;
        }
        if (isCancelled(params)) {
            return "Khach hang da huy giao dich tren cong thanh toan bank.";
        }
        if (isSuccess(params)) {
            return "Thanh toan bank thanh cong.";
        }
        return "Thanh toan bank that bai.";
    }

    public String getPaymentToken(Map<String, String> params) {
        return firstNonBlank(params.get("paymentToken"), params.get("transactionId"), params.get("transId"));
    }

    public String getProviderTransactionId(Map<String, String> params) {
        return firstNonBlank(params.get("bankTransactionId"), params.get("transactionId"), params.get("transId"));
    }

    private String getString(JsonObject body, String key) {
        return body != null && body.has(key) && !body.get(key).isJsonNull() ? body.get(key).getAsString() : null;
    }

    private boolean isSuccessfulCode(String code) {
        return "0".equals(code) || "00".equals(code) || "SUCCESS".equalsIgnoreCase(code);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
