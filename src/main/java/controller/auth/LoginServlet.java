package controller.auth;

import java.io.IOException;
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
import Util.FormHelper;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int REMEMBER_ME_DAYS = 7;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        // Lưu URL redirect (từ parameter hoặc referer)
        String redirectUrl = request.getParameter("redirect");
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.contains("/login") && !referer.contains("/register")) {
                redirectUrl = referer;
            }
        }
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            session.setAttribute("redirectAfterLogin", redirectUrl);
        }
        
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
        
        request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
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
        if(password.equals("null")) valid = false;
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
            request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
            return;
        }
        
        // === ĐĂNG NHẬP ===
        UserDAO dao = new UserDAO();
        User user = dao.loginByEmail(email, password);
        
        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            
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
            String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            session.removeAttribute("redirectAfterLogin"); // Xóa sau khi dùng
            
            if ("admin".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/pages/admin/dashboard");
            } else if (redirectUrl != null && !redirectUrl.isEmpty()) {
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect(request.getContextPath() + "/home");
            }
        } else {
            form.addGeneralError("Email hoặc mật khẩu không đúng!");
            form.applyToRequest();
            request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
        }
    }
}
