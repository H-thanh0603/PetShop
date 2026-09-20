package com.petshop.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import DAO.RememberTokenDAO;
import Model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces LogoutServlet (/logout) 1:1 — deletes remember-me tokens,
 * invalidates the session, clears the cookie, redirects home.
 * Login/register servlets move here in a later phase.
 */
@Controller
public class AuthController {

    private final RememberTokenDAO rememberTokenDAO;

    public AuthController() {
        this(new RememberTokenDAO());
    }

    AuthController(RememberTokenDAO rememberTokenDAO) {
        this.rememberTokenDAO = rememberTokenDAO;
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
}
