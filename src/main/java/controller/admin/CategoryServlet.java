package controller.admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import Context.DBContext;
import DAO.PetTypeDAO;
import Model.PetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/pages/admin/categories")
public class CategoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(CategoryServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lấy danh sách danh mục (distinct category từ products)
        List<String[]> categories = getCategories();
        List<PetType> petTypes = new PetTypeDAO().getAllPetTypes();
        request.setAttribute("categories", categories);
        request.setAttribute("petTypes", petTypes);
        request.getRequestDispatcher("/pages/admin/categories.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        String message;
        String messageType = "success";

        if ("rename".equals(action)) {
            // Đổi tên danh mục (cập nhật tất cả sản phẩm có category cũ)
            String oldName = request.getParameter("oldName");
            String newName = request.getParameter("newName");
            if (oldName == null || newName == null || newName.trim().isEmpty()) {
                message = "Tên danh mục không hợp lệ!";
                messageType = "error";
            } else {
                int updated = updateCategoryName(oldName, newName.trim());
                message = "Đã cập nhật " + updated + " sản phẩm sang danh mục mới!";
            }

        } else if ("assign-pet-type".equals(action)) {
            // Gán pet_type_id cho tất cả sản phẩm thuộc danh mục
            String category = request.getParameter("category");
            int petTypeId = parseIntSafe(request.getParameter("petTypeId"), 0);
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
        response.sendRedirect(request.getContextPath() + "/pages/admin/categories");
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
}
