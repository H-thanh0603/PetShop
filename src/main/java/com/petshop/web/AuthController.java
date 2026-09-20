package com.petshop.web;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import DAO.CartDAO;
import DAO.RememberTokenDAO;
import DAO.SecurityEventDAO;
import DAO.UserDAO;
import Model.CartItem;
import Model.FbAccount.Account;
import Model.GgAccount.GoogleAccount;
import Model.User;
import Util.AppConfig;
import Util.AuthRedirectUtil;
import Util.FormHelper;
import Util.LoginLockout;
import Util.OTPUtil;
import Util.PasswordUtil;
import Util.SocialAuthUtil;
import Util.ValidationUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import services.EmailVerificationService;

/**
 * Auth pages: /login, /logout, /register, /verify-email, /forgot-password,
 * /verify-otp, /reset-password. Social login servlets move here later.
 */
@Controller
public class AuthController {

    private static final int REMEMBER_ME_DAYS = 7;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RememberTokenDAO rememberTokenDAO;
    private final UserDAO userDAO;
    private final SecurityEventDAO securityEventDAO;
    private final Gson gson = new Gson();

    public AuthController() {
        this(new RememberTokenDAO(), new UserDAO(), new SecurityEventDAO());
    }

    AuthController(RememberTokenDAO rememberTokenDAO) {
        this(rememberTokenDAO, new UserDAO(), new SecurityEventDAO());
    }

    AuthController(RememberTokenDAO rememberTokenDAO, UserDAO userDAO) {
        this(rememberTokenDAO, userDAO, new SecurityEventDAO());
    }

    AuthController(RememberTokenDAO rememberTokenDAO, UserDAO userDAO, SecurityEventDAO securityEventDAO) {
        this.rememberTokenDAO = rememberTokenDAO;
        this.userDAO = userDAO;
        this.securityEventDAO = securityEventDAO;
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

    // ── LOGIN (/login) ──

    private void populateLoginViewData(HttpServletRequest request) {
        request.setAttribute("googleAuthUrl", SocialAuthUtil.buildGoogleAuthUrl(request));
        request.setAttribute("facebookAuthUrl", SocialAuthUtil.buildFacebookAuthUrl(request));
        request.setAttribute("googleLoginEnabled", SocialAuthUtil.isGoogleConfigured());
        request.setAttribute("facebookLoginEnabled", SocialAuthUtil.isFacebookConfigured());
    }

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        // Auto-login via remember_token cookie
        User existingUser = (User) session.getAttribute("user");
        if (existingUser == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("remember_token".equals(cookie.getName())) {
                        String plainToken = cookie.getValue();
                        if (plainToken != null && !plainToken.isEmpty()) {
                            int[] outUserId = {-1};
                            int tokenId = rememberTokenDAO.findMatchingToken(plainToken, outUserId);
                            if (tokenId > 0 && outUserId[0] > 0) {
                                User user = userDAO.getUserById(outUserId[0]);
                                if (user != null && user.getStatus()) {
                                    // Token rotation: delete old, issue new
                                    rememberTokenDAO.deleteToken(tokenId);
                                    String newToken = generateSecureToken();
                                    rememberTokenDAO.saveToken(user.getId(), newToken);
                                    Cookie newCookie = buildRememberCookie(newToken, request);
                                    response.addCookie(newCookie);
                                    applySameSiteToRememberCookie(response,
                                            request.getContextPath().isEmpty() ? "/" : request.getContextPath());

                                    // Establish session
                                    session.setAttribute("user", user);
                                    session.setAttribute("username", user.getUsername());
                                    session.setAttribute("role", user.getRole());
                                    loadCartIntoSession(session, user);

                                    String redirectUrl = AuthRedirectUtil.consumeRedirectAfterLogin(request);
                                    if ("admin".equals(user.getRole())) {
                                        return "redirect:" + request.getContextPath() + "/pages/admin/dashboard";
                                    } else if (redirectUrl != null && !redirectUrl.isEmpty()) {
                                        return "redirect:" + redirectUrl;
                                    }
                                    return "redirect:" + request.getContextPath() + "/home";
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        // Lưu URL redirect (từ parameter hoặc referer)
        AuthRedirectUtil.storeRedirectAfterLogin(request);

        moveFlashMessage(session, request, "success");
        moveFlashMessage(session, request, "error");
        moveFlashMessage(session, request, "warning");

        // Read rememberEmail cookie if present
        String savedEmail = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rememberEmail".equals(cookie.getName())) {
                    savedEmail = cookie.getValue();
                    break;
                }
            }
        }

        // Kiểm tra email từ đăng ký mới
        String registeredEmail = (String) session.getAttribute("registeredEmail");
        if (registeredEmail != null) {
            request.setAttribute("savedEmail", registeredEmail);
            session.removeAttribute("registeredEmail"); // Xóa sau khi dùng
        } else if (savedEmail != null) {
            request.setAttribute("savedEmail", savedEmail);
        }
        populateLoginViewData(request);

        return "pages/auth/login";
    }

    @PostMapping("/login")
    public String loginSubmit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        FormHelper form = new FormHelper(request);

        String email = form.get("email");
        String password = form.getRaw("password");
        String rememberMe = request.getParameter("rememberMe");

        // === VALIDATION ===
        boolean valid = true;
        if ("null".equals(password)) valid = false;
        if (!form.validateRequired("email", "Email")) {
            valid = false;
        } else if (!form.validateEmail("email")) {
            form.addError("email", "Email không hợp lệ");
            valid = false;
        }

        if (!form.validateRequiredRaw("password", "Mật khẩu")) {
            valid = false;
        }

        if (!valid) {
            form.applyToRequest();
            populateLoginViewData(request);
            return "pages/auth/login";
        }

        // === BRUTE-FORCE CHECK ===
        // Check lockout for this (email, IP) pair — an attacker from one IP
        // cannot lock the account for users connecting from other IPs.
        if (LoginLockout.isLocked(email, request.getRemoteAddr())) {
            securityEventDAO.log("ACCOUNT_LOCKED_ATTEMPT", email, request.getRemoteAddr(),
                    "Login attempt blocked while the (email, IP) lock is still active.");
            form.addGeneralError("Email hoặc mật khẩu không đúng.");
            form.applyToRequest();
            populateLoginViewData(request);
            return "pages/auth/login";
        }

        // === ĐĂNG NHẬP ===
        User user = userDAO.loginByEmail(email, password);

        if (user != null) {
            // Reset failed attempts on successful login
            userDAO.resetFailedAttempts(email);
            LoginLockout.reset(email, request.getRemoteAddr());

            // Check if user account is deactivated
            if (!user.getStatus()) {
                form.addGeneralError("Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.");
                form.applyToRequest();
                populateLoginViewData(request);
                return "pages/auth/login";
            }

            // Save cart data from old session before invalidation
            HttpSession oldSession = request.getSession(false);
            Map<Integer, CartItem> savedCart = null;
            Integer savedTotalQuantity = null;
            String redirectUrl = AuthRedirectUtil.consumeRedirectAfterLogin(request);
            if (oldSession != null) {
                savedCart = (Map<Integer, CartItem>) oldSession.getAttribute("cart");
                savedTotalQuantity = (Integer) oldSession.getAttribute("totalQuantity");
                oldSession.invalidate();
            }

            // Create new session (session regeneration)
            HttpSession session = request.getSession(true);

            session.setAttribute("user", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            // Restore cart data to new session
            if (savedCart != null) {
                session.setAttribute("cart", savedCart);
            }
            if (savedTotalQuantity != null) {
                session.setAttribute("totalQuantity", savedTotalQuantity);
            }

            // Load giỏ hàng từ database
            CartDAO cartDAO = new CartDAO();

            // Nếu có giỏ hàng trong session (chưa đăng nhập mà đã thêm), sync vào database
            @SuppressWarnings("unchecked")
            Map<Integer, CartItem> sessionCart = (Map<Integer, CartItem>) session.getAttribute("cart");
            if (sessionCart != null && !sessionCart.isEmpty()) {
                cartDAO.syncCartFromSession(user.getId(), sessionCart);
            }

            // Load giỏ hàng từ database vào session
            Map<Integer, CartItem> cart = cartDAO.getCartByUserId(user.getId());
            session.setAttribute("cart", cart);

            // Tính tổng số lượng
            int totalQuantity = 0;
            for (CartItem item : cart.values()) {
                totalQuantity += item.getQuantity();
            }
            session.setAttribute("totalQuantity", totalQuantity);

            // Xử lý "Ghi nhớ đăng nhập" - secure token-based and email cookie
            if ("on".equals(rememberMe)) {
                String plainToken = generateSecureToken();
                rememberTokenDAO.saveToken(user.getId(), plainToken);
                Cookie tokenCookie = buildRememberCookie(plainToken, request);
                response.addCookie(tokenCookie);
                applySameSiteToRememberCookie(response,
                        request.getContextPath().isEmpty() ? "/" : request.getContextPath());

                // Also save rememberEmail cookie
                Cookie emailCookie = new Cookie("rememberEmail", email);
                emailCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                emailCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                response.addCookie(emailCookie);
            } else {
                // Clear any existing remember_token cookie
                Cookie clearCookie = new Cookie("remember_token", "");
                clearCookie.setMaxAge(0);
                clearCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                clearCookie.setHttpOnly(true);
                clearCookie.setSecure(shouldUseSecureCookies(request));
                response.addCookie(clearCookie);

                // Also clear rememberEmail cookie
                Cookie clearEmailCookie = new Cookie("rememberEmail", "");
                clearEmailCookie.setMaxAge(0);
                clearEmailCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                response.addCookie(clearEmailCookie);
            }

            // Redirect theo role hoặc về trang trước
            if ("admin".equals(user.getRole())) {
                return "redirect:" + request.getContextPath() + "/pages/admin/dashboard";
            } else if (redirectUrl != null && !redirectUrl.isEmpty()) {
                return "redirect:" + redirectUrl;
            }
            return "redirect:" + request.getContextPath() + "/home";
        }
        // Record failure per (email, IP); lock that pair after 5 failures
        boolean nowLocked = LoginLockout.recordFailure(email, request.getRemoteAddr());
        if (nowLocked) {
            securityEventDAO.log("ACCOUNT_LOCKED", email, request.getRemoteAddr(),
                    "Login locked for the (email, IP) pair for 15 minutes after repeated failures.");
        }
        form.addGeneralError("Email hoặc mật khẩu không đúng.");

        form.applyToRequest();
        populateLoginViewData(request);
        return "pages/auth/login";
    }

    private void moveFlashMessage(HttpSession session, HttpServletRequest request, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private Cookie buildRememberCookie(String plainToken, HttpServletRequest request) {
        Cookie cookie = new Cookie("remember_token", plainToken);
        cookie.setMaxAge(REMEMBER_ME_DAYS * 24 * 60 * 60);
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        cookie.setHttpOnly(true);
        cookie.setSecure(shouldUseSecureCookies(request));
        return cookie;
    }

    /**
     * Appends SameSite=Lax to the Set-Cookie header for the remember_token cookie.
     * Jakarta Servlet API does not expose a setSameSite() method, so we must
     * rewrite the header manually after the cookie has been added to the response.
     */
    private void applySameSiteToRememberCookie(HttpServletResponse response, String cookiePath) {
        Collection<String> headers = response.getHeaders("Set-Cookie");
        boolean firstHeader = true;
        for (String header : headers) {
            if (header.startsWith("remember_token=")) {
                String updated = header + "; SameSite=Lax";
                if (firstHeader) {
                    response.setHeader("Set-Cookie", updated);
                    firstHeader = false;
                } else {
                    response.addHeader("Set-Cookie", updated);
                }
            } else {
                if (firstHeader) {
                    response.setHeader("Set-Cookie", header);
                    firstHeader = false;
                } else {
                    response.addHeader("Set-Cookie", header);
                }
            }
        }
    }

    private void loadCartIntoSession(HttpSession session, User user) {
        CartDAO cartDAO = new CartDAO();
        Map<Integer, CartItem> cart = cartDAO.getCartByUserId(user.getId());
        session.setAttribute("cart", cart);
        int totalQuantity = 0;
        for (CartItem item : cart.values()) { totalQuantity += item.getQuantity(); }
        session.setAttribute("totalQuantity", totalQuantity);
    }

    private boolean shouldUseSecureCookies(HttpServletRequest request) {
        return AppConfig.getBoolean("app.cookies.secure", request.isSecure());
    }

    // ── REGISTER (/register) ──

    @GetMapping("/register")
    public String registerPage() {
        return "pages/auth/register";
    }

    @PostMapping(value = "/register", params = "action=sendOTP", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String registerSendOtp(@RequestParam(value = "email", required = false) String email) throws IOException {
        if (email == null || email.trim().isEmpty()) {
            return json(false, "Email không được để trống");
        }

        if (userDAO.checkEmailExists(email)) {
            return json(false, "Email này đã được đăng ký");
        }

        boolean sent = OTPUtil.generateAndSendOTP(email);
        if (sent) {
            return json(true, "Đã gửi mã OTP đến email của bạn");
        }
        return json(false, "Không thể gửi OTP. Vui lòng thử lại");
    }

    @PostMapping(value = "/register", params = "action=verifyOTP", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String registerVerifyOtp(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "otp", required = false) String otp,
            HttpSession session) throws IOException {
        if (OTPUtil.verifyOTP(email, otp)) {
            session.setAttribute("emailVerified", email);
            return json(true, "Xác thực thành công");
        }
        return json(false, "Mã OTP không đúng hoặc đã hết hạn");
    }

    @PostMapping(value = "/register", params = "action=checkUsername", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String registerCheckUsername(
            @RequestParam(value = "username", required = false) String username) throws IOException {
        Map<String, Object> result = new HashMap<>();
        if (username == null || username.trim().isEmpty()) {
            result.put("available", false);
            return gson.toJson(result);
        }

        boolean exists = userDAO.checkUsernameExists(username.trim().toLowerCase());
        result.put("available", !exists);
        return gson.toJson(result);
    }

    @PostMapping("/register")
    public String registerSubmit(HttpServletRequest request, HttpSession session) {
        FormHelper form = new FormHelper(request);

        String username = form.get("username");
        String email = form.get("email");
        String fullname = form.get("fullName");
        String phone = form.get("phone");
        String password = form.getRaw("password");
        String confirmPassword = form.getRaw("confirmPassword");

        // === VALIDATION ===
        boolean valid = true;

        // A. Họ và tên - trim, không toàn khoảng trắng, chỉ chữ có dấu + khoảng trắng
        if (fullname == null || fullname.trim().isEmpty()) {
            form.addError("fullName", "Vui lòng nhập họ và tên");
            valid = false;
        } else {
            fullname = fullname.trim();
            if (!ValidationUtil.validateMaxLength(fullname, 200)) {
                form.addError("fullName", "Họ tên không được vượt quá 200 ký tự");
                valid = false;
            } else if (!fullname.matches("^[\\p{L}\\s]+$") || fullname.replaceAll("\\s", "").isEmpty()) {
                form.addError("fullName", "Họ tên chỉ được chứa chữ cái và khoảng trắng");
                valid = false;
            } else if (fullname.length() < 2) {
                form.addError("fullName", "Họ tên phải có ít nhất 2 ký tự");
                valid = false;
            }
        }

        // B. Email
        if (!form.validateRequired("email", "Email")) {
            valid = false;
        } else if (!form.validateEmail("email")) {
            form.addError("email", "Email không hợp lệ");
            valid = false;
        }

        // C. Tên đăng nhập - chữ cái, số, gạch dưới, 3-30 ký tự (tự động lowercase)
        if (username == null || username.trim().isEmpty()) {
            form.addError("username", "Vui lòng nhập tên đăng nhập");
            valid = false;
        } else {
            username = username.trim().toLowerCase();
            if (username.length() < 3 || username.length() > 30) {
                form.addError("username", "Tên đăng nhập phải từ 3-30 ký tự");
                valid = false;
            } else if (!username.matches("^[a-z0-9_]+$")) {
                form.addError("username", "Tên đăng nhập chỉ được chứa chữ thường, số và dấu gạch dưới");
                valid = false;
            }
        }

        // D. Số điện thoại - 10 số, bắt đầu bằng 0
        if (phone != null && !phone.trim().isEmpty()) {
            phone = phone.replaceAll("[^0-9]", "");
            if (!phone.matches("^0\\d{9}$")) {
                form.addError("phone", "Số điện thoại không hợp lệ");
                valid = false;
            }
        }

        // E. Mật khẩu - 8 ký tự, chữ hoa, chữ thường, số, ký tự đặc biệt
        if (password == null || password.isEmpty()) {
            form.addError("password", "Vui lòng nhập mật khẩu");
            valid = false;
        } else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$")) {
            form.addError("password", "Mật khẩu phải có tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            valid = false;
        }

        // F. Xác nhận mật khẩu
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            form.addError("confirmPassword", "Vui lòng xác nhận mật khẩu");
            valid = false;
        } else if (!confirmPassword.equals(password)) {
            form.addError("confirmPassword", "Mật khẩu xác nhận không khớp");
            valid = false;
        }

        if (!valid) {
            form.applyToRequest();
            return "pages/auth/register";
        }

        // === KIỂM TRA TRÙNG ===
        if (userDAO.checkUsernameExists(username)) {
            form.addError("username", "Tên đăng nhập đã tồn tại");
            form.addGeneralError("Tên đăng nhập đã tồn tại!");
            form.applyToRequest();
            return "pages/auth/register";
        }

        if (userDAO.checkEmailExists(email)) {
            form.addError("email", "Email đã được sử dụng");
            form.addGeneralError("Email đã tồn tại!");
            form.applyToRequest();
            return "pages/auth/register";
        }

        if (phone != null && !phone.isEmpty() && userDAO.checkPhoneExists(phone)) {
            form.addError("phone", "Số điện thoại đã được sử dụng");
            form.addGeneralError("Số điện thoại đã tồn tại!");
            form.applyToRequest();
            return "pages/auth/register";
        }

        // === KIỂM TRA OTP ===
        String otp = form.get("otp");
        if (otp == null || otp.trim().isEmpty()) {
            form.addError("otp", "Vui lòng nhập mã OTP");
            form.addGeneralError("Vui lòng nhập mã OTP!");
            form.applyToRequest();
            return "pages/auth/register";
        }

        if (!OTPUtil.verifyOTP(email, otp.trim())) {
            form.addError("otp", "Mã OTP không đúng hoặc đã hết hạn");
            form.addGeneralError("Mã OTP không đúng hoặc đã hết hạn!");
            form.applyToRequest();
            return "pages/auth/register";
        }

        // === ĐĂNG KÝ ===
        boolean success = userDAO.register(username, password, fullname, email);

        if (success) {
            // OTP đã xác thực email — kích hoạt tài khoản ngay
            User newUser = userDAO.getUserByEmail(email);
            if (newUser != null) {
                userDAO.markEmailVerified(newUser.getId());
            }
            session.setAttribute("registeredEmail", email);
            session.setAttribute("success", "Đăng ký thành công! Bạn có thể đăng nhập ngay.");
            return "redirect:/login";
        }
        form.addGeneralError("Đăng ký thất bại! Vui lòng thử lại.");
        form.applyToRequest();
        return "pages/auth/register";
    }

    private String json(boolean success, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        return gson.toJson(result);
    }

    // ── SOCIAL LOGIN (/LoginByGoogleServlet, /LoginByFacebookServlet) ──

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/LoginByGoogleServlet")
    public String loginByGoogle(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request,
            HttpSession session) {
        if (!SocialAuthUtil.isGoogleConfigured()) {
            session.setAttribute("warning", "Google login chưa được cấu hình trên máy này.");
            return "redirect:/login";
        }

        // Người dùng bấm hủy hoặc provider trả lỗi
        if (error != null || code == null || code.isEmpty()) {
            session.setAttribute("warning", "Đăng nhập Google đã bị hủy hoặc không thành công.");
            return "redirect:/login";
        }

        try {
            controller.Google.GoogleLogin gg = new controller.Google.GoogleLogin();
            String accessToken = gg.getToken(code, SocialAuthUtil.buildGoogleRedirectUri(request));
            GoogleAccount acc = gg.getUserInfo(accessToken);

            if (acc == null || acc.getEmail() == null || acc.getEmail().isBlank()) {
                session.setAttribute("error", "Google không trả về email. Không thể đăng nhập.");
                return "redirect:/login";
            }

            finishSocialLogin(session, acc.getEmail(), acc.getName());
            return redirectAfterSocialLogin(request, session);
        } catch (Exception e) {
            logger.error("Google OAuth login failed", e);
            session.setAttribute("error", "Đăng nhập Google thất bại. Kiểm tra lại cấu hình OAuth.");
            return "redirect:/login";
        }
    }

    @GetMapping("/LoginByFacebookServlet")
    public String loginByFacebook(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request,
            HttpSession session) {
        if (!SocialAuthUtil.isFacebookConfigured()) {
            session.setAttribute("warning", "Facebook login chưa được cấu hình trên máy này.");
            return "redirect:/login";
        }

        // Người dùng bấm hủy hoặc provider trả lỗi
        if (error != null || code == null || code.isEmpty()) {
            session.setAttribute("warning", "Đăng nhập Facebook đã bị hủy hoặc không thành công.");
            return "redirect:/login";
        }

        try {
            controller.FaceBook.FaceBookLogin fb = new controller.FaceBook.FaceBookLogin();
            String accessToken = fb.getToken(code, SocialAuthUtil.buildFacebookRedirectUri(request));
            Account acc = fb.getUserInfo(accessToken);

            if (acc == null || acc.getEmail() == null || acc.getEmail().isBlank()) {
                session.setAttribute("error", "Facebook không trả về email. Hãy bật quyền email hoặc dùng đăng nhập thường.");
                return "redirect:/login";
            }

            finishSocialLogin(session, acc.getEmail(), acc.getName());
            return redirectAfterSocialLogin(request, session);
        } catch (Exception e) {
            logger.error("Facebook OAuth login failed", e);
            session.setAttribute("error", "Đăng nhập Facebook thất bại. Kiểm tra lại cấu hình OAuth.");
            return "redirect:/login";
        }
    }

    private void finishSocialLogin(HttpSession session, String email, String name) {
        boolean isEmailAvailable = userDAO.HaveEmail(email);
        User user;
        if (!isEmailAvailable) {
            user = userDAO.getUserByEmail(email);
        } else {
            userDAO.insertUser(name, email);
            user = userDAO.getUserByEmail(email);
        }

        if (user == null) {
            throw new IllegalStateException("Không thể tạo hoặc tải tài khoản social.");
        }

        session.setAttribute("user", user);
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());

        CartDAO cartDAO = new CartDAO();
        @SuppressWarnings("unchecked")
        Map<Integer, CartItem> sessionCart = (Map<Integer, CartItem>) session.getAttribute("cart");
        if (sessionCart != null && !sessionCart.isEmpty()) {
            cartDAO.syncCartFromSession(user.getId(), sessionCart);
        }

        Map<Integer, CartItem> cart = cartDAO.getCartByUserId(user.getId());
        session.setAttribute("cart", cart);
        int totalQuantity = 0;
        for (CartItem item : cart.values()) {
            totalQuantity += item.getQuantity();
        }
        session.setAttribute("totalQuantity", totalQuantity);
    }

    private String redirectAfterSocialLogin(HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        String redirectUrl = AuthRedirectUtil.consumeRedirectAfterLogin(request);

        if (user != null && "admin".equals(user.getRole())) {
            return "redirect:" + request.getContextPath() + "/pages/admin/dashboard";
        } else if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:" + request.getContextPath() + "/home";
    }

    // ── ADMIN LOGIN (/admin/login) ──

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "pages/admin/login";
    }

    @PostMapping("/admin/login")
    public String adminLoginSubmit(HttpServletRequest request, Model model) {
        FormHelper form = new FormHelper(request);

        String email = form.get("email");
        String password = form.getRaw("password");

        if (!form.validateRequired("email", "Email") || !form.validateRequiredRaw("password", "Mật khẩu")) {
            model.addAttribute("error", "Vui lòng nhập đầy đủ email và mật khẩu.");
            return "pages/admin/login";
        }

        // Brute-force check for this (email, IP) pair
        if (Util.LoginLockout.isLocked(email, request.getRemoteAddr())) {
            securityEventDAO.log("ACCOUNT_LOCKED_ATTEMPT", email, request.getRemoteAddr(), "Admin login attempt blocked while the (email, IP) lock is still active.");
            model.addAttribute("error", "Email hoặc mật khẩu không đúng.");
            return "pages/admin/login";
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
                    return "redirect:" + request.getContextPath() + "/admin/orders";
                }
                return "redirect:" + request.getContextPath() + "/pages/admin/dashboard";
            }
            // Wrong role or inactive account
            securityEventDAO.log("AUTH_FAIL", email, request.getRemoteAddr(), "User with role '" + role + "' tried to access admin panel.");
            model.addAttribute("error", "Bạn không có quyền truy cập vào khu vực này.");
            return "pages/admin/login";
        }
        // Record failure per (email, IP); lock that pair after 5 failures
        boolean nowLocked = Util.LoginLockout.recordFailure(email, request.getRemoteAddr());
        if (nowLocked) {
            securityEventDAO.log("ACCOUNT_LOCKED", email, request.getRemoteAddr(),
                    "Admin login locked for the (email, IP) pair for 15 minutes after repeated failures.");
        }
        model.addAttribute("error", "Email hoặc mật khẩu không đúng.");
        return "pages/admin/login";
    }
}
