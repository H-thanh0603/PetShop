package audit;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Order Status Transition Validation.
 *
 * <p><b>Property 10: Order Status Transition Validation</b></p>
 *
 * <p>For any order status update operation that does not validate the transition
 * against a defined state machine (e.g., allowing Completed → Pending), the
 * Audit_Engine SHALL flag it as a HIGH severity logic bug.</p>
 *
 * <p>Valid state machine transitions:</p>
 * <ul>
 *   <li>Pending    → Confirmed, Cancelled</li>
 *   <li>Confirmed  → Shipping,  Cancelled</li>
 *   <li>Shipping   → Completed</li>
 *   <li>Completed  → (none)</li>
 *   <li>Cancelled  → (none)</li>
 * </ul>
 *
 * <p><b>Validates: Requirement 8.3</b></p>
 */
class OrderStatusTransitionPropertyTest {

    // ── Inline severity model (self-contained, no external dependencies) ──

    enum TransitionSeverity {
        HIGH,  // Invalid transition detected — violates the state machine
        SAFE   // Valid transition — conforms to the state machine
    }

    /**
     * Classifies an order status transition for validity against the defined state machine.
     *
     * <ul>
     *   <li>Returns SAFE for valid transitions:
     *       Pending→Confirmed, Pending→Cancelled, Confirmed→Shipping,
     *       Confirmed→Cancelled, Shipping→Completed</li>
     *   <li>Returns HIGH for all other cases: invalid transitions, reflexive
     *       transitions (same→same), null values, and unknown status strings</li>
     * </ul>
     *
     * @param fromStatus the current order status string (case-insensitive)
     * @param toStatus   the target order status string (case-insensitive)
     * @return SAFE if the transition is valid, HIGH otherwise
     */
    static TransitionSeverity classifyTransition(String fromStatus, String toStatus) {
        if (fromStatus == null || toStatus == null) {
            return TransitionSeverity.HIGH;
        }

        String from = fromStatus.trim().toLowerCase();
        String to   = toStatus.trim().toLowerCase();

        // Reflexive transitions are always invalid
        if (from.equals(to)) {
            return TransitionSeverity.HIGH;
        }

        // Valid transitions per the state machine
        switch (from) {
            case "pending":
                if (to.equals("confirmed") || to.equals("cancelled")) {
                    return TransitionSeverity.SAFE;
                }
                break;
            case "confirmed":
                if (to.equals("shipping") || to.equals("cancelled")) {
                    return TransitionSeverity.SAFE;
                }
                break;
            case "shipping":
                if (to.equals("completed")) {
                    return TransitionSeverity.SAFE;
                }
                break;
            case "completed":
                // No valid targets — all transitions from Completed are invalid
                break;
            case "cancelled":
                // No valid targets — all transitions from Cancelled are invalid
                break;
            default:
                // Unknown status — flag as HIGH
                break;
        }

        return TransitionSeverity.HIGH;
    }

    // ── Generators ──

    /**
     * Generates all valid transition pairs as defined by the state machine.
     */
    @Provide
    Arbitrary<Tuple.Tuple2<String, String>> validTransitionPairs() {
        return Arbitraries.of(
                Tuple.of("Pending",   "Confirmed"),
                Tuple.of("Pending",   "Cancelled"),
                Tuple.of("Confirmed", "Shipping"),
                Tuple.of("Confirmed", "Cancelled"),
                Tuple.of("Shipping",  "Completed")
        );
    }

    /**
     * Generates known invalid transition pairs that violate the state machine.
     */
    @Provide
    Arbitrary<Tuple.Tuple2<String, String>> invalidTransitionPairs() {
        return Arbitraries.of(
                Tuple.of("Completed",  "Pending"),
                Tuple.of("Completed",  "Confirmed"),
                Tuple.of("Completed",  "Shipping"),
                Tuple.of("Completed",  "Cancelled"),
                Tuple.of("Shipping",   "Pending"),
                Tuple.of("Shipping",   "Confirmed"),
                Tuple.of("Shipping",   "Cancelled"),
                Tuple.of("Cancelled",  "Pending"),
                Tuple.of("Cancelled",  "Confirmed"),
                Tuple.of("Cancelled",  "Shipping"),
                Tuple.of("Cancelled",  "Completed"),
                Tuple.of("Pending",    "Shipping"),
                Tuple.of("Pending",    "Completed"),
                Tuple.of("Confirmed",  "Pending"),
                Tuple.of("Confirmed",  "Completed")
        );
    }

    /**
     * Generates reflexive transition pairs where from-status equals to-status.
     */
    @Provide
    Arbitrary<Tuple.Tuple2<String, String>> reflexiveTransitionPairs() {
        return allStatusValues().map(status -> Tuple.of(status, status));
    }

    /**
     * Generates any of the 5 known order status strings.
     */
    @Provide
    Arbitrary<String> allStatusValues() {
        return Arbitraries.of("Pending", "Confirmed", "Shipping", "Completed", "Cancelled");
    }

    // ── Property Tests ──

    /**
     * <b>Validates: Requirement 8.3</b>
     *
     * <p>For every valid transition pair defined by the state machine
     * (Pending→Confirmed, Pending→Cancelled, Confirmed→Shipping,
     * Confirmed→Cancelled, Shipping→Completed), the classifier must return SAFE.</p>
     */
    @Property(tries = 100)
    void validTransitionsAreAccepted(
            @ForAll("validTransitionPairs") Tuple.Tuple2<String, String> pair) {
        TransitionSeverity severity = classifyTransition(pair.get1(), pair.get2());
        assertEquals(TransitionSeverity.SAFE, severity,
                "Valid transition " + pair.get1() + " → " + pair.get2() + " should be SAFE");
    }

    /**
     * <b>Validates: Requirement 8.3</b>
     *
     * <p>For every invalid transition pair (e.g., Completed→Pending, Shipping→Pending,
     * Cancelled→Pending, etc.), the classifier must return HIGH severity, indicating
     * a logic bug that violates the state machine.</p>
     */
    @Property(tries = 100)
    void invalidTransitionsAreFlaggedAsHigh(
            @ForAll("invalidTransitionPairs") Tuple.Tuple2<String, String> pair) {
        TransitionSeverity severity = classifyTransition(pair.get1(), pair.get2());
        assertEquals(TransitionSeverity.HIGH, severity,
                "Invalid transition " + pair.get1() + " → " + pair.get2() + " should be HIGH");
    }

    /**
     * <b>Validates: Requirement 8.3</b>
     *
     * <p>For any reflexive transition (same status → same status, e.g., Pending→Pending),
     * the classifier must return HIGH severity. A status cannot transition to itself.</p>
     */
    @Property(tries = 50)
    void reflexiveTransitionsAreFlaggedAsHigh(
            @ForAll("reflexiveTransitionPairs") Tuple.Tuple2<String, String> pair) {
        TransitionSeverity severity = classifyTransition(pair.get1(), pair.get2());
        assertEquals(TransitionSeverity.HIGH, severity,
                "Reflexive transition " + pair.get1() + " → " + pair.get2() + " should be HIGH");
    }

    /**
     * <b>Validates: Requirement 8.3</b>
     *
     * <p>If the from-status is null, the classifier must return HIGH severity,
     * as the transition cannot be validated against the state machine.</p>
     */
    @Property(tries = 50)
    void nullFromStatusIsFlagged(
            @ForAll("allStatusValues") String toStatus) {
        TransitionSeverity severity = classifyTransition(null, toStatus);
        assertEquals(TransitionSeverity.HIGH, severity,
                "Null from-status should be flagged as HIGH (to: " + toStatus + ")");
    }

    /**
     * <b>Validates: Requirement 8.3</b>
     *
     * <p>If the to-status is null, the classifier must return HIGH severity,
     * as the transition cannot be validated against the state machine.</p>
     */
    @Property(tries = 50)
    void nullToStatusIsFlagged(
            @ForAll("allStatusValues") String fromStatus) {
        TransitionSeverity severity = classifyTransition(fromStatus, null);
        assertEquals(TransitionSeverity.HIGH, severity,
                "Null to-status should be flagged as HIGH (from: " + fromStatus + ")");
    }

    /**
     * <b>Validates: Requirement 8.3</b>
     *
     * <p>HIGH severity (invalid transition detected) is strictly more severe than SAFE.
     * The ordinal of HIGH must be less than the ordinal of SAFE, ensuring the severity
     * ordering is consistent throughout the audit engine.</p>
     */
    @Property(tries = 100)
    void severityOrderingIsConsistent(
            @ForAll("invalidTransitionPairs") Tuple.Tuple2<String, String> invalidPair,
            @ForAll("validTransitionPairs")   Tuple.Tuple2<String, String> validPair) {

        TransitionSeverity highSeverity = classifyTransition(invalidPair.get1(), invalidPair.get2());
        TransitionSeverity safeSeverity = classifyTransition(validPair.get1(), validPair.get2());

        assertEquals(TransitionSeverity.HIGH, highSeverity,
                "Invalid pair should produce HIGH: " + invalidPair.get1() + " → " + invalidPair.get2());
        assertEquals(TransitionSeverity.SAFE, safeSeverity,
                "Valid pair should produce SAFE: " + validPair.get1() + " → " + validPair.get2());

        assertTrue(highSeverity.ordinal() < safeSeverity.ordinal(),
                "HIGH ordinal should be less than SAFE ordinal (HIGH is more severe)");
    }
}
