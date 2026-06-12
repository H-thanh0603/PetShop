package Util;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class VnpayUtil {

    public static String createPaymentUrl(HttpServletRequest request, int orderId, BigDecimal amount) {
        String vnpTxnRef = String.valueOf(orderId);
        String vnpOrderInfo = "Thanh toan don hang " + orderId;
        String vnpIpAddr = getIpAddress(request);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(calendar.getTime());

        calendar.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(calendar.getTime());

        long vnpAmount = amount.setScale(0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", VnpayConfig.getTmnCode().trim());
        params.put("vnp_Amount", String.valueOf(vnpAmount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", vnpTxnRef);
        params.put("vnp_OrderInfo", vnpOrderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", VnpayConfig.getReturnUrl().trim());
        params.put("vnp_IpAddr", vnpIpAddr);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            for (String fieldName : fieldNames) {
                String fieldValue = params.get(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {
                    if (hashData.length() > 0) {
                        hashData.append("&");
                        query.append("&");
                    }

                    hashData.append(fieldName)
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, "US-ASCII"));

                    query.append(URLEncoder.encode(fieldName, "US-ASCII"))
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, "US-ASCII"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot encode VNPAY data", e);
        }

        String secureHash = hmacSHA512(VnpayConfig.getHashSecret().trim(), hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return VnpayConfig.getPayUrl().trim() + "?" + query;
    }

    public static boolean verifyReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();

        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()
                    && !"vnp_SecureHash".equals(fieldName)
                    && !"vnp_SecureHashType".equals(fieldName)) {
                fields.put(fieldName, fieldValue);
            }
        }

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = fields.get(fieldName);

            if (i > 0) {
                hashData.append("&");
            }

            hashData.append(fieldName)
                    .append("=")
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
        }

        String secureHash = request.getParameter("vnp_SecureHash");
        String signed = hmacSHA512(VnpayConfig.getHashSecret(), hashData.toString());

        return secureHash != null && secureHash.equalsIgnoreCase(signed);
    }

    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("VNPAY hash secret is empty");
            }

            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.trim().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );
            hmac512.init(secretKey);

            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }

            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot sign VNPAY data", e);
        }
    }

    private static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}