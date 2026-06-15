package controller.admin;

import DAO.ProductDAO;
import DAO.PromotionDAO;
import Model.Product;
import Model.Promotion;
import Util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin/promotions")
public class PromotionServlet extends HttpServlet {
    private final PromotionDAO promotionDAO = new PromotionDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer editId = ValidationUtil.parseIntOrNull(request.getParameter("id"));
        Promotion editingPromotion = editId == null ? null : promotionDAO.getPromotionById(editId);
        request.setAttribute("promotions", promotionDAO.getAllPromotions());
        request.setAttribute("products", productDAO.getAllProducts());
        request.setAttribute("editingPromotion", editingPromotion);
        request.getRequestDispatcher("/pages/admin/promotions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        if ("toggle".equals(action)) {
            handleToggle(request, session);
        } else if ("delete".equals(action)) {
            handleDelete(request, session);
        } else {
            handleSave(request, session);
        }
        response.sendRedirect(request.getContextPath() + "/admin/promotions");
    }

    private void handleSave(HttpServletRequest request, HttpSession session) {
        Promotion promotion = new Promotion();
        Integer id = ValidationUtil.parseIntOrNull(request.getParameter("id"));
        Promotion existing = id != null ? promotionDAO.getPromotionById(id) : null;
        if (id != null) {
            promotion.setId(id);
        }

        String name = trimToEmpty(request.getParameter("name"));
        String discountType = trimToEmpty(request.getParameter("discountType")).toUpperCase();
        boolean isFlashSale = "1".equals(request.getParameter("flashSale"))
                || "on".equalsIgnoreCase(request.getParameter("flashSale"))
                || "true".equalsIgnoreCase(request.getParameter("flashSale"));
        String promotionType = isFlashSale ? "FLASH_SALE" : "NORMAL";
        String discountValueRaw = trimToEmpty(request.getParameter("discountValue"));
        String startRaw = trimToEmpty(request.getParameter("startDate"));
        String endRaw = trimToEmpty(request.getParameter("endDate"));
        String saleQuantityRaw = trimToEmpty(request.getParameter("saleQuantity"));
        String[] productIdsRaw = request.getParameterValues("productIds");

        String validationMessage = validatePromotionInput(name, discountType, promotionType, discountValueRaw, startRaw, endRaw, saleQuantityRaw, productIdsRaw);
        if (validationMessage != null) {
            session.setAttribute("message", validationMessage);
            session.setAttribute("messageType", "error");
            return;
        }

        promotion.setName(name);
        // Mô tả tự lấy từ tên để giữ tương thích với DB cũ; admin không cần nhập riêng.
        promotion.setDescription(existing != null && existing.getDescription() != null ? existing.getDescription() : name);
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
        promotion.setProductIds(parseProductIds(productIdsRaw));

        int savedId = promotionDAO.savePromotion(promotion);
        if (savedId > 0) {
            session.setAttribute("message", promotion.getId() > 0 ? "Cập nhật khuyến mãi thành công." : "Thêm khuyến mãi thành công.");
            session.setAttribute("messageType", "success");
        } else {
            session.setAttribute("message", "Không thể lưu khuyến mãi. Vui lòng kiểm tra lại dữ liệu.");
            session.setAttribute("messageType", "error");
        }
    }

    private void handleToggle(HttpServletRequest request, HttpSession session) {
        Integer id = ValidationUtil.parseIntOrNull(request.getParameter("id"));
        String currentStatus = trimToEmpty(request.getParameter("currentStatus")).toUpperCase();
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

    private void handleDelete(HttpServletRequest request, HttpSession session) {
        Integer id = ValidationUtil.parseIntOrNull(request.getParameter("id"));
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

    private String validatePromotionInput(String name, String discountType, String promotionType, String discountValueRaw,
                                          String startRaw, String endRaw, String saleQuantityRaw, String[] productIdsRaw) {
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
