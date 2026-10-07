package com.petshop.util;

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
        return validate(filePart.getSubmittedFileName(), filePart.getContentType(), filePart.getSize());
    }

    /**
     * Spring MVC port: same rules as {@link #validate(Part)} but driven by a
     * {@code MultipartFile}'s metadata instead of a servlet {@code Part}.
     */
    public static ValidationResult validate(String submittedFileName, String contentType, long size) {
        return validate(submittedFileName, contentType, size, null);
    }

    /**
     * Sniffs the file's leading bytes so a polyglot (client-declared
     * Content-Type + matching extension, but a non-image body) is rejected.
     * {@code head} may be null for metadata-only checks.
     */
    public static boolean hasImageMagicBytes(byte[] head) {
        if (head == null || head.length < 12) {
            return head != null && head.length >= 3 && (startsWith(head, new byte[]{(byte) 0xFF, (byte) 0xD8})
                    || startsWith(head, new byte[]{(byte) 0x89, 'P', 'N', 'G'}));
        }
        return startsWith(head, new byte[]{(byte) 0xFF, (byte) 0xD8})          // JPEG
                || startsWith(head, new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}) // PNG
                || startsWith(head, "GIF87a".getBytes()) || startsWith(head, "GIF89a".getBytes())   // GIF
                || (startsWith(head, "RIFF".getBytes()) && startsWith(head, 8, "WEBP".getBytes())); // WebP
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        return startsWith(data, 0, prefix);
    }

    private static boolean startsWith(byte[] data, int offset, byte[] prefix) {
        if (data == null || data.length < offset + prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[offset + i] != prefix[i]) return false;
        }
        return true;
    }

    public static ValidationResult validate(String submittedFileName, String contentType, long size, byte[] head) {
        if (submittedFileName == null || submittedFileName.isEmpty() || size <= 0) {
            return new ValidationResult(false, "No file uploaded.", null);
        }
        String fileName = submittedFileName;
        if (!isAllowedExtension(fileName)) {
            return new ValidationResult(false, "File type not allowed. Only JPG, PNG, GIF, WebP accepted.", null);
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        if (!isContentTypeMatchingExtension(contentType, ext)) {
            return new ValidationResult(false, "Content type does not match file extension.", null);
        }
        if (head != null && !hasImageMagicBytes(head)) {
            return new ValidationResult(false, "File content is not a valid image.", null);
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
