package audit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;

import java.util.*;
import java.util.regex.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Cookie Security Flag Detection.
 *
 * <p><b>Property 3: Cookie Security Flag Detection</b></p>
 *
 * <p>For any servlet or filter source code that creates a {@code Cookie} object
 * and adds it to the response, the Audit_Engine SHALL:</p>
 * <ul>
 *   <li>Produce a HIGH severity finding when the {@code Secure} flag is absent or
 *       explicitly commented out.</li>
 *   <li>Produce a MEDIUM severity finding when the {@code HttpOnly} flag is absent.</li>
 *   <li>Produce no finding when both {@code Secure} and {@code HttpOnly} flags are set.</li>
 *   <li>Produce a LOW severity finding when {@code SameSite} is absent (informational).</li>
 * </ul>
 *
 * <p><b>Validates: Requirement 3.2</b></p>
 */
class CookieSecurityFlagPropertyTest {

    // ── Inline model ──────────────────────────────────────────────────────────

    enum CookieFindingSeverity {
        HIGH,   // Missing Secure flag — cookie transmitted over plain HTTP
        MEDIUM, // Missing HttpOnly flag — cookie accessible via JavaScript
        LOW,    // Missing SameSite attribute — CSRF risk (informational)
        NONE    // All required flags present
    }

    /**
     * A single finding produced by the cookie security detector.
     *
     * @param cookieName  name of the cookie variable or literal (may be "unknown" if not determinable)
     * @param missingFlag the flag that is absent (e.g., "Secure", "HttpOnly", "SameSite")
     * @param severity    severity of the finding
     */
    record CookieSecurityFinding(String cookieName, String missingFlag, CookieFindingSeverity severity) {
        CookieSecurityFinding {
            Objects.requireNonNull(cookieName, "cookieName must not be null");
            Objects.requireNonNull(missingFlag, "missingFlag must not be null");
            Objects.requireNonNull(severity, "severity must not be null");
        }
    }

    // ── Detector ─────────────────────────────────────────────────────────────

    /**
     * Analyses a snippet of Java servlet source code for cookie security flag issues.
     *
     * <p>Detection rules (applied per {@code new Cookie(...)} block found in the snippet):</p>
     * <ol>
     *   <li>If {@code setSecure(true)} is absent (or only present as a comment) → HIGH</li>
     *   <li>If {@code setHttpOnly(true)} is absent → MEDIUM</li>
     *   <li>If {@code SameSite} is not set via {@code response.setHeader} / {@code addHeader} → LOW</li>
     * </ol>
     *
     * @param servletCode Java source snippet to analyse
     * @return list of findings; empty when all flags are properly set
     */
    static List<CookieSecurityFinding> detectCookieSecurityIssues(String servletCode) {
        if (servletCode == null || servletCode.isBlank()) {
            return List.of();
        }

        List<CookieSecurityFinding> findings = new ArrayList<>();

        // Strip single-line comments so commented-out setSecure() is not counted as present
        String strippedCode = servletCode.replaceAll("//[^\n]*", "");

        // ── 1. Detect missing Secure flag ──────────────────────────────────
        // A Cookie is created but setSecure(true) is never called
        boolean hasCookieCreation = strippedCode.matches("(?si).*new\\s+Cookie\\s*\\(.*");
        boolean hasSetSecureTrue  = strippedCode.matches("(?si).*\\.setSecure\\s*\\(\\s*true\\s*\\).*");

        if (hasCookieCreation && !hasSetSecureTrue) {
            findings.add(new CookieSecurityFinding(
                    extractCookieName(strippedCode),
                    "Secure",
                    CookieFindingSeverity.HIGH
            ));
        }

        // ── 2. Detect missing HttpOnly flag ───────────────────────────────
        boolean hasSetHttpOnlyTrue = strippedCode.matches("(?si).*\\.setHttpOnly\\s*\\(\\s*true\\s*\\).*");

        if (hasCookieCreation && !hasSetHttpOnlyTrue) {
            findings.add(new CookieSecurityFinding(
                    extractCookieName(strippedCode),
                    "HttpOnly",
                    CookieFindingSeverity.MEDIUM
            ));
        }

        // ── 3. Detect missing SameSite attribute ──────────────────────────
        // SameSite must be set via response header manipulation (Servlet API limitation)
        boolean hasSameSite = strippedCode.matches("(?si).*SameSite.*");

        if (hasCookieCreation && !hasSameSite) {
            findings.add(new CookieSecurityFinding(
                    extractCookieName(strippedCode),
                    "SameSite",
                    CookieFindingSeverity.LOW
            ));
        }

        return findings;
    }

    /** Extracts the first cookie variable name from the snippet, or returns "unknown". */
    private static String extractCookieName(String code) {
        Matcher m = Pattern.compile("new\\s+Cookie\\s*\\(\\s*\"([^\"]+)\"").matcher(code);
        return m.find() ? m.group(1) : "unknown";
    }

    // ── Generators ───────────────────────────────────────────────────────────

    /** Cookie names representative of real-world session / auth cookies. */
    @Provide
    Arbitrary<String> cookieNames() {
        return Arbitraries.of(
                "remember_token", "JSESSIONID", "auth_token",
                "session_id", "user_pref", "tracking_id"
        );
    }

    /** Snippets that set NEITHER Secure NOR HttpOnly — should produce HIGH + MEDIUM findings. */
    @Provide
    Arbitrary<String> insecureCookieSnippets() {
        return cookieNames().map(name ->
                "Cookie cookie = new Cookie(\"" + name + "\", token);\n" +
                "cookie.setMaxAge(7 * 24 * 60 * 60);\n" +
                "cookie.setPath(\"/\");\n" +
                "response.addCookie(cookie);\n"
        );
    }

    /** Snippets that set HttpOnly but NOT Secure — should produce a HIGH finding. */
    @Provide
    Arbitrary<String> httpOnlyButNotSecureSnippets() {
        return cookieNames().map(name ->
                "Cookie cookie = new Cookie(\"" + name + "\", token);\n" +
                "cookie.setMaxAge(7 * 24 * 60 * 60);\n" +
                "cookie.setPath(\"/\");\n" +
                "cookie.setHttpOnly(true);\n" +
                "response.addCookie(cookie);\n"
        );
    }

    /** Snippets where setSecure(true) is commented out — should still produce HIGH. */
    @Provide
    Arbitrary<String> commentedOutSecureSnippets() {
        return cookieNames().map(name ->
                "Cookie cookie = new Cookie(\"" + name + "\", token);\n" +
                "cookie.setMaxAge(7 * 24 * 60 * 60);\n" +
                "cookie.setPath(\"/\");\n" +
                "cookie.setHttpOnly(true);\n" +
                "// cookie.setSecure(true); // disabled in dev\n" +
                "response.addCookie(cookie);\n"
        );
    }

    /** Snippets that set both Secure and HttpOnly but omit SameSite — should produce LOW only. */
    @Provide
    Arbitrary<String> secureAndHttpOnlyButNoSameSiteSnippets() {
        return cookieNames().map(name ->
                "Cookie cookie = new Cookie(\"" + name + "\", token);\n" +
                "cookie.setMaxAge(7 * 24 * 60 * 60);\n" +
                "cookie.setPath(\"/\");\n" +
                "cookie.setHttpOnly(true);\n" +
                "cookie.setSecure(true);\n" +
                "response.addCookie(cookie);\n"
        );
    }

    /** Fully secure snippets — Secure, HttpOnly, and SameSite=Lax all present. */
    @Provide
    Arbitrary<String> fullySecureCookieSnippets() {
        return cookieNames().map(name ->
                "Cookie cookie = new Cookie(\"" + name + "\", token);\n" +
                "cookie.setMaxAge(7 * 24 * 60 * 60);\n" +
                "cookie.setPath(\"/\");\n" +
                "cookie.setHttpOnly(true);\n" +
                "cookie.setSecure(true);\n" +
                "response.addCookie(cookie);\n" +
                "// Append SameSite=Lax via header rewrite\n" +
                "for (String h : response.getHeaders(\"Set-Cookie\")) {\n" +
                "    if (h.startsWith(\"" + name + "=\")) {\n" +
                "        response.setHeader(\"Set-Cookie\", h + \"; SameSite=Lax\");\n" +
                "    }\n" +
                "}\n"
        );
    }

    // ── Properties ───────────────────────────────────────────────────────────

    /**
     * <b>Validates: Requirement 3.2</b>
     *
     * <p>Any cookie created without {@code setSecure(true)} produces a HIGH severity finding.</p>
     */
    @Property(tries = 50)
    void missingSecureFlagDetectedAsHigh(
            @ForAll("httpOnlyButNotSecureSnippets") String snippet) {

        List<CookieSecurityFinding> findings = detectCookieSecurityIssues(snippet);

        assertFalse(findings.isEmpty(),
                "Missing Secure flag should produce at least one finding:\n" + snippet);

        boolean hasHighFinding = findings.stream()
                .anyMatch(f -> f.severity() == CookieFindingSeverity.HIGH
                            && "Secure".equals(f.missingFlag()));
        assertTrue(hasHighFinding,
                "Missing Secure flag should produce a HIGH severity finding:\n" + snippet);
    }

    /**
     * <b>Validates: Requirement 3.2</b>
     *
     * <p>A commented-out {@code setSecure(true)} is treated as absent and still
     * produces a HIGH severity finding.</p>
     */
    @Property(tries = 50)
    void commentedOutSecureFlagDetectedAsHigh(
            @ForAll("commentedOutSecureSnippets") String snippet) {

        List<CookieSecurityFinding> findings = detectCookieSecurityIssues(snippet);

        boolean hasHighFinding = findings.stream()
                .anyMatch(f -> f.severity() == CookieFindingSeverity.HIGH
                            && "Secure".equals(f.missingFlag()));
        assertTrue(hasHighFinding,
                "Commented-out Secure flag should still be detected as HIGH:\n" + snippet);
    }

    /**
     * <b>Validates: Requirement 3.2</b>
     *
     * <p>A cookie with neither Secure nor HttpOnly produces both a HIGH and a MEDIUM finding.</p>
     */
    @Property(tries = 50)
    void fullyInsecureCookieProducesHighAndMedium(
            @ForAll("insecureCookieSnippets") String snippet) {

        List<CookieSecurityFinding> findings = detectCookieSecurityIssues(snippet);

        boolean hasHigh = findings.stream()
                .anyMatch(f -> f.severity() == CookieFindingSeverity.HIGH);
        boolean hasMedium = findings.stream()
                .anyMatch(f -> f.severity() == CookieFindingSeverity.MEDIUM);

        assertTrue(hasHigh,
                "Fully insecure cookie should produce a HIGH finding:\n" + snippet);
        assertTrue(hasMedium,
                "Fully insecure cookie should produce a MEDIUM finding:\n" + snippet);
    }

    /**
     * <b>Validates: Requirement 3.2</b>
     *
     * <p>A cookie with Secure and HttpOnly but no SameSite produces only a LOW finding
     * (no HIGH or MEDIUM).</p>
     */
    @Property(tries = 50)
    void secureAndHttpOnlyWithoutSameSiteProducesLowOnly(
            @ForAll("secureAndHttpOnlyButNoSameSiteSnippets") String snippet) {

        List<CookieSecurityFinding> findings = detectCookieSecurityIssues(snippet);

        boolean hasHigh = findings.stream()
                .anyMatch(f -> f.severity() == CookieFindingSeverity.HIGH);
        boolean hasMedium = findings.stream()
                .anyMatch(f -> f.severity() == CookieFindingSeverity.MEDIUM);
        boolean hasLow = findings.stream()
                .anyMatch(f -> f.severity() == CookieFindingSeverity.LOW
                            && "SameSite".equals(f.missingFlag()));

        assertFalse(hasHigh,
                "Cookie with Secure+HttpOnly should not produce a HIGH finding:\n" + snippet);
        assertFalse(hasMedium,
                "Cookie with Secure+HttpOnly should not produce a MEDIUM finding:\n" + snippet);
        assertTrue(hasLow,
                "Cookie without SameSite should produce a LOW finding:\n" + snippet);
    }

    /**
     * <b>Validates: Requirement 3.2</b>
     *
     * <p>A fully secure cookie (Secure + HttpOnly + SameSite) produces no findings.</p>
     */
    @Property(tries = 50)
    void fullySecureCookieProducesNoFindings(
            @ForAll("fullySecureCookieSnippets") String snippet) {

        List<CookieSecurityFinding> findings = detectCookieSecurityIssues(snippet);

        assertTrue(findings.isEmpty(),
                "Fully secure cookie (Secure + HttpOnly + SameSite) should produce no findings:\n"
                + snippet + "\nFindings: " + findings);
    }

    /**
     * <b>Validates: Requirement 3.2</b>
     *
     * <p>Every finding produced by the detector has a non-null, non-blank cookie name,
     * missing flag description, and severity — regardless of the input snippet.</p>
     */
    @Property(tries = 100)
    void allFindingsHaveRequiredFields(
            @ForAll("anyCookieSnippet") String snippet) {

        List<CookieSecurityFinding> findings = detectCookieSecurityIssues(snippet);

        for (CookieSecurityFinding finding : findings) {
            assertNotNull(finding.cookieName(),   "cookieName must not be null");
            assertFalse(finding.cookieName().isBlank(), "cookieName must not be blank");
            assertNotNull(finding.missingFlag(),  "missingFlag must not be null");
            assertFalse(finding.missingFlag().isBlank(), "missingFlag must not be blank");
            assertNotNull(finding.severity(),     "severity must not be null");
        }
    }

    /**
     * <b>Validates: Requirement 3.2</b>
     *
     * <p>The severity ordering is respected: HIGH findings are only produced for
     * missing Secure flags, MEDIUM for missing HttpOnly, LOW for missing SameSite.</p>
     */
    @Property(tries = 100)
    void severityMatchesMissingFlag(
            @ForAll("anyCookieSnippet") String snippet) {

        List<CookieSecurityFinding> findings = detectCookieSecurityIssues(snippet);

        for (CookieSecurityFinding finding : findings) {
            switch (finding.severity()) {
                case HIGH ->
                    assertEquals("Secure", finding.missingFlag(),
                            "HIGH severity must correspond to missing Secure flag");
                case MEDIUM ->
                    assertEquals("HttpOnly", finding.missingFlag(),
                            "MEDIUM severity must correspond to missing HttpOnly flag");
                case LOW ->
                    assertEquals("SameSite", finding.missingFlag(),
                            "LOW severity must correspond to missing SameSite attribute");
                case NONE ->
                    fail("NONE severity should never appear in the findings list");
            }
        }
    }

    @Provide
    Arbitrary<String> anyCookieSnippet() {
        return Arbitraries.oneOf(
                insecureCookieSnippets(),
                httpOnlyButNotSecureSnippets(),
                commentedOutSecureSnippets(),
                secureAndHttpOnlyButNoSameSiteSnippets(),
                fullySecureCookieSnippets()
        );
    }
}
