package Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final Properties properties = new Properties();
    private static final String[] PROPERTY_FILES = {
            // Chỉ còn file legacy chứa secret local (gitignored). Các khoá thường
            // đã chuyển vào application.yml và được đọc qua Spring Environment.
            "secrets.properties"
    };

    // Set by the Spring context (AppConfigBridge); lets configuration live in
    // application.yml / Spring Environment while legacy static callers keep working.
    private static volatile org.springframework.core.env.Environment springEnvironment;

    public static void setSpringEnvironment(org.springframework.core.env.Environment environment) {
        springEnvironment = environment;
    }

    static {
        // Outside a Spring context (unit tests, CLI tools) the bridge is never
        // set, so application.yml is mirrored into the same fallback bucket the
        // legacy property files used to fill — the legacy static facades keep
        // resolving every key. Bucket order: application.yml first,
        // secrets.properties last (wins). Precedence overall is unchanged:
        // system property -> Spring Environment (env var, yml) -> env var ->
        // this bucket.
        loadYamlDefaults("application.yml");
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

        // Precedence (unchanged): system property -> env var -> yml ->
        // secrets.properties. A real Spring Environment already resolves its
        // systemProperties source ahead of application.yml, but the bridged
        // Environment may not (e.g. a plain MockEnvironment in tests), so the
        // system property is checked explicitly first.
        String systemValue = System.getProperty(key);
        if (hasText(systemValue)) {
            return systemValue;
        }

        org.springframework.core.env.Environment env = springEnvironment;
        if (env != null) {
            String springValue = env.getProperty(key);
            if (hasText(springValue)) {
                return springValue;
            }
        }

        String envValue = System.getenv(key);
        if (hasText(envValue)) {
            return envValue;
        }

        String normalizedEnvKey = normalizeEnvKey(key);
        envValue = System.getenv(normalizedEnvKey);
        if (hasText(envValue)) {
            return envValue;
        }

        return properties.getProperty(key);
    }

    private static String normalizeEnvKey(String key) {
        return key.toUpperCase().replaceAll("[^A-Z0-9]", "_");
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

    private static void loadYamlDefaults(String fileName) {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                return;
            }
            Object root = new org.yaml.snakeyaml.Yaml(
                    new org.yaml.snakeyaml.constructor.SafeConstructor(
                            new org.yaml.snakeyaml.LoaderOptions())).load(input);
            if (root instanceof java.util.Map<?, ?> map) {
                flattenYaml("", map);
            }
        } catch (Exception e) {
            logger.warn("Failed to load optional config file {}", fileName, e);
        }
    }

    private static void flattenYaml(String prefix, java.util.Map<?, ?> map) {
        for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
            String key = prefix.isEmpty()
                    ? String.valueOf(entry.getKey())
                    : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof java.util.Map<?, ?> nested) {
                flattenYaml(key, nested);
            } else if (value != null) {
                properties.setProperty(key, String.valueOf(value));
            }
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
