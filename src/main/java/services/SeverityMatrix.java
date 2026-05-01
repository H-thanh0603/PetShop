package services;

import Model.Priority;
import Model.Severity;

/**
 * Computes audit finding severity and remediation priority from impact and likelihood.
 *
 * <h2>Severity Matrix (Impact × Likelihood → Severity)</h2>
 * <pre>
 *              │  HIGH      │  MEDIUM    │  LOW
 * ─────────────┼────────────┼────────────┼──────────
 * HIGH impact  │  CRITICAL  │  HIGH      │  HIGH
 * MEDIUM impact│  HIGH      │  MEDIUM    │  LOW
 * LOW impact   │  LOW       │  LOW       │  LOW
 * </pre>
 *
 * <h2>Remediation Priority Rules</h2>
 * <ol>
 *   <li>If a finding involves <em>data exposure</em> AND is <em>user-facing</em>
 *       → {@link Priority#P0_IMMEDIATE} (regardless of severity)</li>
 *   <li>{@link Severity#CRITICAL} → {@link Priority#P0_IMMEDIATE}</li>
 *   <li>{@link Severity#HIGH}     → {@link Priority#P1_NEXT_SPRINT}</li>
 *   <li>{@link Severity#MEDIUM}   → {@link Priority#P2_BACKLOG}</li>
 *   <li>{@link Severity#LOW} / {@link Severity#INFO} → {@link Priority#P3_NICE_TO_HAVE}</li>
 * </ol>
 *
 * <p><b>Validates: Requirements 14.1 – 14.5 — Properties 14 &amp; 15</b></p>
 */
public final class SeverityMatrix {

    // ── Impact levels ─────────────────────────────────────────────────────────

    /**
     * The potential impact of a finding if exploited or left unaddressed.
     */
    public enum Impact {
        /** Significant harm: data breach, financial loss, full system compromise. */
        HIGH,
        /** Moderate harm: partial data exposure, degraded functionality. */
        MEDIUM,
        /** Minimal harm: cosmetic issues, minor UX degradation. */
        LOW
    }

    // ── Likelihood levels ─────────────────────────────────────────────────────

    /**
     * The likelihood that the issue will be triggered or exploited.
     */
    public enum Likelihood {
        /** Easily triggered or exploited with minimal effort. */
        HIGH,
        /** Requires some effort or specific conditions to trigger. */
        MEDIUM,
        /** Unlikely to be triggered under normal circumstances. */
        LOW
    }

    // ── Private constructor — utility class ───────────────────────────────────

    private SeverityMatrix() {
        throw new UnsupportedOperationException("SeverityMatrix is a utility class");
    }

    // ── Severity calculation ──────────────────────────────────────────────────

    /**
     * Computes the {@link Model.Severity} for a finding given its impact and likelihood.
     *
     * <p>The matrix is defined as follows:</p>
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
     * @param impact      the potential impact of the finding (must not be {@code null})
     * @param likelihood  the likelihood of the finding being triggered (must not be {@code null})
     * @return the computed {@link Model.Severity}
     * @throws NullPointerException if either argument is {@code null}
     */
    public static Severity calculate(Impact impact, Likelihood likelihood) {
        if (impact == null) throw new NullPointerException("impact must not be null");
        if (likelihood == null) throw new NullPointerException("likelihood must not be null");

        // LOW impact always yields LOW regardless of likelihood (Requirement 14.4)
        if (impact == Impact.LOW) {
            return Severity.LOW;
        }

        // HIGH impact + HIGH likelihood = CRITICAL (Requirement 14.1)
        if (impact == Impact.HIGH && likelihood == Likelihood.HIGH) {
            return Severity.CRITICAL;
        }

        // Either HIGH impact or HIGH likelihood (but not both HIGH×HIGH) = HIGH (Requirement 14.2)
        if (impact == Impact.HIGH || likelihood == Likelihood.HIGH) {
            return Severity.HIGH;
        }

        // MEDIUM × MEDIUM = MEDIUM (Requirement 14.3)
        if (impact == Impact.MEDIUM && likelihood == Likelihood.MEDIUM) {
            return Severity.MEDIUM;
        }

        // MEDIUM × LOW = LOW
        return Severity.LOW;
    }

    // ── Priority assignment ───────────────────────────────────────────────────

    /**
     * Assigns a {@link Priority} to a finding based on its severity and contextual flags.
     *
     * <p>The special rule (Requirement 14.5 / Property 15): if the finding involves
     * <em>data exposure</em> AND is <em>user-facing</em>, the priority is always
     * {@link Priority#P0_IMMEDIATE} regardless of the computed severity.</p>
     *
     * @param severity      the finding's severity (must not be {@code null})
     * @param dataExposure  {@code true} if the finding involves exposure of sensitive data
     * @param userFacing    {@code true} if the finding directly affects end users
     * @return the assigned {@link Priority}
     * @throws NullPointerException if {@code severity} is {@code null}
     */
    public static Priority assignPriority(Severity severity,
                                          boolean dataExposure,
                                          boolean userFacing) {
        if (severity == null) throw new NullPointerException("severity must not be null");

        // Requirement 14.5: data exposure + user-facing → P0_IMMEDIATE
        if (dataExposure && userFacing) {
            return Priority.P0_IMMEDIATE;
        }

        return switch (severity) {
            case CRITICAL -> Priority.P0_IMMEDIATE;
            case HIGH     -> Priority.P1_NEXT_SPRINT;
            case MEDIUM   -> Priority.P2_BACKLOG;
            case LOW, INFO -> Priority.P3_NICE_TO_HAVE;
        };
    }

    /**
     * Convenience overload: computes severity from impact/likelihood and then assigns priority.
     *
     * @param impact        the potential impact of the finding
     * @param likelihood    the likelihood of the finding being triggered
     * @param dataExposure  {@code true} if the finding involves exposure of sensitive data
     * @param userFacing    {@code true} if the finding directly affects end users
     * @return the assigned {@link Priority}
     */
    public static Priority assignPriority(Impact impact,
                                          Likelihood likelihood,
                                          boolean dataExposure,
                                          boolean userFacing) {
        Severity severity = calculate(impact, likelihood);
        return assignPriority(severity, dataExposure, userFacing);
    }
}
