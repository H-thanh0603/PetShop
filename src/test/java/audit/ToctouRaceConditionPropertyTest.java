package audit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for TOCTOU Race Condition Detection in Transactional Flows.
 *
 * <p><b>Property 8: TOCTOU Race Condition Detection in Transactional Flows</b></p>
 *
 * <p>For any checkout-like flow where a resource (stock, coupon) is validated outside
 * a database transaction and then consumed inside the transaction using a different
 * data snapshot, the Audit_Engine SHALL flag a TOCTOU race condition as a MEDIUM
 * severity finding.</p>
 *
 * <p><b>Validates: Requirement 6.4</b></p>
 */
class ToctouRaceConditionPropertyTest {

    // ── Inline severity/finding model (self-contained, no external dependencies) ──

    enum ToctouSeverity {
        MEDIUM,  // TOCTOU race condition detected — validate outside tx, consume inside
        SAFE     // Validation and consumption both inside the same transaction
    }

    enum ResourceType {
        STOCK,
        COUPON,
        DISCOUNT_FLAG
    }

    /**
     * Classifies a checkout flow code snippet for TOCTOU race condition risk.
     *
     * <ul>
     *   <li>If the snippet validates a resource (stock check, coupon check, discount check)
     *       BEFORE a transaction begins, and then consumes (decrements, marks used) that
     *       resource INSIDE the transaction → MEDIUM (TOCTOU detected)</li>
     *   <li>If the snippet validates and consumes the resource within the same transaction
     *       (e.g., SELECT FOR UPDATE + UPDATE in same tx block) → SAFE</li>
     * </ul>
     *
     * <p>Detection heuristic: We look for a resource validation pattern (SELECT/check)
     * appearing before a transaction boundary marker (BEGIN/setAutoCommit(false)/startTransaction),
     * followed by a resource consumption pattern (UPDATE/decrement) inside the transaction.
     * If validation is inside the transaction (after BEGIN), it is considered safe.</p>
     */
    static ToctouSeverity classifyCheckoutFlow(String snippet) {
        if (snippet == null || snippet.isBlank()) {
            return ToctouSeverity.SAFE;
        }

        String normalized = snippet.replaceAll("\\s+", " ").toLowerCase();

        // ── Locate transaction boundary ──
        int txStart = findTransactionStart(normalized);

        if (txStart < 0) {
            // No transaction boundary found — cannot have TOCTOU in a transactional sense
            return ToctouSeverity.SAFE;
        }

        // ── Check for resource validation BEFORE the transaction ──
        String beforeTx = normalized.substring(0, txStart);
        boolean hasValidationBeforeTx = containsResourceValidation(beforeTx);

        // ── Check for resource consumption INSIDE the transaction ──
        String insideTx = normalized.substring(txStart);
        boolean hasConsumptionInsideTx = containsResourceConsumption(insideTx);

        // ── Check if validation ALSO occurs inside the transaction (safe pattern) ──
        boolean hasValidationInsideTx = containsResourceValidation(insideTx);

        // TOCTOU: validation outside tx + consumption inside tx, without re-validation inside tx
        if (hasValidationBeforeTx && hasConsumptionInsideTx && !hasValidationInsideTx) {
            return ToctouSeverity.MEDIUM;
        }

        return ToctouSeverity.SAFE;
    }

    /**
     * Finds the index of the transaction start marker in the normalized snippet.
     * Looks for common patterns: setautocommit(false), begin transaction, .begintransaction()
     */
    static int findTransactionStart(String normalized) {
        int idx = -1;

        int i1 = normalized.indexOf("setautocommit(false)");
        int i2 = normalized.indexOf("begin transaction");
        int i3 = normalized.indexOf(".begintransaction()");
        int i4 = normalized.indexOf("start transaction");

        // Return the earliest match
        for (int candidate : new int[]{i1, i2, i3, i4}) {
            if (candidate >= 0 && (idx < 0 || candidate < idx)) {
                idx = candidate;
            }
        }
        return idx;
    }

    /**
     * Checks if the text contains a resource validation pattern:
     * stock checks, coupon availability checks, discount flag checks.
     */
    static boolean containsResourceValidation(String text) {
        // Stock validation patterns
        if (text.contains("select") && text.contains("quantity") && text.contains("from") && text.contains("product")) {
            return true;
        }
        if (text.contains("select") && text.contains("stock") && text.contains("from") && text.contains("product")) {
            return true;
        }
        if (text.contains("getstock(") || text.contains("checkstock(")) {
            return true;
        }
        if (text.contains(".quantity") && (text.contains(">=") || text.contains("> 0"))) {
            return true;
        }
        if (text.contains(".stock") && text.contains(">=")) {
            return true;
        }

        // Coupon validation patterns
        if (text.contains("select") && text.contains("used") && text.contains("from") && text.contains("coupon")) {
            return true;
        }
        if (text.contains("coupon.used") && text.contains("<") && text.contains("coupon.quantity")) {
            return true;
        }
        if (text.contains("checkcoupon(") || text.contains("iscouponvalid(")) {
            return true;
        }
        if (text.contains(".getused()") && text.contains("<")) {
            return true;
        }

        // Discount flag validation patterns
        if (text.contains("select") && text.contains("has_used_discount") && text.contains("from") && text.contains("user")) {
            return true;
        }
        if (text.contains(".hasdiscount(")) {
            return true;
        }
        if (text.contains("has_used_discount") && text.contains("= 0")) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the text contains a resource consumption pattern:
     * stock decrement, coupon usage increment, discount flag set.
     */
    static boolean containsResourceConsumption(String text) {
        // Stock consumption patterns
        if (text.contains("update") && text.contains("product") && text.contains("quantity") && text.contains("quantity -")) {
            return true;
        }
        if (text.contains("decrementstock(") || text.contains("reducestock(")) {
            return true;
        }

        // Coupon consumption patterns
        if (text.contains("update") && text.contains("coupon") && text.contains("used") && text.contains("used + 1")) {
            return true;
        }
        if (text.contains("markcouponused(") || text.contains("incrementcouponusage(")) {
            return true;
        }

        // Discount flag consumption patterns
        if (text.contains("update") && text.contains("user") && text.contains("has_used_discount") && text.contains("= 1")) {
            return true;
        }
        if (text.contains("markdiscountasused(")) {
            return true;
        }

        return false;
    }

    // ── Generators ──

    @Provide
    Arbitrary<String> resourceNames() {
        return Arbitraries.of("stock", "coupon", "discount");
    }

    @Provide
    Arbitrary<String> stockValidationSnippets() {
        return Arbitraries.of(
                "SELECT quantity FROM products WHERE id = ?",
                "SELECT stock FROM products WHERE id = ?",
                "int qty = product.quantity; if (qty >= orderQty)",
                "checkStock(productId, requestedQty)",
                "if (product.quantity > 0)"
        );
    }

    @Provide
    Arbitrary<String> couponValidationSnippets() {
        return Arbitraries.of(
                "SELECT used, quantity FROM coupons WHERE id = ?",
                "if (coupon.used < coupon.quantity)",
                "isCouponValid(couponId)",
                "checkCoupon(couponCode)",
                "if (coupon.getUsed() < coupon.getQuantity())"
        );
    }

    @Provide
    Arbitrary<String> discountValidationSnippets() {
        return Arbitraries.of(
                "SELECT has_used_discount FROM users WHERE id = ?",
                "if (has_used_discount = 0)",
                "user.hasDiscount()"
        );
    }

    @Provide
    Arbitrary<String> stockConsumptionSnippets() {
        return Arbitraries.of(
                "UPDATE products SET quantity = quantity - ?",
                "decrementStock(productId, qty)",
                "reduceStock(productId, orderQty)"
        );
    }

    @Provide
    Arbitrary<String> couponConsumptionSnippets() {
        return Arbitraries.of(
                "UPDATE coupons SET used = used + 1 WHERE id = ?",
                "markCouponUsed(couponId)",
                "incrementCouponUsage(couponId)"
        );
    }

    @Provide
    Arbitrary<String> discountConsumptionSnippets() {
        return Arbitraries.of(
                "UPDATE users SET has_used_discount = 1 WHERE id = ?",
                "markDiscountAsUsed(userId)"
        );
    }

    @Provide
    Arbitrary<String> transactionStartMarkers() {
        return Arbitraries.of(
                "conn.setAutoCommit(false);",
                "BEGIN TRANSACTION;",
                "connection.setAutoCommit(false);",
                "START TRANSACTION;"
        );
    }

    /**
     * Generates VULNERABLE checkout flows: resource validated OUTSIDE the transaction,
     * consumed INSIDE the transaction, with NO re-validation inside.
     */
    @Provide
    Arbitrary<String> vulnerableStockFlows() {
        return Combinators.combine(
                stockValidationSnippets(),
                transactionStartMarkers(),
                stockConsumptionSnippets()
        ).as((validation, txStart, consumption) ->
                validation + "\n" + txStart + "\n" + consumption + "\nconn.commit();"
        );
    }

    @Provide
    Arbitrary<String> vulnerableCouponFlows() {
        return Combinators.combine(
                couponValidationSnippets(),
                transactionStartMarkers(),
                couponConsumptionSnippets()
        ).as((validation, txStart, consumption) ->
                validation + "\n" + txStart + "\n" + consumption + "\nconn.commit();"
        );
    }

    @Provide
    Arbitrary<String> vulnerableDiscountFlows() {
        return Combinators.combine(
                discountValidationSnippets(),
                transactionStartMarkers(),
                discountConsumptionSnippets()
        ).as((validation, txStart, consumption) ->
                validation + "\n" + txStart + "\n" + consumption + "\nconn.commit();"
        );
    }

    /**
     * Combines all vulnerable flow types into a single generator.
     */
    @Provide
    Arbitrary<String> allVulnerableFlows() {
        return Arbitraries.oneOf(
                vulnerableStockFlows(),
                vulnerableCouponFlows(),
                vulnerableDiscountFlows()
        );
    }

    /**
     * Generates SAFE checkout flows: resource validated AND consumed INSIDE the same
     * transaction (e.g., SELECT FOR UPDATE + UPDATE in same tx block).
     */
    @Provide
    Arbitrary<String> safeStockFlows() {
        return Combinators.combine(
                transactionStartMarkers(),
                Arbitraries.of(
                        "SELECT quantity FROM products WHERE id = ? FOR UPDATE",
                        "SELECT stock FROM products WHERE id = ? FOR UPDATE"
                ),
                stockConsumptionSnippets()
        ).as((txStart, validation, consumption) ->
                txStart + "\n" + validation + "\n" + consumption + "\nconn.commit();"
        );
    }

    @Provide
    Arbitrary<String> safeCouponFlows() {
        return Combinators.combine(
                transactionStartMarkers(),
                Arbitraries.of(
                        "SELECT used, quantity FROM coupons WHERE id = ? FOR UPDATE",
                        "SELECT used FROM coupons WHERE id = ? FOR UPDATE"
                ),
                couponConsumptionSnippets()
        ).as((txStart, validation, consumption) ->
                txStart + "\n" + validation + "\n" + consumption + "\nconn.commit();"
        );
    }

    @Provide
    Arbitrary<String> safeDiscountFlows() {
        return Combinators.combine(
                transactionStartMarkers(),
                Arbitraries.of(
                        "SELECT has_used_discount FROM users WHERE id = ? FOR UPDATE"
                ),
                discountConsumptionSnippets()
        ).as((txStart, validation, consumption) ->
                txStart + "\n" + validation + "\n" + consumption + "\nconn.commit();"
        );
    }

    /**
     * Combines all safe flow types into a single generator.
     */
    @Provide
    Arbitrary<String> allSafeFlows() {
        return Arbitraries.oneOf(
                safeStockFlows(),
                safeCouponFlows(),
                safeDiscountFlows()
        );
    }

    // ── Property Tests ──

    /**
     * <b>Validates: Requirement 6.4</b>
     *
     * <p>For any checkout flow where a resource is validated OUTSIDE the transaction
     * and consumed INSIDE the transaction (without re-validation inside), the classifier
     * must detect a TOCTOU race condition and return MEDIUM severity.</p>
     */
    @Property(tries = 200)
    void vulnerableFlowsAreFlaggedAsToctou(
            @ForAll("allVulnerableFlows") String snippet) {
        ToctouSeverity severity = classifyCheckoutFlow(snippet);
        assertEquals(ToctouSeverity.MEDIUM, severity,
                "Validate-outside-tx + consume-inside-tx should be MEDIUM (TOCTOU): " + snippet);
    }

    /**
     * <b>Validates: Requirement 6.4</b>
     *
     * <p>For any checkout flow where both validation and consumption happen INSIDE
     * the same transaction (e.g., SELECT FOR UPDATE + UPDATE), the classifier must
     * return SAFE — no TOCTOU race condition exists.</p>
     */
    @Property(tries = 200)
    void safeFlowsAreNotFlagged(
            @ForAll("allSafeFlows") String snippet) {
        ToctouSeverity severity = classifyCheckoutFlow(snippet);
        assertEquals(ToctouSeverity.SAFE, severity,
                "Validate-and-consume-inside-tx should be SAFE: " + snippet);
    }

    /**
     * <b>Validates: Requirement 6.4</b>
     *
     * <p>For any vulnerable stock checkout flow specifically, the classifier must
     * detect the TOCTOU pattern and return MEDIUM severity.</p>
     */
    @Property(tries = 100)
    void vulnerableStockFlowsDetected(
            @ForAll("vulnerableStockFlows") String snippet) {
        ToctouSeverity severity = classifyCheckoutFlow(snippet);
        assertEquals(ToctouSeverity.MEDIUM, severity,
                "Stock validated outside tx should be MEDIUM: " + snippet);
    }

    /**
     * <b>Validates: Requirement 6.4</b>
     *
     * <p>For any vulnerable coupon checkout flow specifically, the classifier must
     * detect the TOCTOU pattern and return MEDIUM severity.</p>
     */
    @Property(tries = 100)
    void vulnerableCouponFlowsDetected(
            @ForAll("vulnerableCouponFlows") String snippet) {
        ToctouSeverity severity = classifyCheckoutFlow(snippet);
        assertEquals(ToctouSeverity.MEDIUM, severity,
                "Coupon validated outside tx should be MEDIUM: " + snippet);
    }

    /**
     * <b>Validates: Requirement 6.4</b>
     *
     * <p>MEDIUM severity (TOCTOU detected) is strictly more severe than SAFE.
     * This ensures the classification ordering is consistent.</p>
     */
    @Property(tries = 100)
    void severityOrderingIsConsistent(
            @ForAll("allVulnerableFlows") String vulnerableSnippet,
            @ForAll("allSafeFlows") String safeSnippet) {

        ToctouSeverity vulnerable = classifyCheckoutFlow(vulnerableSnippet);
        ToctouSeverity safe = classifyCheckoutFlow(safeSnippet);

        assertTrue(vulnerable.ordinal() < safe.ordinal(),
                "MEDIUM (TOCTOU) should rank higher (lower ordinal) than SAFE");
    }
}
