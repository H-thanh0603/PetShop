package com.petshop.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import DAO.RememberTokenDAO;
import DAO.UserDAO;
import Model.User;
import Util.OTPUtil;
import Util.PasswordUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import services.EmailVerificationService;

/**
 * Auth pages: /logout, /verify-email, /forgot-password, /verify-otp,
 * /reset-password. Login/register/social servlets move here in later phases.
 */
@Controller
public class AuthController {

    private final RememberTokenDAO rememberTokenDAO;
    private final UserDAO userDAO;

    public AuthController() {
        this(new RememberTokenDAO(), new UserDAO());
    }

    AuthController(RememberTokenDAO rememberTokenDAO) {
        this(rememberTokenDAO, new UserDAO());
    }

    AuthController(RememberTokenDAO rememberTokenDAO, UserDAO userDAO) {
        this.rememberTokenDAO = rememberTokenDAO;
        this.userDAO = userDAO;
    }

    @GetMapping("/logout")
    public String logoutGet(HttpServletRequest request, HttpServletResponse response) {
        return logout(request, response);
    }

    @PostMapping("/logout")
    public String logoutPost(HttpServletRequest request, HttpServletResponse response) {
        return logout(request, response);
    }

    private String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                // Delete all remember-me tokens for this user
                rememberTokenDAO.deleteAllTokensForUser(user.getId());
            }
            session.invalidate();
        }

        // Clear remember_token cookie
        Cookie clearCookie = new Cookie("remember_token", "");
        clearCookie.setMaxAge(0);
        clearCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        clearCookie.setHttpOnly(true);
        response.addCookie(clearCookie);

        return "redirect:/home";
    }

    // ── VERIFY EMAIL (/verify-email) ──

    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam(value = "token", required = false) String token,
            Model model,
            HttpSession session) {
        if (token == null || token.isBlank()) {
            model.addAttribute("verifyError", "Link xác thực không hợp lệ.");
            return "pages/auth/verify-email";
        }

        // Check valid (non-expired) token
        User user = userDAO.getUserByVerificationToken(token);
        if (user != null) {
            userDAO.markEmailVerified(user.getId());
            session.setAttribute("success", "Email đã được xác thực thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        }

        // Check expired token
        User expiredUser = userDAO.getUserByExpiredVerificationToken(token);
        if (expiredUser != null) {
            model.addAttribute("verifyError", "Link xác thực đã hết hạn.");
            model.addAttribute("expiredEmail", expiredUser.getEmail());
            return "pages/auth/verify-email";
        }

        // Invalid or already used
        model.addAttribute("verifyError", "Link xác thực không hợp lệ hoặc đã được sử dụng.");
        return "pages/auth/verify-email";
    }

    @PostMapping("/verify-email")
    public String resendVerificationEmail(
            @RequestParam(value = "email", required = false) String email,
            Model model,
            HttpServletRequest request,
            HttpSession session) {
        // Resend verification email
        if (email == null || email.isBlank()) {
            model.addAttribute("verifyError", "Email không hợp lệ.");
            return "pages/auth/verify-email";
        }

        String contextPath = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort() + request.getContextPath();
        EmailVerificationService svc = new EmailVerificationService(userDAO);
        boolean sent = svc.resendVerificationEmail(email, contextPath);

        if (sent) {
            session.setAttribute("success", "Email xác thực đã được gửi lại. Vui lòng kiểm tra hộp thư.");
            return "redirect:/login";
        }
        model.addAttribute("verifyError", "Không thể gửi email. Vui lòng thử lại sau.");
        model.addAttribute("expiredEmail", email);
        return "pages/auth/verify-email";
    }

    // ── FORGOT PASSWORD (/forgot-password, /verify-otp, /reset-password) ──

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "pages/auth/forgot-password";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session) {
        if (session.getAttribute("resetEmail") == null) {
            return "redirect:/forgot-password";
        }
        return "pages/auth/verify-otp";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(HttpSession session) {
        Object otpVerified = session.getAttribute("otpVerified");
        if (otpVerified == null || !(boolean) otpVerified) {
            return "redirect:/forgot-password";
        }
        return "pages/auth/reset-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(
            @RequestParam(value = "email", required = false) String email,
            Model model,
            HttpSession session) {
        String genericSuccessMessage = "Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi.";

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập email");
            return "pages/auth/forgot-password";
        }

        email = email.trim().toLowerCase();

        User user = userDAO.getUserByEmail(email);
        boolean sent = user != null && OTPUtil.generateAndSendOTP(email);
        if (sent) {
            session.setAttribute("resetEmail", email);
            session.setAttribute("otpVerified", false);
            return "redirect:/verify-otp";
        } else if (user == null) {
            model.addAttribute("success", genericSuccessMessage);
            model.addAttribute("email", email);
            return "pages/auth/forgot-password";
        }
        model.addAttribute("error", "Không thể gửi email xác thực. Vui lòng thử lại sau hoặc liên hệ hỗ trợ: support@petshop.vn | 1900-xxxx.");
        model.addAttribute("email", email);
        return "pages/auth/forgot-password";
    }

    @PostMapping("/verify-otp")
    public String verifyOtpSubmit(
            @RequestParam(value = "otp", required = false) String otp,
            Model model,
            HttpSession session) {
        String email = (String) session.getAttribute("resetEmail");

        if (email == null) {
            return "redirect:/forgot-password";
        }

        if (otp == null || otp.trim().isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập mã OTP");
            return "pages/auth/verify-otp";
        }

        if (OTPUtil.verifyOTP(email, otp.trim())) {
            session.setAttribute("otpVerified", true);
            return "redirect:/reset-password";
        }
        model.addAttribute("error", "Mã OTP không đúng hoặc đã hết hạn");
        return "pages/auth/verify-otp";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            Model model,
            HttpSession session) {
        String email = (String) session.getAttribute("resetEmail");
        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");

        if (email == null || otpVerified == null || !otpVerified) {
            return "redirect:/forgot-password";
        }

        // Validate
        if (password == null || !PasswordUtil.isStrongPassword(password)) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            return "pages/auth/reset-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "pages/auth/reset-password";
        }

        // Cập nhật mật khẩu
        boolean updated = userDAO.updatePassword(email, password);

        // Xóa session
        session.removeAttribute("resetEmail");
        session.removeAttribute("otpVerified");

        if (updated) {
            session.setAttribute("success", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        }
        model.addAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại.");
        return "pages/auth/reset-password";
    }
}
