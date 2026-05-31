package Util;

import jakarta.servlet.http.Part;
import java.util.*;

public class FileUploadValidator {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Map<String, String> CONTENT_TYPE_MAP = Map.of(
        "jpg", "image/jpeg", "jpeg", "image/jpeg",
        "png", "image/png", "gif", "image/gif", "webp", "image/webp"
    );

    public static boolean isAllowedExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return false;
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    public static boolean isContentTypeMatchingExtension(String contentType, String extension) {
        if (contentType == null || extension == null) return false;
        String expected = CONTENT_TYPE_MAP.get(extension.toLowerCase());
        return expected != null && expected.equals(contentType);
    }

    public static String generateSecureFileName(String originalFileName) {
        String ext = "jpg";
        if (originalFileName != null && originalFileName.contains(".")) {
            ext = originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase();
        }
        return java.util.UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "." + ext;
    }

    public static ValidationResult validate(Part filePart) {
        if (filePart == null || filePart.getSize() <= 0) {
            return new ValidationResult(false, "No file uploaded.", null);
        }
        String fileName = filePart.getSubmittedFileName();
        if (fileName == null || fileName.isEmpty()) {
            return new ValidationResult(false, "Invalid file.", null);
        }
        if (!isAllowedExtension(fileName)) {
            return new ValidationResult(false, "File type not allowed. Only JPG, PNG, GIF, WebP accepted.", null);
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        if (!isContentTypeMatchingExtension(filePart.getContentType(), ext)) {
            return new ValidationResult(false, "Content type does not match file extension.", null);
        }
        String secureName = generateSecureFileName(fileName);
        return new ValidationResult(true, null, secureName);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final String secureFileName;
        public ValidationResult(boolean valid, String errorMessage, String secureFileName) {
            this.valid = valid; this.errorMessage = errorMessage; this.secureFileName = secureFileName;
        }
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public String getSecureFileName() { return secureFileName; }
    }
}
