package DAO;

import Util.AppConfig;

public class DBProperties {

    public static String host() {
        return AppConfig.getOrDefault("db.host", "localhost");
    }

    public static int port() {
        return AppConfig.getInt("db.port", 3306);
    }

    public static String username() {
        return AppConfig.getOrDefault("db.username", "root", "DB_USERNAME", "PETSHOP_DB_USERNAME", "MYSQL_USER");
    }

    public static String password() {
        String override = AppConfig.get("petshop.db.password", "PETSHOP_DB_PASSWORD", "DB_PASSWORD", "MYSQL_PASSWORD");
        if (override != null) {
            return override.trim();
        }

        String filePw = AppConfig.getOrDefault("db.password", "");
        if (!filePw.isEmpty() && !filePw.equals("YOUR_PASSWORD_HERE")) {
            return filePw;
        }
        return "";
    }

    public static String dbname() {
        return AppConfig.getOrDefault("db.dbname", "petshop", "DB_NAME", "PETSHOP_DB_NAME", "MYSQL_DATABASE");
    }

    public static String option() {
        return AppConfig.getOrDefault("db.option", "useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC");
    }
}
