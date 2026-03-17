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
            prop.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return prop.getProperty(key);
    }
}