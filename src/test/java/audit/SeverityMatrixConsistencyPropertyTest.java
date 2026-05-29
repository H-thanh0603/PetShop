package audit;

import net.jqwik.api.*;
import services.SeverityMatrix;
import services.SeverityMatrix.Impact;
import services.SeverityMatrix.Likelihood;
import Model.Severity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Severity Matrix Consistency.
 *
 * <p><b>Property 14: Severity Matrix Consistency</b></p>
 *
 * <p>For any combination of {@link Impact} (HIGH, MEDIUM, LOW) and
 * {@link Likelihood} (HIGH, MEDIUM, LOW), the {@link SeverityMatrix#calculate}
 * method SHALL produce a severity that matches the defined matrix:</p>
 * <ul>
 *   <li>HIGH × HIGH   → CRITICAL</li>
 *   <li>HIGH × MEDIUM → HIGH</li>
 *   <li>HIGH × LOW    → HIGH</li>
 *   <li>MEDIUM × HIGH → HIGH</li>
 *   <li>MEDIUM × MEDIUM → MEDIUM</li>
 *   <li>MEDIUM × LOW  → LOW</li>
 *   <li>LOW × any     → LOW</li>
 * </ul>
 *
 * <p><b>Validates: Requirements 14.1, 14.2, 14.3, 14.4</b></p>
 */
class SeverityMatrixConsistencyPropertyTest {

    // ── Generators ────────────────────────────────────────────────────────────

    @Provide
    Arbitrary<Impact> anyImpact() {
        return Arbitraries.of(Impact.values());
    }

    @Provide
    Arbitrary<Likelihood> anyLikelihood() {
        return Arbitraries.of(Likelihood.values());
    }

    // ── Exact-cell properties ─────────────────────────────────────────────────

    /**
     * <b>Validates: Requirement 14.1</b>
     *
     * <p>HIGH impact × HIGH likelihood must always yield CRITICAL.</p>
     */
    @Property(tries = 10)
    void highImpactHighLikelihoodIsCritical() {
        Severity result = SeverityMatrix.calculate(Impact.HIGH, Likelihood.HIGH);
        assertEquals(Severity.CRITICAL, result,
                "HIGH × HIGH must be CRITICAL");
    }

    /**
     * <b>Validates: Requirement 14.2</b>
     *
     * <p>HIGH impact × MEDIUM likelihood must yield HIGH.</p>
     */
    @Property(tries = 10)
    void highImpactMediumLikelihoodIsHigh() {
        Severity result = SeverityMatrix.calculate(Impact.HIGH, Likelihood.MEDIUM);
        assertEquals(Severity.HIGH, result,
                "HIGH × MEDIUM must be HIGH");
    }

    /**
     * <b>Validates: Requirement 14.2</b>
     *
     * <p>HIGH impact × LOW likelihood must yield HIGH.</p>
     */
    @Property(tries = 10)
    void highImpactLowLikelihoodIsHigh() {
        Severity result = SeverityMatrix.calculate(Impact.HIGH, Likelihood.LOW);
        assertEquals(Severity.HIGH, result,
                "HIGH × LOW must be HIGH");
    }

    /**
     * <b>Validates: Requirement 14.2</b>
     *
     * <p>MEDIUM impact × HIGH likelihood must yield HIGH.</p>
     */
    @Property(tries = 10)
    void mediumImpactHighLikelihoodIsHigh() {
        Severity result = SeverityMatrix.calculate(Impact.MEDIUM, Likelihood.HIGH);
        assertEquals(Severity.HIGH, result,
                "MEDIUM × HIGH must be HIGH");
    }

    /**
     * <b>Validates: Requirement 14.3</b>
     *
     * <p>MEDIUM impact × MEDIUM likelihood must yield MEDIUM.</p>
     */
    @Property(tries = 10)
    void mediumImpactMediumLikelihoodIsMedium() {
        Severity result = SeverityMatrix.calculate(Impact.MEDIUM, Likelihood.MEDIUM);
        assertEquals(Severity.MEDIUM, result,
                "MEDIUM × MEDIUM must be MEDIUM");
    }

    /**
     * <b>Validates: Requirement 14.4</b>
     *
     * <p>MEDIUM impact × LOW likelihood must yield LOW.</p>
     */
    @Property(tries = 10)
    void mediumImpactLowLikelihoodIsLow() {
        Severity result = SeverityMatrix.calculate(Impact.MEDIUM, Likelihood.LOW);
        assertEquals(Severity.LOW, result,
                "MEDIUM × LOW must be LOW");
    }

    /**
     * <b>Validates: Requirement 14.4</b>
     *
     * <p>LOW impact combined with ANY likelihood must always yield LOW.</p>
     */
    @Property(tries = 50)
    void lowImpactAlwaysYieldsLow(@ForAll("anyLikelihood") Likelihood likelihood) {
        Severity result = SeverityMatrix.calculate(Impact.LOW, likelihood);
        assertEquals(Severity.LOW, result,
                "LOW impact × " + likelihood + " must be LOW");
    }

    // ── Cross-cutting invariant properties ────────────────────────────────────

    /**
     * <b>Validates: Requirements 14.1 – 14.4</b>
     *
     * <p>The result is never {@code null} for any valid Impact × Likelihood combination.</p>
     */
    @Property(tries = 100)
    void calculateNeverReturnsNull(
            @ForAll("anyImpact") Impact impact,
            @ForAll("anyLikelihood") Likelihood likelihood) {
        Severity result = SeverityMatrix.calculate(impact, likelihood);
        assertNotNull(result,
                "calculate(" + impact + ", " + likelihood + ") must not return null");
    }

    /**
     * <b>Validates: Requirements 14.1 – 14.4</b>
     *
     * <p>The result is always one of the defined {@link Severity} values.</p>
     */
    @Property(tries = 100)
    void calculateAlwaysReturnsDefinedSeverity(
            @ForAll("anyImpact") Impact impact,
            @ForAll("anyLikelihood") Likelihood likelihood) {
        Severity result = SeverityMatrix.calculate(impact, likelihood);
        // Verify it is a known enum constant (not INFO — INFO is not produced by the matrix)
        assertTrue(result == Severity.CRITICAL
                        || result == Severity.HIGH
                        || result == Severity.MEDIUM
                        || result == Severity.LOW,
                "calculate(" + impact + ", " + likelihood
                        + ") returned unexpected severity: " + result);
    }

    /**
     * <b>Validates: Requirements 14.1 – 14.4</b>
     *
     * <p>Severity is monotonically non-increasing as impact decreases (holding likelihood fixed).
     * That is: severity(HIGH, L) ≥ severity(MEDIUM, L) ≥ severity(LOW, L) for any L.</p>
     *
     * <p>We compare using ordinal values where lower ordinal = higher severity.</p>
     */
    @Property(tries = 50)
    void severityIsMonotonicallyNonIncreasingAsImpactDecreases(
            @ForAll("anyLikelihood") Likelihood likelihood) {
        Severity highImpact   = SeverityMatrix.calculate(Impact.HIGH,   likelihood);
        Severity mediumImpact = SeverityMatrix.calculate(Impact.MEDIUM, likelihood);
        Severity lowImpact    = SeverityMatrix.calculate(Impact.LOW,    likelihood);

        assertTrue(highImpact.ordinal() <= mediumImpact.ordinal(),
                "severity(HIGH, " + likelihood + ") must be >= severity(MEDIUM, " + likelihood + ")");
        assertTrue(mediumImpact.ordinal() <= lowImpact.ordinal(),
                "severity(MEDIUM, " + likelihood + ") must be >= severity(LOW, " + likelihood + ")");
    }

    /**
     * <b>Validates: Requirements 14.1 – 14.4</b>
     *
     * <p>Severity is monotonically non-increasing as likelihood decreases (holding impact fixed).
     * That is: severity(I, HIGH) ≥ severity(I, MEDIUM) ≥ severity(I, LOW) for any I.</p>
     */
    @Property(tries = 50)
    void severityIsMonotonicallyNonIncreasingAsLikelihoodDecreases(
            @ForAll("anyImpact") Impact impact) {
        Severity highLikelihood   = SeverityMatrix.calculate(impact, Likelihood.HIGH);
        Severity mediumLikelihood = SeverityMatrix.calculate(impact, Likelihood.MEDIUM);
        Severity lowLikelihood    = SeverityMatrix.calculate(impact, Likelihood.LOW);

        assertTrue(highLikelihood.ordinal() <= mediumLikelihood.ordinal(),
                "severity(" + impact + ", HIGH) must be >= severity(" + impact + ", MEDIUM)");
        assertTrue(mediumLikelihood.ordinal() <= lowLikelihood.ordinal(),
                "severity(" + impact + ", MEDIUM) must be >= severity(" + impact + ", LOW)");
    }

    /**
     * <b>Validates: Requirements 14.1 – 14.4</b>
     *
     * <p>CRITICAL is only produced when both impact AND likelihood are HIGH.</p>
     */
    @Property(tries = 100)
    void criticalRequiresBothHighImpactAndHighLikelihood(
            @ForAll("anyImpact") Impact impact,
            @ForAll("anyLikelihood") Likelihood likelihood) {
        Severity result = SeverityMatrix.calculate(impact, likelihood);
        if (result == Severity.CRITICAL) {
            assertEquals(Impact.HIGH, impact,
                    "CRITICAL severity requires HIGH impact, got: " + impact);
            assertEquals(Likelihood.HIGH, likelihood,
                    "CRITICAL severity requires HIGH likelihood, got: " + likelihood);
        }
    }

    /**
     * <b>Validates: Requirement 14.4</b>
     *
     * <p>LOW impact never produces CRITICAL or HIGH severity.</p>
     */
    @Property(tries = 50)
    void lowImpactNeverProducesCriticalOrHigh(
            @ForAll("anyLikelihood") Likelihood likelihood) {
        Severity result = SeverityMatrix.calculate(Impact.LOW, likelihood);
        assertNotEquals(Severity.CRITICAL, result,
                "LOW impact must never produce CRITICAL");
        assertNotEquals(Severity.HIGH, result,
                "LOW impact must never produce HIGH");
    }

    /**
     * <b>Validates: Requirements 14.1 – 14.4</b>
     *
     * <p>Null arguments must throw {@link NullPointerException}.</p>
     */
    @Property(tries = 10)
    void nullImpactThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> SeverityMatrix.calculate(null, Likelihood.HIGH),
                "null impact must throw NullPointerException");
    }

    @Property(tries = 10)
    void nullLikelihoodThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> SeverityMatrix.calculate(Impact.HIGH, null),
                "null likelihood must throw NullPointerException");
    }
}
