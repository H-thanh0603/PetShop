package Util;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class để quản lý OTP (One-Time Password)
 */
public class OTPUtil {
    
    // Lưu trữ OTP: email -> OTPData
    private static final Map<String, OTPData> otpStorage = new ConcurrentHashMap<>();
    
    // Thời gian hết hạn OTP (5 phút)
    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000;

    // Số lần nhập sai tối đa trước khi OTP bị vô hiệu (chống brute-force
    // trên không gian 10^6 mã)
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    // Độ dài OTP
    private static final int OTP_LENGTH = 6;

    /**
     * Tạo OTP mới cho email
     */
    public static String generateOTP(String email) {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }

        // Dọn các OTP đã hết hạn để map không phình to vô hạn
        purgeExpired();

        String otpCode = otp.toString();
        otpStorage.put(email.toLowerCase(), new OTPData(otpCode, System.currentTimeMillis()));

        return otpCode;
    }

    /**
     * Xác thực OTP. Sau MAX_VERIFY_ATTEMPTS lần nhập sai, OTP bị vô hiệu
     * và người dùng phải yêu cầu mã mới.
     */
    public static boolean verifyOTP(String email, String otp) {
        if (email == null || otp == null) return false;

        String key = email.toLowerCase();
        OTPData data = otpStorage.get(key);
        if (data == null) return false;

        // Kiểm tra hết hạn
        if (System.currentTimeMillis() - data.timestamp > OTP_EXPIRY_MS) {
            otpStorage.remove(key);
            return false;
        }

        // Kiểm tra OTP
        if (data.otp.equals(otp.trim())) {
            otpStorage.remove(key); // Xóa sau khi verify thành công
            return true;
        }

        data.attempts++;
        if (data.attempts >= MAX_VERIFY_ATTEMPTS) {
            otpStorage.remove(key); // Vô hiệu OTP sau quá nhiều lần sai
        }
        return false;
    }

    /**
     * Xác thực OTP nhưng không xóa (dùng cho multi-step flow).
     * Cũng bị vô hiệu sau MAX_VERIFY_ATTEMPTS lần sai.
     */
    public static boolean verifyOTPKeep(String email, String otp) {
        if (email == null || otp == null) return false;

        String key = email.toLowerCase();
        OTPData data = otpStorage.get(key);
        if (data == null) return false;

        // Kiểm tra hết hạn
        if (System.currentTimeMillis() - data.timestamp > OTP_EXPIRY_MS) {
            otpStorage.remove(key);
            return false;
        }

        // Kiểm tra OTP (không xóa)
        if (data.otp.equals(otp.trim())) {
            return true;
        }

        data.attempts++;
        if (data.attempts >= MAX_VERIFY_ATTEMPTS) {
            otpStorage.remove(key);
        }
        return false;
    }

    private static void purgeExpired() {
        long now = System.currentTimeMillis();
        otpStorage.entrySet().removeIf(e -> now - e.getValue().timestamp > OTP_EXPIRY_MS);
    }
    
    /**
     * Xóa OTP thủ công
     */
    public static void removeOTP(String email) {
        if (email != null) {
            otpStorage.remove(email.toLowerCase());
        }
    }
    
    /**
     * Gửi OTP qua email
     */
    public static boolean sendOTP(String email, String otp) {
        String subject = "Mã xác thực đăng nhập - PetVaccine";
        String htmlContent = buildOTPEmailHtml(otp);
        return EmailUtil.sendEmail(email, subject, htmlContent);
    }
    
    /**
     * Tạo OTP và gửi email
     */
    public static boolean generateAndSendOTP(String email) {
        String otp = generateOTP(email);
        return sendOTP(email, otp);
    }
    
    /**
     * Build HTML email cho OTP
     */
    private static String buildOTPEmailHtml(String otp) {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head>" +
            "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center;'>" +
            "  <h1 style='color: white; margin: 0;'>🐾 PetVaccine</h1>" +
            "</div>" +
            "<div style='padding: 30px; background: #f9f9f9; text-align: center;'>" +
            "  <h2 style='color: #333;'>Mã xác thực của bạn</h2>" +
            "  <p>Sử dụng mã sau để đăng nhập:</p>" +
            "  <div style='background: #667eea; color: white; font-size: 32px; font-weight: bold; " +
            "              padding: 20px 40px; border-radius: 10px; display: inline-block; " +
            "              letter-spacing: 8px; margin: 20px 0;'>" + otp + "</div>" +
            "  <p style='color: #666;'>Mã này sẽ hết hạn sau <strong>5 phút</strong>.</p>" +
            "  <p style='color: #999; font-size: 12px;'>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.</p>" +
            "</div>" +
            "</body></html>";
    }
    
    /**
     * Class lưu trữ OTP data
     */
    private static class OTPData {
        final String otp;
        final long timestamp;
        volatile int attempts;

        OTPData(String otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }
    }
}
