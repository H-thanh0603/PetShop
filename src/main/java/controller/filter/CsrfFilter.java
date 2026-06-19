package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
        request.setCharacterEncoding("UTF-8");
        String uri = request.getRequestURI();
        
        if (isStaticResource(uri) || isServerToServerWebhook(uri)) {
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
        
        // POST/PUT/DELETE - validate CSRF token from header or parameter
        // Check X-CSRF-Token header first, then fall back to csrfToken parameter
        String submittedToken = request.getHeader("X-CSRF-Token");
        if (submittedToken == null || submittedToken.isEmpty()) {
            submittedToken = request.getParameter("csrfToken");
        }
        System.out.println("=== CSRF CHECK ===");
        System.out.println("Session token: " + sessionToken);
        System.out.println("Submitted token: " + submittedToken);
        if (submittedToken != null && MessageDigest.isEqual(
                submittedToken.getBytes(StandardCharsets.UTF_8),
                sessionToken.getBytes(StandardCharsets.UTF_8))) {
            request.setAttribute("csrfToken", sessionToken);
            chain.doFilter(req, res);
        } else {
            if (isAjaxRequest(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Phiên làm việc đã hết hạn. Vui lòng tải lại trang.\"}");
                return;
            }
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

    private boolean isServerToServerWebhook(String uri) {
        return uri != null && (uri.endsWith("/api/payment/bank-webhook")
                || uri.endsWith("/api/ghn/webhook"));
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }
}
