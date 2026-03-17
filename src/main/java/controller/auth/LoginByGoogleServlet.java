package controller.auth;

import DAO.CartDAO;
import DAO.UserDAO;
import Model.CartItem;
import Model.GgAccount.GoogleAccount;
import Model.User;
import controller.Google.GoogleLogin;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet("/LoginByGoogleServlet")
public class LoginByGoogleServlet extends HttpServlet {
    private UserDAO userDao = new UserDAO();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");
        String error = request.getParameter("error");

        // Người dùng bấm HỦY login Facebook
        if (error != null || code == null || code.isEmpty()) {

            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        GoogleLogin gg = new GoogleLogin();
        String accessToken = gg.getToken(code);
        System.out.println("accessToken: " + accessToken);
        GoogleAccount acc = gg.getUserInfo(accessToken);
        System.out.println("acc: " + acc);
        boolean isEmailAvailable  = userDao.HaveEmail(acc.getEmail());
        User user;
        if(!isEmailAvailable){
            user = userDao.getUserByEmail(acc.getEmail());
        }else{
            userDao.insertUser(acc.getName(), acc.getEmail());
            user = userDao.getUserByEmail(acc.getEmail());
        }
        HttpSession session = request.getSession();

        session.setAttribute("user", user);
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());

        // load cart
        CartDAO cartDAO = new CartDAO();
        Map<Integer, CartItem> cart = cartDAO.getCartByUserId(user.getId());
        session.setAttribute("cart", cart);
// Tính tổng số lượng
        int totalQuantity = 0;
        for (CartItem item : cart.values()) {
            totalQuantity += item.getQuantity();
        }
        session.setAttribute("totalQuantity", totalQuantity);
        response.sendRedirect(request.getContextPath() + "/home");
    }
}
