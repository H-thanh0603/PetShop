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
import Model.CartItem;
import Model.User;
import Util.AuthRedirectUtil;
import Util.FormHelper;
import Util.SocialAuthUtil;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int REMEMBER_ME_DAYS = 7;

    private void populateLoginViewData(HttpServletRequest request) {
        request.setAttribute("googleAuthUrl", SocialAuthUtil.buildGoogleAuthUrl(request));
        request.setAttribute("facebookAuthUrl", SocialAuthUtil.buildFacebookAuthUrl(request));
        request.setAttribute("googleLoginEnabled", SocialAuthUtil.isGoogleConfigured());
        request.setAttribute("facebookLoginEnabled", SocialAuthUtil.isFacebookConfigured());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        
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
        } else {
            // Kiểm tra cookie "remember me"
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("rememberEmail".equals(cookie.getName())) {
                        request.setAttribute("savedEmail", cookie.getValue());
                        break;
                    }
                }
            }
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
            
            // Xử lý "Ghi nhớ đăng nhập"
            if ("on".equals(rememberMe)) {
                Cookie emailCookie = new Cookie("rememberEmail", email);
                emailCookie.setMaxAge(REMEMBER_ME_DAYS * 24 * 60 * 60);
                emailCookie.setPath("/");
                response.addCookie(emailCookie);
            } else {
                // Xóa cookie nếu không chọn ghi nhớ
                Cookie emailCookie = new Cookie("rememberEmail", "");
                emailCookie.setMaxAge(0);
                emailCookie.setPath("/");
                response.addCookie(emailCookie);
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
