package Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final Properties properties = new Properties();
    private static final String[] PROPERTY_FILES = {
            "app.properties",
            "db.properties",
            "secrets.properties",
            "ship.properties",
            "vnpay.properties"
    };

    static {
        for (String fileName : PROPERTY_FILES) {
            loadOptionalProperties(fileName);
        }
    }

    private AppConfig() {
    }

    public static String get(String key, String... fallbackKeys) {
        String value = lookup(key);
        if (hasText(value)) {
            return value.trim();
        }

        if (fallbackKeys != null) {
            for (String fallbackKey : fallbackKeys) {
                value = lookup(fallbackKey);
                if (hasText(value)) {
                    return value.trim();
                }
            }
        }

        return null;
    }

    public static String getOrDefault(String key, String defaultValue, String... fallbackKeys) {
        String value = get(key, fallbackKeys);
        return hasText(value) ? value : defaultValue;
    }

    public static int getInt(String key, int defaultValue, String... fallbackKeys) {
        String value = get(key, fallbackKeys);
        if (!hasText(value)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer config for key {}: {}", key, value);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue, String... fallbackKeys) {
        String value = get(key, fallbackKeys);
        if (!hasText(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    public static boolean hasValue(String key, String... fallbackKeys) {
        return hasText(get(key, fallbackKeys));
    }

    private static String lookup(String key) {
        if (!hasText(key)) {
            return null;
        }

        String systemValue = System.getProperty(key);
        if (hasText(systemValue)) {
            return systemValue;
        }

        String envValue = System.getenv(key);
        if (hasText(envValue)) {
            return envValue;
        }

        String normalizedEnvKey = key.toUpperCase().replace('.', '_');
        envValue = System.getenv(normalizedEnvKey);
        if (hasText(envValue)) {
            return envValue;
        }

        return properties.getProperty(key);
    }

    private static void loadOptionalProperties(String fileName) {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                return;
            }
            Properties fileProperties = new Properties();
            fileProperties.load(input);
            properties.putAll(fileProperties);
        } catch (Exception e) {
            logger.warn("Failed to load optional config file {}", fileName, e);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
