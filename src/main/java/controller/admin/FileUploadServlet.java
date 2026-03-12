package controller.admin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import Util.FileUploadUtil;

/**
 * Servlet upload file sử dụng Servlet 3.0 API (@MultipartConfig)
 * 
 * Cách sử dụng:
 * - Form phải có enctype="multipart/form-data"
 * - Input file có name="file"
 * - Parameter "type" để chọn thư mục: product, blog, hoặc mặc định uploads
 * - Gửi POST request đến /admin/upload?type=product
 * 
 * Response JSON:
 * - Thành công: {"success": true, "fileName": "img_xxx.jpg", "message": "Upload thành công"}
 * - Thất bại: {"success": false, "message": "Lỗi..."}
 */
@WebServlet("/admin/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1MB - Ngưỡng lưu vào memory trước khi ghi disk
    maxFileSize = 5 * 1024 * 1024,         // 5MB - Kích thước tối đa mỗi file
    maxRequestSize = 10 * 1024 * 1024      // 10MB - Tổng kích thước request
)
public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Thư mục lưu ảnh theo loại
    private static final String UPLOAD_FOLDER_PRODUCT = "assets/images/shop_pic";
    private static final String UPLOAD_FOLDER_BLOG = "assets/images/community_pic";
    private static final String UPLOAD_FOLDER_DEFAULT = "assets/images/uploads";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Lấy file từ request
            Part filePart = request.getPart("file");
            
            // Kiểm tra file có tồn tại không
            if (filePart == null || filePart.getSize() == 0) {
                out.print("{\"success\": false, \"message\": \"Vui lòng chọn file để upload\"}");
                return;
            }
            
            // Kiểm tra định dạng file
            if (!FileUploadUtil.isValidImage(filePart)) {
                out.print("{\"success\": false, \"message\": \"Chỉ chấp nhận file ảnh (JPG, PNG, GIF, WebP)\"}");
                return;
            }
            
            // Kiểm tra kích thước
            if (!FileUploadUtil.isValidSize(filePart)) {
                out.print("{\"success\": false, \"message\": \"File quá lớn. Tối đa 5MB\"}");
                return;
            }
            
            // Xác định thư mục upload theo type
            String type = request.getParameter("type");
            String uploadFolder;
            if ("product".equals(type)) {
                uploadFolder = UPLOAD_FOLDER_PRODUCT;
            } else if ("blog".equals(type)) {
                uploadFolder = UPLOAD_FOLDER_BLOG;
            } else {
                uploadFolder = UPLOAD_FOLDER_DEFAULT;
            }
            
            // Lấy đường dẫn thư mục upload (absolute path)
            String uploadDir = getServletContext().getRealPath("") + File.separator + uploadFolder;
            
            // Lưu file
            String fileName = FileUploadUtil.saveFile(filePart, uploadDir, null);
            
            if (fileName != null) {
                // Trả về JSON thành công
                String fileUrl = request.getContextPath() + "/" + uploadFolder + "/" + fileName;
                out.print("{\"success\": true, \"fileName\": \"" + fileName + "\", " +
                         "\"fileUrl\": \"" + fileUrl + "\", " +
                         "\"fileSize\": \"" + FileUploadUtil.formatFileSize(filePart.getSize()) + "\", " +
                         "\"message\": \"Upload thành công!\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"Không thể lưu file\"}");
            }
            
        } catch (IllegalStateException e) {
            // File quá lớn (vượt maxFileSize hoặc maxRequestSize)
            out.print("{\"success\": false, \"message\": \"File quá lớn. Tối đa 5MB\"}");
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Lỗi server: " + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redirect đến trang upload demo
        response.sendRedirect(request.getContextPath() + "/pages/admin/upload-demo.jsp");
    }
}
