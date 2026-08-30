package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Util.AppConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Servlet filter that enforces per-IP, per-endpoint rate limiting using a
 * sliding-window algorithm.
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li><b>Data structure</b>: Each key maps to a {@link ConcurrentLinkedDeque}{@code <Long>}
 *       of request timestamps. {@code ConcurrentLinkedDeque} supports O(1) amortised
 *       {@code addLast} and {@code pollFirst} operations, eliminating the O(n) array-copy
 *       overhead of the previous {@code CopyOnWriteArrayList} implementation.</li>
 *   <li><b>IP resolution</b>: Client IP is resolved from {@code request.getRemoteAddr()}
 *       only. The {@code X-Forwarded-For} header is validated against a configurable
 *       trusted-proxy whitelist before being trusted; if the connecting address is not a
 *       known proxy the header is ignored to prevent rate-limit bypass.</li>
 *   <li><b>Multi-instance limitation</b>: Rate-limit state is stored in JVM heap memory.
 *       In a multi-instance (clustered) deployment each node maintains its own independent
 *       counter, so the effective per-user limit is {@code limit × instanceCount}.
 *       For production deployments with more than one instance, replace this implementation
 *       with a Redis-backed sliding-window counter (e.g., using Redisson or Lettuce with
 *       a Lua script) so that all nodes share a single authoritative counter.
 *       TODO: Replace JVM-memory rate limiting with Redis-backed implementation for
 *       multi-instance deployments.</li>
 * </ul>
 */
public class RateLimitFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final long WINDOW_MS = 60_000;

    /**
     * Trusted reverse-proxy addresses whose {@code X-Forwarded-For} header is accepted.
     * Extend this set via {@code web.xml} init-params or an external config file for
     * production deployments.
     */
    private static final java.util.Set<String> TRUSTED_PROXIES = java.util.Set.of(
            "127.0.0.1",
            "::1"
    );

    // ConcurrentLinkedDeque gives O(1) addLast / pollFirst — no array copy on write.
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> requestLog =
            new ConcurrentHashMap<>();
    private final Map<String, Integer> endpointLimits = new HashMap<>();

    private ScheduledExecutorService scheduler;

    @Override
    public void init(FilterConfig config) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-evictor");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::evictExpiredEntries, 5, 5, TimeUnit.MINUTES);
        loadEndpointLimits();

        // Warn operators that JVM-memory rate limiting is not effective across multiple
        // instances. This message appears once at startup so it is visible in server logs.
        logger.warn(
                "[RateLimitFilter] Rate-limit state is stored in JVM heap memory. "
                + "In a multi-instance (clustered) deployment each node maintains its own "
                + "independent counter, making the effective per-user limit "
                + "'configuredLimit × instanceCount'. "
                + "For production clusters, replace this filter with a Redis-backed "
                + "sliding-window implementation so all nodes share a single counter."
        );
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
            logger.warn("Rate limit exceeded for ip={} path={} limitPerMinute={}", clientIp, path, limit);
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

    /**
     * Resolves the real client IP address.
     *
     * <p>The {@code X-Forwarded-For} header is only trusted when the direct connecting
     * address ({@code request.getRemoteAddr()}) is in the {@link #TRUSTED_PROXIES}
     * whitelist. This prevents an attacker from spoofing their IP by sending a crafted
     * {@code X-Forwarded-For} header directly to the application, which would otherwise
     * allow them to bypass per-IP rate limiting.</p>
     *
     * <p>When the connecting address is a trusted proxy, the <em>leftmost</em> (first)
     * value in the {@code X-Forwarded-For} header is used, as that represents the
     * original client IP appended by the outermost proxy.</p>
     */
    String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        if (TRUSTED_PROXIES.contains(remoteAddr)) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                String candidate = xff.split(",")[0].trim();
                if (!candidate.isEmpty()) {
                    return candidate;
                }
            }
        }

        // Connecting address is not a trusted proxy — use it directly and ignore
        // any X-Forwarded-For header to prevent rate-limit bypass.
        return remoteAddr;
    }

    int getLimitForEndpoint(String path) {
        if (path == null) return -1;
        if (endpointLimits.isEmpty()) {
            loadEndpointLimits();
        }
        return endpointLimits.getOrDefault(path, -1);
    }

    /**
     * Checks whether the given key has exceeded its rate limit within the sliding window.
     *
     * <p>Uses {@link ConcurrentLinkedDeque} for O(1) append and O(1) head-removal,
     * replacing the previous {@code CopyOnWriteArrayList} which incurred an O(n) array
     * copy on every {@code add()} call.</p>
     *
     * <p>Algorithm:</p>
     * <ol>
     *   <li>Append the current timestamp to the tail of the deque.</li>
     *   <li>Drain expired timestamps from the head (timestamps older than
     *       {@code now - WINDOW_MS}).</li>
     *   <li>The remaining deque size is the count of requests within the window.</li>
     * </ol>
     */
    boolean isRateLimited(String key, int limit) {
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_MS;

        ConcurrentLinkedDeque<Long> timestamps =
                requestLog.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        // Record this request.
        timestamps.addLast(now);

        // Evict timestamps that have fallen outside the sliding window.
        // peekFirst() / pollFirst() are O(1) on ConcurrentLinkedDeque.
        Long head;
        while ((head = timestamps.peekFirst()) != null && head < windowStart) {
            timestamps.pollFirst();
        }

        return timestamps.size() > limit;
    }

    /**
     * Periodic cleanup: removes keys whose deques are entirely expired to prevent
     * unbounded memory growth for long-idle clients.
     */
    void evictExpiredEntries() {
        long windowStart = System.currentTimeMillis() - WINDOW_MS;
        for (Map.Entry<String, ConcurrentLinkedDeque<Long>> entry : requestLog.entrySet()) {
            Long head;
            while ((head = entry.getValue().peekFirst()) != null && head < windowStart) {
                entry.getValue().pollFirst();
            }
            if (entry.getValue().isEmpty()) {
                requestLog.remove(entry.getKey());
            }
        }
    }

    private void loadEndpointLimits() {
        endpointLimits.clear();
        endpointLimits.put("/login", AppConfig.getInt("ratelimit.login", 10));
        endpointLimits.put("/register", AppConfig.getInt("ratelimit.register", 6));
        endpointLimits.put("/forgot-password", AppConfig.getInt("ratelimit.forgot-password", 6));
        endpointLimits.put("/verify-otp", AppConfig.getInt("ratelimit.verify-otp", 6));
        endpointLimits.put("/reset-password", AppConfig.getInt("ratelimit.reset-password", 6));
        endpointLimits.put("/checkout", AppConfig.getInt("ratelimit.checkout", 8));
        endpointLimits.put("/api/payment/bank-webhook", AppConfig.getInt("ratelimit.bank-webhook", 60));
        endpointLimits.put("/api/payment/vnpay-ipn", AppConfig.getInt("ratelimit.vnpay-ipn", 60));
        endpointLimits.put("/add-review", AppConfig.getInt("ratelimit.add-review", 5));
        endpointLimits.put("/api/search-autocomplete", AppConfig.getInt("ratelimit.search-autocomplete", 8));
        endpointLimits.put("/shop", 30);
        endpointLimits.put("/add-to-cart", 20);
        endpointLimits.put("/ai-support/chat", 10);
    }
}
