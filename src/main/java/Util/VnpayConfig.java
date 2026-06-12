package Util;

public class VnpayConfig {
    public static String getTmnCode() {
        return AppConfig.getOrDefault("vnpay.tmn-code", "");
    }

    public static String getHashSecret() {
        return AppConfig.getOrDefault("vnpay.hash-secret", "");
    }

    public static String getPayUrl() {
        return AppConfig.getOrDefault("vnpay.pay-url", "");
    }

    public static String getReturnUrl() {
        return AppConfig.getOrDefault("vnpay.return-url", "");
    }
}