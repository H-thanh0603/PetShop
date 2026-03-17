package Util;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Helper class để quản lý form data và validation errors
 * Giúp code Servlet gọn hơn và dễ maintain
 */
public class FormHelper {
    
    private Map<String, String> form;      // Giữ lại giá trị đã nhập
    private Map<String, String> errors;    // Lỗi từng field
    private HttpServletRequest request;
    
    public FormHelper(HttpServletRequest request) {
        this.request = request;
        this.form = new HashMap<>();
        this.errors = new HashMap<>();
    }
    
    // ==================== GET & SET FORM VALUES ====================
    
    /**
     * Lấy giá trị từ request và lưu vào form map
     */
    public String get(String fieldName) {
        String value = ValidationUtil.sanitizeInput(request.getParameter(fieldName));
        form.put(fieldName, value);
        return value;
    }
    
    /**
     * Lấy giá trị raw (không sanitize) - dùng cho password
     */
    public String getRaw(String fieldName) {
        String value = request.getParameter(fieldName);
        return value != null ? value : "";
    }
    
    /**
     * Set giá trị vào form map (dùng khi cần override)
     */
    public void set(String fieldName, String value) {
        form.put(fieldName, value);
    }
    
    /**
     * Lấy giá trị từ form map
     */
    public String getValue(String fieldName) {
        return form.getOrDefault(fieldName, "");
    }
    
    // ==================== ERROR HANDLING ====================
    
    /**
     * Thêm lỗi cho field cụ thể
     */
    public void addError(String fieldName, String message) {
        errors.put(fieldName, message);
    }
    
    /**
     * Thêm lỗi chung (không thuộc field nào)
     */
    public void addGeneralError(String message) {
        errors.put("general", message);
    }
    
    /**
     * Kiểm tra có lỗi không
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Kiểm tra field có lỗi không
     */
    public boolean hasError(String fieldName) {
        return errors.containsKey(fieldName);
    }
    
    /**
     * Lấy message lỗi của field
     */
    public String getError(String fieldName) {
        return errors.get(fieldName);
    }
    
    // ==================== VALIDATION SHORTCUTS ====================
    
    /**
     * Validate required field
     */
    public boolean validateRequired(String fieldName, String displayName) {
        String value = getValue(fieldName);
        if (ValidationUtil.isEmpty(value)) {
            addError(fieldName, displayName + " không được để trống");
            return false;
        }
        return true;
    }
    
    /**
     * Validate required field (raw - dùng cho password)
     */
    public boolean validateRequiredRaw(String fieldName, String displayName) {
        String value = getRaw(fieldName);
        if (ValidationUtil.isEmpty(value)) {
            addError(fieldName, displayName + " không được để trống");
            return false;
        }
        return true;
    }
    
    /**
     * Validate length
     */
    public boolean validateLength(String fieldName, String displayName, int min, int max) {
        String value = getValue(fieldName);
        if (!ValidationUtil.isValidLength(value, min, max)) {
            addError(fieldName, displayName + " phải từ " + min + "-" + max + " ký tự");
            return false;
        }
        return true;
    }
    
    /**
     * Validate email format
     */
    public boolean validateEmail(String fieldName) {
        String value = getValue(fieldName);
        if (!ValidationUtil.isValidEmail(value)) {
            addError(fieldName, "Email không đúng định dạng");
            return false;
        }
        return true;
    }
    
    /**
     * Validate phone format
     */
    public boolean validatePhone(String fieldName) {
        String value = getValue(fieldName);
        if (!ValidationUtil.isValidPhone(value)) {
            addError(fieldName, "Số điện thoại không hợp lệ (VD: 0912345678)");
            return false;
        }
        return true;
    }
    
    /**
     * Validate username format
     */
    public boolean validateUsername(String fieldName) {
        String value = getValue(fieldName);
        if (!ValidationUtil.isValidUsername(value)) {
            addError(fieldName, "Chỉ chứa chữ, số, underscore (3-20 ký tự)");
            return false;
        }
        return true;
    }
    
    /**
     * Validate password
     */
    public boolean validatePassword(String fieldName) {
        String value = getRaw(fieldName);
        if (!ValidationUtil.isValidPassword(value)) {
            addError(fieldName, "Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        return true;
    }
    
    /**
     * Validate password confirmation
     */
    public boolean validatePasswordMatch(String passwordField, String confirmField) {
        String password = getRaw(passwordField);
        String confirm = getRaw(confirmField);
        if (!password.equals(confirm)) {
            addError(confirmField, "Mật khẩu xác nhận không khớp");
            return false;
        }
        return true;
    }
    
    /**
     * Validate positive number
     */
    public boolean validatePositiveNumber(String fieldName, String displayName) {
        String value = getValue(fieldName);
        if (!ValidationUtil.isPositiveNumber(value)) {
            addError(fieldName, displayName + " phải là số dương");
            return false;
        }
        return true;
    }
    
    /**
     * Validate discount (0-100)
     */
    public boolean validateDiscount(String fieldName) {
        String value = getValue(fieldName);
        if (!ValidationUtil.isValidDiscount(value)) {
            addError(fieldName, "Giảm giá phải từ 0-100%");
            return false;
        }
        return true;
    }
    
    /**
     * Validate booking date (not past, not too far)
     */
    public boolean validateBookingDate(String fieldName) {
        String value = getValue(fieldName);
        if (ValidationUtil.isEmpty(value)) {
            addError(fieldName, "Vui lòng chọn ngày hẹn");
            return false;
        }
        if (!ValidationUtil.isValidBookingDate(value)) {
            addError(fieldName, ValidationUtil.getMaxBookingDateMessage());
            return false;
        }
        return true;
    }
    
    // ==================== APPLY TO REQUEST ====================
    
    /**
     * Set form và errors vào request attributes để JSP sử dụng
     */
    public void applyToRequest() {
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        
        // Set error chung nếu có
        if (errors.containsKey("general")) {
            request.setAttribute("error", errors.get("general"));
        } else if (hasErrors()) {
            // Lấy lỗi đầu tiên làm error chung
            request.setAttribute("error", errors.values().iterator().next());
        }
    }
    
    /**
     * Get form map
     */
    public Map<String, String> getFormMap() {
        return form;
    }
    
    /**
     * Get errors map
     */
    public Map<String, String> getErrorsMap() {
        return errors;
    }
}
