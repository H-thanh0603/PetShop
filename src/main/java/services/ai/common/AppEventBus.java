package services.ai.common;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * App-event queue port (upstream: host queues an app event on the session;
 * the next turn reads it). Used for out-of-conversation completions such as
 * payment finishing on the checkout page — the next chat turn then knows.
 */
public final class AppEventBus {
    public record AppEvent(String type, String payload, long timestamp) {}

    private static final int MAX_PER_KEY = 20;
    private static final Map<String, Deque<AppEvent>> QUEUES = new ConcurrentHashMap<>();

    private AppEventBus() {}

    public static void publish(String sessionKey, String type, String payload) {
        if (sessionKey == null || type == null) return;
        Deque<AppEvent> q = QUEUES.computeIfAbsent(sessionKey, k -> new ArrayDeque<>());
        synchronized (q) {
            q.addLast(new AppEvent(type, Fence.sanitize(payload), System.currentTimeMillis()));
            while (q.size() > MAX_PER_KEY) q.removeFirst();
        }
    }

    /** Non-destructive read (digests, queues); use drain to consume. */
    public static List<AppEvent> peek(String sessionKey) {
        Deque<AppEvent> q = QUEUES.get(sessionKey);
        if (q == null) return List.of();
        synchronized (q) {
            return new ArrayList<>(q);
        }
    }

    /** Drains pending events for a session (consumed by the next turn). */
    public static List<AppEvent> drain(String sessionKey) {        Deque<AppEvent> q = QUEUES.get(sessionKey);
        if (q == null) return List.of();
        synchronized (q) {
            List<AppEvent> out = new ArrayList<>(q);
            q.clear();
            return out;
        }
    }
}
