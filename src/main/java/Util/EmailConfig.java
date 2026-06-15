package Util;

public final class EmailConfig {
    private EmailConfig() {
    }

    public static String getSmtpHost() {
        return AppConfig.getOrDefault("mail.smtp.host", "smtp.gmail.com", "SMTP_HOST");
    }

    public static String getSmtpPort() {
        return AppConfig.getOrDefault("mail.smtp.port", "587", "SMTP_PORT");
    }

    public static boolean isSmtpAuth() {
        return AppConfig.getBoolean("mail.smtp.auth", true, "SMTP_AUTH");
    }

    public static boolean isSmtpStarttls() {
        return AppConfig.getBoolean("mail.smtp.starttls.enable", true, "SMTP_STARTTLS");
    }

    public static String getSmtpEmail() {
        return AppConfig.getOrDefault("mail.smtp.email", "", "SMTP_EMAIL", "EMAIL_USERNAME");
    }

    public static String getSmtpPassword() {
        return AppConfig.getOrDefault("mail.smtp.password", "", "SMTP_PASSWORD", "EMAIL_PASSWORD");
    }

    public static String getSenderName() {
        return AppConfig.getOrDefault("mail.sender.name", "MeliPet", "SMTP_SENDER_NAME", "EMAIL_SENDER_NAME");
    }
}
