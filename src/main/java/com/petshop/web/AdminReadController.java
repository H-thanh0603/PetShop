package com.petshop.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import Context.DBContext;
import DAO.PetTypeDAO;
import DAO.ReportDAO;
import Model.Order;
import Model.PetType;
import Model.Product;
import Model.Review;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import services.PetTypeCache;

/**
 * Read-mostly admin pages: /pages/admin/dashboard, /pages/admin/categories,
 * /pages/admin/pet-types, /admin/reports, /admin/statistics,
 * /admin/notifications. Writes (rename category, pet-type CRUD) kept 1:1.
 * AuthorizationFilter still guards all admin paths.
 */
@Controller
public class AdminReadController {

    private static final Logger logger = LoggerFactory.getLogger(AdminReadController.class);

    private final ReportDAO reportDAO;
    private final PetTypeDAO petTypeDAO;

    public AdminReadController() {
        this(new ReportDAO(), new PetTypeDAO());
    }

    AdminReadController(ReportDAO reportDAO, PetTypeDAO petTypeDAO) {
        this.reportDAO = reportDAO;
        this.petTypeDAO = petTypeDAO;
    }

    // ── DASHBOARD (/pages/admin/dashboard) ──

    @GetMapping("/pages/admin/dashboard")
    public String dashboard(Model model) {
        int year = Calendar.getInstance().get(Calendar.YEAR);

        Map<String, Integer> overview = reportDAO.getOverviewStats();
        model.addAttribute("overview", overview);
        model.addAttribute("totalRevenue", reportDAO.getTotalRevenue());
        model.addAttribute("currentMonthRevenue", reportDAO.getCurrentMonthRevenue());
        model.addAttribute("completedOrders", reportDAO.getCompletedOrdersCount());

        List<Order> recentOrders = reportDAO.getRecentOrders(5);
        List<Product> lowStockProducts = reportDAO.getLowStockProducts(10, 5);
        List<Review> recentReviews = reportDAO.getRecentReviews(5);
        List<Map<String, Object>> topProducts = reportDAO.getTopSellingProducts(5);

        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("recentReviews", recentReviews);
        model.addAttribute("topProducts", topProducts);

        // Chart data
        List<Map<String, Object>> revenueByMonth = reportDAO.getRevenueByMonth(year);
        model.addAttribute("revenueByMonthJson", toJsonRevenue(revenueByMonth));

        List<Map<String, Object>> orderStatus = reportDAO.getOrdersByStatus();
        model.addAttribute("orderStatusJson", toJsonCount(orderStatus, "status"));

        return "pages/admin/dashboard";
    }

    @PostMapping("/pages/admin/dashboard")
    public String dashboardPost(Model model) {
        return dashboard(model);
    }

    private String toJsonRevenue(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = list.get(i);
            sb.append("{\"month\":").append(item.get("month"))
              .append(",\"revenue\":").append(item.get("revenue")).append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonCount(List<Map<String, Object>> list, String labelKey) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = list.get(i);
            String label = item.get(labelKey) != null ? item.get(labelKey).toString().replace("\"", "\\\"") : "";
            sb.append("{\"label\":\"").append(label)
              .append("\",\"count\":").append(item.get("count")).append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonOrders(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = list.get(i);
            sb.append("{\"month\":").append(item.get("month"))
              .append(",\"pending\":").append(item.get("pending"))
              .append(",\"completed\":").append(item.get("completed"))
              .append(",\"total\":").append(item.get("total")).append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // ── CATEGORIES (/pages/admin/categories) ──

    @GetMapping("/pages/admin/categories")
    public String categories(Model model) {
        // Lấy danh sách danh mục (distinct category từ products)
        List<String[]> categories = getCategories();
        List<PetType> petTypes = petTypeDAO.getAllPetTypes();
        model.addAttribute("categories", categories);
        model.addAttribute("petTypes", petTypes);
        return "pages/admin/categories";
    }

    @PostMapping("/pages/admin/categories")
    public String categoriesPost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "oldName", required = false) String oldName,
            @RequestParam(value = "newName", required = false) String newName,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "petTypeId", required = false) String petTypeIdRaw,
            HttpServletRequest request,
            HttpSession session) {
        String message;
        String messageType = "success";

        if ("rename".equals(action)) {
            // Đổi tên danh mục (cập nhật tất cả sản phẩm có category cũ)
            if (oldName == null || newName == null || newName.trim().isEmpty()) {
                message = "Tên danh mục không hợp lệ!";
                messageType = "error";
            } else {
                int updated = updateCategoryName(oldName, newName.trim());
                message = "Đã cập nhật " + updated + " sản phẩm sang danh mục mới!";
            }

        } else if ("assign-pet-type".equals(action)) {
            // Gán pet_type_id cho tất cả sản phẩm thuộc danh mục
            int petTypeId = parseIntSafe(petTypeIdRaw, 0);
            if (category == null || petTypeId <= 0) {
                message = "Dữ liệu không hợp lệ!";
                messageType = "error";
            } else {
                int updated = assignPetTypeToCategory(category, petTypeId);
                message = "Đã gán loại thú cưng cho " + updated + " sản phẩm!";
            }

        } else {
            message = "Hành động không hợp lệ!";
            messageType = "error";
        }

        session.setAttribute("message", message);
        session.setAttribute("messageType", messageType);
        return "redirect:" + request.getContextPath() + "/pages/admin/categories";
    }

    // Lấy danh sách danh mục kèm số lượng sản phẩm
    private List<String[]> getCategories() {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT category, COUNT(*) as cnt, " +
                       "(SELECT pt.name FROM pet_types pt WHERE pt.id = " +
                       "(SELECT p2.pet_type_id FROM products p2 WHERE p2.category = p.category LIMIT 1)) as pet_type_name " +
                       "FROM products p WHERE category IS NOT NULL AND category != '' " +
                       "GROUP BY category ORDER BY category";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("category"),
                    String.valueOf(rs.getInt("cnt")),
                    rs.getString("pet_type_name") != null ? rs.getString("pet_type_name") : "Chưa phân loại"
                });
            }
        } catch (Exception e) {
            logger.error("Error loading categories", e);
        }
        return list;
    }

    private int updateCategoryName(String oldName, String newName) {
        String query = "UPDATE products SET category = ? WHERE category = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            return ps.executeUpdate();
        } catch (Exception e) { logger.error("Error renaming category from '{}' to '{}'", oldName, newName, e); }
        return 0;
    }

    private int assignPetTypeToCategory(String category, int petTypeId) {
        String query = "UPDATE products SET pet_type_id = ? WHERE category = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, petTypeId);
            ps.setString(2, category);
            return ps.executeUpdate();
        } catch (Exception e) { logger.error("Error assigning pet type {} to category '{}'", petTypeId, category, e); }
        return 0;
    }

    private int parseIntSafe(String val, int def) {
        try { return Integer.parseInt(val); } catch (Exception e) { return def; }
    }

    // ── PET TYPES (/pages/admin/pet-types) ──

    @GetMapping("/pages/admin/pet-types")
    public String petTypes(Model model) {
        List<PetType> petTypes = petTypeDAO.getAllPetTypes();
        model.addAttribute("petTypes", petTypes);
        return "pages/admin/pet-types";
    }

    @PostMapping("/pages/admin/pet-types")
    public String petTypesPost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "id", required = false) String idRaw,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "icon", required = false) String icon,
            @RequestParam(value = "displayOrder", required = false) String displayOrderRaw,
            @RequestParam(value = "isActive", required = false) String isActiveRaw,
            HttpServletRequest request,
            HttpSession session) {
        String message;
        String messageType = "success";

        if ("add".equals(action)) {
            int displayOrder = parseIntSafe(displayOrderRaw, 0);
            boolean isActive = "true".equals(isActiveRaw);

            if (code == null || code.trim().isEmpty() || name == null || name.trim().isEmpty()) {
                message = "Mã code và tên không được để trống!";
                messageType = "error";
            } else {
                PetType pt = new PetType(0, code.trim().toLowerCase(), name.trim(),
                        icon != null ? icon.trim() : "bx bxs-dog", displayOrder, isActive);
                if (petTypeDAO.addPetType(pt)) {
                    PetTypeCache.getInstance().invalidate();
                    message = "Thêm loại thú cưng thành công!";
                } else {
                    message = "Lỗi khi thêm! Mã code có thể đã tồn tại.";
                    messageType = "error";
                }
            }

        } else if ("edit".equals(action)) {
            int id = parseIntSafe(idRaw, 0);
            int displayOrder = parseIntSafe(displayOrderRaw, 0);
            boolean isActive = "true".equals(isActiveRaw);

            if (id <= 0 || name == null || name.trim().isEmpty()) {
                message = "Dữ liệu không hợp lệ!";
                messageType = "error";
            } else {
                PetType pt = new PetType(id, "", name.trim(),
                        icon != null ? icon.trim() : "bx bxs-dog", displayOrder, isActive);
                if (petTypeDAO.updatePetType(pt)) {
                    PetTypeCache.getInstance().invalidate();
                    message = "Cập nhật thành công!";
                } else {
                    message = "Lỗi khi cập nhật!";
                    messageType = "error";
                }
            }

        } else if ("toggle".equals(action)) {
            int id = parseIntSafe(idRaw, 0);
            boolean isActive = "true".equals(isActiveRaw);
            if (petTypeDAO.togglePetTypeStatus(id, isActive)) {
                PetTypeCache.getInstance().invalidate();
                message = isActive ? "Đã kích hoạt!" : "Đã vô hiệu hóa!";
            } else {
                message = "Lỗi khi cập nhật trạng thái!";
                messageType = "error";
            }

        } else {
            message = "Hành động không hợp lệ!";
            messageType = "error";
        }

        session.setAttribute("message", message);
        session.setAttribute("messageType", messageType);
        return "redirect:" + request.getContextPath() + "/pages/admin/pet-types";
    }

    // ── REPORTS (/admin/reports) ──

    @GetMapping("/admin/reports")
    public String reports(
            @RequestParam(value = "year", required = false) String yearParam,
            Model model) throws IOException {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int year = yearParam != null ? Integer.parseInt(yearParam) : currentYear;

        Map<String, Integer> overview = reportDAO.getOverviewStats();
        List<Map<String, Object>> topProducts = reportDAO.getTopSellingProducts(10);
        List<Map<String, Object>> topCustomers = reportDAO.getTopCustomers(10);
        List<Map<String, Object>> couponUsage = reportDAO.getCouponUsage(10);
        List<Map<String, Object>> orderStatus = reportDAO.getOrdersByStatus();
        List<Map<String, Object>> revenueByMonth = reportDAO.getRevenueByMonth(year);
        List<Product> lowStockProducts = reportDAO.getLowStockProducts(10, 10);
        List<Review> lowRatingReviews = reportDAO.getRecentLowRatingReviews(10);

        model.addAttribute("overview", overview);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("topCustomers", topCustomers);
        model.addAttribute("couponUsage", couponUsage);
        model.addAttribute("orderStatus", orderStatus);
        model.addAttribute("revenueByMonth", revenueByMonth);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("lowRatingReviews", lowRatingReviews);
        model.addAttribute("totalRevenue", reportDAO.getTotalRevenue());
        model.addAttribute("currentMonthRevenue", reportDAO.getCurrentMonthRevenue());
        model.addAttribute("completedOrders", reportDAO.getCompletedOrdersCount());
        model.addAttribute("selectedYear", year);
        model.addAttribute("currentYear", currentYear);

        return "pages/admin/reports";
    }

    // ── STATISTICS (/admin/statistics) ──

    @GetMapping("/admin/statistics")
    public String statistics(
            @RequestParam(value = "year", required = false) String yearParam,
            Model model) throws IOException {
        int year = yearParam != null ? Integer.parseInt(yearParam) : Calendar.getInstance().get(Calendar.YEAR);

        Map<String, Integer> overview = reportDAO.getOverviewStats();
        model.addAttribute("overview", overview);

        // 1. Doanh thu theo tháng
        List<Map<String, Object>> revenueByMonth = reportDAO.getRevenueByMonth(year);
        model.addAttribute("revenueByMonthJson", toJsonRevenue(revenueByMonth));

        // 2. Sản phẩm bán chạy
        List<Map<String, Object>> topProducts = reportDAO.getTopSellingProducts(5);
        model.addAttribute("topProductsJson", toJsonCount(topProducts, "product"));

        // 3. Trạng thái đơn hàng
        List<Map<String, Object>> orderStatus = reportDAO.getOrdersByStatus();
        model.addAttribute("orderStatusJson", toJsonCount(orderStatus, "status"));

        // 4. Đơn hàng theo tháng
        List<Map<String, Object>> ordersByMonth = reportDAO.getOrdersByMonthWithStatus(year);
        model.addAttribute("ordersByMonthJson", toJsonOrders(ordersByMonth));

        model.addAttribute("selectedYear", year);
        model.addAttribute("currentYear", Calendar.getInstance().get(Calendar.YEAR));

        return "pages/admin/statistics";
    }

    // ── ADMIN NOTIFICATIONS (/admin/notifications) ──

    @GetMapping("/admin/notifications")
    public String adminNotifications(Model model) {
        List<Order> pendingOrders = new ArrayList<>();
        for (Order order : reportDAO.getRecentOrders(10)) {
            if ("Pending".equalsIgnoreCase(order.getStatus())) {
                pendingOrders.add(order);
            }
        }
        List<Product> lowStockProducts = reportDAO.getLowStockProducts(10, 10);
        List<Review> lowRatingReviews = reportDAO.getRecentLowRatingReviews(10);
        List<Map<String, Object>> storedNotifications = reportDAO.getStoredNotifications(10);

        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("lowRatingReviews", lowRatingReviews);
        model.addAttribute("storedNotifications", storedNotifications);
        model.addAttribute("pendingOrderCount", pendingOrders.size());
        model.addAttribute("lowStockCount", lowStockProducts.size());
        model.addAttribute("lowRatingCount", lowRatingReviews.size());

        return "pages/admin/notifications";
    }
}
