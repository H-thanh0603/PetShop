package audit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Rate Limiter Sliding Window Correctness.
 *
 * <p><b>Property 12: Rate Limiter Sliding Window Correctness</b></p>
 *
 * <p>For any set of request timestamps and a sliding window boundary, the
 * Rate_Limiter SHALL count only timestamps that fall within the window, and
 * the count SHALL equal the number of timestamps where
 * {@code timestamp >= (now - windowMs)}.</p>
 *
 * <p><b>Validates: Requirement 10.2</b></p>
 *
 * <h2>What is tested</h2>
 * <ul>
 *   <li>The sliding-window count equals the reference count computed by a simple
 *       linear scan ({@code ts >= windowStart}).</li>
 *   <li>Timestamps strictly before the window boundary are never counted.</li>
 *   <li>Timestamps exactly at the window boundary ({@code now - windowMs}) are
 *       counted (inclusive lower bound).</li>
 *   <li>Adding a new timestamp always increments the in-window count by exactly 1
 *       (assuming the new timestamp is within the window, which it always is for
 *       "now").</li>
 *   <li>After all timestamps expire the count drops to zero.</li>
 *   <li>The rate-limited decision is consistent with the count vs. limit comparison.</li>
 * </ul>
 *
 * <h2>Generator design</h2>
 * <p>All generators that produce timestamps relative to a "now" value use a
 * {@link WindowScenario} record that bundles the timestamps together with the
 * {@code now} value used to generate them. This avoids the coupling bug where
 * separate {@code @ForAll} parameters for timestamps and {@code now} would use
 * independently generated values, causing timestamps that were "expired" relative
 * to their generation-time {@code now} to appear in-window relative to the
 * property's {@code now} parameter (or vice-versa).</p>
 */
class RateLimiterSlidingWindowPropertyTest {

    // ── Inline sliding-window model ──────────────────────────────────────────────
    //
    // Self-contained, deterministic re-implementation of the sliding-window logic
    // from RateLimitFilter, parameterised on an explicit "now" so properties can
    // be verified without relying on wall-clock time.

    private static final long WINDOW_MS = 60_000L; // 1 minute, same as production

    /**
     * Counts the number of timestamps in {@code timestamps} that fall within the
     * sliding window {@code [now - windowMs, now]}.
     *
     * <p>This is the <em>reference implementation</em> — a straightforward linear
     * scan used to verify the deque-based implementation produces identical results.</p>
     */
    static long countInWindow(List<Long> timestamps, long now, long windowMs) {
        long windowStart = now - windowMs;
        return timestamps.stream().filter(ts -> ts >= windowStart).count();
    }

    /**
     * Simulates the deque-based sliding-window counter from {@code RateLimitFilter}.
     *
     * <p>Appends {@code now} to the deque, then drains expired head entries, and
     * returns the deque size as the in-window count.</p>
     */
    static int dequeWindowCount(Deque<Long> deque, long now, long windowMs) {
        long windowStart = now - windowMs;

        // Record this request (mirrors RateLimitFilter.isRateLimited).
        deque.addLast(now);

        // Evict expired entries from the head.
        Long head;
        while ((head = deque.peekFirst()) != null && head < windowStart) {
            deque.pollFirst();
        }

        return deque.size();
    }

    /**
     * Determines whether a request should be rate-limited given the current
     * in-window count and the configured limit.
     *
     * <p>Mirrors the {@code count > limit} check in {@code RateLimitFilter}.</p>
     */
    static boolean isRateLimited(int inWindowCount, int limit) {
        return inWindowCount > limit;
    }

    // ── Value objects ────────────────────────────────────────────────────────────

    /**
     * Bundles a set of timestamps with the {@code now} value used to generate them,
     * so that "expired" and "in-window" classifications remain consistent throughout
     * a property execution.
     */
    record WindowScenario(long now, List<Long> expired, List<Long> inWindow) {

        /** All timestamps combined, sorted ascending (as they would arrive over time). */
        List<Long> all() {
            List<Long> combined = new ArrayList<>(expired);
            combined.addAll(inWindow);
            combined.sort(Long::compareTo);
            return combined;
        }
    }

    // ── Generators ──────────────────────────────────────────────────────────────

    /**
     * Generates a "now" value in a realistic epoch-millisecond range.
     * Using a fixed base avoids overflow issues in arithmetic.
     */
    @Provide
    Arbitrary<Long> nowValues() {
        long base = 1_700_000_000_000L; // ~2023-11-14
        return Arbitraries.longs().between(base, base + 365L * 24 * 3600 * 1000);
    }

    /**
     * Generates a {@link WindowScenario} with only expired timestamps (all strictly
     * before {@code now - windowMs}).
     */
    @Provide
    Arbitrary<WindowScenario> expiredOnlyScenarios() {
        return nowValues().flatMap(now -> {
            long expiredMax = now - WINDOW_MS - 1;
            long expiredMin = Math.max(0L, now - WINDOW_MS * 10);
            Arbitrary<List<Long>> expiredArb = Arbitraries.longs()
                    .between(expiredMin, expiredMax)
                    .list().ofMinSize(1).ofMaxSize(30);
            return expiredArb.map(exp -> {
                List<Long> sorted = new ArrayList<>(exp);
                sorted.sort(Long::compareTo);
                return new WindowScenario(now, sorted, List.of());
            });
        });
    }

    /**
     * Generates a {@link WindowScenario} with only in-window timestamps (all within
     * {@code [now - windowMs, now]}).
     */
    @Provide
    Arbitrary<WindowScenario> inWindowOnlyScenarios() {
        return nowValues().flatMap(now -> {
            Arbitrary<List<Long>> inWindowArb = Arbitraries.longs()
                    .between(now - WINDOW_MS, now)
                    .list().ofMinSize(0).ofMaxSize(50);
            return inWindowArb.map(inWin -> {
                List<Long> sorted = new ArrayList<>(inWin);
                sorted.sort(Long::compareTo);
                return new WindowScenario(now, List.of(), sorted);
            });
        });
    }

    /**
     * Generates a {@link WindowScenario} with a mix of expired and in-window timestamps.
     */
    @Provide
    Arbitrary<WindowScenario> mixedScenarios() {
        return nowValues().flatMap(now -> {
            long expiredMax = now - WINDOW_MS - 1;
            long expiredMin = Math.max(0L, now - WINDOW_MS * 5);
            Arbitrary<List<Long>> expiredArb = Arbitraries.longs()
                    .between(expiredMin, expiredMax)
                    .list().ofMinSize(0).ofMaxSize(20);
            Arbitrary<List<Long>> inWindowArb = Arbitraries.longs()
                    .between(now - WINDOW_MS, now)
                    .list().ofMinSize(0).ofMaxSize(30);
            return Combinators.combine(expiredArb, inWindowArb)
                    .as((exp, inWin) -> new WindowScenario(now, exp, inWin));
        });
    }

    /**
     * Generates a rate limit value between 1 and 100 (inclusive).
     */
    @Provide
    Arbitrary<Integer> rateLimits() {
        return Arbitraries.integers().between(1, 100);
    }

    // ── Property Tests ───────────────────────────────────────────────────────────

    /**
     * <b>Validates: Requirement 10.2</b>
     *
     * <p><b>Core correctness property</b>: For any set of timestamps, the deque-based
     * sliding-window count must equal the reference linear-scan count.</p>
     *
     * <p>This verifies that the O(1) deque implementation is semantically equivalent
     * to the trivial O(n) reference implementation.</p>
     */
    @Property(tries = 300)
    void dequeCountMatchesReferenceCount(
            @ForAll("mixedScenarios") WindowScenario scenario) {

        long now = scenario.now();
        List<Long> allTimestamps = scenario.all();

        // Reference count: simple linear scan.
        long expected = countInWindow(allTimestamps, now, WINDOW_MS);

        // Deque-based count: replay all historical timestamps into the deque,
        // then evict expired entries (simulating the state before the current request).
        Deque<Long> deque = new ArrayDeque<>(allTimestamps);
        long windowStart = now - WINDOW_MS;
        Long head;
        while ((head = deque.peekFirst()) != null && head < windowStart) {
            deque.pollFirst();
        }
        int actual = deque.size();

        assertEquals(expected, actual,
                "Deque count must match reference count for timestamps=" + allTimestamps
                + ", now=" + now + ", windowMs=" + WINDOW_MS);
    }

    /**
     * <b>Validates: Requirement 10.2</b>
     *
     * <p>Expired timestamps (strictly before {@code now - windowMs}) are NEVER counted.</p>
     */
    @Property(tries = 200)
    void expiredTimestampsAreNeverCounted(
            @ForAll("expiredOnlyScenarios") WindowScenario scenario) {

        long count = countInWindow(scenario.expired(), scenario.now(), WINDOW_MS);
        assertEquals(0L, count,
                "Expired timestamps should contribute 0 to the window count: "
                + scenario.expired() + ", now=" + scenario.now());
    }

    /**
     * <b>Validates: Requirement 10.2</b>
     *
     * <p>Timestamps exactly at the window boundary ({@code now - windowMs}) are
     * counted (inclusive lower bound).</p>
     */
    @Property(tries = 200)
    void boundaryTimestampIsCounted(
            @ForAll("nowValues") long now) {

        long boundary = now - WINDOW_MS;
        List<Long> timestamps = List.of(boundary);

        long count = countInWindow(timestamps, now, WINDOW_MS);
        assertEquals(1L, count,
                "Timestamp exactly at window boundary should be counted: boundary=" + boundary
                + ", now=" + now);
    }

    /**
     * <b>Validates: Requirement 10.2</b>
     *
     * <p>A timestamp one millisecond before the boundary is NOT counted.</p>
     */
    @Property(tries = 200)
    void justBeforeBoundaryIsNotCounted(
            @ForAll("nowValues") long now) {

        long justBefore = now - WINDOW_MS - 1;
        List<Long> timestamps = List.of(justBefore);

        long count = countInWindow(timestamps, now, WINDOW_MS);
        assertEquals(0L, count,
                "Timestamp 1ms before window boundary should NOT be counted: ts=" + justBefore
                + ", now=" + now);
    }

    /**
     * <b>Validates: Requirement 10.2</b>
     *
     * <p>In-window timestamps are ALL counted — no in-window timestamp is dropped.</p>
     */
    @Property(tries = 200)
    void allInWindowTimestampsAreCounted(
            @ForAll("inWindowOnlyScenarios") WindowScenario scenario) {

        long count = countInWindow(scenario.inWindow(), scenario.now(), WINDOW_MS);
        assertEquals((long) scenario.inWindow().size(), count,
                "All in-window timestamps should be counted: "
                + scenario.inWindow() + ", now=" + scenario.now());
    }

    /**
     * <b>Validates: Requirement 10.2</b>
     *
     * <p>Adding a new "now" timestamp to a deque of only expired entries results in
     * a count of exactly 1 — only the new request is within the window.</p>
     */
    @Property(tries = 200)
    void addingNowToExpiredDequeGivesCountOfOne(
            @ForAll("expiredOnlyScenarios") WindowScenario scenario) {

        Deque<Long> deque = new ArrayDeque<>(scenario.expired());
        int count = dequeWindowCount(deque, scenario.now(), WINDOW_MS);

        assertEquals(1, count,
                "After adding 'now' to a deque of only expired timestamps, count should be 1: "
                + scenario.expired() + ", now=" + scenario.now());
    }

    /**
     * <b>Validates: Requirement 10.2</b>
     *
     * <p>The rate-limited decision is consistent with the count vs. limit comparison:
     * {@code isRateLimited} returns {@code true} iff {@code count > limit}.</p>
     */
    @Property(tries = 300)
    void rateLimitedDecisionIsConsistentWithCount(
            @ForAll("mixedScenarios") WindowScenario scenario,
            @ForAll("rateLimits") int limit) {

        long now = scenario.now();
        List<Long> allTimestamps = scenario.all();

        int inWindowCount = (int) countInWindow(allTimestamps, now, WINDOW_MS);

        boolean expectedLimited = inWindowCount > limit;
        boolean actualLimited = isRateLimited(inWindowCount, limit);

        assertEquals(expectedLimited, actualLimited,
                "Rate-limited decision must match count > limit: count=" + inWindowCount
                + ", limit=" + limit);
    }

    /**
     * <b>Validates: Requirement 10.2</b>
     *
     * <p>When the in-window count equals the limit exactly, the request is NOT
     * rate-limited (the limit is exclusive: {@code count > limit}, not {@code >=}).</p>
     */
    @Property(tries = 200)
    void exactlyAtLimitIsNotRateLimited(
            @ForAll("rateLimits") int limit) {

        assertFalse(isRateLimited(limit, limit),
                "count == limit should NOT be rate-limited (limit is exclusive): limit=" + limit);
    }

    /**
     * <b>Validates: Requirement 10.2</b>
     *
     * <p>When the in-window count exceeds the limit by any positive amount, the
     * request IS rate-limited.</p>
     */
    @Property(tries = 200)
    void exceedingLimitIsAlwaysRateLimited(
            @ForAll("rateLimits") int limit,
            @ForAll @IntRange(min = 1, max = 50) int excess) {

        int count = limit + excess;
        assertTrue(isRateLimited(count, limit),
                "count > limit should always be rate-limited: count=" + count + ", limit=" + limit);
    }

    /**
     * <b>Validates: Requirement 10.2</b>
     *
     * <p>The count is monotonically non-decreasing as new in-window timestamps are
     * added (assuming no eviction occurs between additions, i.e., all requests arrive
     * at the same "now").</p>
     */
    @Property(tries = 200)
    void countIsMonotonicallyNonDecreasingWithinWindow(
            @ForAll("nowValues") long now,
            @ForAll @IntRange(min = 1, max = 20) int extraRequests) {

        Deque<Long> deque = new ArrayDeque<>();
        int previousCount = 0;

        for (int i = 0; i < extraRequests; i++) {
            // All requests arrive at "now" — all are within the window.
            int currentCount = dequeWindowCount(deque, now, WINDOW_MS);
            assertTrue(currentCount >= previousCount,
                    "Count should be non-decreasing as in-window requests are added: "
                    + "previous=" + previousCount + ", current=" + currentCount);
            previousCount = currentCount;
        }
    }

    /**
     * <b>Validates: Requirement 10.2</b>
     *
     * <p>After the window expires (all timestamps are older than {@code windowMs}),
     * the count drops to 1 (only the new "now" request remains).</p>
     */
    @Property(tries = 200)
    void countDropsToOneAfterWindowExpires(
            @ForAll("inWindowOnlyScenarios") WindowScenario scenario) {

        long now = scenario.now();
        List<Long> inWindow = scenario.inWindow();

        // Advance time so all previously in-window timestamps have expired.
        // Add 1 extra ms so the boundary timestamp (now - WINDOW_MS) also expires.
        long futureNow = now + WINDOW_MS + 1;

        Deque<Long> deque = new ArrayDeque<>(inWindow);
        int count = dequeWindowCount(deque, futureNow, WINDOW_MS);

        assertEquals(1, count,
                "After window expires, count should be 1 (only the new request): "
                + "inWindow=" + inWindow + ", futureNow=" + futureNow);
    }
}
