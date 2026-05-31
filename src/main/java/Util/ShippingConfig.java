package Util;

public class ShippingConfig {
    public static String get(String key) {
        return AppConfig.get("shipping." + key.toLowerCase().replace('_', '-'), key);
    }
    public static int getInt(String key) {
        return AppConfig.getInt("shipping." + key.toLowerCase().replace('_', '-'), 0, key);
    }
}
