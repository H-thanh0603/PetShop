package audit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotBlank;
import Model.AuditFinding;
import Model.Domain;
import Model.Priority;
import Model.Severity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Finding Structural Completeness.
 *
 * <p><b>Property 16: Finding Structural Completeness</b></p>
 *
 * <p>For any finding produced by the Audit Engine, the finding SHALL have a
 * non-empty unique ID, severity, domain, title, description, affected file path,
 * affected line number, and remediation guidance.</p>
 *
 * <p>This test verifies:</p>
 * <ol>
 *   <li>Valid findings built via {@link AuditFinding.Builder} satisfy all structural
 *       completeness requirements.</li>
 *   <li>The builder rejects findings with any required field missing or blank.</li>
 *   <li>Accessor methods return exactly the values supplied to the builder.</li>
 *   <li>Equality and hash-code contracts hold.</li>
 * </ol>
 *
 * <p><b>Validates: Requirement 15.1</b></p>
 */
class FindingStructuralCompletenessPropertyTest {

    // ── Generators ────────────────────────────────────────────────────────────

    @Provide
    Arbitrary<Severity> anySeverity() {
        return Arbitraries.of(Severity.values());
    }

    @Provide
    Arbitrary<Domain> anyDomain() {
        return Arbitraries.of(Domain.values());
    }

    @Provide
    Arbitrary<Priority> anyPriority() {
        return Arbitraries.of(Priority.values());
    }

    /** Representative finding IDs in the format used by the audit engine. */
    @Provide
    Arbitrary<String> validFindingIds() {
        return Arbitraries.of(
                "SEC-001", "SEC-042", "LOGIC-007", "SCALE-003",
                "UX-015", "SEC-100", "LOGIC-001", "SCALE-099"
        );
    }

    /** Representative source file paths. */
    @Provide
    Arbitrary<String> validFilePaths() {
        return Arbitraries.of(
                "src/main/java/DAO/UserDAO.java",
                "src/main/java/controller/shop/CheckoutServlet.java",
                "src/main/java/controller/filter/CsrfFilter.java",
                "src/main/java/services/ShippingService.java",
                "src/main/webapp/pages/shop/shop.jsp",
                "src/main/java/DAO/OrderDAO.java",
                "src/main/java/controller/auth/LoginServlet.java"
        );
    }

    /** Representative finding titles. */
    @Provide
    Arbitrary<String> validTitles() {
        return Arbitraries.of(
                "SQL Injection in UserDAO.findByEmail()",
                "Missing CSRF token validation on checkout endpoint",
                "Cookie missing Secure flag in LoginServlet",
                "N+1 query in OrderDAO.getOrdersByUserId()",
                "Floating-point monetary calculation in CheckoutServlet",
                "Missing session regeneration after login",
                "Unclosed database connection in ProductDAO"
        );
    }

    /** Representative remediation strings. */
    @Provide
    Arbitrary<String> validRemediations() {
        return Arbitraries.of(
                "Use a PreparedStatement with ? placeholders instead of string concatenation.",
                "Remove the blanket Accept: application/json CSRF bypass and require tokens.",
                "Add cookie.setSecure(true) to all authentication cookies.",
                "Replace per-order item loading with a single JOIN or batch IN query.",
                "Replace double with BigDecimal for all monetary calculations.",
                "Call request.getSession().invalidate() then request.getSession(true) after login.",
                "Wrap Connection usage in try-with-resources to ensure it is always closed."
        );
    }

    /**
     * Builds a fully valid {@link AuditFinding} from the provided parameters.
     * Used as a helper to avoid repetition across property tests.
     */
    private AuditFinding buildValidFinding(String id, Severity severity, Domain domain,
                                            String title, String description,
                                            String affectedFile, int affectedLine,
                                            String remediation, Priority priority) {
        return new AuditFinding.Builder()
                .id(id)
                .severity(severity)
                .domain(domain)
                .title(title)
                .description(description)
                .affectedFile(affectedFile)
                .affectedLine(affectedLine)
                .remediation(remediation)
                .remediationPriority(priority)
                .build();
    }

    // ── Structural completeness properties ────────────────────────────────────

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>Every successfully built finding has a non-null, non-blank ID.</p>
     */
    @Property(tries = 100)
    void validFindingHasNonBlankId(
            @ForAll("validFindingIds") String id,
            @ForAll("anySeverity") Severity severity,
            @ForAll("anyDomain") Domain domain,
            @ForAll("validTitles") String title,
            @ForAll("validFilePaths") String file,
            @ForAll("anyPriority") Priority priority) {

        AuditFinding finding = buildValidFinding(id, severity, domain, title,
                "A detailed description of the issue.", file, 10, "Fix it.", priority);

        assertNotNull(finding.getId(), "id must not be null");
        assertFalse(finding.getId().isBlank(), "id must not be blank");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>Every successfully built finding has a non-null severity.</p>
     */
    @Property(tries = 100)
    void validFindingHasNonNullSeverity(
            @ForAll("validFindingIds") String id,
            @ForAll("anySeverity") Severity severity,
            @ForAll("anyDomain") Domain domain,
            @ForAll("anyPriority") Priority priority) {

        AuditFinding finding = buildValidFinding(id, severity, domain,
                "Title", "Description.",
                "src/main/java/DAO/UserDAO.java", 0, "Remediation.", priority);

        assertNotNull(finding.getSeverity(), "severity must not be null");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>Every successfully built finding has a non-null domain.</p>
     */
    @Property(tries = 100)
    void validFindingHasNonNullDomain(
            @ForAll("validFindingIds") String id,
            @ForAll("anySeverity") Severity severity,
            @ForAll("anyDomain") Domain domain,
            @ForAll("anyPriority") Priority priority) {

        AuditFinding finding = buildValidFinding(id, severity, domain,
                "Title", "Description.",
                "src/main/java/DAO/UserDAO.java", 0, "Remediation.", priority);

        assertNotNull(finding.getDomain(), "domain must not be null");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>Every successfully built finding has a non-blank title.</p>
     */
    @Property(tries = 100)
    void validFindingHasNonBlankTitle(
            @ForAll("validFindingIds") String id,
            @ForAll("anySeverity") Severity severity,
            @ForAll("anyDomain") Domain domain,
            @ForAll("validTitles") String title,
            @ForAll("anyPriority") Priority priority) {

        AuditFinding finding = buildValidFinding(id, severity, domain, title,
                "Description.", "src/main/java/DAO/UserDAO.java", 0, "Remediation.", priority);

        assertNotNull(finding.getTitle(), "title must not be null");
        assertFalse(finding.getTitle().isBlank(), "title must not be blank");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>Every successfully built finding has a non-blank description.</p>
     */
    @Property(tries = 100)
    void validFindingHasNonBlankDescription(
            @ForAll("validFindingIds") String id,
            @ForAll("anySeverity") Severity severity,
            @ForAll("anyDomain") Domain domain,
            @ForAll("anyPriority") Priority priority) {

        String description = "The email parameter is concatenated directly into the SQL query.";
        AuditFinding finding = buildValidFinding(id, severity, domain,
                "Title", description,
                "src/main/java/DAO/UserDAO.java", 0, "Remediation.", priority);

        assertNotNull(finding.getDescription(), "description must not be null");
        assertFalse(finding.getDescription().isBlank(), "description must not be blank");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>Every successfully built finding has a non-blank affected file path.</p>
     */
    @Property(tries = 100)
    void validFindingHasNonBlankAffectedFile(
            @ForAll("validFindingIds") String id,
            @ForAll("anySeverity") Severity severity,
            @ForAll("anyDomain") Domain domain,
            @ForAll("validFilePaths") String filePath,
            @ForAll("anyPriority") Priority priority) {

        AuditFinding finding = buildValidFinding(id, severity, domain,
                "Title", "Description.", filePath, 0, "Remediation.", priority);

        assertNotNull(finding.getAffectedFile(), "affectedFile must not be null");
        assertFalse(finding.getAffectedFile().isBlank(), "affectedFile must not be blank");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>Every successfully built finding has a non-blank remediation string.</p>
     */
    @Property(tries = 100)
    void validFindingHasNonBlankRemediation(
            @ForAll("validFindingIds") String id,
            @ForAll("anySeverity") Severity severity,
            @ForAll("anyDomain") Domain domain,
            @ForAll("validRemediations") String remediation,
            @ForAll("anyPriority") Priority priority) {

        AuditFinding finding = buildValidFinding(id, severity, domain,
                "Title", "Description.",
                "src/main/java/DAO/UserDAO.java", 0, remediation, priority);

        assertNotNull(finding.getRemediation(), "remediation must not be null");
        assertFalse(finding.getRemediation().isBlank(), "remediation must not be blank");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>Every successfully built finding has a non-null remediation priority.</p>
     */
    @Property(tries = 100)
    void validFindingHasNonNullRemediationPriority(
            @ForAll("validFindingIds") String id,
            @ForAll("anySeverity") Severity severity,
            @ForAll("anyDomain") Domain domain,
            @ForAll("anyPriority") Priority priority) {

        AuditFinding finding = buildValidFinding(id, severity, domain,
                "Title", "Description.",
                "src/main/java/DAO/UserDAO.java", 0, "Remediation.", priority);

        assertNotNull(finding.getRemediationPriority(), "remediationPriority must not be null");
    }

    // ── Builder validation: reject incomplete findings ─────────────────────────

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>The builder must reject a finding with a null or blank ID.</p>
     */
    @Property(tries = 10)
    void builderRejectsNullId() {
        assertThrows(IllegalStateException.class, () ->
                new AuditFinding.Builder()
                        .id(null)
                        .severity(Severity.HIGH)
                        .domain(Domain.SECURITY)
                        .title("Title")
                        .description("Description.")
                        .affectedFile("src/main/java/DAO/UserDAO.java")
                        .affectedLine(1)
                        .remediation("Fix it.")
                        .remediationPriority(Priority.P1_NEXT_SPRINT)
                        .build(),
                "Builder must reject null id");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>The builder must reject a finding with a blank ID.</p>
     */
    @Property(tries = 10)
    void builderRejectsBlankId() {
        assertThrows(IllegalStateException.class, () ->
                new AuditFinding.Builder()
                        .id("   ")
                        .severity(Severity.HIGH)
                        .domain(Domain.SECURITY)
                        .title("Title")
                        .description("Description.")
                        .affectedFile("src/main/java/DAO/UserDAO.java")
                        .affectedLine(1)
                        .remediation("Fix it.")
                        .remediationPriority(Priority.P1_NEXT_SPRINT)
                        .build(),
                "Builder must reject blank id");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>The builder must reject a finding with a null severity.</p>
     */
    @Property(tries = 10)
    void builderRejectsNullSeverity() {
        assertThrows(IllegalStateException.class, () ->
                new AuditFinding.Builder()
                        .id("SEC-001")
                        .severity(null)
                        .domain(Domain.SECURITY)
                        .title("Title")
                        .description("Description.")
                        .affectedFile("src/main/java/DAO/UserDAO.java")
                        .affectedLine(1)
                        .remediation("Fix it.")
                        .remediationPriority(Priority.P1_NEXT_SPRINT)
                        .build(),
                "Builder must reject null severity");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>The builder must reject a finding with a null domain.</p>
     */
    @Property(tries = 10)
    void builderRejectsNullDomain() {
        assertThrows(IllegalStateException.class, () ->
                new AuditFinding.Builder()
                        .id("SEC-001")
                        .severity(Severity.HIGH)
                        .domain(null)
                        .title("Title")
                        .description("Description.")
                        .affectedFile("src/main/java/DAO/UserDAO.java")
                        .affectedLine(1)
                        .remediation("Fix it.")
                        .remediationPriority(Priority.P1_NEXT_SPRINT)
                        .build(),
                "Builder must reject null domain");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>The builder must reject a finding with a blank title.</p>
     */
    @Property(tries = 10)
    void builderRejectsBlankTitle() {
        assertThrows(IllegalStateException.class, () ->
                new AuditFinding.Builder()
                        .id("SEC-001")
                        .severity(Severity.HIGH)
                        .domain(Domain.SECURITY)
                        .title("")
                        .description("Description.")
                        .affectedFile("src/main/java/DAO/UserDAO.java")
                        .affectedLine(1)
                        .remediation("Fix it.")
                        .remediationPriority(Priority.P1_NEXT_SPRINT)
                        .build(),
                "Builder must reject blank title");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>The builder must reject a finding with a blank description.</p>
     */
    @Property(tries = 10)
    void builderRejectsBlankDescription() {
        assertThrows(IllegalStateException.class, () ->
                new AuditFinding.Builder()
                        .id("SEC-001")
                        .severity(Severity.HIGH)
                        .domain(Domain.SECURITY)
                        .title("Title")
                        .description("  ")
                        .affectedFile("src/main/java/DAO/UserDAO.java")
                        .affectedLine(1)
                        .remediation("Fix it.")
                        .remediationPriority(Priority.P1_NEXT_SPRINT)
                        .build(),
                "Builder must reject blank description");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>The builder must reject a finding with a blank affected file path.</p>
     */
    @Property(tries = 10)
    void builderRejectsBlankAffectedFile() {
        assertThrows(IllegalStateException.class, () ->
                new AuditFinding.Builder()
                        .id("SEC-001")
                        .severity(Severity.HIGH)
                        .domain(Domain.SECURITY)
                        .title("Title")
                        .description("Description.")
                        .affectedFile("")
                        .affectedLine(1)
                        .remediation("Fix it.")
                        .remediationPriority(Priority.P1_NEXT_SPRINT)
                        .build(),
                "Builder must reject blank affectedFile");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>The builder must reject a finding with a blank remediation string.</p>
     */
    @Property(tries = 10)
    void builderRejectsBlankRemediation() {
        assertThrows(IllegalStateException.class, () ->
                new AuditFinding.Builder()
                        .id("SEC-001")
                        .severity(Severity.HIGH)
                        .domain(Domain.SECURITY)
                        .title("Title")
                        .description("Description.")
                        .affectedFile("src/main/java/DAO/UserDAO.java")
                        .affectedLine(1)
                        .remediation("")
                        .remediationPriority(Priority.P1_NEXT_SPRINT)
                        .build(),
                "Builder must reject blank remediation");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>The builder must reject a finding with a null remediation priority.</p>
     */
    @Property(tries = 10)
    void builderRejectsNullRemediationPriority() {
        assertThrows(IllegalStateException.class, () ->
                new AuditFinding.Builder()
                        .id("SEC-001")
                        .severity(Severity.HIGH)
                        .domain(Domain.SECURITY)
                        .title("Title")
                        .description("Description.")
                        .affectedFile("src/main/java/DAO/UserDAO.java")
                        .affectedLine(1)
                        .remediation("Fix it.")
                        .remediationPriority(null)
                        .build(),
                "Builder must reject null remediationPriority");
    }

    // ── Accessor round-trip ───────────────────────────────────────────────────

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>Accessor methods return exactly the values supplied to the builder
     * (round-trip property).</p>
     */
    @Property(tries = 100)
    void accessorsReturnExactlyBuilderValues(
            @ForAll("validFindingIds") String id,
            @ForAll("anySeverity") Severity severity,
            @ForAll("anyDomain") Domain domain,
            @ForAll("validTitles") String title,
            @ForAll("validFilePaths") String filePath,
            @ForAll("validRemediations") String remediation,
            @ForAll("anyPriority") Priority priority,
            @ForAll @IntRange(min = 0, max = 5000) int line) {

        String description = "Detailed description of the issue at line " + line + ".";
        AuditFinding finding = buildValidFinding(id, severity, domain, title,
                description, filePath, line, remediation, priority);

        assertAll("accessor round-trip",
                () -> assertEquals(id,          finding.getId(),                  "id mismatch"),
                () -> assertEquals(severity,    finding.getSeverity(),            "severity mismatch"),
                () -> assertEquals(domain,      finding.getDomain(),              "domain mismatch"),
                () -> assertEquals(title,       finding.getTitle(),               "title mismatch"),
                () -> assertEquals(description, finding.getDescription(),         "description mismatch"),
                () -> assertEquals(filePath,    finding.getAffectedFile(),        "affectedFile mismatch"),
                () -> assertEquals(line,        finding.getAffectedLine(),        "affectedLine mismatch"),
                () -> assertEquals(remediation, finding.getRemediation(),         "remediation mismatch"),
                () -> assertEquals(priority,    finding.getRemediationPriority(), "priority mismatch")
        );
    }

    // ── Equality and hash-code ────────────────────────────────────────────────

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>Two findings built with identical parameters are equal and share the same hash code.</p>
     */
    @Property(tries = 50)
    void equalFindingsHaveSameHashCode(
            @ForAll("validFindingIds") String id,
            @ForAll("anySeverity") Severity severity,
            @ForAll("anyDomain") Domain domain,
            @ForAll("anyPriority") Priority priority) {

        AuditFinding f1 = buildValidFinding(id, severity, domain,
                "Title", "Description.", "src/main/java/DAO/UserDAO.java",
                42, "Fix it.", priority);
        AuditFinding f2 = buildValidFinding(id, severity, domain,
                "Title", "Description.", "src/main/java/DAO/UserDAO.java",
                42, "Fix it.", priority);

        assertEquals(f1, f2, "Identical findings must be equal");
        assertEquals(f1.hashCode(), f2.hashCode(),
                "Equal findings must have the same hash code");
    }

    /**
     * <b>Validates: Requirement 15.1</b>
     *
     * <p>A finding is equal to itself (reflexivity).</p>
     */
    @Property(tries = 50)
    void findingIsEqualToItself(
            @ForAll("validFindingIds") String id,
            @ForAll("anySeverity") Severity severity,
            @ForAll("anyDomain") Domain domain,
            @ForAll("anyPriority") Priority priority) {

        AuditFinding finding = buildValidFinding(id, severity, domain,
                "Title", "Description.", "src/main/java/DAO/UserDAO.java",
                1, "Fix it.", priority);

        assertEquals(finding, finding, "A finding must be equal to itself");
    }
}
