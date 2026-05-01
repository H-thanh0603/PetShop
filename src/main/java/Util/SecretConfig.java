package Util;

public class SecretConfig {

    public static String get(String key) {
        return AppConfig.get(key);
    }

    public static boolean hasValue(String key) {
        return AppConfig.hasValue(key);
    }
}
