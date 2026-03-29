package Util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class để hash và verify mật khẩu sử dụng BCrypt
 */
public class PasswordUtil {
    
    // Cost factor cho BCrypt (10-12 là phù hợp cho production)
    private static final int BCRYPT_ROUNDS = 12;
    
    /**
     * Hash mật khẩu sử dụng BCrypt
     * @param plainPassword Mật khẩu gốc
     * @return Mật khẩu đã được hash
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
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
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Nếu hash không hợp lệ (ví dụ: mật khẩu cũ chưa hash)
            // So sánh trực tiếp để hỗ trợ migration
            return plainPassword.equals(hashedPassword);
        }
    }
    
    /**
     * Kiểm tra xem mật khẩu đã được hash chưa
     * BCrypt hash bắt đầu bằng $2a$, $2b$, hoặc $2y$
     */
    public static boolean isHashed(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
