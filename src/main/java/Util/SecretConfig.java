package Util;

import java.io.InputStream;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

public class SecretConfig {

    private static final Properties prop = new Properties();

    static {
        try {
            InputStream input =
                    SecretConfig.class.getClassLoader()
                            .getResourceAsStream("secrets.properties");
            if (input != null) {
                prop.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }

        String normalizedKey = key.trim();
        String fromProperties = trimToNull(prop.getProperty(normalizedKey));
        if (fromProperties != null) {
            return fromProperties;
        }

        String envKey = normalizedKey.toUpperCase().replace('.', '_');
        return trimToNull(System.getenv(envKey));
    }

    public static boolean hasValue(String key) {
        String value = get(key);
        return value != null && !value.trim().isEmpty();
    }
}
