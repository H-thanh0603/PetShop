package services.payment;

import Util.PaymentCryptoUtil;
import Util.SecretConfig;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class VnpayPaymentService {
    private static final String DEFAULT_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final DateTimeFormatter VNPAY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public PaymentGatewayInitResult createPayment(PaymentGatewayRequest request) {
        String tmnCode = SecretConfig.get("vnpay_tmn_code");
        String hashSecret = SecretConfig.get("vnpay_hash_secret");
        String payUrl = getOrDefault("vnpay_pay_url", DEFAULT_PAY_URL);

        if (isBlank(tmnCode) || isBlank(hashSecret)) {
            List<String> missingKeys = SecretConfig.getMissingKeys(
                    "vnpay_tmn_code",
                    "vnpay_hash_secret"
            );
            return PaymentGatewayInitResult.failure(
                    "CONFIG_MISSING",
                    "Chua cau hinh VNPAY: " + String.join(", ", missingKeys),
                    null
            );
        }

        LocalDateTime now = LocalDateTime.now();
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(request.getAmount() * 100L));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", request.getProviderOrderId());
        params.put("vnp_OrderInfo", normalizeOrderInfo(request.getOrderInfo()));
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", request.getRedirectUrl());
        params.put("vnp_IpAddr", request.getClientIp());
        params.put("vnp_CreateDate", now.format(VNPAY_TIME_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(VNPAY_TIME_FORMAT));
        String bankCode = getOrDefault("vnpay_bank_code", "VNBANK");
        params.put("vnp_BankCode", bankCode);

        String query = PaymentCryptoUtil.buildSortedQueryString(params);
        String secureHash = PaymentCryptoUtil.hmacSha512(hashSecret, query);
        String redirectUrl = payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;

        return PaymentGatewayInitResult.success(
                redirectUrl,
                request.getRequestId(),
                "00",
                "VNPAY redirect created.",
                query
        );
    }

    public boolean validateSignature(Map<String, String> params) {
        String hashSecret = SecretConfig.get("vnpay_hash_secret");
        if (isBlank(hashSecret)) {
            return false;
        }

        String receivedHash = defaultString(params.get("vnp_SecureHash"));
        Map<String, String> signParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) {
                continue;
            }
            if (key.startsWith("vnp_")) {
                signParams.put(key, defaultString(entry.getValue()));
            }
        }

        String data = PaymentCryptoUtil.buildSortedQueryString(signParams);
        return PaymentCryptoUtil.hmacSha512(hashSecret, data).equalsIgnoreCase(receivedHash);
    }

    public boolean isSuccess(Map<String, String> params) {
        return "00".equals(defaultString(params.get("vnp_ResponseCode")))
                && "00".equals(defaultString(params.get("vnp_TransactionStatus")));
    }

    public boolean isCancelled(Map<String, String> params) {
        return "24".equals(defaultString(params.get("vnp_ResponseCode")));
    }

    public String getProviderOrderId(Map<String, String> params) {
        return defaultString(params.get("vnp_TxnRef"));
    }

    public String getResultCode(Map<String, String> params) {
        return defaultString(params.get("vnp_ResponseCode"));
    }

    public String getMessage(Map<String, String> params) {
        String responseCode = defaultString(params.get("vnp_ResponseCode"));
        if ("24".equals(responseCode)) {
            return "Khach hang da huy giao dich tren VNPAY.";
        }
        if ("00".equals(responseCode)) {
            return "Thanh toan VNPAY thanh cong.";
        }
        return "Thanh toan VNPAY that bai. Ma loi: " + responseCode;
    }

    public String getPaymentToken(Map<String, String> params) {
        return defaultString(params.get("vnp_TransactionNo"));
    }

    public String getProviderTransactionId(Map<String, String> params) {
        String bankTransactionNo = defaultString(params.get("vnp_BankTranNo"));
        return isBlank(bankTransactionNo)
                ? defaultString(params.get("vnp_TransactionNo"))
                : bankTransactionNo;
    }

    private String normalizeOrderInfo(String value) {
        String normalized = Normalizer.normalize(defaultString(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Za-z0-9\\s:.-]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.isEmpty()) {
            normalized = "Thanh toan don hang";
        }
        return normalized;
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
