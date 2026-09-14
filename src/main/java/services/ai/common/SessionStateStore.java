package services.ai.common;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cross-turn session state port (upstream: provenance maps written back with
 * the session). Keeps per-conversation provenance (product/order ids tools
 * returned) so gates hold across turns, not just within one model call.
 * Bounded (PROVENANCE_CAP) with idle expiry; holds ids only, never content.
 */
public final class SessionStateStore {
    private static final int PROVENANCE_CAP = 100;
    private static final long IDLE_EXPIRY_MS = 2 * 60 * 60 * 1000L;

    public static final class SessionState {
        public final Set<Integer> seenProductIds = new LinkedHashSet<>();
        public Integer seenOrderId;
        public long lastActive = System.currentTimeMillis();
    }

    private static final Map<String, SessionState> STATES = new ConcurrentHashMap<>();

    private SessionStateStore() {}

    public static SessionState get(String sessionKey) {
        if (sessionKey == null) return new SessionState();
        prune();
        return STATES.computeIfAbsent(sessionKey, k -> new SessionState());
    }

    public static void rememberProducts(String sessionKey, Set<Integer> ids) {
        if (sessionKey == null || ids == null) return;
        SessionState s = get(sessionKey);
        synchronized (s) {
            s.seenProductIds.addAll(ids);
            while (s.seenProductIds.size() > PROVENANCE_CAP) {
                s.seenProductIds.remove(s.seenProductIds.iterator().next());
            }
            s.lastActive = System.currentTimeMillis();
        }
    }

    public static void rememberOrder(String sessionKey, Integer orderId) {
        if (sessionKey == null || orderId == null) return;
        SessionState s = get(sessionKey);
        synchronized (s) {
            s.seenOrderId = orderId;
            s.lastActive = System.currentTimeMillis();
        }
    }

    private static void prune() {
        long now = System.currentTimeMillis();
        STATES.entrySet().removeIf(e -> now - e.getValue().lastActive > IDLE_EXPIRY_MS);
    }
}
