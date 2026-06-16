package controller.auth;

import DAO.UserDAO;
import DAO.SecurityEventDAO;
import Model.User;
import Util.FormHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Handles login for admin, staff, and shiper roles.
 */
@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final SecurityEventDAO securityEventDAO = new SecurityEventDAO();

    /**
     * Displays the admin login page.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/pages/admin/login.jsp").forward(request, response);
    }

    /**
     * Processes the admin login form submission.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        FormHelper form = new FormHelper(request);

        String email = form.get("email");
        String password = form.getRaw("password");

        if (!form.validateRequired("email", "Email") || !form.validateRequiredRaw("password", "Mật khẩu")) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ email và mật khẩu.");
            request.getRequestDispatcher("/pages/admin/login.jsp").forward(request, response);
            return;
        }

        // Brute-force check
        if (userDAO.isAccountLocked(email)) {
            securityEventDAO.log("ACCOUNT_LOCKED_ATTEMPT", email, request.getRemoteAddr(), "Admin login attempt blocked while account is locked.");
            request.setAttribute("error", "Tài khoản đã bị khóa. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/pages/admin/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.loginByEmail(email, password);

        if (user != null) {
            // Check for correct role
            String role = user.getRole();
            boolean isAdminOrStaffOrShiper = "admin".equals(role) || "staff".equals(role) || "shiper".equals(role);

            if (isAdminOrStaffOrShiper && user.getStatus()) {
                // Reset failed attempts
                userDAO.resetFailedAttempts(email);

                // Create new session
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());

                // Redirect based on role
                if ("shiper".equals(role)) {
                    response.sendRedirect(request.getContextPath() + "/admin/orders");
                } else {
                    response.sendRedirect(request.getContextPath() + "/pages/admin/dashboard");
                }
            } else {
                // Wrong role or inactive account
                securityEventDAO.log("AUTH_FAIL", email, request.getRemoteAddr(), "User with role '" + role + "' tried to access admin panel.");
                request.setAttribute("error", "Bạn không có quyền truy cập vào khu vực này.");
                request.getRequestDispatcher("/pages/admin/login.jsp").forward(request, response);
            }
        } else {
            // Failed login
            userDAO.incrementFailedAttempts(email);
            int failedAttempts = userDAO.getFailedLoginAttempts(email);
            if (failedAttempts >= 5) {
                userDAO.lockAccount(email, 15);
                securityEventDAO.log("ACCOUNT_LOCKED", email, request.getRemoteAddr(), "Account locked on admin login after " + failedAttempts + " attempts.");
            }
            request.setAttribute("error", "Email hoặc mật khẩu không đúng.");
            request.getRequestDispatcher("/pages/admin/login.jsp").forward(request, response);
        }
    }
}
