package controller.admin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import Util.AppConfig;
import Util.FileUploadUtil;
import Util.FileUploadValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet upload file sử dụng Servlet 3.0 API (@MultipartConfig)
 * 
 * Cách sử dụng:
 * - Form phải có enctype="multipart/form-data"
 * - Input file có name="file"
 * - Parameter "type" để chọn thư mục: product hoặc mặc định uploads
 * - Gửi POST request đến /admin/upload?type=product
 * 
 * Response JSON:
 * - Thành công: {"success": true, "fileName": "img_xxx.jpg", "message": "Upload thành công"}
 * - Thất bại: {"success": false, "message": "Lỗi..."}
 */

public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(FileUploadServlet.class);
    private final Gson gson = new Gson();

    // Sub-folders under the configured upload directory (app.upload-dir)
    private static final String UPLOAD_FOLDER_PRODUCT = "shop_pic";
    private static final String UPLOAD_FOLDER_DEFAULT = "uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            // Lấy file từ request
            Part filePart = request.getPart("file");

            // Validate file using FileUploadValidator
            FileUploadValidator.ValidationResult validationResult = FileUploadValidator.validate(filePart);
            if (!validationResult.isValid()) {
                writeJsonResponse(response, false, validationResult.getErrorMessage());
                return;
            }

            // Kiểm tra kích thước
            if (!FileUploadUtil.isValidSize(filePart)) {
                writeJsonResponse(response, false, "File quá lớn. Tối đa 5MB");
                return;
            }

            // Xác định thư mục upload theo type
            String type = request.getParameter("type");
            String uploadFolder;
            if ("product".equals(type)) {
                uploadFolder = UPLOAD_FOLDER_PRODUCT;
            } else {
                uploadFolder = UPLOAD_FOLDER_DEFAULT;
            }

            // Store outside the WAR (configurable) so redeploys keep images.
            // Served back at /assets/images/<folder>/<file> by WebMvcConfig.
            String baseDir = AppConfig.getOrDefault("app.upload-dir",
                    System.getProperty("user.dir") + File.separator + "uploads");
            String uploadDir = baseDir + File.separator + uploadFolder;
            
            // Use secure filename from validator
            String fileName = validationResult.getSecureFileName();
            
            // Ensure upload directory exists
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            
            // Write file
            filePart.write(uploadDir + File.separator + fileName);
            
            if (fileName != null) {
                // Trả về JSON thành công
                String fileUrl = request.getContextPath() + "/assets/images/" + uploadFolder + "/" + fileName;
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("fileName", fileName);
                result.put("fileUrl", fileUrl);
                result.put("fileSize", FileUploadUtil.formatFileSize(filePart.getSize()));
                result.put("message", "Upload thành công!");
                response.getWriter().write(gson.toJson(result));
            } else {
                writeJsonResponse(response, false, "Không thể lưu file");
            }
            
        } catch (IllegalStateException e) {
            // File quá lớn (vượt maxFileSize hoặc maxRequestSize)
            writeJsonResponse(response, false, "File quá lớn. Tối đa 5MB");
        } catch (Exception e) {
            logger.error("Error uploading file", e);
            writeJsonResponse(response, false, "Lỗi server khi upload file");
        }
    }
    
    private void writeJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        response.getWriter().write(gson.toJson(result));
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Project ecommerce chỉ hỗ trợ upload ảnh phục vụ quản lý sản phẩm.
        response.sendRedirect(request.getContextPath() + "/pages/admin/products");
    }
}
