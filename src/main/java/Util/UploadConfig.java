package Util;

import java.io.File;

/**
 * Cấu hình thư mục upload ảnh
 * Lưu ảnh vào thư mục bên ngoài webapp để không bị mất khi redeploy
 */
public class UploadConfig {
    
    // Thư mục gốc lưu ảnh - nằm trong thư mục project
    // Có thể thay đổi thành đường dẫn tuyệt đối nếu cần
    private static String UPLOAD_BASE_DIR = null;
    
    // Các thư mục con
    public static final String PRODUCTS_DIR = "shop_pic";
    public static final String BLOGS_DIR = "community_pic";
    public static final String PETS_DIR = "pets_pic";
    
    /**
     * Lấy thư mục gốc upload
     * Mặc định: {user.home}/petvaccine_uploads/
     */
    public static String getUploadBaseDir() {
        if (UPLOAD_BASE_DIR == null) {
            // Lưu vào thư mục home của user
            UPLOAD_BASE_DIR = System.getProperty("user.home") + File.separator + "petvaccine_uploads";
        }
        return UPLOAD_BASE_DIR;
    }
    
    /**
     * Lấy đường dẫn thư mục upload cho products
     */
    public static String getProductsUploadDir() {
        String dir = getUploadBaseDir() + File.separator + PRODUCTS_DIR;
        ensureDirectoryExists(dir);
        return dir;
    }
    
    /**
     * Lấy đường dẫn thư mục upload cho blogs
     */
    public static String getBlogsUploadDir() {
        String dir = getUploadBaseDir() + File.separator + BLOGS_DIR;
        ensureDirectoryExists(dir);
        return dir;
    }
    
    /**
     * Lấy đường dẫn thư mục upload cho pets
     */
    public static String getPetsUploadDir() {
        String dir = getUploadBaseDir() + File.separator + PETS_DIR;
        ensureDirectoryExists(dir);
        return dir;
    }
    
    /**
     * Đảm bảo thư mục tồn tại
     */
    public static void ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Lấy đường dẫn đầy đủ của file
     */
    public static String getFullPath(String subDir, String fileName) {
        return getUploadBaseDir() + File.separator + subDir + File.separator + fileName;
    }
}
