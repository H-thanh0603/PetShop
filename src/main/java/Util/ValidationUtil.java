package Util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utility class cho validation input
 * Tập trung tất cả logic validation ở đây
 */
public class ValidationUtil {
    
    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(0|\\+84)[0-9]{9,10}$"
    );
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,20}$"
    );
    
    // Booking date limits
    private static final int MAX_BOOKING_DAYS_AHEAD = 60;
    
    // ==================== BASIC CHECKS ====================
    
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    public static boolean isValidLength(String str, int min, int max) {
        if (str == null) return min == 0;
        int len = str.trim().length();
        return len >= min && len <= max;
    }
    
    // ==================== FORMAT VALIDATION ====================
    
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate phone number (VN format)
     * Accepts: 0xxxxxxxxx, +84xxxxxxxxx
     * Call normalizePhone() first if input may contain spaces/dashes
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        String normalized = normalizePhone(phone);
        return PHONE_PATTERN.matcher(normalized).matches();
    }
    
    /**
     * Normalize phone: remove spaces, dashes, dots
     */
    public static String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[\\s\\-\\.]", "").trim();
    }
    
    public static boolean isValidUsername(String username) {
        if (isEmpty(username)) return false;
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }
    
    public static boolean isValidPassword(String password) {
        return isNotEmpty(password) && password.length() >= 6;
    }
    
    // ==================== NUMBER VALIDATION ====================
    
    public static boolean isPositiveNumber(String str) {
        Double num = parseDoubleOrNull(str);
        return num != null && num > 0;
    }
    
    public static boolean isNonNegativeNumber(String str) {
        Double num = parseDoubleOrNull(str);
        return num != null && num >= 0;
    }
    
    public static boolean isValidInteger(String str) {
        return parseIntOrNull(str) != null;
    }
    
    public static boolean isValidDiscount(String str) {
        Integer discount = parseIntOrNull(str);
        return discount != null && discount >= 0 && discount <= 100;
    }
    
    // ==================== DATE VALIDATION ====================
    
    public static boolean isValidDate(String dateStr) {
        return parseDateOrNull(dateStr) != null;
    }
    
    /**
     * Check date is not in the past
     */
    public static boolean isNotPastDate(String dateStr) {
        LocalDate date = parseDateOrNull(dateStr);
        if (date == null) return false;
        return !date.isBefore(LocalDate.now());
    }
    
    /**
     * Check date is within allowed booking range (not past, not too far future)
     */
    public static boolean isValidBookingDate(String dateStr) {
        LocalDate date = parseDateOrNull(dateStr);
        if (date == null) return false;
        
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(MAX_BOOKING_DAYS_AHEAD);
        
        return !date.isBefore(today) && !date.isAfter(maxDate);
    }
    
    /**
     * Get max booking date message for error display
     */
    public static String getMaxBookingDateMessage() {
        return "Chỉ được đặt lịch trong vòng " + MAX_BOOKING_DAYS_AHEAD + " ngày tới";
    }
    
    // ==================== SAFE PARSING (returns null if invalid) ====================
    
    /**
     * Parse int, returns null if invalid
     */
    public static Integer parseIntOrNull(String str) {
        if (isEmpty(str)) return null;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parse double, returns null if invalid
     */
    public static Double parseDoubleOrNull(String str) {
        if (isEmpty(str)) return null;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parse date (yyyy-MM-dd), returns null if invalid
     */
    public static LocalDate parseDateOrNull(String dateStr) {
        if (isEmpty(dateStr)) return null;
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            return null;
        }
    }
    
    // ==================== SAFE PARSING (with default value) ====================
    
    /**
     * Parse int with default value fallback
     */
    public static int parseIntOrDefault(String str, int defaultValue) {
        Integer result = parseIntOrNull(str);
        return result != null ? result : defaultValue;
    }
    
    /**
     * Parse double with default value fallback
     */
    public static double parseDoubleOrDefault(String str, double defaultValue) {
        Double result = parseDoubleOrNull(str);
        return result != null ? result : defaultValue;
    }
    
    // ==================== SANITIZATION ====================
    
    /**
     * Basic sanitize for plain text fields (username, fullname, title, author)
     * Escapes HTML special characters to prevent XSS
     * NOTE: This is for fields that should NOT contain HTML
     */
    public static String escapeHtml(String str) {
        if (str == null) return "";
        return str.trim()
                  .replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
    
    /**
     * Sanitize plain text input - trim and basic cleanup
     * Use this for input validation, NOT for XSS prevention
     * XSS prevention should be done at output (JSP) level
     */
    public static String sanitizeInput(String str) {
        if (str == null) return "";
        return str.trim();
    }
    
    /**
     * Sanitize for fields that must not contain any HTML (username, etc)
     * Strips all HTML-like content
     */
    public static String stripHtml(String str) {
        if (str == null) return "";
        return str.replaceAll("<[^>]*>", "").trim();
    }
}
