package audit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for SQL Injection Detection Classification.
 *
 * <p><b>Property 1: SQL Injection Detection Classifies Severity Correctly</b></p>
 *
 * <p>For any source artifact containing a SQL query, if the query constructs clauses
 * through string concatenation with user-controlled string variables, the classifier
 * SHALL return CRITICAL; if the concatenation involves only integer IDs, it SHALL
 * return MEDIUM; and if the query uses parameterized placeholders, it SHALL return SAFE.</p>
 *
 * <p><b>Validates: Requirements 1.1, 1.2, 1.3</b></p>
 */
class SqlInjectionClassificationPropertyTest {

    // ── Inline severity model (self-contained, no dependency on future Task 17 classes) ──

    enum SqlInjectionSeverity {
        CRITICAL,   // String-concatenated user-controlled input in SQL
        MEDIUM,     // Integer-only ID concatenation in SQL IN clauses
        SAFE        // Parameterized queries using ? placeholders
    }

    /**
     * Classifies a SQL code snippet for injection risk.
     *
     * <ul>
     *   <li>If the snippet contains string concatenation with a user-controlled
     *       string variable in a WHERE/IN/ORDER BY clause → CRITICAL</li>
     *   <li>If the snippet contains concatenation of integer-only IDs
     *       (e.g., in an IN clause) → MEDIUM</li>
     *   <li>If the snippet uses only {@code ?} parameter placeholders → SAFE</li>
     * </ul>
     *
     * <p>The classifier checks for integer-only IN-clause concatenation <em>before</em>
     * general string concatenation because the IN-clause pattern is a strict subset
     * (the variable is wrapped in parentheses, not SQL string quotes).</p>
     */
    static SqlInjectionSeverity classifySqlSnippet(String snippet) {
        if (snippet == null || snippet.isBlank()) {
            return SqlInjectionSeverity.SAFE;
        }

        // ── 1. Detect integer-only ID concatenation in IN clauses (check FIRST) ──
        // Pattern: "WHERE id IN (" + intIds + ")"
        // The variable is surrounded by SQL parentheses, NOT SQL string-literal quotes.
        boolean hasIntegerInConcat = snippet.matches(
                "(?si).*IN\\s*\\(\"\\s*\\+\\s*\\w+\\s*\\+\\s*\"\\).*");

        if (hasIntegerInConcat) {
            return SqlInjectionSeverity.MEDIUM;
        }

        // ── 2. Detect ORDER BY concatenation ──
        // Pattern: "ORDER BY " + sortColumn  (no SQL quotes around the variable)
        boolean hasOrderByConcat = snippet.matches(
                "(?si).*ORDER\\s+BY\\s*\"\\s*\\+\\s*\\w+.*");

        if (hasOrderByConcat) {
            return SqlInjectionSeverity.CRITICAL;
        }

        // ── 3. Detect string concatenation with user-controlled string variables ──
        // Patterns like:
        //   "WHERE name = '" + userInput + "'"
        //   "WHERE col LIKE '%" + param + "%'"
        // The key indicator is SQL string-literal quotes (single-quotes) adjacent
        // to the Java concatenation operator, possibly with wildcard chars (%) in between.
        boolean hasStringConcat = snippet.matches(
                "(?s).*'[%]?\"\\s*\\+\\s*\\w+\\s*\\+\\s*\"[%]?'.*");

        if (hasStringConcat) {
            return SqlInjectionSeverity.CRITICAL;
        }

        // ── 4. Parameterized queries or no concatenation → SAFE ──
        return SqlInjectionSeverity.SAFE;
    }

    // ── Generators ──

    @Provide
    Arbitrary<String> userControlledStringNames() {
        return Arbitraries.of(
                "userInput", "searchKeyword", "userName", "email",
                "queryParam", "filterValue", "sortColumn", "categoryName",
                "description", "fullName", "address", "note"
        );
    }

    @Provide
    Arbitrary<String> sqlClausePrefixes() {
        return Arbitraries.of(
                "SELECT * FROM users WHERE name = '",
                "SELECT * FROM products WHERE description LIKE '%",
                "DELETE FROM orders WHERE address = '",
                "UPDATE users SET email = '",
                "SELECT * FROM reviews WHERE content = '"
        );
    }

    @Provide
    Arbitrary<String> sqlClauseSuffixes() {
        return Arbitraries.of(
                "'",
                "%'",
                "' AND 1=1",
                "' OR ''='"
        );
    }

    @Provide
    Arbitrary<String> integerIdVarNames() {
        return Arbitraries.of(
                "intIds", "orderIds", "productIds", "itemIds",
                "userIds", "ids", "idList"
        );
    }

    @Provide
    Arbitrary<String> stringConcatSnippets() {
        return Combinators.combine(
                sqlClausePrefixes(),
                userControlledStringNames(),
                sqlClauseSuffixes()
        ).as((prefix, varName, suffix) ->
                "\"" + prefix + "\" + " + varName + " + \"" + suffix + "\""
        );
    }

    @Provide
    Arbitrary<String> integerConcatSnippets() {
        return integerIdVarNames().map(varName ->
                "\"SELECT * FROM products WHERE id IN (\" + " + varName + " + \")\""
        );
    }

    @Provide
    Arbitrary<String> parameterizedSnippets() {
        return Arbitraries.of(
                "\"SELECT * FROM users WHERE name = ?\"",
                "\"SELECT * FROM products WHERE id = ? AND category = ?\"",
                "\"INSERT INTO orders (user_id, total) VALUES (?, ?)\"",
                "\"UPDATE users SET email = ? WHERE id = ?\"",
                "\"DELETE FROM cart WHERE user_id = ? AND product_id = ?\"",
                "\"SELECT * FROM orders WHERE status = ? ORDER BY created_at DESC\"",
                "\"SELECT * FROM products WHERE id IN (?, ?, ?)\"",
                "\"SELECT * FROM reviews WHERE product_id = ? LIMIT ? OFFSET ?\""
        );
    }

    @Provide
    Arbitrary<String> orderByConcatSnippets() {
        return userControlledStringNames().map(varName ->
                "\"SELECT * FROM products ORDER BY \" + " + varName
        );
    }

    // ── Property Tests ──

    /**
     * <b>Validates: Requirements 1.1, 1.3</b>
     *
     * <p>For any SQL snippet that concatenates a user-controlled string variable
     * into a WHERE clause, the classifier must return CRITICAL severity.</p>
     */
    @Property(tries = 200)
    void stringConcatenatedUserInputIsClassifiedCritical(
            @ForAll("stringConcatSnippets") String snippet) {
        SqlInjectionSeverity severity = classifySqlSnippet(snippet);
        assertEquals(SqlInjectionSeverity.CRITICAL, severity,
                "String-concatenated user input should be CRITICAL: " + snippet);
    }

    /**
     * <b>Validates: Requirement 1.2</b>
     *
     * <p>For any SQL snippet that concatenates integer-only IDs in an IN clause,
     * the classifier must return MEDIUM severity.</p>
     */
    @Property(tries = 50)
    void integerOnlyConcatenationIsClassifiedMedium(
            @ForAll("integerConcatSnippets") String snippet) {
        SqlInjectionSeverity severity = classifySqlSnippet(snippet);
        assertEquals(SqlInjectionSeverity.MEDIUM, severity,
                "Integer-only ID concatenation should be MEDIUM: " + snippet);
    }

    /**
     * <b>Validates: Requirements 1.1, 1.2, 1.3</b>
     *
     * <p>For any SQL snippet that uses only parameterized placeholders (?),
     * the classifier must return SAFE.</p>
     */
    @Property(tries = 50)
    void parameterizedQueriesAreClassifiedSafe(
            @ForAll("parameterizedSnippets") String snippet) {
        SqlInjectionSeverity severity = classifySqlSnippet(snippet);
        assertEquals(SqlInjectionSeverity.SAFE, severity,
                "Parameterized query should be SAFE: " + snippet);
    }

    /**
     * <b>Validates: Requirements 1.1, 1.3</b>
     *
     * <p>For any SQL snippet that concatenates a user-controlled variable into
     * an ORDER BY clause, the classifier must return CRITICAL severity.</p>
     */
    @Property(tries = 50)
    void orderByConcatenationIsClassifiedCritical(
            @ForAll("orderByConcatSnippets") String snippet) {
        SqlInjectionSeverity severity = classifySqlSnippet(snippet);
        assertEquals(SqlInjectionSeverity.CRITICAL, severity,
                "ORDER BY concatenation should be CRITICAL: " + snippet);
    }

    /**
     * <b>Validates: Requirements 1.1, 1.2, 1.3</b>
     *
     * <p>CRITICAL severity is strictly higher than MEDIUM, and MEDIUM is strictly
     * higher than SAFE. This ensures the classification ordering is consistent.</p>
     */
    @Property(tries = 200)
    void severityOrderingIsConsistent(
            @ForAll("stringConcatSnippets") String criticalSnippet,
            @ForAll("integerConcatSnippets") String mediumSnippet,
            @ForAll("parameterizedSnippets") String safeSnippet) {

        SqlInjectionSeverity critical = classifySqlSnippet(criticalSnippet);
        SqlInjectionSeverity medium = classifySqlSnippet(mediumSnippet);
        SqlInjectionSeverity safe = classifySqlSnippet(safeSnippet);

        assertTrue(critical.ordinal() < medium.ordinal(),
                "CRITICAL should rank higher (lower ordinal) than MEDIUM");
        assertTrue(medium.ordinal() < safe.ordinal(),
                "MEDIUM should rank higher (lower ordinal) than SAFE");
    }
}
