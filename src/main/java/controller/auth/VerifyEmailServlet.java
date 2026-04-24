package controller.auth;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.UserDAO;
import Model.User;
import services.EmailVerificationService;

@WebServlet("/verify-email")
public class VerifyEmailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");

        if (token == null || token.isBlank()) {
            request.setAttribute("verifyError", "Link xác thực không hợp lệ.");
            request.getRequestDispatcher("/pages/auth/verify-email.jsp").forward(request, response);
            return;
        }

        // Check valid (non-expired) token
        User user = userDAO.getUserByVerificationToken(token);
        if (user != null) {
            userDAO.markEmailVerified(user.getId());
            HttpSession session = request.getSession();
            session.setAttribute("success", "Email đã được xác thực thành công! Vui lòng đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Check expired token
        User expiredUser = userDAO.getUserByExpiredVerificationToken(token);
        if (expiredUser != null) {
            request.setAttribute("verifyError", "Link xác thực đã hết hạn.");
            request.setAttribute("expiredEmail", expiredUser.getEmail());
            request.getRequestDispatcher("/pages/auth/verify-email.jsp").forward(request, response);
            return;
        }

        // Invalid or already used
        request.setAttribute("verifyError", "Link xác thực không hợp lệ hoặc đã được sử dụng.");
        request.getRequestDispatcher("/pages/auth/verify-email.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Resend verification email
        String email = request.getParameter("email");
        if (email == null || email.isBlank()) {
            request.setAttribute("verifyError", "Email không hợp lệ.");
            request.getRequestDispatcher("/pages/auth/verify-email.jsp").forward(request, response);
            return;
        }

        String contextPath = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort() + request.getContextPath();
        EmailVerificationService svc = new EmailVerificationService(userDAO);
        boolean sent = svc.resendVerificationEmail(email, contextPath);

        if (sent) {
            HttpSession session = request.getSession();
            session.setAttribute("success", "Email xác thực đã được gửi lại. Vui lòng kiểm tra hộp thư.");
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            request.setAttribute("verifyError", "Không thể gửi email. Vui lòng thử lại sau.");
            request.setAttribute("expiredEmail", email);
            request.getRequestDispatcher("/pages/auth/verify-email.jsp").forward(request, response);
        }
    }
}
