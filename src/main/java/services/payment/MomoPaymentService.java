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

public class MomoPaymentService {
    private static final String DEFAULT_CREATE_URL = "https://test-payment.momo.vn/v2/gateway/api/create";
    private static final String REQUEST_TYPE = "captureWallet";

    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public PaymentGatewayInitResult createPayment(PaymentGatewayRequest request) {
        String partnerCode = SecretConfig.get("momo_partner_code");
        String accessKey = SecretConfig.get("momo_access_key");
        String secretKey = SecretConfig.get("momo_secret_key");
        String createUrl = getOrDefault("momo_create_url", DEFAULT_CREATE_URL);

        if (isBlank(partnerCode) || isBlank(accessKey) || isBlank(secretKey)) {
            List<String> missingKeys = SecretConfig.getMissingKeys(
                    "momo_partner_code",
                    "momo_access_key",
                    "momo_secret_key"
            );
            return PaymentGatewayInitResult.failure(
                    "CONFIG_MISSING",
                    "Chua cau hinh MoMo: " + String.join(", ", missingKeys),
                    null
            );
        }

        String extraData = "";
        String signaturePayload = "accessKey=" + accessKey
                + "&amount=" + request.getAmount()
                + "&extraData=" + extraData
                + "&ipnUrl=" + request.getIpnUrl()
                + "&orderId=" + request.getProviderOrderId()
                + "&orderInfo=" + request.getOrderInfo()
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + request.getRedirectUrl()
                + "&requestId=" + request.getRequestId()
                + "&requestType=" + REQUEST_TYPE;

        JsonObject payload = new JsonObject();
        payload.addProperty("partnerCode", partnerCode);
        payload.addProperty("requestId", request.getRequestId());
        payload.addProperty("amount", String.valueOf(request.getAmount()));
        payload.addProperty("orderId", request.getProviderOrderId());
        payload.addProperty("orderInfo", request.getOrderInfo());
        payload.addProperty("redirectUrl", request.getRedirectUrl());
        payload.addProperty("ipnUrl", request.getIpnUrl());
        payload.addProperty("requestType", REQUEST_TYPE);
        payload.addProperty("extraData", extraData);
        payload.addProperty("lang", "vi");
        payload.addProperty("autoCapture", true);
        payload.addProperty("signature", PaymentCryptoUtil.hmacSha256(secretKey, signaturePayload));

        if (!isBlank(request.getCustomerName()) || !isBlank(request.getCustomerEmail()) || !isBlank(request.getCustomerPhone())) {
            JsonObject userInfo = new JsonObject();
            userInfo.addProperty("name", defaultString(request.getCustomerName()));
            userInfo.addProperty("email", defaultString(request.getCustomerEmail()));
            userInfo.addProperty("phoneNumber", defaultString(request.getCustomerPhone()));
            payload.add("userInfo", userInfo);
        }

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(createUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject body = gson.fromJson(response.body(), JsonObject.class);
            int resultCode = body != null && body.has("resultCode") ? body.get("resultCode").getAsInt() : -1;
            String message = body != null && body.has("message") ? body.get("message").getAsString() : "Khong tao duoc giao dich MoMo.";
            String payUrl = body != null && body.has("payUrl") ? body.get("payUrl").getAsString() : null;
            String returnedRequestId = body != null && body.has("requestId") ? body.get("requestId").getAsString() : request.getRequestId();

            if (response.statusCode() >= 200 && response.statusCode() < 300 && resultCode == 0 && !isBlank(payUrl)) {
                return PaymentGatewayInitResult.success(payUrl, returnedRequestId, String.valueOf(resultCode), message, response.body());
            }

            return PaymentGatewayInitResult.failure(String.valueOf(resultCode), message, response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentGatewayInitResult.failure("HTTP_ERROR", "Khong ket noi duoc MoMo.", e.getMessage());
        } catch (IOException e) {
            return PaymentGatewayInitResult.failure("HTTP_ERROR", "Khong ket noi duoc MoMo.", e.getMessage());
        }
    }

    public boolean validateCallbackSignature(Map<String, String> params) {
        String accessKey = SecretConfig.get("momo_access_key");
        String secretKey = SecretConfig.get("momo_secret_key");
        if (isBlank(accessKey) || isBlank(secretKey)) {
            return false;
        }

        String signature = defaultString(params.get("signature"));
        String data = "accessKey=" + accessKey
                + "&amount=" + defaultString(params.get("amount"))
                + "&extraData=" + defaultString(params.get("extraData"))
                + "&message=" + defaultString(params.get("message"))
                + "&orderId=" + defaultString(params.get("orderId"))
                + "&orderInfo=" + defaultString(params.get("orderInfo"))
                + "&orderType=" + defaultString(params.get("orderType"))
                + "&partnerCode=" + defaultString(params.get("partnerCode"))
                + "&payType=" + defaultString(params.get("payType"))
                + "&requestId=" + defaultString(params.get("requestId"))
                + "&responseTime=" + defaultString(params.get("responseTime"))
                + "&resultCode=" + defaultString(params.get("resultCode"))
                + "&transId=" + defaultString(params.get("transId"));

        return PaymentCryptoUtil.hmacSha256(secretKey, data).equals(signature);
    }

    public boolean isSuccess(Map<String, String> params) {
        return "0".equals(defaultString(params.get("resultCode")));
    }

    public String getResultCode(Map<String, String> params) {
        return defaultString(params.get("resultCode"));
    }

    public String getMessage(Map<String, String> params) {
        return defaultString(params.get("message"));
    }

    public String getProviderOrderId(Map<String, String> params) {
        return defaultString(params.get("orderId"));
    }

    public String getRequestId(Map<String, String> params) {
        return defaultString(params.get("requestId"));
    }

    public String getPaymentToken(Map<String, String> params) {
        return defaultString(params.get("transId"));
    }

    public String getProviderTransactionId(Map<String, String> params) {
        return defaultString(params.get("transId"));
    }

    private String getOrDefault(String key, String defaultValue) {
        String value = SecretConfig.get(key);
        return isBlank(value) ? defaultValue : value.trim();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
