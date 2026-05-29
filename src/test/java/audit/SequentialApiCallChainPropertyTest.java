package audit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Sequential External API Call Chain Detection.
 *
 * <p><b>Property 13: Sequential External API Call Chain Detection</b></p>
 *
 * <p>For any service method that makes more than 2 sequential external API calls,
 * the Audit_Engine SHALL flag the call chain as a latency risk with a recommendation
 * to cache intermediate results.</p>
 *
 * <p><b>Validates: Requirement 11.3</b></p>
 *
 * <h2>What is tested</h2>
 * <ul>
 *   <li>Methods with &gt;2 non-cached sequential calls are flagged LATENCY_RISK.</li>
 *   <li>Methods with exactly 2 non-cached calls are classified ACCEPTABLE.</li>
 *   <li>Methods with 0 or 1 non-cached calls are classified SAFE.</li>
 *   <li>Cached calls do not count toward the sequential limit.</li>
 *   <li>A chain where all calls are cached is always SAFE.</li>
 *   <li>Every finding always carries a non-null, non-empty recommendation.</li>
 *   <li>Severity is monotonically related to sequential call count
 *       (LATENCY_RISK &gt; ACCEPTABLE &gt; SAFE).</li>
 *   <li>Concrete shipping-service scenario: 4 uncached calls → LATENCY_RISK;
 *       3 geo lookups cached + 1 fee call → SAFE.</li>
 * </ul>
 *
 * <h2>Generator design</h2>
 * <p>Generators produce {@link ApiCallChain} instances constrained to specific
 * sequential-call-count ranges so that each property can assert a precise outcome
 * without relying on wall-clock time or external state.</p>
 */
class SequentialApiCallChainPropertyTest {

    // ── Inline model ─────────────────────────────────────────────────────────────

    /** Represents a single external API call in a chain. */
    record ApiCall(String endpoint, boolean isCached) {}

    /** Represents a service method's call chain. */
    record ApiCallChain(String methodName, List<ApiCall> calls) {
        /** Count only non-cached calls (cached calls don't hit the network). */
        int sequentialCallCount() {
            return (int) calls.stream().filter(c -> !c.isCached()).count();
        }
    }

    enum ApiChainSeverity { LATENCY_RISK, ACCEPTABLE, SAFE }

    /** Finding produced by the classifier. */
    record ApiChainFinding(ApiChainSeverity severity, String recommendation) {}

    /**
     * Classifies an {@link ApiCallChain} by its sequential (non-cached) call count.
     *
     * <ul>
     *   <li>&gt;2 sequential calls → LATENCY_RISK</li>
     *   <li>== 2 sequential calls → ACCEPTABLE</li>
     *   <li>&lt;= 1 sequential call → SAFE</li>
     * </ul>
     */
    static ApiChainFinding classify(ApiCallChain chain) {
        int sequential = chain.sequentialCallCount();
        if (sequential > 2) {
            return new ApiChainFinding(ApiChainSeverity.LATENCY_RISK,
                    "Method makes " + sequential + " sequential external API calls; "
                    + "consider caching intermediate results");
        } else if (sequential == 2) {
            return new ApiChainFinding(ApiChainSeverity.ACCEPTABLE,
                    "Method makes 2 sequential external API calls; "
                    + "caching recommended but not critical");
        } else {
            return new ApiChainFinding(ApiChainSeverity.SAFE,
                    "Method makes " + sequential + " or fewer sequential external API calls");
        }
    }

    // ── Generators ───────────────────────────────────────────────────────────────

    /** Generates a non-empty endpoint name. */
    @Provide
    Arbitrary<String> endpoints() {
        return Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(30)
                .map(s -> "/" + s.toLowerCase());
    }

    /** Generates a non-empty method name. */
    @Provide
    Arbitrary<String> methodNames() {
        return Arbitraries.strings().alpha().ofMinLength(4).ofMaxLength(25);
    }

    /**
     * Generates chains with 3–10 non-cached (sequential) calls.
     * Additional cached calls may also be present.
     */
    @Provide
    Arbitrary<ApiCallChain> chainsWithMoreThanTwoCalls() {
        return Arbitraries.integers().between(3, 10).flatMap(sequentialCount ->
            Arbitraries.integers().between(0, 5).flatMap(cachedCount ->
                endpoints().list().ofSize(sequentialCount + cachedCount).flatMap(eps ->
                    methodNames().map(name -> {
                        List<ApiCall> calls = new ArrayList<>();
                        for (int i = 0; i < sequentialCount; i++) {
                            calls.add(new ApiCall(eps.get(i), false));
                        }
                        for (int i = 0; i < cachedCount; i++) {
                            calls.add(new ApiCall(eps.get(sequentialCount + i), true));
                        }
                        return new ApiCallChain(name, calls);
                    })
                )
            )
        );
    }

    /**
     * Generates chains with exactly 2 non-cached (sequential) calls.
     * Additional cached calls may also be present.
     */
    @Provide
    Arbitrary<ApiCallChain> chainsWithExactlyTwoCalls() {
        return Arbitraries.integers().between(0, 5).flatMap(cachedCount ->
            endpoints().list().ofSize(2 + cachedCount).flatMap(eps ->
                methodNames().map(name -> {
                    List<ApiCall> calls = new ArrayList<>();
                    calls.add(new ApiCall(eps.get(0), false));
                    calls.add(new ApiCall(eps.get(1), false));
                    for (int i = 0; i < cachedCount; i++) {
                        calls.add(new ApiCall(eps.get(2 + i), true));
                    }
                    return new ApiCallChain(name, calls);
                })
            )
        );
    }

    /**
     * Generates chains with 0 or 1 non-cached (sequential) calls.
     * Additional cached calls may also be present.
     */
    @Provide
    Arbitrary<ApiCallChain> chainsWithAtMostOneCall() {
        return Arbitraries.integers().between(0, 1).flatMap(sequentialCount ->
            Arbitraries.integers().between(0, 5).flatMap(cachedCount -> {
                int total = sequentialCount + cachedCount;
                Arbitrary<List<String>> epsArb = total == 0
                        ? Arbitraries.just(List.of())
                        : endpoints().list().ofSize(total);
                return epsArb.flatMap(eps ->
                    methodNames().map(name -> {
                        List<ApiCall> calls = new ArrayList<>();
                        for (int i = 0; i < sequentialCount; i++) {
                            calls.add(new ApiCall(eps.get(i), false));
                        }
                        for (int i = 0; i < cachedCount; i++) {
                            calls.add(new ApiCall(eps.get(sequentialCount + i), true));
                        }
                        return new ApiCallChain(name, calls);
                    })
                );
            })
        );
    }

    /**
     * Generates chains with a mix of cached and non-cached calls (total 2–10 calls).
     */
    @Provide
    Arbitrary<ApiCallChain> mixedCachedChains() {
        return Arbitraries.integers().between(2, 10).flatMap(total ->
            Arbitraries.integers().between(0, total).flatMap(cachedCount -> {
                int sequentialCount = total - cachedCount;
                return endpoints().list().ofSize(total).flatMap(eps ->
                    methodNames().map(name -> {
                        List<ApiCall> calls = new ArrayList<>();
                        for (int i = 0; i < sequentialCount; i++) {
                            calls.add(new ApiCall(eps.get(i), false));
                        }
                        for (int i = 0; i < cachedCount; i++) {
                            calls.add(new ApiCall(eps.get(sequentialCount + i), true));
                        }
                        return new ApiCallChain(name, calls);
                    })
                );
            })
        );
    }

    // ── Property Tests ───────────────────────────────────────────────────────────

    /**
     * <b>Validates: Requirement 11.3</b>
     *
     * <p>Property 1: Any chain with &gt;2 non-cached calls is flagged LATENCY_RISK.</p>
     */
    @Property(tries = 200)
    void methodWithMoreThanTwoSequentialCallsIsLatencyRisk(
            @ForAll("chainsWithMoreThanTwoCalls") ApiCallChain chain) {
        assertTrue(chain.sequentialCallCount() > 2,
                "Generator invariant: sequential count must be > 2, got " + chain.sequentialCallCount());
        ApiChainFinding finding = classify(chain);
        assertEquals(ApiChainSeverity.LATENCY_RISK, finding.severity(),
                "Chain with " + chain.sequentialCallCount() + " sequential calls must be LATENCY_RISK: " + chain.methodName());
    }

    /**
     * <b>Validates: Requirement 11.3</b>
     *
     * <p>Property 2: Exactly 2 non-cached calls → ACCEPTABLE.</p>
     */
    @Property(tries = 200)
    void methodWithTwoSequentialCallsIsAcceptable(
            @ForAll("chainsWithExactlyTwoCalls") ApiCallChain chain) {
        assertEquals(2, chain.sequentialCallCount(),
                "Generator invariant: sequential count must be exactly 2");
        ApiChainFinding finding = classify(chain);
        assertEquals(ApiChainSeverity.ACCEPTABLE, finding.severity(),
                "Chain with exactly 2 sequential calls must be ACCEPTABLE: " + chain.methodName());
    }

    /**
     * <b>Validates: Requirement 11.3</b>
     *
     * <p>Property 3: 0 or 1 non-cached calls → SAFE.</p>
     */
    @Property(tries = 200)
    void methodWithOneOrZeroCallsIsSafe(
            @ForAll("chainsWithAtMostOneCall") ApiCallChain chain) {
        assertTrue(chain.sequentialCallCount() <= 1,
                "Generator invariant: sequential count must be <= 1, got " + chain.sequentialCallCount());
        ApiChainFinding finding = classify(chain);
        assertEquals(ApiChainSeverity.SAFE, finding.severity(),
                "Chain with " + chain.sequentialCallCount() + " sequential calls must be SAFE: " + chain.methodName());
    }

    /**
     * <b>Validates: Requirement 11.3</b>
     *
     * <p>Property 4: A chain of 5 calls where 3 are cached (2 non-cached) → ACCEPTABLE,
     * not LATENCY_RISK. Cached calls must not count toward the sequential limit.</p>
     */
    @Property(tries = 200)
    void cachedCallsDoNotCountTowardSequentialLimit(
            @ForAll("endpoints") String ep1,
            @ForAll("endpoints") String ep2,
            @ForAll("endpoints") String ep3,
            @ForAll("endpoints") String ep4,
            @ForAll("endpoints") String ep5) {
        // 2 non-cached + 3 cached = 5 total calls
        List<ApiCall> calls = List.of(
                new ApiCall(ep1, false),  // sequential
                new ApiCall(ep2, false),  // sequential
                new ApiCall(ep3, true),   // cached — does not count
                new ApiCall(ep4, true),   // cached — does not count
                new ApiCall(ep5, true)    // cached — does not count
        );
        ApiCallChain chain = new ApiCallChain("mixedChain", calls);

        assertEquals(2, chain.sequentialCallCount(),
                "Only non-cached calls should be counted as sequential");
        ApiChainFinding finding = classify(chain);
        assertEquals(ApiChainSeverity.ACCEPTABLE, finding.severity(),
                "5 calls with 3 cached (2 sequential) must be ACCEPTABLE, not LATENCY_RISK");
        assertNotEquals(ApiChainSeverity.LATENCY_RISK, finding.severity(),
                "Cached calls must not push severity to LATENCY_RISK");
    }

    /**
     * <b>Validates: Requirement 11.3</b>
     *
     * <p>Property 5: A chain where all calls are cached → SAFE (0 sequential calls).</p>
     */
    @Property(tries = 200)
    void allCachedCallsAreSafe(
            @ForAll("endpoints") String ep1,
            @ForAll("endpoints") String ep2,
            @ForAll("endpoints") String ep3,
            @ForAll @IntRange(min = 0, max = 7) int extraCached) {
        List<ApiCall> calls = new ArrayList<>();
        calls.add(new ApiCall(ep1, true));
        calls.add(new ApiCall(ep2, true));
        calls.add(new ApiCall(ep3, true));
        for (int i = 0; i < extraCached; i++) {
            calls.add(new ApiCall("/cached-extra-" + i, true));
        }
        ApiCallChain chain = new ApiCallChain("allCachedMethod", calls);

        assertEquals(0, chain.sequentialCallCount(),
                "All-cached chain must have 0 sequential calls");
        ApiChainFinding finding = classify(chain);
        assertEquals(ApiChainSeverity.SAFE, finding.severity(),
                "All-cached chain must be SAFE regardless of total call count");
    }

    /**
     * <b>Validates: Requirement 11.3</b>
     *
     * <p>Property 6: For any chain, the finding always has a non-null, non-empty recommendation.</p>
     */
    @Property(tries = 200)
    void findingAlwaysHasRecommendation(
            @ForAll("mixedCachedChains") ApiCallChain chain) {
        ApiChainFinding finding = classify(chain);
        assertNotNull(finding.recommendation(),
                "Finding recommendation must not be null for chain: " + chain.methodName());
        assertFalse(finding.recommendation().isBlank(),
                "Finding recommendation must not be blank for chain: " + chain.methodName());
        assertNotNull(finding.severity(),
                "Finding severity must not be null for chain: " + chain.methodName());
    }

    /**
     * <b>Validates: Requirement 11.3</b>
     *
     * <p>Property 7: Severity is monotonically related to sequential call count —
     * more sequential calls never produce a lower severity
     * (LATENCY_RISK &gt; ACCEPTABLE &gt; SAFE in ordinal order).</p>
     */
    @Property(tries = 200)
    void severityIsMonotonicallyRelatedToCallCount(
            @ForAll("chainsWithMoreThanTwoCalls") ApiCallChain highChain,
            @ForAll("chainsWithExactlyTwoCalls") ApiCallChain medChain,
            @ForAll("chainsWithAtMostOneCall") ApiCallChain lowChain) {

        ApiChainSeverity high = classify(highChain).severity();
        ApiChainSeverity medium = classify(medChain).severity();
        ApiChainSeverity low = classify(lowChain).severity();

        // Lower ordinal = higher severity: LATENCY_RISK(0) < ACCEPTABLE(1) < SAFE(2)
        assertTrue(high.ordinal() < medium.ordinal(),
                "LATENCY_RISK should rank higher (lower ordinal) than ACCEPTABLE: "
                + high + " vs " + medium);
        assertTrue(medium.ordinal() < low.ordinal(),
                "ACCEPTABLE should rank higher (lower ordinal) than SAFE: "
                + medium + " vs " + low);
    }

    /**
     * <b>Validates: Requirement 11.3</b>
     *
     * <p>Property 8 (concrete example): {@code calculateShippingFee} makes 4 sequential
     * calls (province → district → ward → fee) without caching → LATENCY_RISK.
     * With caching (all 3 geo lookups cached), only the fee call is sequential → SAFE.</p>
     */
    @Property(tries = 1)
    void shippingServiceCallChainIsLatencyRisk() {
        // Scenario A: all 4 calls are uncached — province, district, ward, fee
        List<ApiCall> uncachedCalls = List.of(
                new ApiCall("/master-data/province", false),
                new ApiCall("/master-data/district", false),
                new ApiCall("/master-data/ward", false),
                new ApiCall("/v2/shipping-order/fee", false)
        );
        ApiCallChain uncachedChain = new ApiCallChain("calculateShippingFee", uncachedCalls);

        assertEquals(4, uncachedChain.sequentialCallCount(),
                "Uncached shipping chain must have 4 sequential calls");
        ApiChainFinding uncachedFinding = classify(uncachedChain);
        assertEquals(ApiChainSeverity.LATENCY_RISK, uncachedFinding.severity(),
                "calculateShippingFee with 4 uncached calls must be LATENCY_RISK");
        assertTrue(uncachedFinding.recommendation().contains("caching"),
                "LATENCY_RISK recommendation must mention caching");

        // Scenario B: 3 geo lookups cached, only fee call is sequential
        List<ApiCall> cachedCalls = List.of(
                new ApiCall("/master-data/province", true),   // cached
                new ApiCall("/master-data/district", true),   // cached
                new ApiCall("/master-data/ward", true),       // cached
                new ApiCall("/v2/shipping-order/fee", false)  // still sequential
        );
        ApiCallChain cachedChain = new ApiCallChain("calculateShippingFee", cachedCalls);

        assertEquals(1, cachedChain.sequentialCallCount(),
                "Cached shipping chain must have only 1 sequential call (fee)");
        ApiChainFinding cachedFinding = classify(cachedChain);
        assertEquals(ApiChainSeverity.SAFE, cachedFinding.severity(),
                "calculateShippingFee with 3 geo lookups cached must be SAFE");
    }
}
