package controller.updateinformation;

import DAO.UserDAO;
import Model.User;
import Util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class UpdateProfileCheckoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String fullname = request.getParameter("fullname") == null ? "" : request.getParameter("fullname").trim();
        String phone = ValidationUtil.normalizePhone(request.getParameter("phone"));
        String redirect = request.getParameter("redirect");

        if (fullname.isEmpty() || !fullname.matches("^[\\p{L}\\s]+$") || fullname.replaceAll("\\s", "").length() < 2) {
            session.setAttribute("toastMessage", "Họ tên không hợp lệ.");
            session.setAttribute("toastType", "warning");
            response.sendRedirect(resolveRedirect(request));
            return;
        }

        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            session.setAttribute("toastMessage", "Số điện thoại không hợp lệ.");
            session.setAttribute("toastType", "warning");
            response.sendRedirect(resolveRedirect(request));
            return;
        }

        UserDAO dao = new UserDAO();
        dao.updateProfile(user.getId(), fullname, phone);

        user.setFullname(fullname);
        user.setPhone(phone);

        session.setAttribute("user", user);
        session.setAttribute("toastMessage", "Đã cập nhật thông tin nhận hàng.");
        session.setAttribute("toastType", "success");

        response.sendRedirect(resolveRedirect(request));
    }

    private String resolveRedirect(HttpServletRequest request) {
        String redirect = request.getParameter("redirect");
        if ("account".equalsIgnoreCase(redirect)) {
            return request.getContextPath() + "/my-account";
        }
        return request.getContextPath() + "/checkout";
    }
}
