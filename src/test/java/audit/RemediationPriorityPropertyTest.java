package audit;

import net.jqwik.api.*;
import services.SeverityMatrix;
import services.SeverityMatrix.Impact;
import services.SeverityMatrix.Likelihood;
import Model.Priority;
import Model.Severity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Remediation Priority Assignment.
 *
 * <p><b>Property 15: Remediation Priority Assignment</b></p>
 *
 * <p>For any finding that involves data exposure and is user-facing, the
 * {@link SeverityMatrix#assignPriority} method SHALL assign
 * {@link Priority#P0_IMMEDIATE} regardless of other attributes.</p>
 *
 * <p>Additionally, the standard severity-to-priority mapping is verified:</p>
 * <ul>
 *   <li>CRITICAL → P0_IMMEDIATE</li>
 *   <li>HIGH     → P1_NEXT_SPRINT</li>
 *   <li>MEDIUM   → P2_BACKLOG</li>
 *   <li>LOW / INFO → P3_NICE_TO_HAVE</li>
 * </ul>
 *
 * <p><b>Validates: Requirement 14.5</b></p>
 */
class RemediationPriorityPropertyTest {

    // ── Generators ────────────────────────────────────────────────────────────

    @Provide
    Arbitrary<Severity> anySeverity() {
        return Arbitraries.of(Severity.values());
    }

    @Provide
    Arbitrary<Impact> anyImpact() {
        return Arbitraries.of(Impact.values());
    }

    @Provide
    Arbitrary<Likelihood> anyLikelihood() {
        return Arbitraries.of(Likelihood.values());
    }

    // ── Special rule: data exposure + user-facing → P0_IMMEDIATE ─────────────

    /**
     * <b>Validates: Requirement 14.5</b>
     *
     * <p>When a finding involves data exposure AND is user-facing, the priority
     * MUST be P0_IMMEDIATE regardless of the severity level.</p>
     */
    @Property(tries = 100)
    void dataExposureAndUserFacingAlwaysYieldsP0Immediate(
            @ForAll("anySeverity") Severity severity) {
        Priority priority = SeverityMatrix.assignPriority(severity,
                /* dataExposure= */ true,
                /* userFacing=   */ true);
        assertEquals(Priority.P0_IMMEDIATE, priority,
                "dataExposure=true + userFacing=true must always yield P0_IMMEDIATE, "
                        + "but got " + priority + " for severity=" + severity);
    }

    /**
     * <b>Validates: Requirement 14.5</b>
     *
     * <p>The special P0 rule applies even for LOW and INFO severity findings
     * when both data-exposure and user-facing flags are set.</p>
     */
    @Property(tries = 10)
    void dataExposureAndUserFacingOverridesLowSeverity() {
        Priority priorityLow  = SeverityMatrix.assignPriority(Severity.LOW,  true, true);
        Priority priorityInfo = SeverityMatrix.assignPriority(Severity.INFO, true, true);
        assertEquals(Priority.P0_IMMEDIATE, priorityLow,
                "LOW severity with dataExposure+userFacing must be P0_IMMEDIATE");
        assertEquals(Priority.P0_IMMEDIATE, priorityInfo,
                "INFO severity with dataExposure+userFacing must be P0_IMMEDIATE");
    }

    // ── Standard severity-to-priority mapping ─────────────────────────────────

    /**
     * <b>Validates: Requirement 14.5 (standard path)</b>
     *
     * <p>CRITICAL severity without the special override yields P0_IMMEDIATE.</p>
     */
    @Property(tries = 10)
    void criticalSeverityYieldsP0Immediate() {
        // dataExposure=false, userFacing=false — standard path
        Priority p = SeverityMatrix.assignPriority(Severity.CRITICAL, false, false);
        assertEquals(Priority.P0_IMMEDIATE, p,
                "CRITICAL must yield P0_IMMEDIATE on the standard path");
    }

    /**
     * <b>Validates: Requirement 14.5 (standard path)</b>
     *
     * <p>HIGH severity without the special override yields P1_NEXT_SPRINT.</p>
     */
    @Property(tries = 10)
    void highSeverityYieldsP1NextSprint() {
        Priority p = SeverityMatrix.assignPriority(Severity.HIGH, false, false);
        assertEquals(Priority.P1_NEXT_SPRINT, p,
                "HIGH must yield P1_NEXT_SPRINT on the standard path");
    }

    /**
     * <b>Validates: Requirement 14.5 (standard path)</b>
     *
     * <p>MEDIUM severity without the special override yields P2_BACKLOG.</p>
     */
    @Property(tries = 10)
    void mediumSeverityYieldsP2Backlog() {
        Priority p = SeverityMatrix.assignPriority(Severity.MEDIUM, false, false);
        assertEquals(Priority.P2_BACKLOG, p,
                "MEDIUM must yield P2_BACKLOG on the standard path");
    }

    /**
     * <b>Validates: Requirement 14.5 (standard path)</b>
     *
     * <p>LOW severity without the special override yields P3_NICE_TO_HAVE.</p>
     */
    @Property(tries = 10)
    void lowSeverityYieldsP3NiceToHave() {
        Priority p = SeverityMatrix.assignPriority(Severity.LOW, false, false);
        assertEquals(Priority.P3_NICE_TO_HAVE, p,
                "LOW must yield P3_NICE_TO_HAVE on the standard path");
    }

    /**
     * <b>Validates: Requirement 14.5 (standard path)</b>
     *
     * <p>INFO severity without the special override yields P3_NICE_TO_HAVE.</p>
     */
    @Property(tries = 10)
    void infoSeverityYieldsP3NiceToHave() {
        Priority p = SeverityMatrix.assignPriority(Severity.INFO, false, false);
        assertEquals(Priority.P3_NICE_TO_HAVE, p,
                "INFO must yield P3_NICE_TO_HAVE on the standard path");
    }

    // ── Partial flag combinations ─────────────────────────────────────────────

    /**
     * <b>Validates: Requirement 14.5</b>
     *
     * <p>Data exposure alone (without user-facing) does NOT trigger the P0 override;
     * the standard severity mapping applies.</p>
     */
    @Property(tries = 100)
    void dataExposureAloneDoesNotOverridePriority(
            @ForAll("anySeverity") Severity severity) {
        Priority withOverride    = SeverityMatrix.assignPriority(severity, true, true);
        Priority withoutOverride = SeverityMatrix.assignPriority(severity, true, false);

        // Only CRITICAL should still be P0 on the standard path
        if (severity == Severity.CRITICAL) {
            assertEquals(Priority.P0_IMMEDIATE, withoutOverride,
                    "CRITICAL with dataExposure=true, userFacing=false must still be P0");
        } else {
            // For non-CRITICAL, the standard path should NOT be P0
            assertNotEquals(Priority.P0_IMMEDIATE, withoutOverride,
                    "Non-CRITICAL with dataExposure=true, userFacing=false must not be P0. "
                            + "severity=" + severity);
        }
        // The override path is always P0
        assertEquals(Priority.P0_IMMEDIATE, withOverride,
                "dataExposure=true + userFacing=true must always be P0");
    }

    /**
     * <b>Validates: Requirement 14.5</b>
     *
     * <p>User-facing alone (without data exposure) does NOT trigger the P0 override;
     * the standard severity mapping applies.</p>
     */
    @Property(tries = 100)
    void userFacingAloneDoesNotOverridePriority(
            @ForAll("anySeverity") Severity severity) {
        Priority withoutOverride = SeverityMatrix.assignPriority(severity, false, true);

        if (severity == Severity.CRITICAL) {
            assertEquals(Priority.P0_IMMEDIATE, withoutOverride,
                    "CRITICAL with userFacing=true, dataExposure=false must still be P0");
        } else {
            assertNotEquals(Priority.P0_IMMEDIATE, withoutOverride,
                    "Non-CRITICAL with userFacing=true, dataExposure=false must not be P0. "
                            + "severity=" + severity);
        }
    }

    // ── Convenience overload ──────────────────────────────────────────────────

    /**
     * <b>Validates: Requirement 14.5</b>
     *
     * <p>The convenience overload {@code assignPriority(Impact, Likelihood, boolean, boolean)}
     * produces the same result as calling {@code calculate} then {@code assignPriority}.</p>
     */
    @Property(tries = 100)
    void convenienceOverloadMatchesExplicitCalculation(
            @ForAll("anyImpact") Impact impact,
            @ForAll("anyLikelihood") Likelihood likelihood) {
        boolean dataExposure = true;
        boolean userFacing   = true;

        Severity  severity  = SeverityMatrix.calculate(impact, likelihood);
        Priority  expected  = SeverityMatrix.assignPriority(severity, dataExposure, userFacing);
        Priority  actual    = SeverityMatrix.assignPriority(impact, likelihood, dataExposure, userFacing);

        assertEquals(expected, actual,
                "Convenience overload must match explicit calculate+assignPriority for "
                        + impact + " × " + likelihood);
    }

    // ── Null-safety ───────────────────────────────────────────────────────────

    /**
     * <b>Validates: Requirement 14.5</b>
     *
     * <p>Passing {@code null} as severity must throw {@link NullPointerException}.</p>
     */
    @Property(tries = 10)
    void nullSeverityThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> SeverityMatrix.assignPriority((Severity) null, false, false),
                "null severity must throw NullPointerException");
    }

    /**
     * <b>Validates: Requirement 14.5</b>
     *
     * <p>The result is never {@code null} for any valid input combination.</p>
     */
    @Property(tries = 100)
    void assignPriorityNeverReturnsNull(
            @ForAll("anySeverity") Severity severity) {
        Priority p1 = SeverityMatrix.assignPriority(severity, false, false);
        Priority p2 = SeverityMatrix.assignPriority(severity, true,  false);
        Priority p3 = SeverityMatrix.assignPriority(severity, false, true);
        Priority p4 = SeverityMatrix.assignPriority(severity, true,  true);

        assertNotNull(p1, "assignPriority must not return null (false, false)");
        assertNotNull(p2, "assignPriority must not return null (true, false)");
        assertNotNull(p3, "assignPriority must not return null (false, true)");
        assertNotNull(p4, "assignPriority must not return null (true, true)");
    }
}
