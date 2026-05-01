package Model;

/**
 * Severity levels for audit findings, ordered from most to least severe.
 *
 * <p>The ordinal order (CRITICAL=0 … INFO=4) is intentional: lower ordinal means
 * higher severity, which allows natural sorting with {@code Comparator.naturalOrder()}.</p>
 *
 * <p><b>Validates: Requirements 14.1 – 14.4</b></p>
 */
public enum Severity {

    /** Both impact and likelihood are HIGH. Requires immediate remediation. */
    CRITICAL,

    /** Either impact or likelihood is HIGH (but not both). */
    HIGH,

    /** Both impact and likelihood are MEDIUM. */
    MEDIUM,

    /** Impact is LOW regardless of likelihood. */
    LOW,

    /** Informational observation — no direct risk. */
    INFO
}
