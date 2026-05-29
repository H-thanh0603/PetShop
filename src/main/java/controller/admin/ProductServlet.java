package controller.admin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import DAO.InventoryBatchDAO;
import DAO.ProductDAO;
import DAO.PetTypeDAO;
import DAO.AdminActionLogDAO;
import Model.InventoryBatch;
import Model.Product;
import Model.ProductAdminInventoryView;
import Model.PetType;
import Model.User;
import Util.ValidationUtil;
import Util.FileUploadValidator;

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
    private final AdminActionLogDAO actionLog = new AdminActionLogDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductDAO dao = new ProductDAO();
        InventoryBatchDAO inventoryBatchDAO = new InventoryBatchDAO();
        List<Product> products = dao.getAllProducts();
        int totalProducts = dao.getTotalProducts();
        int discountedProducts = dao.getDiscountedProducts();
        Map<Integer, ProductAdminInventoryView> inventoryByProduct = inventoryBatchDAO.getProductAdminInventoryViews(30);
        int lowStockProducts = 0;
        int nearExpiryProducts = 0;
        int expiredProducts = 0;
        int missingBatchProducts = 0;

        for (Product product : products) {
            if (product.getStock() > 0 && product.getStock() < 10) {
                lowStockProducts++;
            }
            ProductAdminInventoryView inventory = inventoryByProduct.get(product.getId());
            if (inventory == null || inventory.getActiveBatchCount() == 0) {
                missingBatchProducts++;
                continue;
            }
            if (inventory.getExpiredQuantity() > 0) {
                expiredProducts++;
            } else if (inventory.getNearExpiryQuantity() > 0) {
                nearExpiryProducts++;
            }
        }
        
        request.setAttribute("products", products);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("discountedProducts", discountedProducts);
        request.setAttribute("lowStockProducts", lowStockProducts);
        request.setAttribute("nearExpiryProducts", nearExpiryProducts);
        request.setAttribute("expiredProducts", expiredProducts);
        request.setAttribute("missingBatchProducts", missingBatchProducts);
        request.setAttribute("inventoryByProduct", inventoryByProduct);
        
        List<PetType> petTypes = new PetTypeDAO().getAllPetTypes();
        request.setAttribute("petTypes", petTypes);
        
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
            
            BigDecimal price = BigDecimal.ZERO;
            try {
                price = new BigDecimal(priceStr);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
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
            
            // === VALIDATE STOCK ===
            String stockStr = request.getParameter("stock");
            int stock = 0;
            if (stockStr != null && !stockStr.trim().isEmpty()) {
                try {
                    stock = Integer.parseInt(stockStr.trim());
                    if (stock < 0) {
                        session.setAttribute("message", "Tồn kho phải là số nguyên không âm.");
                        session.setAttribute("messageType", "error");
                        response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                        return;
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute("message", "Tồn kho phải là số nguyên không âm.");
                    session.setAttribute("messageType", "error");
                    response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                    return;
                }
            }
            
            // === VALIDATE WEIGHT ===
            String weightStr = request.getParameter("weight");
            int weight = 0;
            if (weightStr != null && !weightStr.trim().isEmpty()) {
                try {
                    weight = Integer.parseInt(weightStr.trim());
                    if (weight < 0) {
                        session.setAttribute("message", "Trọng lượng phải là số nguyên không âm (gram).");
                        session.setAttribute("messageType", "error");
                        response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                        return;
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute("message", "Trọng lượng phải là số nguyên không âm (gram).");
                    session.setAttribute("messageType", "error");
                    response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                    return;
                }
            }
            
            // === PARSE CATEGORY & PET TYPE ===
            String category = request.getParameter("category");
            if (category == null) category = "";
            
            String petTypeIdStr = request.getParameter("petTypeId");
            int petTypeId = 0;
            if (petTypeIdStr != null && !petTypeIdStr.trim().isEmpty()) {
                try {
                    petTypeId = Integer.parseInt(petTypeIdStr.trim());
                } catch (NumberFormatException e) {
                    petTypeId = 0;
                }
            }

            // === PARSE OPTIONAL IMPORT BATCH ===
            String batchCode = trimToEmpty(request.getParameter("batchCode"));
            String batchQuantityStr = request.getParameter("batchQuantity");
            String batchUnitCostStr = request.getParameter("batchUnitCost");
            String batchExpiryDateStr = trimToEmpty(request.getParameter("batchExpiryDate"));
            String batchNote = trimToEmpty(request.getParameter("batchNote"));
            int batchQuantity = 0;
            BigDecimal batchUnitCost = BigDecimal.ZERO;
            Timestamp batchExpiryDate = null;

            if (batchQuantityStr != null && !batchQuantityStr.trim().isEmpty()) {
                try {
                    batchQuantity = Integer.parseInt(batchQuantityStr.trim());
                    if (batchQuantity < 0) {
                        session.setAttribute("message", "Số lượng nhập lô phải là số nguyên không âm.");
                        session.setAttribute("messageType", "error");
                        response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                        return;
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute("message", "Số lượng nhập lô phải là số nguyên không âm.");
                    session.setAttribute("messageType", "error");
                    response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                    return;
                }
            }

            if (batchUnitCostStr != null && !batchUnitCostStr.trim().isEmpty()) {
                try {
                    batchUnitCost = new BigDecimal(batchUnitCostStr.trim());
                    if (batchUnitCost.compareTo(BigDecimal.ZERO) < 0) {
                        session.setAttribute("message", "Giá vốn lô hàng không được âm.");
                        session.setAttribute("messageType", "error");
                        response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                        return;
                    }
                } catch (Exception e) {
                    session.setAttribute("message", "Giá vốn lô hàng không hợp lệ.");
                    session.setAttribute("messageType", "error");
                    response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                    return;
                }
            }

            if (!batchExpiryDateStr.isEmpty()) {
                try {
                    LocalDate expiryDate = LocalDate.parse(batchExpiryDateStr);
                    if (expiryDate.isBefore(LocalDate.now())) {
                        session.setAttribute("message", "Hạn sử dụng của lô nhập mới không được là ngày đã qua.");
                        session.setAttribute("messageType", "error");
                        response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                        return;
                    }
                    batchExpiryDate = Timestamp.valueOf(expiryDate.atStartOfDay());
                } catch (DateTimeParseException e) {
                    session.setAttribute("message", "Hạn sử dụng lô hàng không hợp lệ.");
                    session.setAttribute("messageType", "error");
                    response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                    return;
                }
            }
            
            // === HANDLE FILE UPLOAD (Servlet 3.0) ===
            String imageName = existingImage; // Giữ ảnh cũ nếu không upload mới
            
            Part filePart = request.getPart("imageFile");
            if (filePart != null && filePart.getSize() > 0) {
                // Validate file using FileUploadValidator
                FileUploadValidator.ValidationResult validationResult = FileUploadValidator.validate(filePart);
                if (!validationResult.isValid()) {
                    message = validationResult.getErrorMessage();
                    messageType = "error";
                    session.setAttribute("message", message);
                    session.setAttribute("messageType", messageType);
                    response.sendRedirect(request.getContextPath() + "/pages/admin/products");
                    return;
                }
                
                // Use secure filename from validator
                imageName = validationResult.getSecureFileName();
                
                // Lưu ảnh vào webapp/assets/images/shop_pic
                String uploadPath = getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + "shop_pic";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                
                String filePath = uploadPath + File.separator + imageName;
                filePart.write(filePath);
            }
            
            // === BUSINESS LOGIC ===
            Integer savedProductId = null;
            if ("add".equals(action)) {
                int initialStock = batchQuantity > 0 ? 0 : stock;
                int newProductId = dao.addProductAndReturnId(name, imageName, price, discount, description, initialStock, weight, category, petTypeId);
                if (newProductId > 0) {
                    savedProductId = newProductId;
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
                } else if (dao.updateProduct(id, name, imageName, price, discount, description, stock, weight, category, petTypeId)) {
                    savedProductId = id;
                    message = "Cập nhật sản phẩm thành công!";
                } else {
                    message = "Có lỗi xảy ra khi cập nhật!";
                    messageType = "error";
                }
            }

            if (savedProductId != null && batchQuantity > 0 && "success".equals(messageType)) {
                if (batchCode.isEmpty()) {
                    batchCode = "LOT-" + savedProductId + "-" + System.currentTimeMillis();
                }

                InventoryBatch batch = new InventoryBatch();
                batch.setProductId(savedProductId);
                batch.setBatchCode(batchCode);
                batch.setReceivedQuantity(batchQuantity);
                batch.setRemainingQuantity(batchQuantity);
                batch.setUnitCost(batchUnitCost);
                batch.setExpiryDate(batchExpiryDate);
                batch.setNote(batchNote);

                User admin = (User) session.getAttribute("user");
                Integer adminId = admin != null ? admin.getId() : null;
                if (new InventoryBatchDAO().recordImportBatch(batch, adminId)) {
                    message += " Đã nhập thêm lô " + batchCode + " (" + batchQuantity + " sản phẩm).";
                } else {
                    message = "Sản phẩm đã lưu nhưng nhập lô hàng thất bại. Kiểm tra mã lô có bị trùng không.";
                    messageType = "error";
                }
            }
        } else if ("delete".equals(action)) {
            String idStr = request.getParameter("id");
            Integer id = ValidationUtil.parseIntOrNull(idStr);
            HttpSession session2 = request.getSession();
            User admin = (User) session2.getAttribute("user");
            int adminId = admin != null ? admin.getId() : 1;
            
            if (id == null) {
                message = "ID sản phẩm không hợp lệ!";
                messageType = "error";
            } else if (dao.softDeleteProduct(id)) {
                actionLog.log(adminId, "DELETE_PRODUCT", "product", id, null);
                message = "Ẩn sản phẩm thành công!";
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

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
