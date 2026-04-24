package Util;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

/**
 * Utility for sending HTML emails via SMTP.
 * Supports retry logic (up to 3 attempts with 2-second delay).
 */
public class EmailUtil {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    /**
     * Send an HTML email with retry logic.
     * Throws RuntimeException only after all retries are exhausted.
     */
    public static void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                doSend(toEmail, subject, htmlBody);
                return; // success
            } catch (Exception e) {
                lastException = e;
                System.err.println("[EmailUtil] Attempt " + attempt + " failed for " + toEmail + ": " + e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        System.err.println("[EmailUtil] All " + MAX_RETRIES + " attempts failed for " + toEmail + ": " + lastException.getMessage());
        throw new RuntimeException("Email delivery failed after " + MAX_RETRIES + " attempts", lastException);
    }

    /**
     * Send asynchronously (fire-and-forget). Failures are logged but not propagated.
     */
    public static void sendHtmlEmailAsync(String toEmail, String subject, String htmlBody) {
        Thread t = new Thread(() -> {
            try {
                sendHtmlEmail(toEmail, subject, htmlBody);
            } catch (Exception e) {
                System.err.println("[EmailUtil] Async send failed for " + toEmail + ": " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private static void doSend(String toEmail, String subject, String htmlBody) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", EmailConfig.getSmtpHost());
        props.put("mail.smtp.port", EmailConfig.getSmtpPort());
        props.put("mail.smtp.auth", String.valueOf(EmailConfig.isSmtpAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(EmailConfig.isSmtpStarttls()));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailConfig.getSmtpEmail(), EmailConfig.getSmtpPassword());
            }
        });

        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(EmailConfig.getSmtpEmail(), EmailConfig.getSenderName(), "UTF-8"));
            message.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
        } catch (java.io.UnsupportedEncodingException e) {
            message.setFrom(new InternetAddress(EmailConfig.getSmtpEmail()));
            message.setSubject(subject);
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setContent(htmlBody, "text/html; charset=UTF-8");
        Transport.send(message);
    }

    /**
     * Legacy method for backward compatibility with OTPUtil.
     */
    public static boolean sendEmail(String toEmail, String subject, String htmlBody) {
        try {
            sendHtmlEmail(toEmail, subject, htmlBody);
            return true;
        } catch (Exception e) {
            System.err.println("[EmailUtil] sendEmail failed: " + e.getMessage());
            return false;
        }
    }
}
