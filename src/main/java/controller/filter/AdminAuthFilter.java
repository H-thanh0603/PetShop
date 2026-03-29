package controller.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import Model.User;

/**
 * Filter bảo vệ các trang admin
 * Chỉ cho phép user có role "admin" truy cập
 */
@WebFilter(urlPatterns = {"/admin/*", "/pages/admin/*"})
public class AdminAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Không cần khởi tạo gì
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        // Kiểm tra user đã đăng nhập chưa
        User user = null;
        if (session != null) {
            user = (User) session.getAttribute("user");
        }
        
        // Nếu chưa đăng nhập hoặc không phải admin
        if (user == null) {
            // Lưu URL hiện tại để redirect sau khi login
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            String redirectURL = requestURI + (queryString != null ? "?" + queryString : "");
            
            session = httpRequest.getSession(true);
            session.setAttribute("redirectAfterLogin", redirectURL);
            session.setAttribute("toastMessage", "Vui lòng đăng nhập để tiếp tục");
            session.setAttribute("toastType", "warning");
            
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }
        
        // Kiểm tra role admin
        if (!"admin".equalsIgnoreCase(user.getRole())) {
            session.setAttribute("toastMessage", "Bạn không có quyền truy cập trang này");
            session.setAttribute("toastType", "error");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/home");
            return;
        }
        
        // User là admin, cho phép truy cập
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Không cần cleanup
    }
}
