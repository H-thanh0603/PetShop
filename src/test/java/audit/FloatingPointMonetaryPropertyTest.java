package audit;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Floating-Point Monetary Calculation Detection.
 *
 * <p><b>Property 9: Floating-Point Monetary Calculation Detection</b></p>
 *
 * <p>For any source artifact that performs monetary calculations using {@code double}
 * or {@code float} types (in Java code) or uses non-DECIMAL column types for monetary
 * values (in SQL schema), the Audit_Engine SHALL flag it as a MEDIUM severity
 * precision risk.</p>
 *
 * <p><b>Validates: Requirements 7.1, 7.3</b></p>
 */
class FloatingPointMonetaryPropertyTest {

    // ── Inline severity model (self-contained, no dependency on future Task 17 classes) ──

    enum FloatingPointSeverity {
        MEDIUM,  // Floating-point type used for monetary value
        SAFE     // Fixed-precision type (BigDecimal / DECIMAL) used
    }

    // ── Monetary variable/column name lists ──

    private static final String[] JAVA_MONETARY_VARS = {
            "totalAmount", "price", "discount", "finalTotal", "shippingFee",
            "subtotal", "revenue", "amount", "cost", "fee", "total"
    };

    private static final String[] SQL_MONETARY_COLS = {
            "price", "total_amount", "discount", "shipping_fee",
            "amount", "cost", "fee"
    };

    /**
     * Classifies a code snippet for floating-point monetary precision risk.
     *
     * <ul>
     *   <li>For Java code: if a {@code double} or {@code float} variable has a
     *       monetary name → MEDIUM</li>
     *   <li>For Java code: if a {@code BigDecimal} variable has a monetary name → SAFE</li>
     *   <li>For SQL DDL: if a {@code FLOAT} or {@code DOUBLE} column has a monetary
     *       name → MEDIUM</li>
     *   <li>For SQL DDL: if a {@code DECIMAL} column has a monetary name → SAFE</li>
     * </ul>
     */
    static FloatingPointSeverity classifyMonetarySnippet(String snippet) {
        if (snippet == null || snippet.isBlank()) {
            return FloatingPointSeverity.SAFE;
        }

        String upper = snippet.toUpperCase();

        // ── SQL DDL detection ──
        // Look for CREATE TABLE or column definitions typical of DDL
        if (upper.contains("CREATE TABLE") || upper.contains("ALTER TABLE")
                || upper.matches("(?si).*\\b\\w+\\s+(FLOAT|DOUBLE|DECIMAL)\\b.*")) {

            for (String col : SQL_MONETARY_COLS) {
                // Check if this monetary column name appears in the snippet
                String colUpper = col.toUpperCase();
                if (upper.contains(colUpper)) {
                    // Check if the column uses FLOAT or DOUBLE type
                    // Pattern: column_name FLOAT or column_name DOUBLE (with optional size)
                    String colPattern = "(?si).*\\b" + col + "\\b\\s+(FLOAT|DOUBLE)\\b.*";
                    if (snippet.matches(colPattern)) {
                        return FloatingPointSeverity.MEDIUM;
                    }
                    // Check if the column uses DECIMAL type → SAFE
                    String decimalPattern = "(?si).*\\b" + col + "\\b\\s+DECIMAL\\b.*";
                    if (snippet.matches(decimalPattern)) {
                        return FloatingPointSeverity.SAFE;
                    }
                }
            }
        }

        // ── Java code detection ──
        for (String varName : JAVA_MONETARY_VARS) {
            // Check if this monetary variable name appears in the snippet
            if (snippet.contains(varName)) {
                // Check for double/float declaration: "double totalAmount" or "float price"
                String floatPattern = "(?s).*\\b(double|float)\\s+" + varName + "\\b.*";
                if (snippet.matches(floatPattern)) {
                    return FloatingPointSeverity.MEDIUM;
                }
                // Check for BigDecimal declaration: "BigDecimal totalAmount"
                String bigDecimalPattern = "(?s).*\\bBigDecimal\\s+" + varName + "\\b.*";
                if (snippet.matches(bigDecimalPattern)) {
                    return FloatingPointSeverity.SAFE;
                }
            }
        }

        return FloatingPointSeverity.SAFE;
    }

    // ── Generators ──

    @Provide
    Arbitrary<String> floatingPointJavaSnippets() {
        Arbitrary<String> types = Arbitraries.of("double", "float");
        Arbitrary<String> varNames = Arbitraries.of(JAVA_MONETARY_VARS);
        Arbitrary<String> operations = Arbitraries.of(
                " = 0.0;",
                " = cart.getTotal();",
                " = price * quantity;",
                " = totalAmount * discountPercent / 100.0;",
                " = subtotal + shippingFee;"
        );

        return Combinators.combine(types, varNames, operations)
                .as((type, varName, op) -> type + " " + varName + op);
    }

    @Provide
    Arbitrary<String> bigDecimalJavaSnippets() {
        Arbitrary<String> varNames = Arbitraries.of(JAVA_MONETARY_VARS);
        Arbitrary<String> initializers = Arbitraries.of(
                " = BigDecimal.ZERO;",
                " = new BigDecimal(\"0.00\");",
                " = cart.getTotal();",
                " = price.multiply(quantity);",
                " = totalAmount.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);"
        );

        return Combinators.combine(varNames, initializers)
                .as((varName, init) -> "BigDecimal " + varName + init);
    }

    @Provide
    Arbitrary<String> floatSqlSnippets() {
        Arbitrary<String> colNames = Arbitraries.of(SQL_MONETARY_COLS);
        Arbitrary<String> colTypes = Arbitraries.of("FLOAT", "DOUBLE");
        Arbitrary<String> tableNames = Arbitraries.of(
                "orders", "products", "order_items", "cart_items", "coupons"
        );

        return Combinators.combine(tableNames, colNames, colTypes)
                .as((table, col, colType) ->
                        "CREATE TABLE " + table + " (\n"
                                + "  id INT PRIMARY KEY,\n"
                                + "  " + col + " " + colType + " NOT NULL\n"
                                + ");");
    }

    @Provide
    Arbitrary<String> decimalSqlSnippets() {
        Arbitrary<String> colNames = Arbitraries.of(SQL_MONETARY_COLS);
        Arbitrary<String> tableNames = Arbitraries.of(
                "orders", "products", "order_items", "cart_items", "coupons"
        );
        Arbitrary<String> precisions = Arbitraries.of(
                "DECIMAL(18,2)", "DECIMAL(10,2)", "DECIMAL(18,0)"
        );

        return Combinators.combine(tableNames, colNames, precisions)
                .as((table, col, precision) ->
                        "CREATE TABLE " + table + " (\n"
                                + "  id INT PRIMARY KEY,\n"
                                + "  " + col + " " + precision + " NOT NULL\n"
                                + ");");
    }

    // ── Property Tests ──

    /**
     * <b>Validates: Requirements 7.1, 7.3</b>
     *
     * <p>For any Java code snippet that declares a monetary variable using
     * {@code double} or {@code float}, the classifier must return MEDIUM severity.</p>
     */
    @Property(tries = 200)
    void floatingPointJavaMonetaryIsFlaggedMedium(
            @ForAll("floatingPointJavaSnippets") String snippet) {
        FloatingPointSeverity severity = classifyMonetarySnippet(snippet);
        assertEquals(FloatingPointSeverity.MEDIUM, severity,
                "double/float monetary variable should be MEDIUM: " + snippet);
    }

    /**
     * <b>Validates: Requirements 7.1, 7.3</b>
     *
     * <p>For any Java code snippet that declares a monetary variable using
     * {@code BigDecimal}, the classifier must return SAFE.</p>
     */
    @Property(tries = 200)
    void bigDecimalJavaMonetaryIsSafe(
            @ForAll("bigDecimalJavaSnippets") String snippet) {
        FloatingPointSeverity severity = classifyMonetarySnippet(snippet);
        assertEquals(FloatingPointSeverity.SAFE, severity,
                "BigDecimal monetary variable should be SAFE: " + snippet);
    }

    /**
     * <b>Validates: Requirements 7.1, 7.3</b>
     *
     * <p>For any SQL DDL snippet that defines a monetary column using
     * {@code FLOAT} or {@code DOUBLE}, the classifier must return MEDIUM severity.</p>
     */
    @Property(tries = 200)
    void floatSqlMonetaryIsFlaggedMedium(
            @ForAll("floatSqlSnippets") String snippet) {
        FloatingPointSeverity severity = classifyMonetarySnippet(snippet);
        assertEquals(FloatingPointSeverity.MEDIUM, severity,
                "FLOAT/DOUBLE SQL monetary column should be MEDIUM: " + snippet);
    }

    /**
     * <b>Validates: Requirements 7.1, 7.3</b>
     *
     * <p>For any SQL DDL snippet that defines a monetary column using
     * {@code DECIMAL}, the classifier must return SAFE.</p>
     */
    @Property(tries = 200)
    void decimalSqlMonetaryIsSafe(
            @ForAll("decimalSqlSnippets") String snippet) {
        FloatingPointSeverity severity = classifyMonetarySnippet(snippet);
        assertEquals(FloatingPointSeverity.SAFE, severity,
                "DECIMAL SQL monetary column should be SAFE: " + snippet);
    }

    /**
     * <b>Validates: Requirements 7.1, 7.3</b>
     *
     * <p>MEDIUM severity has a lower ordinal than SAFE, ensuring consistent
     * severity ordering where MEDIUM is more severe than SAFE.</p>
     */
    @Property(tries = 1)
    void severityOrderingIsConsistent() {
        assertTrue(FloatingPointSeverity.MEDIUM.ordinal() < FloatingPointSeverity.SAFE.ordinal(),
                "MEDIUM should rank higher (lower ordinal) than SAFE");
    }
}
