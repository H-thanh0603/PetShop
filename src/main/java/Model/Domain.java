package Model;

/**
 * Audit domain — the category of quality concern a finding belongs to.
 *
 * <p><b>Validates: Requirement 15.1</b></p>
 */
public enum Domain {

    /** Business-logic errors, race conditions, and data-integrity violations. */
    LOGIC,

    /** Security vulnerabilities: injection, auth bypass, CSRF, session management, XSS. */
    SECURITY,

    /** Scalability risks: connection leaks, N+1 queries, resource waste, API timeouts. */
    SCALABILITY,

    /** User-experience issues: error handling, user feedback, accessibility. */
    UX
}
