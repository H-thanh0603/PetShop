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
        return AppConfig.getOrDefault("db.username", "root");
    }

    public static String password() {
        String filePw = AppConfig.getOrDefault("db.password", "");
        if (!filePw.isEmpty() && !filePw.equals("YOUR_PASSWORD_HERE")) {
            return filePw;
        }
        String override = AppConfig.get("petshop.db.password", "PETSHOP_DB_PASSWORD", "MYSQL_PASSWORD");
        return override != null ? override.trim() : "";
    }

    public static String dbname() {
        return AppConfig.getOrDefault("db.dbname", "petshop");
    }

    public static String option() {
        return AppConfig.getOrDefault("db.option", "useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC");
    }
}
