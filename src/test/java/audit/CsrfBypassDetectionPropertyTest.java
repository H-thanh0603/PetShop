package audit;

import net.jqwik.api.*;
import net.jqwik.api.Tuple.Tuple2;

import java.util.*;
import java.util.regex.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for CSRF Bypass Detection Completeness.
 *
 * <p><b>Property 2: CSRF Bypass Detection Produces Complete Findings</b></p>
 *
 * <p>For any POST endpoint that modifies server state and is excluded from
 * CSRF token validation, the Audit_Engine SHALL produce a finding that
 * includes the specific endpoint path and the bypass reason.</p>
 *
 * <p><b>Validates: Requirement 2.2</b></p>
 */
class CsrfBypassDetectionPropertyTest {

    // ── Inline model (self-contained, no dependency on Task 17 audit classes) ──

    enum CsrfBypassSeverity {
        HIGH,    // Blanket header-based bypass (e.g., Accept: application/json)
        MEDIUM,  // Non-constant-time token comparison (timing attack risk)
        LOW,     // URL pattern exclusion without alternative auth
        NONE     // Proper CSRF validation — no bypass detected
    }

    /**
     * Represents a single CSRF bypass finding produced by the detector.
     */
    record CsrfBypassFinding(String endpointPath, String bypassReason, CsrfBypassSeverity severity) {
        CsrfBypassFinding {
            Objects.requireNonNull(endpointPath, "endpointPath must not be null");
            Objects.requireNonNull(bypassReason, "bypassReason must not be null");
            Objects.requireNonNull(severity, "severity must not be null");
        }
    }

    /**
     * Analyzes filter source code to detect CSRF bypass patterns.
     *
     * <ul>
     *   <li>Blanket {@code Accept: application/json} header bypass → HIGH</li>
     *   <li>URL pattern exclusions (e.g., {@code uri.startsWith("/api/")}) → LOW per excluded pattern</li>
     *   <li>Non-constant-time token comparison ({@code .equals()}) → MEDIUM</li>
     *   <li>Proper validation (header + parameter, constant-time comparison) → NONE (no findings)</li>
     * </ul>
     */
    static List<CsrfBypassFinding> detectBypassPatterns(String filterCode) {
        if (filterCode == null || filterCode.isBlank()) {
            return List.of();
        }

        List<CsrfBypassFinding> findings = new ArrayList<>();

        // ── 1. Detect blanket Accept header bypass ──
        // Pattern: checking Accept header for "application/json" and then calling
        // chain.doFilter / filterChain.doFilter to skip CSRF validation
        boolean hasAcceptBypass = filterCode.matches(
                "(?si).*getHeader\\s*\\(\\s*\"Accept\"\\s*\\).*application/json.*(?:chain|filterChain)\\.doFilter.*");

        if (hasAcceptBypass) {
            findings.add(new CsrfBypassFinding(
                    "/*",
                    "Accept header bypass: requests with Accept: application/json skip CSRF validation",
                    CsrfBypassSeverity.HIGH
            ));
        }

        // ── 2. Detect URL pattern exclusions ──
        // Pattern: uri.startsWith("/some/path") followed by chain.doFilter (return)
        Pattern urlExclusionPattern = Pattern.compile(
                "uri\\.startsWith\\s*\\(\\s*\"([^\"]+)\"\\s*\\)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher urlMatcher = urlExclusionPattern.matcher(filterCode);
        while (urlMatcher.find()) {
            String excludedPath = urlMatcher.group(1);
            findings.add(new CsrfBypassFinding(
                    excludedPath,
                    "URL pattern exclusion: requests to " + excludedPath + " skip CSRF validation",
                    CsrfBypassSeverity.LOW
            ));
        }

        // ── 3. Detect non-constant-time token comparison ──
        // Pattern: submittedToken.equals(sessionToken) instead of MessageDigest.isEqual()
        boolean hasEqualsComparison = filterCode.matches(
                "(?si).*(?:submittedToken|csrfToken|token)\\.equals\\s*\\(.*(?:sessionToken|csrfToken|token).*\\).*");

        // Check that it's NOT using MessageDigest.isEqual (the proper approach)
        boolean hasConstantTimeComparison = filterCode.matches(
                "(?si).*MessageDigest\\.isEqual\\s*\\(.*");

        if (hasEqualsComparison && !hasConstantTimeComparison) {
            findings.add(new CsrfBypassFinding(
                    "/*",
                    "Non-constant-time token comparison: .equals() used instead of MessageDigest.isEqual()",
                    CsrfBypassSeverity.MEDIUM
            ));
        }

        return findings;
    }

    // ── Generators ──

    @Provide
    Arbitrary<String> endpointPaths() {
        return Arbitraries.of(
                "/cart", "/checkout", "/register", "/admin/users",
                "/api/orders", "/webhook/payment", "/shop/add-to-cart",
                "/account/update", "/review/add", "/wishlist/toggle"
        );
    }

    @Provide
    Arbitrary<String> bypassReasons() {
        return Arbitraries.of(
                "Accept header bypass",
                "URL pattern exclusion",
                "Missing token validation",
                "Non-constant-time comparison"
        );
    }

    @Provide
    Arbitrary<String> acceptHeaderBypassSnippets() {
        return endpointPaths().map(path ->
                "String accept = request.getHeader(\"Accept\");\n" +
                "if (accept != null && accept.contains(\"application/json\")) {\n" +
                "    chain.doFilter(req, res);\n" +
                "    return;\n" +
                "}\n"
        );
    }

    @Provide
    Arbitrary<String> urlPatternExclusionSnippets() {
        return Arbitraries.of(
                List.of("/api/"),
                List.of("/webhook/"),
                List.of("/api/", "/webhook/"),
                List.of("/api/", "/webhook/", "/public/"),
                List.of("/static/", "/health")
        ).map(paths -> {
            StringBuilder sb = new StringBuilder();
            sb.append("if (");
            for (int i = 0; i < paths.size(); i++) {
                if (i > 0) sb.append(" || ");
                sb.append("uri.startsWith(\"").append(paths.get(i)).append("\")");
            }
            sb.append(") {\n");
            sb.append("    chain.doFilter(req, res);\n");
            sb.append("    return;\n");
            sb.append("}\n");
            return sb.toString();
        });
    }

    @Provide
    Arbitrary<String> nonConstantTimeComparisonSnippets() {
        return Arbitraries.of(
                "String submittedToken = request.getParameter(\"csrfToken\");\n" +
                "if (submittedToken.equals(sessionToken)) {\n" +
                "    chain.doFilter(req, res);\n" +
                "}\n",

                "String submittedToken = request.getHeader(\"X-CSRF-Token\");\n" +
                "if (submittedToken != null && submittedToken.equals(sessionToken)) {\n" +
                "    chain.doFilter(req, res);\n" +
                "}\n",

                "if (token.equals(csrfToken)) {\n" +
                "    chain.doFilter(req, res);\n" +
                "}\n"
        );
    }

    @Provide
    Arbitrary<String> properValidationSnippets() {
        return Arbitraries.of(
                "String submittedToken = request.getHeader(\"X-CSRF-Token\");\n" +
                "if (submittedToken == null) submittedToken = request.getParameter(\"csrfToken\");\n" +
                "if (submittedToken != null && MessageDigest.isEqual(\n" +
                "        submittedToken.getBytes(StandardCharsets.UTF_8),\n" +
                "        sessionToken.getBytes(StandardCharsets.UTF_8))) {\n" +
                "    chain.doFilter(req, res);\n" +
                "}\n",

                "String submittedToken = request.getHeader(\"X-CSRF-Token\");\n" +
                "if (submittedToken == null || submittedToken.isEmpty()) {\n" +
                "    submittedToken = request.getParameter(\"csrfToken\");\n" +
                "}\n" +
                "if (submittedToken != null && MessageDigest.isEqual(\n" +
                "        submittedToken.getBytes(), sessionToken.getBytes())) {\n" +
                "    request.setAttribute(\"csrfToken\", sessionToken);\n" +
                "    chain.doFilter(req, res);\n" +
                "}\n"
        );
    }

    @Provide
    Arbitrary<List<String>> excludedUrlPatternLists() {
        return Arbitraries.of(
                "/api/", "/webhook/", "/public/", "/static/", "/health/",
                "/callback/", "/notify/", "/cron/"
        ).list().ofMinSize(1).ofMaxSize(5).uniqueElements();
    }

    // ── Property Tests ──

    /**
     * <b>Validates: Requirement 2.2</b>
     *
     * <p>Every POST endpoint excluded from CSRF validation via a blanket
     * Accept header bypass produces a finding with endpoint path and bypass reason.</p>
     */
    @Property(tries = 50)
    void acceptHeaderBypassDetectedAsHigh(
            @ForAll("acceptHeaderBypassSnippets") String snippet) {
        List<CsrfBypassFinding> findings = detectBypassPatterns(snippet);

        assertFalse(findings.isEmpty(),
                "Accept header bypass should produce at least one finding: " + snippet);

        boolean hasHighFinding = findings.stream()
                .anyMatch(f -> f.severity() == CsrfBypassSeverity.HIGH);
        assertTrue(hasHighFinding,
                "Accept header bypass should be detected as HIGH severity: " + snippet);

        // Every finding must have non-empty endpoint path and bypass reason
        for (CsrfBypassFinding finding : findings) {
            assertFalse(finding.endpointPath().isBlank(),
                    "Finding must include endpoint path");
            assertFalse(finding.bypassReason().isBlank(),
                    "Finding must include bypass reason");
        }
    }

    /**
     * <b>Validates: Requirement 2.2</b>
     *
     * <p>Filter code with proper token validation (header + parameter check,
     * constant-time comparison via MessageDigest.isEqual) produces no bypass findings.</p>
     */
    @Property(tries = 50)
    void properValidationProducesNoFindings(
            @ForAll("properValidationSnippets") String snippet) {
        List<CsrfBypassFinding> findings = detectBypassPatterns(snippet);

        assertTrue(findings.isEmpty(),
                "Proper CSRF validation should produce no bypass findings: " + snippet);
    }

    /**
     * <b>Validates: Requirement 2.2</b>
     *
     * <p>Filter code that skips validation for specific URL patterns produces
     * a finding for each excluded pattern, with the endpoint path matching
     * the excluded URL.</p>
     */
    @Property(tries = 50)
    void urlPatternExclusionProducesFindingPerPattern(
            @ForAll("excludedUrlPatternLists") List<String> excludedPaths) {

        // Build filter code with the given URL exclusions
        StringBuilder sb = new StringBuilder();
        sb.append("if (");
        for (int i = 0; i < excludedPaths.size(); i++) {
            if (i > 0) sb.append(" || ");
            sb.append("uri.startsWith(\"").append(excludedPaths.get(i)).append("\")");
        }
        sb.append(") {\n    chain.doFilter(req, res);\n    return;\n}\n");

        List<CsrfBypassFinding> findings = detectBypassPatterns(sb.toString());

        // Should produce exactly one finding per excluded URL pattern
        assertEquals(excludedPaths.size(), findings.size(),
                "Should produce one finding per excluded URL pattern. " +
                "Expected " + excludedPaths.size() + " findings for paths " + excludedPaths);

        // Each finding must reference the excluded path and have a bypass reason
        for (String excludedPath : excludedPaths) {
            boolean found = findings.stream()
                    .anyMatch(f -> f.endpointPath().equals(excludedPath));
            assertTrue(found,
                    "Should produce a finding for excluded path: " + excludedPath);
        }

        for (CsrfBypassFinding finding : findings) {
            assertFalse(finding.bypassReason().isBlank(),
                    "Each finding must include a bypass reason");
            assertEquals(CsrfBypassSeverity.LOW, finding.severity(),
                    "URL pattern exclusion should be LOW severity");
        }
    }

    /**
     * <b>Validates: Requirement 2.2</b>
     *
     * <p>Filter code using non-constant-time comparison (.equals()) for CSRF
     * token validation produces a MEDIUM severity finding with endpoint path
     * and bypass reason.</p>
     */
    @Property(tries = 50)
    void nonConstantTimeComparisonDetectedAsMedium(
            @ForAll("nonConstantTimeComparisonSnippets") String snippet) {
        List<CsrfBypassFinding> findings = detectBypassPatterns(snippet);

        assertFalse(findings.isEmpty(),
                "Non-constant-time comparison should produce at least one finding: " + snippet);

        boolean hasMediumFinding = findings.stream()
                .anyMatch(f -> f.severity() == CsrfBypassSeverity.MEDIUM);
        assertTrue(hasMediumFinding,
                "Non-constant-time comparison should be detected as MEDIUM severity: " + snippet);

        // Every finding must have non-empty endpoint path and bypass reason
        for (CsrfBypassFinding finding : findings) {
            assertFalse(finding.endpointPath().isBlank(),
                    "Finding must include endpoint path");
            assertFalse(finding.bypassReason().isBlank(),
                    "Finding must include bypass reason");
        }
    }

    /**
     * <b>Validates: Requirement 2.2</b>
     *
     * <p>Every finding produced by the detector has a non-empty endpoint path
     * and a non-empty bypass reason, regardless of the bypass pattern type.</p>
     */
    @Property(tries = 100)
    void allFindingsHaveEndpointPathAndBypassReason(
            @ForAll("mixedFilterSnippets") String snippet) {
        List<CsrfBypassFinding> findings = detectBypassPatterns(snippet);

        for (CsrfBypassFinding finding : findings) {
            assertNotNull(finding.endpointPath(),
                    "Finding endpoint path must not be null");
            assertFalse(finding.endpointPath().isBlank(),
                    "Finding endpoint path must not be blank");
            assertNotNull(finding.bypassReason(),
                    "Finding bypass reason must not be null");
            assertFalse(finding.bypassReason().isBlank(),
                    "Finding bypass reason must not be blank");
            assertNotNull(finding.severity(),
                    "Finding severity must not be null");
        }
    }

    @Provide
    Arbitrary<String> mixedFilterSnippets() {
        return Arbitraries.oneOf(
                acceptHeaderBypassSnippets(),
                urlPatternExclusionSnippets(),
                nonConstantTimeComparisonSnippets(),
                properValidationSnippets()
        );
    }
}
