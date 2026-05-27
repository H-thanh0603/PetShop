package Context;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBContext {

    public static Connection getConnection() throws Exception {
        Properties props = new Properties();

        InputStream input = DBContext.class.getClassLoader().getResourceAsStream("db.properties");

        if (input == null) {
            throw new RuntimeException("Không tìm thấy file db.properties");
        }

        props.load(input);

        String host = props.getProperty("db.host").trim();
        String port = props.getProperty("db.port").trim();
        String username = props.getProperty("db.username").trim();
        String password = props.getProperty("db.password");
        String dbname = props.getProperty("db.dbname").trim();
        String option = props.getProperty("db.option").trim();

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbname + "?" + option;

        Class.forName("com.mysql.cj.jdbc.Driver");

        return DriverManager.getConnection(url, username, password);
    }
}