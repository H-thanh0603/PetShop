package Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import jakarta.servlet.http.Part;

/**
 * Utility class for file upload using Servlet 3.0 API
 * Sử dụng @MultipartConfig annotation trong Servlet
 */
public class FileUploadUtil {
    
    // Allowed image extensions
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};
    
    // Max file size: 5MB
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * Lấy tên file từ Part (Servlet 3.0)
     */
    public static String getFileName(Part part) {
        if (part == null) return null;
        
        // Servlet 3.1+ có method getSubmittedFileName()
        String fileName = part.getSubmittedFileName();
        if (fileName != null && !fileName.isEmpty()) {
            return Paths.get(fileName).getFileName().toString();
        }
        
        // Fallback cho Servlet 3.0
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String token : contentDisposition.split(";")) {
                if (token.trim().startsWith("filename")) {
                    return token.substring(token.indexOf('=') + 1).trim()
                               .replace("\"", "");
                }
            }
        }
        return null;
    }
    
    /**
     * Lấy extension của file
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Kiểm tra file có phải là ảnh hợp lệ không
     */
    public static boolean isValidImage(Part part) {
        if (part == null || part.getSize() == 0) {
            return false;
        }
        
        String fileName = getFileName(part);
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        String extension = getFileExtension(fileName);
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Kiểm tra kích thước file
     */
    public static boolean isValidSize(Part part) {
        return part != null && part.getSize() <= MAX_FILE_SIZE;
    }
    
    /**
     * Tạo tên file unique để tránh trùng lặp
     */
    public static String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        long timestamp = System.currentTimeMillis();
        return "img_" + timestamp + "_" + uniqueId + "." + extension;
    }
    
    /**
     * Lưu file vào thư mục chỉ định
     * @param part - Part từ request.getPart()
     * @param uploadDir - Đường dẫn thư mục upload (absolute path)
     * @param customFileName - Tên file tùy chỉnh (null để tự generate)
     * @return Tên file đã lưu, hoặc null nếu lỗi
     */
    public static String saveFile(Part part, String uploadDir, String customFileName) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        
        // Tạo thư mục nếu chưa tồn tại
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // Xác định tên file
        String originalFileName = getFileName(part);
        String fileName = (customFileName != null) ? customFileName : generateUniqueFileName(originalFileName);
        
        // Lưu file
        String filePath = uploadDir + File.separator + fileName;
        part.write(filePath);
        
        return fileName;
    }
    
    /**
     * Xóa file
     */
    public static boolean deleteFile(String uploadDir, String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        File file = new File(uploadDir + File.separator + fileName);
        return file.exists() && file.delete();
    }
    
    /**
     * Format kích thước file để hiển thị
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024));
        }
    }
}
