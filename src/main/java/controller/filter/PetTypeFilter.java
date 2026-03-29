package controller.filter;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import DAO.PetTypeDAO;
import Model.PetType;

/**
 * Filter để load danh sách loại thú cưng cho tất cả các trang
 * Giúp navbar hiển thị động các loại thú cưng
 */
@WebFilter("/*")
public class PetTypeFilter implements Filter {

    private PetTypeDAO petTypeDao;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        petTypeDao = new PetTypeDAO();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Chỉ load cho các request HTML (không phải static resources)
        String uri = httpRequest.getRequestURI();
        if (!isStaticResource(uri)) {
            // Kiểm tra nếu chưa có petTypes trong request
            if (httpRequest.getAttribute("petTypes") == null) {
                try {
                    List<PetType> petTypes = petTypeDao.getActivePetTypes();
                    httpRequest.setAttribute("petTypes", petTypes);
                } catch (Exception e) {
                    // Ignore - navbar sẽ dùng fallback
                }
            }
        }
        
        chain.doFilter(request, response);
    }

    private boolean isStaticResource(String uri) {
        return uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png") 
            || uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".gif")
            || uri.endsWith(".ico") || uri.endsWith(".woff") || uri.endsWith(".woff2")
            || uri.endsWith(".ttf") || uri.endsWith(".svg") || uri.endsWith(".map");
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
