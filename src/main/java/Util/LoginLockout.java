package Util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * In-memory login-failure lockout keyed by (email, IP).
 *
 * Replaces the account-wide lockout that lived on the users row: that
 * version let anyone lock any user's account (including admin) for 15
 * minutes with 5 bad submissions from a single source. Keying by email+IP
 * means the legitimate user, coming from a different IP, is unaffected.
 *
 * Single-node assumption (rate limiting and OTP store share it): moving to
 * multiple instances requires a shared store (Redis/DB) with the same
 * semantics.
 */
public final class LoginLockout {

    private static final int MAX_FAILURES = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;
    private static final long FAILURE_WINDOW_MS = 15 * 60 * 1000L;

    private static final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    private LoginLockout() {
    }

    public static boolean isLocked(String email, String ip) {
        AttemptRecord record = attempts.get(key(email, ip));
        if (record == null) {
            return false;
        }
        if (record.lockedUntil > System.currentTimeMillis()) {
            return true;
        }
        if (record.lockedUntil > 0) {
            // Lock expired: start over
            attempts.remove(key(email, ip));
        }
        return false;
    }

    /**
     * Records a failed login. @return true when this failure triggered a lock.
     */
    public static boolean recordFailure(String email, String ip) {
        String k = key(email, ip);
        AttemptRecord record = attempts.compute(k,
                (key, existing) -> {
                    long now = System.currentTimeMillis();
                    if (existing == null || now - existing.windowStart > FAILURE_WINDOW_MS) {
                        return new AttemptRecord(now);
                    }
                    existing.count++;
                    return existing;
                });
        if (record.count >= MAX_FAILURES) {
            record.lockedUntil = System.currentTimeMillis() + LOCK_DURATION_MS;
            return true;
        }
        return false;
    }

    public static void reset(String email, String ip) {
        attempts.remove(key(email, ip));
    }

    private static String key(String email, String ip) {
        return (email == null ? "" : email.trim().toLowerCase()) + "|" + (ip == null ? "" : ip);
    }

    private static final class AttemptRecord {
        int count = 1;
        final long windowStart;
        volatile long lockedUntil;

        AttemptRecord(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
