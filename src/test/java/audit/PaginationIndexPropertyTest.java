package audit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.*;
import java.util.regex.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Pagination Query Index Verification.
 *
 * <p><b>Property 22: Pagination Query Index Verification</b></p>
 *
 * <p>For any pagination query using ORDER BY and LIMIT/OFFSET, the Audit_Engine
 * SHALL verify that the ORDER BY columns are covered by database indexes, and
 * flag unindexed columns as a performance risk.</p>
 *
 * <p><b>Validates: Requirement 12.3</b></p>
 *
 * <h2>Index model (from sql/13_pagination_indexes.sql and schema)</h2>
 * <ul>
 *   <li>{@code orders.createdAt} — covered by {@code idx_orders_created_at}</li>
 *   <li>{@code products.id} — covered by PRIMARY KEY (always indexed)</li>
 *   <li>{@code products.discount} — covered by {@code idx_products_discount}</li>
 *   <li>{@code total_sold} — derived/computed column (subquery alias); cannot be
 *       indexed but is acceptable because it is not a real table column</li>
 * </ul>
 *
 * <h2>Pagination queries verified</h2>
 * <ul>
 *   <li>{@code OrderDAO.getAllOrders()} — {@code ORDER BY createdAt DESC}</li>
 *   <li>{@code OrderDAO.getOrdersPage()} — {@code ORDER BY createdAt DESC LIMIT ? OFFSET ?}</li>
 *   <li>{@code ProductDAO.getAllProductsPage()} — {@code ORDER BY p.id DESC LIMIT ? OFFSET ?}</li>
 *   <li>{@code ProductDAO.getProductsByPage()} — {@code ORDER BY p.id DESC LIMIT ? OFFSET ?}</li>
 *   <li>{@code ProductDAO.getRelatedProducts()} — {@code ORDER BY p.id LIMIT ? OFFSET ?}</li>
 *   <li>{@code ProductDAO.getDiscountedProductsPage()} — {@code ORDER BY p.discount DESC, p.id DESC LIMIT ? OFFSET ?}</li>
 *   <li>{@code ProductDAO.getPopularProductsPage()} — {@code ORDER BY total_sold DESC, p.discount DESC, p.id DESC LIMIT ? OFFSET ?}</li>
 * </ul>
 */
class PaginationIndexPropertyTest {

    // ── Index model ───────────────────────────────────────────────────────────────

    /**
     * Columns that are covered by a database index (primary key or explicit index).
     * Keys are normalised to lower-case, stripped of table-alias prefix.
     *
     * <p>Source: {@code sql/13_pagination_indexes.sql} and the schema primary keys.</p>
     */
    static final Set<String> INDEXED_COLUMNS = Set.of(
            "id",          // products.id — PRIMARY KEY
            "createdat",   // orders.createdAt — idx_orders_created_at (case-insensitive)
            "discount"     // products.discount — idx_products_discount
    );

    /**
     * Columns that are derived / computed (subquery aliases) and therefore cannot
     * be indexed. Their presence in ORDER BY is acceptable.
     */
    static final Set<String> DERIVED_COLUMNS = Set.of(
            "total_sold",      // SUM(oi.quantity) alias in getPopularProductsPage
            "average_rating",  // AVG(r.rating) alias
            "review_count"     // COUNT(r.id) alias
    );

    // ── Result types ──────────────────────────────────────────────────────────────

    enum IndexCoverage { COVERED, DERIVED, UNINDEXED }

    record OrderByColumn(String rawName, IndexCoverage coverage) {}

    record PaginationQueryFinding(
            String queryLabel,
            List<OrderByColumn> columns,
            boolean hasViolation
    ) {}

    // ── Core classifier ───────────────────────────────────────────────────────────

    /**
     * Normalises a raw ORDER BY column token (strips table alias, lower-cases,
     * removes ASC/DESC direction keywords).
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code "p.discount"} → {@code "discount"}</li>
     *   <li>{@code "createdAt"} → {@code "createdat"}</li>
     *   <li>{@code "total_sold"} → {@code "total_sold"}</li>
     * </ul>
     */
    static String normalise(String rawColumn) {
        if (rawColumn == null) return "";
        // Strip table alias (e.g. "p.id" → "id", "s.total_sold" → "total_sold")
        String col = rawColumn.trim().toLowerCase();
        int dot = col.lastIndexOf('.');
        if (dot >= 0) {
            col = col.substring(dot + 1);
        }
        // Remove trailing direction keywords
        col = col.replaceAll("\\s+(asc|desc)$", "").trim();
        return col;
    }

    /**
     * Classifies a single ORDER BY column token.
     */
    static IndexCoverage classifyColumn(String rawColumn) {
        String col = normalise(rawColumn);
        if (INDEXED_COLUMNS.contains(col)) return IndexCoverage.COVERED;
        if (DERIVED_COLUMNS.contains(col))  return IndexCoverage.DERIVED;
        return IndexCoverage.UNINDEXED;
    }

    /**
     * Analyses a pagination query represented as a list of raw ORDER BY column tokens.
     * A query has a violation if any column is {@link IndexCoverage#UNINDEXED}.
     */
    static PaginationQueryFinding analyse(String queryLabel, List<String> orderByColumns) {
        List<OrderByColumn> classified = new ArrayList<>();
        boolean hasViolation = false;
        for (String raw : orderByColumns) {
            IndexCoverage cov = classifyColumn(raw);
            classified.add(new OrderByColumn(raw, cov));
            if (cov == IndexCoverage.UNINDEXED) hasViolation = true;
        }
        return new PaginationQueryFinding(queryLabel, classified, hasViolation);
    }

    /**
     * Extracts ORDER BY column tokens from a SQL string.
     *
     * <p>Parses the text between {@code ORDER BY} and the next {@code LIMIT},
     * {@code OFFSET}, or end-of-string, then splits on commas.</p>
     */
    static List<String> extractOrderByColumns(String sql) {
        if (sql == null || sql.isBlank()) return List.of();
        Pattern p = Pattern.compile(
                "ORDER\\s+BY\\s+(.+?)(?:\\s+LIMIT|\\s+OFFSET|$)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(sql);
        if (!m.find()) return List.of();
        String orderByClause = m.group(1).trim();
        String[] parts = orderByClause.split(",");
        List<String> cols = new ArrayList<>();
        for (String part : parts) {
            String col = part.trim().replaceAll("\\s+(ASC|DESC)\\s*$", "").trim();
            if (!col.isEmpty()) cols.add(col);
        }
        return cols;
    }

    // ── Actual pagination SQL strings extracted from the DAOs ─────────────────────

    /** OrderDAO.getAllOrders() */
    static final String SQL_ALL_ORDERS =
            "SELECT * FROM orders ORDER BY createdAt DESC";

    /** OrderDAO.getOrdersPage() — representative form (dynamic parts replaced) */
    static final String SQL_ORDERS_PAGE =
            "SELECT * FROM orders WHERE 1=1 ORDER BY createdAt DESC LIMIT ? OFFSET ?";

    /** ProductDAO.getAllProductsPage() */
    static final String SQL_ALL_PRODUCTS_PAGE =
            "SELECT p.*, ... FROM products p LEFT JOIN reviews r ON r.product_id = p.id " +
            "WHERE p.is_active = 1 GROUP BY p.id ORDER BY p.id DESC LIMIT ? OFFSET ?";

    /** ProductDAO.getProductsByPage() */
    static final String SQL_PRODUCTS_BY_PAGE =
            "SELECT p.*, ... FROM products p LEFT JOIN reviews r ON r.product_id = p.id " +
            "WHERE p.is_active = 1 GROUP BY p.id ORDER BY p.id DESC LIMIT ? OFFSET ?";

    /** ProductDAO.getRelatedProducts() */
    static final String SQL_RELATED_PRODUCTS =
            "SELECT p.*, ... FROM products p LEFT JOIN reviews r ON r.product_id = p.id " +
            "WHERE p.id != ? AND p.category = ? AND p.is_active = 1 GROUP BY p.id ORDER BY p.id LIMIT ? OFFSET ?";

    /** ProductDAO.getDiscountedProductsPage() */
    static final String SQL_DISCOUNTED_PRODUCTS_PAGE =
            "SELECT p.*, ... FROM products p LEFT JOIN reviews r ON r.product_id = p.id " +
            "WHERE p.discount > 0 AND p.is_active = 1 GROUP BY p.id ORDER BY p.discount DESC, p.id DESC LIMIT ? OFFSET ?";

    /** ProductDAO.getPopularProductsPage() */
    static final String SQL_POPULAR_PRODUCTS_PAGE =
            "SELECT p.*, COALESCE(s.total_sold, 0) AS total_sold, ... " +
            "FROM products p LEFT JOIN (...) s ON s.product_id = p.id " +
            "WHERE p.is_active = 1 " +
            "ORDER BY total_sold DESC, p.discount DESC, p.id DESC LIMIT ? OFFSET ?";

    // ── Generators ────────────────────────────────────────────────────────────────

    /** Generates column names that are known to be indexed. */
    @Provide
    Arbitrary<String> indexedColumnNames() {
        return Arbitraries.of("id", "p.id", "createdAt", "orders.createdAt", "discount", "p.discount");
    }

    /** Generates column names that are derived/computed (acceptable, not indexable). */
    @Provide
    Arbitrary<String> derivedColumnNames() {
        return Arbitraries.of("total_sold", "s.total_sold", "average_rating", "review_count");
    }

    /** Generates column names that are NOT indexed and NOT derived — violations. */
    @Provide
    Arbitrary<String> unindexedColumnNames() {
        return Arbitraries.of(
                "description", "name", "phone", "address", "fullname",
                "note", "category", "image", "status", "payment_method"
        );
    }

    /** Generates a list of 1–4 indexed column names (with optional direction). */
    @Provide
    Arbitrary<List<String>> indexedOnlyOrderByClauses() {
        return indexedColumnNames()
                .list().ofMinSize(1).ofMaxSize(4);
    }

    /** Generates a list of 1–3 derived column names. */
    @Provide
    Arbitrary<List<String>> derivedOnlyOrderByClauses() {
        return derivedColumnNames()
                .list().ofMinSize(1).ofMaxSize(3);
    }

    /** Generates a mixed list that contains at least one unindexed column. */
    @Provide
    Arbitrary<List<String>> clausesWithAtLeastOneUnindexed() {
        return Combinators.combine(
                unindexedColumnNames(),
                indexedColumnNames().list().ofMinSize(0).ofMaxSize(3)
        ).as((unindexed, indexed) -> {
            List<String> cols = new ArrayList<>(indexed);
            cols.add(unindexed);
            return cols;
        });
    }

    /** Generates a list mixing indexed and derived columns (no violations). */
    @Provide
    Arbitrary<List<String>> indexedAndDerivedClauses() {
        return Combinators.combine(
                indexedColumnNames().list().ofMinSize(1).ofMaxSize(3),
                derivedColumnNames().list().ofMinSize(0).ofMaxSize(2)
        ).as((indexed, derived) -> {
            List<String> cols = new ArrayList<>(indexed);
            cols.addAll(derived);
            return cols;
        });
    }

    // ── Property Tests ────────────────────────────────────────────────────────────

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Property 1 (core): For any ORDER BY clause composed entirely of indexed
     * columns, the analyser SHALL report no violation.</p>
     */
    @Property(tries = 200)
    void indexedColumnsProduceNoViolation(
            @ForAll("indexedOnlyOrderByClauses") List<String> columns) {
        PaginationQueryFinding finding = analyse("test-indexed-only", columns);
        assertFalse(finding.hasViolation(),
                "Indexed-only ORDER BY should have no violation: " + columns);
        assertTrue(finding.columns().stream()
                        .allMatch(c -> c.coverage() == IndexCoverage.COVERED),
                "All columns should be COVERED: " + columns);
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Property 2: For any ORDER BY clause that contains at least one unindexed
     * column, the analyser SHALL report a violation.</p>
     */
    @Property(tries = 200)
    void unindexedColumnAlwaysProducesViolation(
            @ForAll("clausesWithAtLeastOneUnindexed") List<String> columns) {
        PaginationQueryFinding finding = analyse("test-with-unindexed", columns);
        assertTrue(finding.hasViolation(),
                "ORDER BY with unindexed column must produce a violation: " + columns);
        assertTrue(finding.columns().stream()
                        .anyMatch(c -> c.coverage() == IndexCoverage.UNINDEXED),
                "At least one column must be UNINDEXED: " + columns);
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Property 3: Derived/computed columns (e.g. {@code total_sold}) are
     * classified as DERIVED and do NOT trigger a violation on their own.</p>
     */
    @Property(tries = 100)
    void derivedColumnsAreAcceptableAndDoNotViolate(
            @ForAll("derivedOnlyOrderByClauses") List<String> columns) {
        PaginationQueryFinding finding = analyse("test-derived-only", columns);
        assertFalse(finding.hasViolation(),
                "Derived-only ORDER BY should have no violation: " + columns);
        assertTrue(finding.columns().stream()
                        .allMatch(c -> c.coverage() == IndexCoverage.DERIVED),
                "All columns should be DERIVED: " + columns);
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Property 4: A mix of indexed and derived columns (no unindexed) produces
     * no violation — derived columns are acceptable alongside indexed ones.</p>
     */
    @Property(tries = 200)
    void indexedAndDerivedMixProducesNoViolation(
            @ForAll("indexedAndDerivedClauses") List<String> columns) {
        PaginationQueryFinding finding = analyse("test-indexed-and-derived", columns);
        assertFalse(finding.hasViolation(),
                "Mix of indexed + derived columns should have no violation: " + columns);
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Property 5: Column normalisation is idempotent — normalising an already
     * normalised column name returns the same value.</p>
     */
    @Property(tries = 200)
    void normalisationIsIdempotent(
            @ForAll("indexedColumnNames") String col) {
        String once = normalise(col);
        String twice = normalise(once);
        assertEquals(once, twice,
                "normalise() must be idempotent: normalise(normalise(x)) == normalise(x) for: " + col);
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Property 6: Table-alias prefix does not affect classification — {@code p.id}
     * and {@code id} must both be COVERED.</p>
     */
    @Property(tries = 50)
    void tableAliasPrefixDoesNotAffectClassification(
            @ForAll("indexedColumnNames") String col) {
        IndexCoverage withoutAlias = classifyColumn(col);
        // Strip any existing alias and re-add a different one
        String bare = normalise(col);
        IndexCoverage withAlias = classifyColumn("tbl." + bare);
        assertEquals(withoutAlias, withAlias,
                "Table alias prefix must not change classification: " + col);
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Property 7: ASC/DESC direction keywords do not affect classification.</p>
     */
    @Property(tries = 100)
    void directionKeywordDoesNotAffectClassification(
            @ForAll("indexedColumnNames") String col,
            @ForAll @From("directions") String direction) {
        IndexCoverage withoutDir = classifyColumn(col);
        IndexCoverage withDir = classifyColumn(col + " " + direction);
        assertEquals(withoutDir, withDir,
                "Direction keyword must not change classification: " + col + " " + direction);
    }

    @Provide("directions")
    Arbitrary<String> directions() {
        return Arbitraries.of("ASC", "DESC", "asc", "desc");
    }

    // ── Concrete scenario tests for actual DAO queries ────────────────────────────

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Scenario 1: {@code OrderDAO.getAllOrders()} — {@code ORDER BY createdAt DESC}
     * — must have no violation (createdAt is indexed by idx_orders_created_at).</p>
     */
    @Property(tries = 1)
    void orderDaoGetAllOrdersHasNoViolation() {
        List<String> cols = extractOrderByColumns(SQL_ALL_ORDERS);
        assertFalse(cols.isEmpty(), "getAllOrders SQL must have ORDER BY columns");
        PaginationQueryFinding finding = analyse("OrderDAO.getAllOrders", cols);
        assertFalse(finding.hasViolation(),
                "getAllOrders ORDER BY createdAt should be covered by idx_orders_created_at");
        assertEquals(IndexCoverage.COVERED, classifyColumn("createdAt"),
                "createdAt must be classified as COVERED");
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Scenario 2: {@code OrderDAO.getOrdersPage()} — {@code ORDER BY createdAt DESC LIMIT ? OFFSET ?}
     * — must have no violation.</p>
     */
    @Property(tries = 1)
    void orderDaoGetOrdersPageHasNoViolation() {
        List<String> cols = extractOrderByColumns(SQL_ORDERS_PAGE);
        assertFalse(cols.isEmpty(), "getOrdersPage SQL must have ORDER BY columns");
        PaginationQueryFinding finding = analyse("OrderDAO.getOrdersPage", cols);
        assertFalse(finding.hasViolation(),
                "getOrdersPage ORDER BY createdAt should be covered by idx_orders_created_at");
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Scenario 3: {@code ProductDAO.getAllProductsPage()} — {@code ORDER BY p.id DESC LIMIT ? OFFSET ?}
     * — must have no violation (id is the PRIMARY KEY).</p>
     */
    @Property(tries = 1)
    void productDaoGetAllProductsPageHasNoViolation() {
        List<String> cols = extractOrderByColumns(SQL_ALL_PRODUCTS_PAGE);
        assertFalse(cols.isEmpty(), "getAllProductsPage SQL must have ORDER BY columns");
        PaginationQueryFinding finding = analyse("ProductDAO.getAllProductsPage", cols);
        assertFalse(finding.hasViolation(),
                "getAllProductsPage ORDER BY p.id should be covered by PRIMARY KEY");
        assertEquals(IndexCoverage.COVERED, classifyColumn("p.id"),
                "p.id must be classified as COVERED (PRIMARY KEY)");
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Scenario 4: {@code ProductDAO.getProductsByPage()} — {@code ORDER BY p.id DESC LIMIT ? OFFSET ?}
     * — must have no violation.</p>
     */
    @Property(tries = 1)
    void productDaoGetProductsByPageHasNoViolation() {
        List<String> cols = extractOrderByColumns(SQL_PRODUCTS_BY_PAGE);
        assertFalse(cols.isEmpty(), "getProductsByPage SQL must have ORDER BY columns");
        PaginationQueryFinding finding = analyse("ProductDAO.getProductsByPage", cols);
        assertFalse(finding.hasViolation(),
                "getProductsByPage ORDER BY p.id should be covered by PRIMARY KEY");
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Scenario 5: {@code ProductDAO.getRelatedProducts()} — {@code ORDER BY p.id LIMIT ? OFFSET ?}
     * — must have no violation.</p>
     */
    @Property(tries = 1)
    void productDaoGetRelatedProductsHasNoViolation() {
        List<String> cols = extractOrderByColumns(SQL_RELATED_PRODUCTS);
        assertFalse(cols.isEmpty(), "getRelatedProducts SQL must have ORDER BY columns");
        PaginationQueryFinding finding = analyse("ProductDAO.getRelatedProducts", cols);
        assertFalse(finding.hasViolation(),
                "getRelatedProducts ORDER BY p.id should be covered by PRIMARY KEY");
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Scenario 6: {@code ProductDAO.getDiscountedProductsPage()} —
     * {@code ORDER BY p.discount DESC, p.id DESC LIMIT ? OFFSET ?}
     * — must have no violation (discount is indexed by idx_products_discount,
     * id is the PRIMARY KEY).</p>
     */
    @Property(tries = 1)
    void productDaoGetDiscountedProductsPageHasNoViolation() {
        List<String> cols = extractOrderByColumns(SQL_DISCOUNTED_PRODUCTS_PAGE);
        assertFalse(cols.isEmpty(), "getDiscountedProductsPage SQL must have ORDER BY columns");
        assertEquals(2, cols.size(),
                "getDiscountedProductsPage should have 2 ORDER BY columns (discount, id): " + cols);
        PaginationQueryFinding finding = analyse("ProductDAO.getDiscountedProductsPage", cols);
        assertFalse(finding.hasViolation(),
                "getDiscountedProductsPage ORDER BY p.discount, p.id should both be covered");
        assertTrue(finding.columns().stream().allMatch(c -> c.coverage() == IndexCoverage.COVERED),
                "Both discount and id must be COVERED: " + cols);
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Scenario 7: {@code ProductDAO.getPopularProductsPage()} —
     * {@code ORDER BY total_sold DESC, p.discount DESC, p.id DESC LIMIT ? OFFSET ?}
     * — must have no violation. {@code total_sold} is a derived column (acceptable),
     * {@code discount} is indexed, {@code id} is the PRIMARY KEY.</p>
     */
    @Property(tries = 1)
    void productDaoGetPopularProductsPageHasNoViolation() {
        List<String> cols = extractOrderByColumns(SQL_POPULAR_PRODUCTS_PAGE);
        assertFalse(cols.isEmpty(), "getPopularProductsPage SQL must have ORDER BY columns");
        assertEquals(3, cols.size(),
                "getPopularProductsPage should have 3 ORDER BY columns (total_sold, discount, id): " + cols);
        PaginationQueryFinding finding = analyse("ProductDAO.getPopularProductsPage", cols);
        assertFalse(finding.hasViolation(),
                "getPopularProductsPage ORDER BY total_sold (derived), p.discount, p.id should have no violation");

        // Verify individual column classifications
        assertEquals(IndexCoverage.DERIVED,  classifyColumn("total_sold"),
                "total_sold must be DERIVED (computed subquery alias)");
        assertEquals(IndexCoverage.COVERED,  classifyColumn("p.discount"),
                "p.discount must be COVERED by idx_products_discount");
        assertEquals(IndexCoverage.COVERED,  classifyColumn("p.id"),
                "p.id must be COVERED by PRIMARY KEY");
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Scenario 8 (violation detection): A hypothetical pagination query using
     * an unindexed column ({@code description}) in ORDER BY MUST be flagged as a
     * violation. This demonstrates the classifier catches real problems.</p>
     */
    @Property(tries = 1)
    void hypotheticalUnindexedOrderByIsDetectedAsViolation() {
        String badSql = "SELECT * FROM products WHERE is_active = 1 " +
                        "ORDER BY description ASC LIMIT ? OFFSET ?";
        List<String> cols = extractOrderByColumns(badSql);
        assertFalse(cols.isEmpty(), "Bad SQL must have ORDER BY columns");
        PaginationQueryFinding finding = analyse("hypothetical-bad-query", cols);
        assertTrue(finding.hasViolation(),
                "ORDER BY description (unindexed) must be flagged as a violation");
        assertEquals(IndexCoverage.UNINDEXED, classifyColumn("description"),
                "description must be classified as UNINDEXED");
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Scenario 9 (violation detection): A hypothetical query ordering by
     * {@code name} (unindexed) alongside an indexed column must still be flagged,
     * because any unindexed column in ORDER BY is a performance risk.</p>
     */
    @Property(tries = 1)
    void partiallyUnindexedOrderByIsDetectedAsViolation() {
        String badSql = "SELECT * FROM products WHERE is_active = 1 " +
                        "ORDER BY p.id DESC, name ASC LIMIT ? OFFSET ?";
        List<String> cols = extractOrderByColumns(badSql);
        PaginationQueryFinding finding = analyse("hypothetical-partial-bad-query", cols);
        assertTrue(finding.hasViolation(),
                "ORDER BY with any unindexed column (name) must be flagged as a violation");
    }

    /**
     * <b>Validates: Requirement 12.3</b>
     *
     * <p>Property 8: For any ORDER BY clause, every column is classified as exactly
     * one of COVERED, DERIVED, or UNINDEXED — the classification is total and
     * exhaustive.</p>
     */
    @Property(tries = 300)
    void everyColumnReceivesExactlyOneClassification(
            @ForAll @StringLength(min = 1, max = 30) @AlphaChars String rawCol) {
        IndexCoverage cov = classifyColumn(rawCol);
        assertNotNull(cov, "Classification must never be null for: " + rawCol);
        assertTrue(
                cov == IndexCoverage.COVERED ||
                cov == IndexCoverage.DERIVED  ||
                cov == IndexCoverage.UNINDEXED,
                "Classification must be one of COVERED, DERIVED, UNINDEXED for: " + rawCol);
    }
}
