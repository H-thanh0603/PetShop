package Util;

import java.io.InputStream;
import java.util.Properties;

public class ShippingConfig {
    private static Properties prop = new Properties();
    static {
        try {
            InputStream input =
                    ShippingConfig.class.getClassLoader()
                            .getResourceAsStream("ship.properties");
            prop.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String get(String key) {
        return prop.getProperty(key);
    }
    public static int getInt(String key) {return Integer.parseInt(prop.getProperty(key));}
}
