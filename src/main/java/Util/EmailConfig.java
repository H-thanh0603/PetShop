package Util;

public class EmailConfig {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final boolean SMTP_AUTH = true;
    private static final boolean SMTP_STARTTLS = true;
    private static final String SMTP_EMAIL = "phamdat7879@gmail.com";
    private static final String SMTP_PASSWORD = "xbeu ovud rhqy wzkx";
    private static final String SENDER_NAME = "PetShop";

    public static String getSmtpHost() { return SMTP_HOST; }
    public static String getSmtpPort() { return SMTP_PORT; }
    public static boolean isSmtpAuth() { return SMTP_AUTH; }
    public static boolean isSmtpStarttls() { return SMTP_STARTTLS; }
    public static String getSmtpEmail() { return SMTP_EMAIL; }
    public static String getSmtpPassword() { return SMTP_PASSWORD; }
    public static String getSenderName() { return SENDER_NAME; }
}
