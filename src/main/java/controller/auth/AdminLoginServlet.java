package controller.auth;

import DAO.UserDAO;
import DAO.SecurityEventDAO;
import Model.User;
import Util.FormHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Handles login for admin, staff, and shipper roles.
 */

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

        // Brute-force check for this (email, IP) pair
        if (Util.LoginLockout.isLocked(email, request.getRemoteAddr())) {
            securityEventDAO.log("ACCOUNT_LOCKED_ATTEMPT", email, request.getRemoteAddr(), "Admin login attempt blocked while the (email, IP) lock is still active.");
            request.setAttribute("error", "Email hoặc mật khẩu không đúng.");
            request.getRequestDispatcher("/pages/admin/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.loginByEmail(email, password);

        if (user != null) {
            // Check for correct role
            String role = user.getRole();
            boolean isAdminOrStaffOrShipper = "admin".equals(role) || "staff".equals(role) || "shipper".equals(role);

            if (isAdminOrStaffOrShipper && user.getStatus()) {
                // Reset failed attempts
                userDAO.resetFailedAttempts(email);
                Util.LoginLockout.reset(email, request.getRemoteAddr());

                // Session regeneration: invalidate the pre-auth session (and its
                // fixed JSESSIONID + CSRF token) so a session-fixation attempt on
                // /admin/login fails; the new session gets a fresh CSRF token too.
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());

                // Redirect based on role
                if ("shipper".equals(role)) {
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
            // Record failure per (email, IP); lock that pair after 5 failures
            boolean nowLocked = Util.LoginLockout.recordFailure(email, request.getRemoteAddr());
            if (nowLocked) {
                securityEventDAO.log("ACCOUNT_LOCKED", email, request.getRemoteAddr(),
                        "Admin login locked for the (email, IP) pair for 15 minutes after repeated failures.");
            }
            request.setAttribute("error", "Email hoặc mật khẩu không đúng.");
            request.getRequestDispatcher("/pages/admin/login.jsp").forward(request, response);
        }
    }
}
