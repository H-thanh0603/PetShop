package controller.auth;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.CartDAO;
import DAO.UserDAO;
import DAO.RememberTokenDAO;
import Model.CartItem;
import Model.User;
import Util.AuthRedirectUtil;
import Util.FormHelper;
import Util.SocialAuthUtil;

import java.security.SecureRandom;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int REMEMBER_ME_DAYS = 7;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final RememberTokenDAO rememberTokenDAO = new RememberTokenDAO();

    private void populateLoginViewData(HttpServletRequest request) {
        request.setAttribute("googleAuthUrl", SocialAuthUtil.buildGoogleAuthUrl(request));
        request.setAttribute("facebookAuthUrl", SocialAuthUtil.buildFacebookAuthUrl(request));
        request.setAttribute("googleLoginEnabled", SocialAuthUtil.isGoogleConfigured());
        request.setAttribute("facebookLoginEnabled", SocialAuthUtil.isFacebookConfigured());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();

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
                                UserDAO dao = new UserDAO();
                                User user = dao.getUserById(outUserId[0]);
                                if (user != null && user.getStatus()) {
                                    // Token rotation: delete old, issue new
                                    rememberTokenDAO.deleteToken(tokenId);
                                    String newToken = generateSecureToken();
                                    rememberTokenDAO.saveToken(user.getId(), newToken);
                                    Cookie newCookie = buildRememberCookie(newToken, request);
                                    response.addCookie(newCookie);

                                    // Establish session
                                    session.setAttribute("user", user);
                                    session.setAttribute("username", user.getUsername());
                                    session.setAttribute("role", user.getRole());
                                    loadCartIntoSession(session, user, dao);

                                    String redirectUrl = AuthRedirectUtil.consumeRedirectAfterLogin(request);
                                    if ("admin".equals(user.getRole())) {
                                        response.sendRedirect(request.getContextPath() + "/pages/admin/dashboard");
                                    } else if (redirectUrl != null && !redirectUrl.isEmpty()) {
                                        response.sendRedirect(redirectUrl);
                                    } else {
                                        response.sendRedirect(request.getContextPath() + "/home");
                                    }
                                    return;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        // Lưu URL redirect (từ parameter hoặc referer)
        // Dùng helper chung để mọi cách vào trang login đều lưu đúng URL cần quay lại.
        AuthRedirectUtil.storeRedirectAfterLogin(request);
        
        moveFlashMessage(session, request, "success");
        moveFlashMessage(session, request, "error");
        moveFlashMessage(session, request, "warning");

        // Kiểm tra email từ đăng ký mới
        String registeredEmail = (String) session.getAttribute("registeredEmail");
        if (registeredEmail != null) {
            request.setAttribute("savedEmail", registeredEmail);
            session.removeAttribute("registeredEmail"); // Xóa sau khi dùng
        }
        populateLoginViewData(request);
        
        request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
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
        // Secure flag: only set in production (HTTPS). In dev (HTTP) skip to avoid cookie not being sent.
        // cookie.setSecure(true);
        return cookie;
    }

    private void loadCartIntoSession(HttpSession session, User user, UserDAO dao) {
        CartDAO cartDAO = new CartDAO();
        Map<Integer, CartItem> cart = cartDAO.getCartByUserId(user.getId());
        session.setAttribute("cart", cart);
        int totalQuantity = 0;
        for (CartItem item : cart.values()) { totalQuantity += item.getQuantity(); }
        session.setAttribute("totalQuantity", totalQuantity);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
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
            request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
            return;
        }
        
        // === BRUTE-FORCE CHECK ===
        UserDAO dao = new UserDAO();
        
        // Check if account is locked before password verification
        if (dao.isAccountLocked(email)) {
            Timestamp lockedUntil = dao.getLockedUntil(email);
            long remainingMs = lockedUntil.getTime() - System.currentTimeMillis();
            long remainingMinutes = (remainingMs / 60000) + 1; // Round up
            form.addGeneralError("Tài khoản đã bị khóa tạm thời. Vui lòng thử lại sau " + remainingMinutes + " phút.");
            form.applyToRequest();
            populateLoginViewData(request);
            request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
            return;
        }
        
        // === ĐĂNG NHẬP ===
        User user = dao.loginByEmail(email, password);
        
        if (user != null) {
            // Reset failed attempts on successful login
            dao.resetFailedAttempts(email);
            
            // Check if user account is deactivated
            if (!user.getStatus()) {
                form.addGeneralError("Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.");
                form.applyToRequest();
                populateLoginViewData(request);
                request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
                return;
            }
            
            // Save cart data from old session before invalidation
            HttpSession oldSession = request.getSession(false);
            Map<Integer, CartItem> savedCart = null;
            Integer savedTotalQuantity = null;
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
            
            // Xử lý "Ghi nhớ đăng nhập" - secure token-based
            if ("on".equals(rememberMe)) {
                String plainToken = generateSecureToken();
                rememberTokenDAO.saveToken(user.getId(), plainToken);
                Cookie tokenCookie = buildRememberCookie(plainToken, request);
                response.addCookie(tokenCookie);
            } else {
                // Clear any existing remember_token cookie
                Cookie clearCookie = new Cookie("remember_token", "");
                clearCookie.setMaxAge(0);
                clearCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                clearCookie.setHttpOnly(true);
                response.addCookie(clearCookie);
            }
            
            // Redirect theo role hoặc về trang trước
            String redirectUrl = AuthRedirectUtil.consumeRedirectAfterLogin(request);
            
            if ("admin".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/pages/admin/dashboard");
            } else if (redirectUrl != null && !redirectUrl.isEmpty()) {
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect(request.getContextPath() + "/home");
            }
        } else {
            // Increment failed attempts on failed login
            dao.incrementFailedAttempts(email);
            int failedAttempts = dao.getFailedLoginAttempts(email);
            
            if (failedAttempts >= 5) {
                dao.lockAccount(email, 15);
                form.addGeneralError("Tài khoản đã bị khóa tạm thời. Vui lòng thử lại sau 15 phút.");
            } else {
                int remaining = 5 - failedAttempts;
                form.addGeneralError("Email hoặc mật khẩu không đúng! Bạn còn " + remaining + " lần thử.");
            }
            
            form.applyToRequest();
            populateLoginViewData(request);
            request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
        }
    }
}
