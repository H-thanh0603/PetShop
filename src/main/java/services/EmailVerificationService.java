package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DAO.UserDAO;
import Util.EmailUtil;

import java.security.SecureRandom;

/**
 * Handles email verification token generation, sending, and validation.
 */
public class EmailVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);


    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long EXPIRY_MS = 24L * 60 * 60 * 1000; // 24 hours

    private final UserDAO userDAO;

    public EmailVerificationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Generate a token, store it, and send the verification email.
     * @return true if email was sent successfully
     */
    public boolean sendVerificationEmail(int userId, String email, String contextPath) {
        String token = generateToken();
        long expiry = System.currentTimeMillis() + EXPIRY_MS;
        userDAO.saveVerificationToken(userId, token, new java.sql.Timestamp(expiry));

        String verifyUrl = contextPath + "/verify-email?token=" + token;
        String subject = "Xác thực email - PetShop";
        String html = buildEmailHtml(verifyUrl);

        try {
            EmailUtil.sendHtmlEmail(email, subject, html);
            return true;
        } catch (Exception e) {
            logger.warn("[EmailVerification] Failed to send to " + email + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Resend verification email (generates a new token).
     */
    public boolean resendVerificationEmail(String email, String contextPath) {
        var user = userDAO.getUserByEmail(email);
        if (user == null) return false;
        return sendVerificationEmail(user.getId(), email, contextPath);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String buildEmailHtml(String verifyUrl) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>" +
               "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;'>" +
               "<h2 style='color:#e67e22;'>🐾 PetShop - Xác thực email</h2>" +
               "<p>Cảm ơn bạn đã đăng ký tài khoản tại PetShop!</p>" +
               "<p>Vui lòng nhấn vào nút bên dưới để xác thực địa chỉ email của bạn:</p>" +
               "<div style='text-align:center;margin:30px 0;'>" +
               "<a href='" + verifyUrl + "' style='background:#2563eb;color:#fff;padding:14px 32px;" +
               "border-radius:8px;text-decoration:none;font-weight:bold;font-size:16px;'>Xác thực email</a>" +
               "</div>" +
               "<p style='color:#888;font-size:13px;'>Link có hiệu lực trong 24 giờ. " +
               "Nếu bạn không đăng ký tài khoản, hãy bỏ qua email này.</p>" +
               "<hr style='border:none;border-top:1px solid #eee;'>" +
               "<p style='color:#aaa;font-size:12px;'>Hoặc copy link: " + verifyUrl + "</p>" +
               "</div></body></html>";
    }
}
