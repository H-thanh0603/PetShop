package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.security.SecureRandom;

public class CsrfFilter implements Filter {
    private static final SecureRandom RANDOM = new SecureRandom();
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
        
        HttpSession session = request.getSession(true);
        String sessionToken = (String) session.getAttribute("csrfToken");
        if (sessionToken == null) {
            sessionToken = generateToken();
            session.setAttribute("csrfToken", sessionToken);
        }
        
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            request.setAttribute("csrfToken", sessionToken);
            chain.doFilter(req, res);
            return;
        }
        
        // POST/PUT/DELETE - validate token
        // Exclude JSON API endpoints
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            request.setAttribute("csrfToken", sessionToken);
            chain.doFilter(req, res);
            return;
        }
        
        String submittedToken = request.getParameter("csrfToken");
        if (submittedToken != null && submittedToken.equals(sessionToken)) {
            request.setAttribute("csrfToken", sessionToken);
            chain.doFilter(req, res);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
        }
    }
    
    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
