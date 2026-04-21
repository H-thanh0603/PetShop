package Util;

import java.io.InputStream;
import java.util.Properties;

public class SecretConfig {

    private static Properties prop = new Properties();

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
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue.trim();
        }

        String envValue = System.getenv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        return prop.getProperty(key);
    }

    public static boolean hasValue(String key) {
        String value = get(key);
        return value != null && !value.trim().isEmpty();
    }
}
