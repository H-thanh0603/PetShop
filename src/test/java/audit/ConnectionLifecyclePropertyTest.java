package audit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Database Connection Lifecycle Completeness.
 *
 * <p><b>Property 11: Database Connection Lifecycle Completeness</b></p>
 *
 * <p>For any DAO method pattern, if the connection is never closed (no try-with-resources
 * and no manual close in finally), the classifier SHALL return HIGH severity.
 * If the connection is properly closed via try-with-resources or a manual finally block,
 * the classifier SHALL return SAFE. If the connection is properly closed but obtained via
 * the instance constructor instead of the static pool method, the classifier SHALL return
 * MEDIUM (not HIGH).</p>
 *
 * <p><b>Validates: Requirements 9.1, 9.4</b></p>
 */
class ConnectionLifecyclePropertyTest {

    // ── Inline model (self-contained, no external dependencies) ──

    enum ConnectionSeverity {
        HIGH,   // Connection never closed — definite leak
        MEDIUM, // Connection closed but using instance constructor instead of static pool
        LOW,    // Minor issue (e.g., ResultSet not closed but connection is)
        SAFE    // Connection properly closed via try-with-resources or manual finally
    }

    /**
     * Represents the connection-management pattern of a single DAO method.
     */
    record ConnectionPattern(
            boolean hasTryWithResources,
            boolean hasManualClose,
            boolean hasStaticGetConnection
    ) {}

    /**
     * A finding produced by the connection lifecycle classifier.
     */
    record ConnectionLifecycleFinding(ConnectionSeverity severity, String reason) {}

    /**
     * Classifies a DAO method's connection-management pattern.
     *
     * <ul>
     *   <li>If neither try-with-resources nor manual close → HIGH (connection never closed)</li>
     *   <li>If try-with-resources or manual close present → SAFE (connection properly closed)</li>
     *   <li>If properly closed but uses instance constructor (not static) → MEDIUM</li>
     * </ul>
     *
     * <p>Note: MEDIUM takes precedence over SAFE when the connection is closed but the
     * instance constructor is used, because using {@code new DBContext().getConnection()}
     * bypasses the static HikariCP pool and creates unnecessary overhead.</p>
     */
    static ConnectionLifecycleFinding classify(ConnectionPattern pattern) {
        boolean properlyClosedByTwr = pattern.hasTryWithResources();
        boolean properlyClosedByFinally = pattern.hasManualClose();
        boolean properlyClosed = properlyClosedByTwr || properlyClosedByFinally;

        if (!properlyClosed) {
            return new ConnectionLifecycleFinding(
                    ConnectionSeverity.HIGH,
                    "Connection is never closed — definite resource leak"
            );
        }

        // Connection is closed; check whether the static pool method is used
        if (!pattern.hasStaticGetConnection()) {
            return new ConnectionLifecycleFinding(
                    ConnectionSeverity.MEDIUM,
                    "Connection is closed but obtained via instance constructor instead of DBContext.getConnection()"
            );
        }

        return new ConnectionLifecycleFinding(
                ConnectionSeverity.SAFE,
                "Connection is properly closed via try-with-resources or manual finally block"
        );
    }

    // ── Generators ──

    @Provide
    Arbitrary<ConnectionPattern> unclosedPatterns() {
        // hasTryWithResources=false, hasManualClose=false — connection is never closed
        return Arbitraries.of(Boolean.TRUE, Boolean.FALSE)
                .map(staticGet -> new ConnectionPattern(false, false, staticGet));
    }

    @Provide
    Arbitrary<ConnectionPattern> twrPatterns() {
        // hasTryWithResources=true — connection is always closed by JVM
        return Arbitraries.of(Boolean.TRUE, Boolean.FALSE)
                .map(staticGet -> new ConnectionPattern(true, false, staticGet));
    }

    @Provide
    Arbitrary<ConnectionPattern> manualClosePatterns() {
        // hasManualClose=true — connection closed in finally block
        return Arbitraries.of(Boolean.TRUE, Boolean.FALSE)
                .map(staticGet -> new ConnectionPattern(false, true, staticGet));
    }

    @Provide
    Arbitrary<ConnectionPattern> instanceConstructorClosedPatterns() {
        // Connection is closed (twr or manual) but uses instance constructor
        return Arbitraries.of(Boolean.TRUE, Boolean.FALSE)
                .flatMap(useTwr -> Arbitraries.just(
                        new ConnectionPattern(useTwr, !useTwr, false)
                ));
    }

    @Provide
    Arbitrary<ConnectionPattern> allPatterns() {
        return Combinators.combine(
                Arbitraries.of(Boolean.TRUE, Boolean.FALSE),
                Arbitraries.of(Boolean.TRUE, Boolean.FALSE),
                Arbitraries.of(Boolean.TRUE, Boolean.FALSE)
        ).as(ConnectionPattern::new);
    }

    // ── Property Tests ──

    /**
     * <b>Validates: Requirements 9.1, 9.4</b>
     *
     * <p>Property 1: Any pattern without try-with-resources AND without manual close
     * must be flagged HIGH severity.</p>
     */
    @Property(tries = 100)
    void unclosedConnectionIsAlwaysHigh(
            @ForAll("unclosedPatterns") ConnectionPattern pattern) {
        ConnectionLifecycleFinding finding = classify(pattern);
        assertEquals(ConnectionSeverity.HIGH, finding.severity(),
                "Connection never closed should be HIGH: " + pattern);
    }

    /**
     * <b>Validates: Requirements 9.1, 9.4</b>
     *
     * <p>Property 2: Any pattern with try-with-resources is SAFE (when using static pool)
     * or MEDIUM (when using instance constructor), but never HIGH.</p>
     */
    @Property(tries = 100)
    void twrPatternIsNeverHigh(
            @ForAll("twrPatterns") ConnectionPattern pattern) {
        ConnectionLifecycleFinding finding = classify(pattern);
        assertNotEquals(ConnectionSeverity.HIGH, finding.severity(),
                "try-with-resources should never be HIGH: " + pattern);
    }

    /**
     * <b>Validates: Requirements 9.1, 9.4</b>
     *
     * <p>Property 3: Any pattern with try-with-resources AND static pool method is SAFE.</p>
     */
    @Property(tries = 50)
    void twrWithStaticConnectionIsSafe(
            @ForAll boolean hasManualClose) {
        ConnectionPattern pattern = new ConnectionPattern(true, hasManualClose, true);
        ConnectionLifecycleFinding finding = classify(pattern);
        assertEquals(ConnectionSeverity.SAFE, finding.severity(),
                "try-with-resources + static pool should be SAFE: " + pattern);
    }

    /**
     * <b>Validates: Requirements 9.1, 9.4</b>
     *
     * <p>Property 4: Any pattern with manual close in finally AND static pool method is SAFE.</p>
     */
    @Property(tries = 50)
    void manualCloseWithStaticConnectionIsSafe(
            @ForAll boolean hasTwr) {
        ConnectionPattern pattern = new ConnectionPattern(hasTwr, true, true);
        ConnectionLifecycleFinding finding = classify(pattern);
        assertEquals(ConnectionSeverity.SAFE, finding.severity(),
                "manual close + static pool should be SAFE: " + pattern);
    }

    /**
     * <b>Validates: Requirements 9.1, 9.4</b>
     *
     * <p>Property 5: Patterns using instance constructor but with proper closure
     * are MEDIUM (not HIGH) — the connection is closed, but the pool is bypassed.</p>
     */
    @Property(tries = 100)
    void instanceConstructorWithClosureIsMediumNotHigh(
            @ForAll("instanceConstructorClosedPatterns") ConnectionPattern pattern) {
        ConnectionLifecycleFinding finding = classify(pattern);
        assertEquals(ConnectionSeverity.MEDIUM, finding.severity(),
                "Instance constructor + closed connection should be MEDIUM: " + pattern);
        assertNotEquals(ConnectionSeverity.HIGH, finding.severity(),
                "Instance constructor + closed connection must NOT be HIGH: " + pattern);
    }

    /**
     * <b>Validates: Requirements 9.1, 9.4</b>
     *
     * <p>Property 6: Severity ordering is consistent — HIGH > MEDIUM > LOW > SAFE
     * (lower ordinal = higher severity).</p>
     */
    @Property(tries = 200)
    void severityOrderingIsConsistent(
            @ForAll("unclosedPatterns") ConnectionPattern unclosed,
            @ForAll("instanceConstructorClosedPatterns") ConnectionPattern instanceClosed,
            @ForAll boolean hasTwr) {
        ConnectionPattern safe = new ConnectionPattern(hasTwr, !hasTwr, true);

        ConnectionSeverity high = classify(unclosed).severity();
        ConnectionSeverity medium = classify(instanceClosed).severity();
        ConnectionSeverity safeLevel = classify(safe).severity();

        assertTrue(high.ordinal() < medium.ordinal(),
                "HIGH should rank higher (lower ordinal) than MEDIUM");
        assertTrue(medium.ordinal() < safeLevel.ordinal(),
                "MEDIUM should rank higher (lower ordinal) than SAFE");
    }

    /**
     * <b>Validates: Requirements 9.1, 9.4</b>
     *
     * <p>Property 7: For any pattern, the finding always has a non-null, non-empty reason.</p>
     */
    @Property(tries = 200)
    void findingAlwaysHasReason(
            @ForAll("allPatterns") ConnectionPattern pattern) {
        ConnectionLifecycleFinding finding = classify(pattern);
        assertNotNull(finding.reason(), "Finding reason must not be null");
        assertFalse(finding.reason().isBlank(), "Finding reason must not be blank");
        assertNotNull(finding.severity(), "Finding severity must not be null");
    }
}
