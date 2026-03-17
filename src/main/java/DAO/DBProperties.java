package DAO;

import java.io.IOException;
import java.util.Properties;

public class DBProperties {
    private static Properties prop = new Properties();

    static {
        try {
            prop.load(DBProperties.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties"));  // Đúng cú pháp
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String host() {
        return prop.getProperty("db.host");
    }

    public static int port() {
        try {
            return Integer.parseInt(prop.getProperty("db.port"));
        } catch (NumberFormatException e) {
            return 3306; // mặc định
        }
    }

    public static String username() {
        return prop.getProperty("db.username");
    }

    public static String password() {
        return prop.getProperty("db.password");
    }

    public static String dbname() {
        return prop.getProperty("db.dbname");
    }

    public static String option() {
        return prop.getProperty("db.option");
    }
}
