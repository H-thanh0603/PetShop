package controller.filter;

import Model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter for handling role-based authorization for admin pages.
 */
public class AuthorizationFilter implements Filter {

    private final List<String> staffAllowedPaths = Arrays.asList(
        "/pages/admin/dashboard", 
        "/pages/admin/products", 
        "/pages/admin/pet-types", 
        "/pages/admin/categories", 
        "/admin/orders", 
        "/pages/admin/reviews", 
        "/admin/notifications"
    );
    private final List<String> shipperAllowedPaths = Arrays.asList("/admin/orders");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String path = req.getServletPath();

        // Allow access to the admin login page and its resources
        if ("/admin/login".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/admin/login");
            return;
        }

        String role = user.getRole();

        switch (role) {
            case "admin":
                // Admin can access everything
                chain.doFilter(request, response);
                break;
            case "staff":
                // Staff can access specific paths
                if (isPathAllowed(path, staffAllowedPaths)) {
                    chain.doFilter(request, response);
                } else {
                    res.sendRedirect(req.getContextPath() + "/pages/error/403.jsp");
                }
                break;
            case "shipper":
                 // Shipper can only access orders
                if (isPathAllowed(path, shipperAllowedPaths)) {
                    chain.doFilter(request, response);
                } else {
                    res.sendRedirect(req.getContextPath() + "/pages/error/403.jsp");
                }
                break;
            case "user":
                // Regular users are denied access to all admin pages
                res.sendRedirect(req.getContextPath() + "/pages/error/403.jsp");
                break;
            default:
                // Unknown roles are denied
                res.sendRedirect(req.getContextPath() + "/pages/error/403.jsp");
                break;
        }
    }
    
    /**
     * Checks if the requested path starts with any of the allowed paths.
     */
    private boolean isPathAllowed(String requestPath, List<String> allowedPaths) {
        for (String allowedPath : allowedPaths) {
            if (requestPath.startsWith(allowedPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}
