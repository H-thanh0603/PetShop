package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

public class RateLimitFilter implements Filter {
    private static final long WINDOW_MS = 60_000;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Long>> requestLog = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    @Override
    public void init(FilterConfig config) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-evictor");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::evictExpiredEntries, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String path = request.getServletPath();
        int limit = getLimitForEndpoint(path);
        if (limit < 0) {
            chain.doFilter(req, res);
            return;
        }
        
        String clientIp = resolveClientIp(request);
        String key = clientIp + ":" + path;
        
        if (isRateLimited(key, limit)) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Too many requests. Please try again later.");
            return;
        }
        
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        if (scheduler != null) scheduler.shutdownNow();
    }

    String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    int getLimitForEndpoint(String path) {
        if (path == null) return -1;
        if (path.equals("/login")) return 10;
        if (path.equals("/shop")) return 30;
        if (path.equals("/add-to-cart")) return 20;
        if (path.equals("/add-review")) return 5;
        return -1;
    }

    boolean isRateLimited(String key, int limit) {
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_MS;
        
        requestLog.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        CopyOnWriteArrayList<Long> timestamps = requestLog.get(key);
        timestamps.add(now);
        
        long count = 0;
        for (Long ts : timestamps) {
            if (ts >= windowStart) count++;
        }
        
        return count > limit;
    }

    void evictExpiredEntries() {
        long windowStart = System.currentTimeMillis() - WINDOW_MS;
        for (Map.Entry<String, CopyOnWriteArrayList<Long>> entry : requestLog.entrySet()) {
            entry.getValue().removeIf(ts -> ts < windowStart);
            if (entry.getValue().isEmpty()) {
                requestLog.remove(entry.getKey());
            }
        }
    }
}
