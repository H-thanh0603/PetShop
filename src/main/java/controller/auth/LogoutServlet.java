package controller.auth;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import DAO.RememberTokenDAO;
import Model.User;

public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RememberTokenDAO rememberTokenDAO = new RememberTokenDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
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
        
        response.sendRedirect(request.getContextPath() + "/home");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}

