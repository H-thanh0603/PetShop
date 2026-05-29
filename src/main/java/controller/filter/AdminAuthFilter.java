package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import Model.User;

public class AdminAuthFilter implements Filter {
    private static final String[] STATIC_EXTENSIONS = {
        ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".ico",
        ".woff", ".woff2", ".ttf", ".svg", ".map", ".webp"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String uri = request.getRequestURI();
        
        if (isStaticResource(uri)) {
            chain.doFilter(req, res);
            return;
        }
        
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user != null && "admin".equals(user.getRole())) {
            chain.doFilter(req, res);
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
    
    private boolean isStaticResource(String uri) {
        if (uri == null) return false;
        String lower = uri.toLowerCase();
        for (String ext : STATIC_EXTENSIONS) {
            if (lower.endsWith(ext)) return true;
        }
        return false;
    }
}
