package controller.auth;

import DAO.CartDAO;
import DAO.UserDAO;
import Model.CartItem;
import Model.FbAccount.Account;
import Model.User;
import Util.AuthRedirectUtil;
import Util.SocialAuthUtil;
import controller.FaceBook.FaceBookLogin;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class LoginByFacebookServlet extends HttpServlet {
    private UserDAO userDao = new UserDAO();
    private static final Logger logger = LoggerFactory.getLogger(LoginByFacebookServlet.class);
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        if (!SocialAuthUtil.isFacebookConfigured()) {
            session.setAttribute("warning", "Facebook login chưa được cấu hình trên máy này.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String code = request.getParameter("code");
        String error = request.getParameter("error");

        // Người dùng bấm hủy hoặc provider trả lỗi
        if (error != null || code == null || code.isEmpty()) {
            session.setAttribute("warning", "Đăng nhập Facebook đã bị hủy hoặc không thành công.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            FaceBookLogin fb = new FaceBookLogin();
            String accessToken = fb.getToken(code, SocialAuthUtil.buildFacebookRedirectUri(request));
            Account acc = fb.getUserInfo(accessToken);

            if (acc == null || acc.getEmail() == null || acc.getEmail().isBlank()) {
                session.setAttribute("error", "Facebook không trả về email. Hãy bật quyền email hoặc dùng đăng nhập thường.");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            boolean isEmailAvailable  = userDao.HaveEmail(acc.getEmail());
            User user;
            if(!isEmailAvailable){
                user = userDao.getUserByEmail(acc.getEmail());
            }else{
                userDao.insertUser(acc.getName(), acc.getEmail());
                user = userDao.getUserByEmail(acc.getEmail());
            }

            if (user == null) {
                session.setAttribute("error", "Không thể tạo hoặc tải tài khoản Facebook.");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
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

            String redirectUrl = AuthRedirectUtil.consumeRedirectAfterLogin(request);

            if ("admin".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/pages/admin/dashboard");
            } else if (redirectUrl != null && !redirectUrl.isEmpty()) {
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect(request.getContextPath() + "/home");
            }
        } catch (Exception e) {
            logger.error("Facebook OAuth login failed", e);
            session.setAttribute("error", "Đăng nhập Facebook thất bại. Kiểm tra lại cấu hình OAuth.");
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
}
