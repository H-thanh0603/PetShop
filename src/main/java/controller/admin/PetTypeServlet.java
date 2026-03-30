package controller.admin;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.PetTypeDAO;
import Model.PetType;

@WebServlet("/pages/admin/pet-types")
public class PetTypeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private PetTypeDAO dao = new PetTypeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<PetType> petTypes = dao.getAllPetTypes();
        request.setAttribute("petTypes", petTypes);
        request.getRequestDispatcher("/pages/admin/pet-types.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        String message;
        String messageType = "success";

        if ("add".equals(action)) {
            String code = request.getParameter("code");
            String name = request.getParameter("name");
            String icon = request.getParameter("icon");
            int displayOrder = parseIntSafe(request.getParameter("displayOrder"), 0);
            boolean isActive = "true".equals(request.getParameter("isActive"));

            if (code == null || code.trim().isEmpty() || name == null || name.trim().isEmpty()) {
                message = "Mã code và tên không được để trống!";
                messageType = "error";
            } else {
                PetType pt = new PetType(0, code.trim().toLowerCase(), name.trim(),
                        icon != null ? icon.trim() : "bx bxs-dog", displayOrder, isActive);
                if (dao.addPetType(pt)) {
                    message = "Thêm loại thú cưng thành công!";
                } else {
                    message = "Lỗi khi thêm! Mã code có thể đã tồn tại.";
                    messageType = "error";
                }
            }

        } else if ("edit".equals(action)) {
            int id = parseIntSafe(request.getParameter("id"), 0);
            String name = request.getParameter("name");
            String icon = request.getParameter("icon");
            int displayOrder = parseIntSafe(request.getParameter("displayOrder"), 0);
            boolean isActive = "true".equals(request.getParameter("isActive"));

            if (id <= 0 || name == null || name.trim().isEmpty()) {
                message = "Dữ liệu không hợp lệ!";
                messageType = "error";
            } else {
                PetType pt = new PetType(id, "", name.trim(),
                        icon != null ? icon.trim() : "bx bxs-dog", displayOrder, isActive);
                if (dao.updatePetType(pt)) {
                    message = "Cập nhật thành công!";
                } else {
                    message = "Lỗi khi cập nhật!";
                    messageType = "error";
                }
            }

        } else if ("toggle".equals(action)) {
            int id = parseIntSafe(request.getParameter("id"), 0);
            boolean isActive = "true".equals(request.getParameter("isActive"));
            if (dao.togglePetTypeStatus(id, isActive)) {
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
        response.sendRedirect(request.getContextPath() + "/pages/admin/pet-types");
    }

    private int parseIntSafe(String val, int def) {
        try { return Integer.parseInt(val); } catch (Exception e) { return def; }
    }
}
