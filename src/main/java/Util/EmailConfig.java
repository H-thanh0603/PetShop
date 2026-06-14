package Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = EmailConfig.class.getClassLoader()
                .getResourceAsStream("email.properties")) {
            if (input == null) {
                throw new RuntimeException("Không tìm thấy file email.properties trong resources");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc email.properties", e);
        }
    }

    public static String getSmtpHost() { return props.getProperty("smtp.host"); }
    public static String getSmtpPort() { return props.getProperty("smtp.port"); }
    public static boolean isSmtpAuth() { return Boolean.parseBoolean(props.getProperty("smtp.auth")); }
    public static boolean isSmtpStarttls() { return Boolean.parseBoolean(props.getProperty("smtp.starttls")); }
    public static String getSmtpEmail() { return props.getProperty("smtp.email"); }
    public static String getSmtpPassword() { return props.getProperty("smtp.password"); }
    public static String getSenderName() { return props.getProperty("smtp.senderName"); }
}
