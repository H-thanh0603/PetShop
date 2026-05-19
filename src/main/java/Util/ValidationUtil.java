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
    private static final Pattern ADDRESS_DETAIL_PATTERN = Pattern.compile(
            "^[\\p{L}0-9\\s,./-]+$"
    );

    private static final Pattern ADDRESS_HAS_MEANING_PATTERN = Pattern.compile(
            ".*[\\p{L}].*"
    );

    private static final Pattern ADDRESS_REPEATED_SPECIAL_PATTERN = Pattern.compile(
            ".*[,./-]{2,}.*"
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
        return PasswordUtil.isStrongPassword(password);
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
     * Strip all HTML tags from input string and trim whitespace.
     * Returns empty string for null input.
     */
    public static String stripHtmlTags(String input) {
        if (input == null) return "";
        return input.replaceAll("<[^>]*>", "").trim();
    }
    
    /**
     * Validate that input does not exceed the specified maximum length.
     * Returns false if input is null or exceeds maxLength.
     */
    public static boolean validateMaxLength(String input, int maxLength) {
        return input != null && input.length() <= maxLength;
    }
    
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

    public static String normalizeAddressDetail(String addressDetail) {
        if (addressDetail == null) return "";
        return sanitizeInput(addressDetail).replaceAll("\\s+", " ");
    }
    public static String validateAddressDetail(String addressDetail) {
        if (isEmpty(addressDetail)) {
            return "Chi tiết địa chỉ không được để trống";
        }

        String input = normalizeAddressDetail(addressDetail);

        if (!isValidLength(input, 5, 255)) {
            return "Chi tiết địa chỉ phải từ 5 đến 255 ký tự";
        }

        if (!ADDRESS_DETAIL_PATTERN.matcher(input).matches()) {
            return "Chi tiết địa chỉ chỉ được chứa chữ, số, khoảng trắng và các ký tự , . / -";
        }

        if (!ADDRESS_HAS_MEANING_PATTERN.matcher(input).matches()) {
            return "Chi tiết địa chỉ phải có ít nhất một chữ cái";
        }

        if (ADDRESS_REPEATED_SPECIAL_PATTERN.matcher(input).matches()) {
            return "Chi tiết địa chỉ không được chứa nhiều ký tự đặc biệt liên tiếp";
        }

        if (input.startsWith(",") || input.startsWith(".") || input.startsWith("/")
                || input.startsWith("-") || input.endsWith(",") || input.endsWith(".")
                || input.endsWith("/") || input.endsWith("-")) {
            return "Chi tiết địa chỉ không được bắt đầu hoặc kết thúc bằng dấu câu";
        }

        if (input.matches("^[0-9\\s,./-]+$")) {
            return "Chi tiết địa chỉ không được chỉ gồm số và ký tự đặc biệt";
        }

        if (input.matches("^([\\p{L}0-9])\\1{4,}$")) {
            return "Chi tiết địa chỉ không hợp lệ";
        }

        return null;
    }
}
