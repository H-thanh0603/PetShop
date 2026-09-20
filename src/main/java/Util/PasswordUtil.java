package Util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Hash và verify mật khẩu bằng BCrypt (spring-security-crypto, maintained).
 * Format $2a$ tương thích hash jBCrypt cũ trong DB, cost 12 giữ nguyên.
 */
public class PasswordUtil {

    // Cost factor cho BCrypt (10-12 là phù hợp cho production)
    private static final int BCRYPT_STRENGTH = 12;
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(BCRYPT_STRENGTH);

    /**
     * Hash mật khẩu sử dụng BCrypt
     * @param plainPassword Mật khẩu gốc
     * @return Mật khẩu đã được hash
     */
    public static String hashPassword(String plainPassword) {
        return ENCODER.encode(plainPassword);
    }

    /**
     * Kiểm tra mật khẩu có khớp với hash không
     * @param plainPassword Mật khẩu gốc
     * @param hashedPassword Mật khẩu đã hash
     * @return true nếu khớp, false nếu không
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return ENCODER.matches(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Malformed/legacy hash: never fall back to plaintext comparison.
            // Plaintext passwords must be migrated to BCrypt (hashPassword) instead.
            return false;
        }
    }

    /**
     * Kiểm tra xem mật khẩu đã được hash chưa
     * BCrypt hash bắt đầu bằng $2a$, $2b$, hoặc $2y$
     */
    public static boolean isHashed(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    public static boolean isStrongPassword(String password) {
        return password != null
                && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,}$");
    }
}
