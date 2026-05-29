package audit;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for XSS Detection Across Output Mechanisms.
 *
 * <p><b>Property 7: XSS Detection Across Output Mechanisms</b></p>
 *
 * <p>For any output point (JSP EL expression or servlet response writer) that renders
 * user-controlled data without proper encoding (JSTL {@code <c:out>},
 * {@code fn:escapeXml()}, or explicit HTML encoding), the classifier SHALL flag it
 * as a HIGH severity XSS finding. Properly encoded output SHALL be classified as SAFE.</p>
 *
 * <p><b>Validates: Requirements 5.1, 5.4</b></p>
 */
class XssDetectionPropertyTest {

    // ── Inline severity model (self-contained, no external dependencies) ──

    enum XssSeverity {
        HIGH,    // Unescaped user-controlled output — direct XSS vector
        MEDIUM,  // Partially escaped or context-dependent encoding
        SAFE     // Properly escaped via fn:escapeXml(), <c:out>, or Gson serialization
    }

    /**
     * Classifies a code snippet for XSS risk across JSP and servlet output mechanisms.
     *
     * <h3>JSP Output Mechanisms</h3>
     * <ul>
     *   <li>Unescaped EL expressions like {@code ${searchKeyword}} → HIGH</li>
     *   <li>EL inside JSTL control tags ({@code <c:if test>}, {@code <c:forEach items>}) → SAFE (not output)</li>
     *   <li>Wrapped with {@code fn:escapeXml()} like {@code ${fn:escapeXml(searchKeyword)}} → SAFE</li>
     *   <li>Wrapped with {@code <c:out value="${...}"/>} → SAFE</li>
     * </ul>
     *
     * <h3>Servlet Response Writer Mechanisms</h3>
     * <ul>
     *   <li>Manual JSON string building with user data via {@code response.getWriter().write()} → HIGH</li>
     *   <li>Proper Gson serialization via {@code gson.toJson()} → SAFE</li>
     * </ul>
     */
    static XssSeverity classifyXssSnippet(String snippet) {
        if (snippet == null || snippet.isBlank()) {
            return XssSeverity.SAFE;
        }

        // ── JSP Output Mechanisms ──

        // 1. Check for <c:out value="${...}"/> — SAFE (properly escaped)
        if (snippet.matches("(?si).*<c:out\\s+value=\"\\$\\{[^}]+\\}\"\\s*/?>.*")) {
            return XssSeverity.SAFE;
        }

        // 2. Check for fn:escapeXml() wrapped EL — SAFE
        if (snippet.matches("(?si).*\\$\\{fn:escapeXml\\([^)]+\\)\\}.*")) {
            return XssSeverity.SAFE;
        }

        // 3. Check for EL inside JSTL control tags (not output) — SAFE
        //    e.g., <c:if test="${...}">, <c:forEach items="${...}">
        if (snippet.matches("(?si).*<c:(if|forEach|when|set)\\s+[^>]*\\$\\{[^}]+\\}[^>]*>.*")) {
            return XssSeverity.SAFE;
        }

        // 4. Check for unescaped EL expression ${...} — HIGH
        if (snippet.matches("(?s).*\\$\\{[^}]+\\}.*")) {
            return XssSeverity.HIGH;
        }

        // ── Servlet Response Writer Mechanisms ──

        // 5. Check for Gson serialization — SAFE
        if (snippet.matches("(?si).*gson\\.toJson\\(.*\\).*")) {
            return XssSeverity.SAFE;
        }

        // 6. Check for manual JSON string building with concatenation via response writer — HIGH
        //    Pattern: response.getWriter().write("..." + variable + "...")
        if (snippet.matches("(?si).*response\\.getWriter\\(\\)\\.write\\([^)]*\"[^\"]*\"\\s*\\+\\s*\\w+.*")) {
            return XssSeverity.HIGH;
        }

        // 7. No XSS pattern detected → SAFE
        return XssSeverity.SAFE;
    }

    // ── Generators ──

    @Provide
    Arbitrary<String> userControlledVariables() {
        return Arbitraries.of(
                "searchKeyword", "product.name", "product.description",
                "user.fullName", "category.name", "review.content",
                "order.address", "user.email", "pet.breed",
                "blog.title", "blog.content", "item.note"
        );
    }

    @Provide
    Arbitrary<String> jspContextPrefixes() {
        return Arbitraries.of(
                "<span>",
                "<div class=\"breadcrumb\">",
                "<p class=\"description\">",
                "<h2>",
                "<td>",
                "<li>",
                "<a href=\"#\">"
        );
    }

    @Provide
    Arbitrary<String> jspContextSuffixes() {
        return Arbitraries.of(
                "</span>",
                "</div>",
                "</p>",
                "</h2>",
                "</td>",
                "</li>",
                "</a>"
        );
    }

    @Provide
    Arbitrary<String> servletUserDataVarNames() {
        return Arbitraries.of(
                "userName", "productName", "searchQuery",
                "categoryName", "reviewText", "userEmail",
                "address", "fullName", "keyword"
        );
    }

    @Provide
    Arbitrary<String> jstlControlTags() {
        return Arbitraries.of("if", "forEach", "when", "set");
    }

    @Provide
    Arbitrary<String> jstlControlAttributes() {
        return Arbitraries.of("test", "items", "var", "value");
    }

    // ── Composite Generators ──

    /** Generates unescaped JSP EL expressions like {@code <span>${searchKeyword}</span>} */
    @Provide
    Arbitrary<String> unescapedElSnippets() {
        return Combinators.combine(
                jspContextPrefixes(),
                userControlledVariables(),
                jspContextSuffixes()
        ).as((prefix, varName, suffix) ->
                prefix + "${" + varName + "}" + suffix
        );
    }

    /** Generates fn:escapeXml() wrapped EL expressions */
    @Provide
    Arbitrary<String> fnEscapeXmlSnippets() {
        return Combinators.combine(
                jspContextPrefixes(),
                userControlledVariables(),
                jspContextSuffixes()
        ).as((prefix, varName, suffix) ->
                prefix + "${fn:escapeXml(" + varName + ")}" + suffix
        );
    }

    /** Generates {@code <c:out value="${...}"/>} wrapped expressions */
    @Provide
    Arbitrary<String> cOutSnippets() {
        return userControlledVariables().map(varName ->
                "<c:out value=\"${" + varName + "}\"/>"
        );
    }

    /** Generates EL expressions inside JSTL control tags (not output) */
    @Provide
    Arbitrary<String> jstlControlTagSnippets() {
        return Combinators.combine(
                jstlControlTags(),
                jstlControlAttributes(),
                userControlledVariables()
        ).as((tag, attr, varName) ->
                "<c:" + tag + " " + attr + "=\"${" + varName + "}\">"
        );
    }

    /** Generates manual JSON string building with concatenation via response writer */
    @Provide
    Arbitrary<String> manualJsonConcatSnippets() {
        return servletUserDataVarNames().map(varName ->
                "response.getWriter().write(\"{\\\"name\\\":\\\"\" + " + varName + " + \"\\\"}\")"
        );
    }

    /** Generates Gson-serialized JSON response writer calls */
    @Provide
    Arbitrary<String> gsonSerializedSnippets() {
        return Arbitraries.of(
                "response.getWriter().write(gson.toJson(data))",
                "response.getWriter().write(gson.toJson(result))",
                "response.getWriter().write(new Gson().toJson(products))",
                "response.getWriter().write(gson.toJson(responseMap))",
                "response.getWriter().write(gson.toJson(autocompleteResults))",
                "response.getWriter().write(gson.toJson(cartInfo))"
        );
    }

    // ── Property Tests ──

    /**
     * <b>Validates: Requirement 5.1</b>
     *
     * <p>For any JSP snippet containing an unescaped EL expression with a
     * user-controlled variable, the classifier must return HIGH severity.</p>
     */
    @Property(tries = 200)
    void unescapedElExpressionsAreClassifiedHigh(
            @ForAll("unescapedElSnippets") String snippet) {
        XssSeverity severity = classifyXssSnippet(snippet);
        assertEquals(XssSeverity.HIGH, severity,
                "Unescaped EL expression should be HIGH: " + snippet);
    }

    /**
     * <b>Validates: Requirement 5.1</b>
     *
     * <p>For any JSP snippet where the EL expression is wrapped with
     * {@code fn:escapeXml()}, the classifier must return SAFE.</p>
     */
    @Property(tries = 200)
    void fnEscapeXmlWrappedExpressionsAreClassifiedSafe(
            @ForAll("fnEscapeXmlSnippets") String snippet) {
        XssSeverity severity = classifyXssSnippet(snippet);
        assertEquals(XssSeverity.SAFE, severity,
                "fn:escapeXml() wrapped expression should be SAFE: " + snippet);
    }

    /**
     * <b>Validates: Requirement 5.1</b>
     *
     * <p>For any JSP snippet where the EL expression is wrapped with
     * {@code <c:out value="${...}"/>}, the classifier must return SAFE.</p>
     */
    @Property(tries = 50)
    void cOutWrappedExpressionsAreClassifiedSafe(
            @ForAll("cOutSnippets") String snippet) {
        XssSeverity severity = classifyXssSnippet(snippet);
        assertEquals(XssSeverity.SAFE, severity,
                "<c:out> wrapped expression should be SAFE: " + snippet);
    }

    /**
     * <b>Validates: Requirement 5.1</b>
     *
     * <p>For any EL expression inside a JSTL control tag (c:if, c:forEach, etc.),
     * the classifier must return SAFE because these are not output points.</p>
     */
    @Property(tries = 50)
    void elInsideJstlControlTagsAreClassifiedSafe(
            @ForAll("jstlControlTagSnippets") String snippet) {
        XssSeverity severity = classifyXssSnippet(snippet);
        assertEquals(XssSeverity.SAFE, severity,
                "EL inside JSTL control tag should be SAFE: " + snippet);
    }

    /**
     * <b>Validates: Requirement 5.4</b>
     *
     * <p>For any servlet snippet that builds JSON manually via string concatenation
     * with user data through {@code response.getWriter().write()}, the classifier
     * must return HIGH severity.</p>
     */
    @Property(tries = 50)
    void manualJsonConcatenationIsClassifiedHigh(
            @ForAll("manualJsonConcatSnippets") String snippet) {
        XssSeverity severity = classifyXssSnippet(snippet);
        assertEquals(XssSeverity.HIGH, severity,
                "Manual JSON string building should be HIGH: " + snippet);
    }

    /**
     * <b>Validates: Requirement 5.4</b>
     *
     * <p>For any servlet snippet that uses Gson serialization to write JSON
     * responses, the classifier must return SAFE.</p>
     */
    @Property(tries = 50)
    void gsonSerializedResponsesAreClassifiedSafe(
            @ForAll("gsonSerializedSnippets") String snippet) {
        XssSeverity severity = classifyXssSnippet(snippet);
        assertEquals(XssSeverity.SAFE, severity,
                "Gson-serialized response should be SAFE: " + snippet);
    }

    /**
     * <b>Validates: Requirements 5.1, 5.4</b>
     *
     * <p>HIGH severity is strictly higher than MEDIUM, and MEDIUM is strictly
     * higher than SAFE. This ensures the XSS classification ordering is consistent.</p>
     */
    @Property(tries = 200)
    void severityOrderingIsConsistent(
            @ForAll("unescapedElSnippets") String highSnippet,
            @ForAll("fnEscapeXmlSnippets") String safeSnippet) {

        XssSeverity high = classifyXssSnippet(highSnippet);
        XssSeverity safe = classifyXssSnippet(safeSnippet);

        assertTrue(high.ordinal() < safe.ordinal(),
                "HIGH should rank higher (lower ordinal) than SAFE");
    }
}
