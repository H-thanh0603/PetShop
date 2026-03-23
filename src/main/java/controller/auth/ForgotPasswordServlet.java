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
import Util.OTPUtil;

@WebServlet(urlPatterns = {"/forgot-password", "/verify-otp", "/reset-password"})
public class ForgotPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        switch (path) {
            case "/forgot-password":
                request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
                break;
            case "/verify-otp":
                HttpSession session = request.getSession();
                if (session.getAttribute("resetEmail") == null) {
                    response.sendRedirect(request.getContextPath() + "/forgot-password");
                    return;
                }
                request.getRequestDispatcher("/pages/auth/verify-otp.jsp").forward(request, response);
                break;
            case "/reset-password":
                session = request.getSession();
                if (session.getAttribute("otpVerified") == null || !(boolean) session.getAttribute("otpVerified")) {
                    response.sendRedirect(request.getContextPath() + "/forgot-password");
                    return;
                }
                request.getRequestDispatcher("/pages/auth/reset-password.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        
        switch (path) {
            case "/forgot-password":
                handleForgotPassword(request, response);
                break;
            case "/verify-otp":
                handleVerifyOTP(request, response);
                break;
            case "/reset-password":
                handleResetPassword(request, response);
                break;
        }
    }


    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        
        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập email");
            request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
            return;
        }
        
        email = email.trim().toLowerCase();
        
        // Kiểm tra email tồn tại
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            request.setAttribute("error", "Email không tồn tại trong hệ thống");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
            return;
        }
        
        // Tạo và gửi OTP
        boolean sent = OTPUtil.generateAndSendOTP(email);
        if (sent) {
            HttpSession session = request.getSession();
            session.setAttribute("resetEmail", email);
            session.setAttribute("otpVerified", false);
            response.sendRedirect(request.getContextPath() + "/verify-otp");
        } else {
            request.setAttribute("error", "Không thể gửi email. Vui lòng thử lại sau.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
        }
    }

    private void handleVerifyOTP(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("resetEmail");
        
        if (email == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }
        
        String otp = request.getParameter("otp");
        if (otp == null || otp.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập mã OTP");
            request.getRequestDispatcher("/pages/auth/verify-otp.jsp").forward(request, response);
            return;
        }
        
        if (OTPUtil.verifyOTP(email, otp.trim())) {
            session.setAttribute("otpVerified", true);
            response.sendRedirect(request.getContextPath() + "/reset-password");
        } else {
            request.setAttribute("error", "Mã OTP không đúng hoặc đã hết hạn");
            request.getRequestDispatcher("/pages/auth/verify-otp.jsp").forward(request, response);
        }
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("resetEmail");
        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
        
        if (email == null || otpVerified == null || !otpVerified) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }
        
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // Validate
        if (password == null || password.length() < 6) {
            request.setAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự");
            request.getRequestDispatcher("/pages/auth/reset-password.jsp").forward(request, response);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp");
            request.getRequestDispatcher("/pages/auth/reset-password.jsp").forward(request, response);
            return;
        }
        
        // Cập nhật mật khẩu
        boolean updated = userDAO.updatePassword(email, password);
        
        // Xóa session
        session.removeAttribute("resetEmail");
        session.removeAttribute("otpVerified");
        
        if (updated) {
            session.setAttribute("success", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            request.setAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại.");
            request.getRequestDispatcher("/pages/auth/reset-password.jsp").forward(request, response);
        }
    }
}
