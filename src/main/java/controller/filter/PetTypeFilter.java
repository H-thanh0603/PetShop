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
import services.PetTypeCache;

/**
 * Filter để load danh sách loại thú cưng cho tất cả các trang.
 * Dùng PetTypeCache với TTL 1 giờ để tránh query DB mỗi request.
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
        
        String uri = httpRequest.getRequestURI();
        if (!isStaticResource(uri)) {
            if (httpRequest.getAttribute("petTypes") == null) {
                PetTypeCache cache = PetTypeCache.getInstance();
                if (cache.isStale()) {
                    try {
                        List<PetType> petTypes = petTypeDao.getActivePetTypes();
                        cache.update(petTypes);
                    } catch (Exception e) {
                        // Retain previously cached data on DB failure
                        System.err.println("[PetTypeFilter] DB reload failed, using cached data: " + e.getMessage());
                    }
                }
                httpRequest.setAttribute("petTypes", cache.get());
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
    public void destroy() {}
}
