package com.petshop.web;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import DAO.ProductDAO;
import DAO.PromotionDAO;
import Model.Product;
import Model.Promotion;
import Util.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces PromotionServlet (/admin/promotions) 1:1 — same list/edit page,
 * same save/toggle/delete validation messages. AuthorizationFilter still
 * guards all admin paths.
 */
@Controller
public class AdminPromotionController {

    private final PromotionDAO promotionDAO;
    private final ProductDAO productDAO;

    public AdminPromotionController() {
        this(new PromotionDAO(), new ProductDAO());
    }

    AdminPromotionController(PromotionDAO promotionDAO, ProductDAO productDAO) {
        this.promotionDAO = promotionDAO;
        this.productDAO = productDAO;
    }

    @GetMapping("/admin/promotions")
    public String promotions(
            @RequestParam(value = "id", required = false) String idRaw,
            Model model) {
        Integer editId = ValidationUtil.parseIntOrNull(idRaw);
        Promotion editingPromotion = editId == null ? null : promotionDAO.getPromotionById(editId);
        model.addAttribute("promotions", promotionDAO.getAllPromotions());
        model.addAttribute("products", productDAO.getAllProducts());
        model.addAttribute("editingPromotion", editingPromotion);
        return "pages/admin/promotions";
    }

    @PostMapping("/admin/promotions")
    public String promotionsPost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "id", required = false) String idRaw,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "discountType", required = false) String discountType,
            @RequestParam(value = "flashSale", required = false) String flashSale,
            @RequestParam(value = "discountValue", required = false) String discountValueRaw,
            @RequestParam(value = "startDate", required = false) String startRaw,
            @RequestParam(value = "endDate", required = false) String endRaw,
            @RequestParam(value = "saleQuantity", required = false) String saleQuantityRaw,
            @RequestParam(value = "productIds", required = false) List<String> productIdsRaw,
            @RequestParam(value = "currentStatus", required = false) String currentStatus,
            HttpServletRequest request,
            HttpSession session) {
        if ("toggle".equals(action)) {
            handleToggle(idRaw, currentStatus, session);
        } else if ("delete".equals(action)) {
            handleDelete(idRaw, session);
        } else {
            handleSave(idRaw, name, discountType, flashSale, discountValueRaw,
                    startRaw, endRaw, saleQuantityRaw, productIdsRaw, session);
        }
        return "redirect:" + request.getContextPath() + "/admin/promotions";
    }

    private void handleSave(String idRaw, String name, String discountTypeRaw, String flashSale,
                            String discountValueRaw, String startRaw, String endRaw,
                            String saleQuantityRaw, List<String> productIdsRaw, HttpSession session) {
        Promotion promotion = new Promotion();
        Integer id = ValidationUtil.parseIntOrNull(idRaw);
        Promotion existing = id != null ? promotionDAO.getPromotionById(id) : null;
        if (id != null) {
            promotion.setId(id);
        }

        String promoName = trimToEmpty(name);
        String discountType = trimToEmpty(discountTypeRaw).toUpperCase();
        boolean isFlashSale = "1".equals(flashSale)
                || "on".equalsIgnoreCase(flashSale)
                || "true".equalsIgnoreCase(flashSale);
        String promotionType = isFlashSale ? "FLASH_SALE" : "NORMAL";
        String[] productIdsArr = productIdsRaw == null ? null : productIdsRaw.toArray(new String[0]);

        String validationMessage = validatePromotionInput(promoName, discountType, promotionType,
                trimToEmpty(discountValueRaw), trimToEmpty(startRaw), trimToEmpty(endRaw),
                trimToEmpty(saleQuantityRaw), productIdsArr);
        if (validationMessage != null) {
            session.setAttribute("message", validationMessage);
            session.setAttribute("messageType", "error");
            return;
        }

        promotion.setName(promoName);
        // Mô tả tự lấy từ tên để giữ tương thích với DB cũ; admin không cần nhập riêng.
        promotion.setDescription(existing != null && existing.getDescription() != null ? existing.getDescription() : promoName);
        promotion.setDiscountType(discountType);
        promotion.setPromotionType(promotionType);
        // Khi tạo mới mặc định bật; khi sửa giữ nguyên trạng thái hiện có (admin bật/tắt qua nút riêng).
        promotion.setStatus(existing != null && existing.getStatus() != null ? existing.getStatus() : "ACTIVE");
        promotion.setDiscountValue(new BigDecimal(discountValueRaw));
        promotion.setStartDate(Timestamp.valueOf(LocalDateTime.parse(startRaw)));
        promotion.setEndDate(Timestamp.valueOf(LocalDateTime.parse(endRaw)));
        if (isFlashSale) {
            promotion.setSaleQuantity(Integer.parseInt(saleQuantityRaw));
        }
        promotion.setProductIds(parseProductIds(productIdsArr));

        int savedId = promotionDAO.savePromotion(promotion);
        if (savedId > 0) {
            session.setAttribute("message", promotion.getId() > 0 ? "Cập nhật khuyến mãi thành công." : "Thêm khuyến mãi thành công.");
            session.setAttribute("messageType", "success");
        } else {
            session.setAttribute("message", "Không thể lưu khuyến mãi. Vui lòng kiểm tra lại dữ liệu.");
            session.setAttribute("messageType", "error");
        }
    }

    private void handleToggle(String idRaw, String currentStatusRaw, HttpSession session) {
        Integer id = ValidationUtil.parseIntOrNull(idRaw);
        String currentStatus = trimToEmpty(currentStatusRaw).toUpperCase();
        if (id == null) {
            session.setAttribute("message", "Mã khuyến mãi không hợp lệ.");
            session.setAttribute("messageType", "error");
            return;
        }
        String nextStatus = "ACTIVE".equals(currentStatus) ? "INACTIVE" : "ACTIVE";
        if (promotionDAO.updatePromotionStatus(id, nextStatus)) {
            session.setAttribute("message", "ACTIVE".equals(nextStatus)
                    ? "Đã bật khuyến mãi. Sản phẩm sẽ áp dụng giá giảm ngay lập tức."
                    : "Đã tắt khuyến mãi. Sản phẩm trở về giá thường.");
            session.setAttribute("messageType", "success");
        } else {
            session.setAttribute("message", "Không thể cập nhật trạng thái khuyến mãi. Vui lòng thử lại.");
            session.setAttribute("messageType", "error");
        }
    }

    private void handleDelete(String idRaw, HttpSession session) {
        Integer id = ValidationUtil.parseIntOrNull(idRaw);
        if (id == null) {
            session.setAttribute("message", "Mã khuyến mãi không hợp lệ.");
            session.setAttribute("messageType", "error");
            return;
        }
        if (promotionDAO.deletePromotion(id)) {
            session.setAttribute("message", "Đã xóa khuyến mãi khỏi hệ thống.");
            session.setAttribute("messageType", "success");
        } else {
            session.setAttribute("message", "Không thể xóa khuyến mãi này vì đã được áp dụng cho đơn hàng. Bạn có thể tắt khuyến mãi để dừng áp dụng.");
            session.setAttribute("messageType", "warning");
        }
    }

    private String validatePromotionInput(String name, String discountType, String promotionType,
                                          String discountValueRaw, String startRaw, String endRaw,
                                          String saleQuantityRaw, String[] productIdsRaw) {
        if (name.isEmpty()) {
            return "Tên khuyến mãi không được để trống.";
        }
        if (!"PERCENT".equals(discountType) && !"FIXED".equals(discountType)) {
            return "Kiểu giảm giá không hợp lệ.";
        }
        if (!"NORMAL".equals(promotionType) && !"FLASH_SALE".equals(promotionType)) {
            return "Loại khuyến mãi không hợp lệ.";
        }
        BigDecimal discountValue;
        try {
            discountValue = new BigDecimal(discountValueRaw);
        } catch (Exception e) {
            return "Giá trị giảm phải là số hợp lệ.";
        }
        if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            return "Giá trị giảm phải lớn hơn 0.";
        }
        if ("PERCENT".equals(discountType) && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            return "Khuyến mãi phần trăm không được lớn hơn 100.";
        }
        LocalDateTime startDate;
        LocalDateTime endDate;
        try {
            startDate = LocalDateTime.parse(startRaw);
            endDate = LocalDateTime.parse(endRaw);
        } catch (Exception e) {
            return "Ngày bắt đầu hoặc ngày kết thúc không hợp lệ.";
        }
        if (!endDate.isAfter(startDate)) {
            return "Ngày kết thúc phải lớn hơn ngày bắt đầu.";
        }
        if (productIdsRaw == null || productIdsRaw.length == 0) {
            return "Khuyến mãi phải có ít nhất một sản phẩm áp dụng.";
        }
        if ("FLASH_SALE".equals(promotionType)) {
            try {
                int saleQuantity = Integer.parseInt(saleQuantityRaw);
                if (saleQuantity <= 0) {
                    return "Flash Sale phải có số lượng lớn hơn 0.";
                }
            } catch (Exception e) {
                return "Số lượng Flash Sale không hợp lệ.";
            }
        }
        return null;
    }

    private List<Integer> parseProductIds(String[] productIdsRaw) {
        List<Integer> ids = new ArrayList<>();
        if (productIdsRaw == null) {
            return ids;
        }
        for (String productIdRaw : productIdsRaw) {
            Integer id = ValidationUtil.parseIntOrNull(productIdRaw);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
