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
import java.net.URL;

/**
 * Adds aggressive cache headers for static assets so storefront pages avoid
 * re-downloading unchanged images, CSS, and JS on every request.
 *
 * <ul>
 *   <li>Cache-Control: public, max-age=N (configurable via app.static.cache.max-age-seconds)</li>
 *   <li>Vary: Accept-Encoding — tells proxies to keep separate copies per encoding</li>
 *   <li>Last-Modified — enables conditional GET (304 Not Modified) via If-Modified-Since</li>
 *   <li>ETag — enables conditional GET (304 Not Modified) via If-None-Match</li>
 * </ul>
 */
public class StaticAssetCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        if (!isCacheableMethod(method)) {
            chain.doFilter(request, response);
            return;
        }

        int maxAgeSeconds = Math.max(0, AppConfig.getInt("app.static.cache.max-age-seconds", 86400));

        // Resolve last-modified time from the real file on disk so we can serve ETags
        long lastModified = resolveLastModified(httpRequest);

        if (lastModified > 0) {
            // Build a lightweight ETag from the last-modified timestamp
            String etag = "\"" + Long.toHexString(lastModified) + "\"";

            // --- Conditional GET: If-None-Match ---
            String ifNoneMatch = httpRequest.getHeader("If-None-Match");
            if (etag.equals(ifNoneMatch)) {
                httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            // --- Conditional GET: If-Modified-Since ---
            long ifModifiedSince = httpRequest.getDateHeader("If-Modified-Since");
            if (ifModifiedSince >= (lastModified / 1000L * 1000L)) {
                httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            httpResponse.setDateHeader("Last-Modified", lastModified);
            httpResponse.setHeader("ETag", etag);
        }

        // Cache-Control
        httpResponse.setHeader("Cache-Control", "public, max-age=" + maxAgeSeconds);
        // Vary tells CDN/proxy to store separate cached copies per encoding (gzip vs identity)
        httpResponse.setHeader("Vary", "Accept-Encoding");

        chain.doFilter(request, response);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isCacheableMethod(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
    }

    /**
     * Resolves the last-modified timestamp of the physical file behind the request URI.
     * Returns -1 if the resource cannot be located (e.g. servlet-generated responses).
     */
    private long resolveLastModified(HttpServletRequest request) {
        try {
            URL resourceUrl = request.getServletContext().getResource(request.getServletPath());
            if (resourceUrl != null) {
                return resourceUrl.openConnection().getLastModified();
            }
        } catch (Exception ignored) {
            // Not a physical file — skip Last-Modified/ETag
        }
        return -1L;
    }
}

