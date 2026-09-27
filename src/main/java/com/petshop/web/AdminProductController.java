package com.petshop.web;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.petshop.dao.AdminActionLogDAO;
import com.petshop.dao.PetTypeDAO;
import com.petshop.dao.ProductDAO;
import com.petshop.model.PetType;
import com.petshop.model.Product;
import com.petshop.model.User;
import com.petshop.util.AppConfig;
import com.petshop.util.FileUploadValidator;
import com.petshop.util.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces ProductServlet (/pages/admin/products) 1:1 — same list page,
 * same add/edit/delete validation messages, same upload handling.
 * AuthorizationFilter still guards all admin paths.
 */
@Controller
public class AdminProductController {

    private final ProductDAO productDAO;
    private final PetTypeDAO petTypeDAO;
    private final AdminActionLogDAO actionLog;

    public AdminProductController() {
        this(new ProductDAO(), new PetTypeDAO(), new AdminActionLogDAO());
    }

    AdminProductController(ProductDAO productDAO, PetTypeDAO petTypeDAO, AdminActionLogDAO actionLog) {
        this.productDAO = productDAO;
        this.petTypeDAO = petTypeDAO;
        this.actionLog = actionLog;
    }

    @GetMapping("/pages/admin/products")
    public String products(Model model) {
        List<Product> products = productDAO.getAllProducts();
        int totalProducts = productDAO.getTotalProducts();
        int discountedProducts = productDAO.getDiscountedProducts();

        model.addAttribute("products", products);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("discountedProducts", discountedProducts);

        List<PetType> petTypes = petTypeDAO.getAllPetTypes();
        model.addAttribute("petTypes", petTypes);

        return "pages/admin/products";
    }

    @PostMapping("/pages/admin/products")
    public String productsPost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "id", required = false) String idRaw,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "existingImage", required = false) String existingImage,
            @RequestParam(value = "price", required = false) String priceStr,
            @RequestParam(value = "discount", required = false) String discountStr,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "weight", required = false) String weightStr,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "petTypeId", required = false) String petTypeIdStr,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            HttpServletRequest request,
            HttpSession session) throws Exception {
        String redirect = "redirect:" + request.getContextPath() + "/pages/admin/products";

        String message = "";
        String messageType = "success";

        if ("add".equals(action)) {
            String error = validateNamePriceDiscount(name, priceStr, discountStr);
            if (error != null) {
                session.setAttribute("message", error);
                session.setAttribute("messageType", "error");
                return redirect;
            }

            Integer weight = parseWeight(weightStr, session, redirect);
            if (weight == null) {
                return redirect;
            }

            if (category == null) category = "";
            int petTypeId = parsePetTypeId(petTypeIdStr);

            String imageName = existingImage;
            if (imageFile != null && !imageFile.isEmpty()) {
                FileUploadValidator.ValidationResult validationResult = FileUploadValidator.validate(
                        imageFile.getOriginalFilename(), imageFile.getContentType(), imageFile.getSize());
                if (!validationResult.isValid()) {
                    session.setAttribute("message", validationResult.getErrorMessage());
                    session.setAttribute("messageType", "error");
                    return redirect;
                }
                imageName = validationResult.getSecureFileName();
                writeUploadFile(imageFile, imageName);
            }

            BigDecimal price = new BigDecimal(priceStr);
            int discount = parseDiscount(discountStr);
            int newProductId = productDAO.addProductAndReturnId(name, imageName, price, discount,
                    description, weight, category, petTypeId);
            if (newProductId > 0) {
                message = "Thêm sản phẩm thành công!";
            } else {
                message = "Có lỗi xảy ra khi thêm sản phẩm!";
                messageType = "error";
            }
        } else if ("edit".equals(action)) {
            String error = validateNamePriceDiscount(name, priceStr, discountStr);
            if (error != null) {
                session.setAttribute("message", error);
                session.setAttribute("messageType", "error");
                return redirect;
            }

            Integer weight = parseWeight(weightStr, session, redirect);
            if (weight == null) {
                return redirect;
            }

            if (category == null) category = "";
            int petTypeId = parsePetTypeId(petTypeIdStr);

            String imageName = existingImage;
            if (imageFile != null && !imageFile.isEmpty()) {
                FileUploadValidator.ValidationResult validationResult = FileUploadValidator.validate(
                        imageFile.getOriginalFilename(), imageFile.getContentType(), imageFile.getSize());
                if (!validationResult.isValid()) {
                    session.setAttribute("message", validationResult.getErrorMessage());
                    session.setAttribute("messageType", "error");
                    return redirect;
                }
                imageName = validationResult.getSecureFileName();
                writeUploadFile(imageFile, imageName);
            }

            Integer id = ValidationUtil.parseIntOrNull(idRaw);
            BigDecimal price = new BigDecimal(priceStr);
            int discount = parseDiscount(discountStr);

            if (id == null) {
                message = "ID sản phẩm không hợp lệ!";
                messageType = "error";
            } else if (productDAO.updateProduct(id, name, imageName, price, discount,
                    description, weight, category, petTypeId)) {
                message = "Cập nhật sản phẩm thành công!";
            } else {
                message = "Có lỗi xảy ra khi cập nhật!";
                messageType = "error";
            }
        } else if ("delete".equals(action)) {
            Integer id = ValidationUtil.parseIntOrNull(idRaw);
            User admin = (User) session.getAttribute("user");
            int adminId = admin != null ? admin.getId() : 1;

            if (id == null) {
                message = "ID sản phẩm không hợp lệ!";
                messageType = "error";
            } else if (productDAO.softDeleteProduct(id)) {
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
        return redirect;
    }

    private String validateNamePriceDiscount(String name, String priceStr, String discountStr) {
        StringBuilder errors = new StringBuilder();

        if (name == null || name.trim().isEmpty()) {
            errors.append("Tên sản phẩm không được để trống. ");
        } else if (name.length() < 2 || name.length() > 200) {
            errors.append("Tên sản phẩm phải từ 2-200 ký tự. ");
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                errors.append("Giá bán phải lớn hơn 0. ");
            }
        } catch (Exception e) {
            errors.append("Giá bán không hợp lệ. ");
        }

        if (discountStr != null && !discountStr.trim().isEmpty()) {
            try {
                int discount = Integer.parseInt(discountStr);
                if (discount < 0 || discount > 100) {
                    errors.append("Giảm giá phải từ 0-100%. ");
                }
            } catch (Exception e) {
                errors.append("Giảm giá không hợp lệ. ");
            }
        }

        return errors.length() == 0 ? null : errors.toString().trim();
    }

    private Integer parseWeight(String weightStr, HttpSession session, String redirect) {
        int weight = 0;
        if (weightStr != null && !weightStr.trim().isEmpty()) {
            try {
                weight = Integer.parseInt(weightStr.trim());
                if (weight < 0) {
                    session.setAttribute("message", "Trọng lượng phải là số nguyên không âm (gram).");
                    session.setAttribute("messageType", "error");
                    return null;
                }
            } catch (NumberFormatException e) {
                session.setAttribute("message", "Trọng lượng phải là số nguyên không âm (gram).");
                session.setAttribute("messageType", "error");
                return null;
            }
        }
        return weight;
    }

    private int parsePetTypeId(String petTypeIdStr) {
        if (petTypeIdStr != null && !petTypeIdStr.trim().isEmpty()) {
            try {
                return Integer.parseInt(petTypeIdStr.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private int parseDiscount(String discountStr) {
        if (discountStr != null && !discountStr.trim().isEmpty()) {
            try {
                return Integer.parseInt(discountStr);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    private void writeUploadFile(MultipartFile imageFile, String imageName) throws Exception {
        // Same visible path as before (assets/images/shop_pic/...) but stored
        // under app.upload-dir so redeploys keep images; WebMvcConfig serves it.
        String baseDir = AppConfig.getOrDefault("app.upload-dir",
                System.getProperty("user.dir") + File.separator + "uploads");
        String uploadPath = baseDir + File.separator + "shop_pic";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();
        imageFile.transferTo(new File(uploadPath + File.separator + imageName));
    }
}
