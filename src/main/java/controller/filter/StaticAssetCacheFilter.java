package controller.filter;

import Util.AppConfig;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Adds cache headers for static assets so storefront pages avoid re-downloading
 * unchanged images, CSS, and JS on every request.
 */
public class StaticAssetCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isCacheableMethod(httpRequest.getMethod())) {
            int maxAgeSeconds = Math.max(0, AppConfig.getInt("app.static.cache.max-age-seconds", 86400));
            httpResponse.setHeader("Cache-Control", "public, max-age=" + maxAgeSeconds);
            httpResponse.setHeader("Vary", "Accept-Encoding");
        }

        chain.doFilter(request, response);
    }

    private boolean isCacheableMethod(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
    }
}
