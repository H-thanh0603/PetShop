package Model;

import java.util.Objects;

/**
 * Represents a single finding produced by the Audit Engine.
 *
 * <p>Every finding carries a unique identifier, severity classification, domain,
 * human-readable title and description, the exact source location (file + line),
 * actionable remediation guidance, and a remediation priority.</p>
 *
 * <p>Use {@link Builder} to construct instances; all required fields are validated
 * at build time so that no partially-populated finding can escape into the system.</p>
 *
 * <p><b>Validates: Requirement 15.1 — Property 16: Finding Structural Completeness</b></p>
 */
public final class AuditFinding {

    // ── Required fields ───────────────────────────────────────────────────────

    /** Unique finding identifier, e.g. {@code "SEC-001"} or {@code "LOGIC-003"}. */
    private final String id;

    /** Severity of the finding as computed by the {@link services.SeverityMatrix}. */
    private final Severity severity;

    /** Audit domain this finding belongs to. */
    private final Domain domain;

    /** Short, human-readable title (one line). */
    private final String title;

    /** Detailed description of the issue, including context and evidence. */
    private final String description;

    /** Relative path of the source file where the issue was detected. */
    private final String affectedFile;

    /**
     * Line number within {@link #affectedFile} where the issue was detected.
     * Use {@code 0} when the issue spans the entire file or the line is unknown.
     */
    private final int affectedLine;

    /** Actionable remediation steps a developer can follow to fix the issue. */
    private final String remediation;

    /** Remediation priority derived from severity, data-exposure, and user-facing flags. */
    private final Priority remediationPriority;

    // ── Constructor (private — use Builder) ───────────────────────────────────

    private AuditFinding(Builder builder) {
        this.id                  = builder.id;
        this.severity            = builder.severity;
        this.domain              = builder.domain;
        this.title               = builder.title;
        this.description         = builder.description;
        this.affectedFile        = builder.affectedFile;
        this.affectedLine        = builder.affectedLine;
        this.remediation         = builder.remediation;
        this.remediationPriority = builder.remediationPriority;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** @return unique finding identifier, never {@code null} or blank */
    public String getId() { return id; }

    /** @return severity level, never {@code null} */
    public Severity getSeverity() { return severity; }

    /** @return audit domain, never {@code null} */
    public Domain getDomain() { return domain; }

    /** @return short title, never {@code null} or blank */
    public String getTitle() { return title; }

    /** @return detailed description, never {@code null} or blank */
    public String getDescription() { return description; }

    /** @return relative path of the affected source file, never {@code null} or blank */
    public String getAffectedFile() { return affectedFile; }

    /** @return line number of the issue (0 = unknown / whole-file) */
    public int getAffectedLine() { return affectedLine; }

    /** @return actionable remediation guidance, never {@code null} or blank */
    public String getRemediation() { return remediation; }

    /** @return remediation priority, never {@code null} */
    public Priority getRemediationPriority() { return remediationPriority; }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditFinding other)) return false;
        return affectedLine == other.affectedLine
                && Objects.equals(id, other.id)
                && severity == other.severity
                && domain == other.domain
                && Objects.equals(title, other.title)
                && Objects.equals(description, other.description)
                && Objects.equals(affectedFile, other.affectedFile)
                && Objects.equals(remediation, other.remediation)
                && remediationPriority == other.remediationPriority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, severity, domain, title, description,
                affectedFile, affectedLine, remediation, remediationPriority);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s/%s) @ %s:%d — priority=%s",
                id, title, severity, domain, affectedFile, affectedLine, remediationPriority);
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    /**
     * Fluent builder for {@link AuditFinding}.
     *
     * <p>All fields except {@link #affectedLine} are required and must be non-null
     * and non-blank. {@link #affectedLine} defaults to {@code 0} (unknown).</p>
     *
     * <pre>{@code
     * AuditFinding finding = new AuditFinding.Builder()
     *     .id("SEC-001")
     *     .severity(Severity.CRITICAL)
     *     .domain(Domain.SECURITY)
     *     .title("SQL Injection in UserDAO.findByEmail()")
     *     .description("The email parameter is concatenated directly into the SQL query.")
     *     .affectedFile("src/main/java/DAO/UserDAO.java")
     *     .affectedLine(42)
     *     .remediation("Use a PreparedStatement with a ? placeholder for the email parameter.")
     *     .remediationPriority(Priority.P0_IMMEDIATE)
     *     .build();
     * }</pre>
     */
    public static final class Builder {

        private String   id;
        private Severity severity;
        private Domain   domain;
        private String   title;
        private String   description;
        private String   affectedFile;
        private int      affectedLine = 0;
        private String   remediation;
        private Priority remediationPriority;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder domain(Domain domain) {
            this.domain = domain;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder affectedFile(String affectedFile) {
            this.affectedFile = affectedFile;
            return this;
        }

        public Builder affectedLine(int affectedLine) {
            this.affectedLine = affectedLine;
            return this;
        }

        public Builder remediation(String remediation) {
            this.remediation = remediation;
            return this;
        }

        public Builder remediationPriority(Priority remediationPriority) {
            this.remediationPriority = remediationPriority;
            return this;
        }

        /**
         * Builds the {@link AuditFinding}, validating that all required fields are present.
         *
         * @throws IllegalStateException if any required field is null or blank
         */
        public AuditFinding build() {
            requireNonBlank(id,                  "id");
            requireNonNull(severity,             "severity");
            requireNonNull(domain,               "domain");
            requireNonBlank(title,               "title");
            requireNonBlank(description,         "description");
            requireNonBlank(affectedFile,        "affectedFile");
            requireNonBlank(remediation,         "remediation");
            requireNonNull(remediationPriority,  "remediationPriority");
            return new AuditFinding(this);
        }

        private static void requireNonNull(Object value, String fieldName) {
            if (value == null) {
                throw new IllegalStateException("AuditFinding." + fieldName + " must not be null");
            }
        }

        private static void requireNonBlank(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                throw new IllegalStateException(
                        "AuditFinding." + fieldName + " must not be null or blank");
            }
        }
    }
}
