package Model;

/**
 * Remediation priority for an audit finding.
 *
 * <p>Priority is derived from the finding's severity combined with whether it
 * involves data exposure and/or is user-facing (see {@link services.SeverityMatrix}).</p>
 *
 * <p><b>Validates: Requirements 14.5, 15.1</b></p>
 */
public enum Priority {

    /** Fix immediately — typically CRITICAL findings with data exposure and user-facing impact. */
    P0_IMMEDIATE,

    /** Fix in the next sprint — HIGH severity findings. */
    P1_NEXT_SPRINT,

    /** Add to the backlog — MEDIUM severity findings. */
    P2_BACKLOG,

    /** Nice-to-have — LOW or INFO severity findings. */
    P3_NICE_TO_HAVE
}
