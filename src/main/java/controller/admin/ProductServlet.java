package controller.admin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import DAO.ProductDAO;
import Model.Product;
import Util.UploadConfig;
import Util.ValidationUtil;

@WebServlet("/pages/admin/products")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1 MB - ngưỡng lưu vào bộ nhớ
    maxFileSize = 1024 * 1024 * 5,        // 5 MB - kích thước file tối đa
    maxRequestSize = 1024 * 1024 * 20     // 20 MB - kích thước request tối đa
)
public class ProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Đường dẫn upload ảnh - phải khớp với đường dẫn hiển thị trong JSP
    private static final String UPLOAD_DIR = "assets/images/shop_pic";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductDAO dao = new ProductDAO();
        List<Product> products = dao.getAllProducts();
        int totalProducts = dao.getTotalProducts();
        int discountedProducts = dao.getDiscountedProducts();
        
        request.setAttribute("products", products);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("discountedProducts", discountedProducts);
        
        request.getRequestDispatcher("/pages/admin/products.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        
        String action = request.getParameter("action");
        ProductDAO dao = new ProductDAO();
        
        String message;
        String messageType = "success";
        
        if ("add".equals(action) || "edit".equals(action)) {
            String name = request.getParameter("name");
            String existingImage = request.getParameter("existingImage");
            String priceStr = request.getParameter("price");
            String discountStr = request.getParameter("discount");
            String description = request.getParameter("description");
            
            // === VALIDATION ===
            boolean valid = true;
            StringBuilder errors = new StringBuilder();
            
            if (name == null || name.trim().isEmpty()) {
                valid = false;
                errors.append("Tên sản phẩm không được để trống. ");
            } else if (name.length() < 2 || name.length() > 200) {
                valid = false;
                errors.append("Tên sản phẩm phải từ 2-200 ký tự. ");
            }
            
            double price = 0;
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    valid = false;
                    errors.append("Giá bán phải lớn hơn 0. ");
                }
            } catch (Exception e) {
                valid = false;
                errors.append("Giá bán không hợp lệ. ");
            }
            
            int discount = 0;
            if (discountStr != null && !discountStr.trim().isEmpty()) {
                try {
                    discount = Integer.parseInt(discountStr);
                    if (discount < 0 || discount > 100) {
                        valid = false;
                        errors.append("Giảm giá phải từ 0-100%. ");
                    }
                } catch (Exception e) {
                    valid = false;
                    errors.append("Giảm giá không hợp lệ. ");
                }
            }
            
            if (!valid) {
                message = errors.toString().trim();
                messageType = "error";
                session.setAttribute("message", message);
                session.setAttribute("messageType", messageType);
                response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                return;
            }
            
            // === HANDLE FILE UPLOAD (Servlet 3.0) ===
            String imageName = existingImage; // Giữ ảnh cũ nếu không upload mới
            
            Part filePart = request.getPart("imageFile");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = getSubmittedFileName(filePart);
                if (fileName != null && !fileName.isEmpty()) {
                    // Validate file type
                    String contentType = filePart.getContentType();
                    if (!isValidImageType(contentType)) {
                        message = "Chỉ chấp nhận file ảnh (JPG, PNG, GIF, WebP)!";
                        messageType = "error";
                        session.setAttribute("message", message);
                        session.setAttribute("messageType", messageType);
                        response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                        return;
                    }
                    
                    // Generate unique filename
                    String extension = getFileExtension(fileName);
                    imageName = "product_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + extension;
                    
                    // Save file vào thư mục external (không bị mất khi redeploy)
                    String uploadPath = UploadConfig.getProductsUploadDir();
                    
                    String filePath = uploadPath + File.separator + imageName;
                    filePart.write(filePath);
                }
            }
            
            // === BUSINESS LOGIC ===
            if ("add".equals(action)) {
                if (dao.addProduct(name, imageName, price, discount, description)) {
                    message = "Thêm sản phẩm thành công!";
                } else {
                    message = "Có lỗi xảy ra khi thêm sản phẩm!";
                    messageType = "error";
                }
            } else {
                String idStr = request.getParameter("id");
                Integer id = ValidationUtil.parseIntOrNull(idStr);
                
                if (id == null) {
                    message = "ID sản phẩm không hợp lệ!";
                    messageType = "error";
                } else if (dao.updateProduct(id, name, imageName, price, discount, description)) {
                    message = "Cập nhật sản phẩm thành công!";
                } else {
                    message = "Có lỗi xảy ra khi cập nhật!";
                    messageType = "error";
                }
            }
        } else if ("delete".equals(action)) {
            String idStr = request.getParameter("id");
            Integer id = ValidationUtil.parseIntOrNull(idStr);
            
            if (id == null) {
                message = "ID sản phẩm không hợp lệ!";
                messageType = "error";
            } else if (dao.deleteProduct(id)) {
                message = "Xóa sản phẩm thành công!";
            } else {
                message = "Có lỗi xảy ra khi xóa!";
                messageType = "error";
            }
        } else {
            message = "Hành động không hợp lệ!";
            messageType = "error";
        }
        
        session.setAttribute("message", message);
        session.setAttribute("messageType", messageType);
        response.sendRedirect(request.getContextPath() + "/pages/admin/products");
    }
    
    // Lấy tên file từ Part (Servlet 3.0)
    private String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp != null) {
            for (String token : contentDisp.split(";")) {
                if (token.trim().startsWith("filename")) {
                    String fileName = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                    // Handle IE which sends full path
                    int index = fileName.lastIndexOf(File.separator);
                    if (index >= 0) {
                        fileName = fileName.substring(index + 1);
                    }
                    return fileName;
                }
            }
        }
        return null;
    }
    
    // Kiểm tra loại file ảnh hợp lệ
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif") ||
            contentType.equals("image/webp")
        );
    }
    
    // Lấy extension của file
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot).toLowerCase();
        }
        return ".jpg";
    }
}