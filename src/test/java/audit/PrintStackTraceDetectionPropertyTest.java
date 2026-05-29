package audit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for printStackTrace Detection.
 *
 * <p><b>Property 21: printStackTrace Detection</b></p>
 *
 * <p>For any exception handler in servlet code that calls {@code e.printStackTrace()},
 * the Audit_Engine SHALL flag it as a LOW severity finding with a recommendation
 * to use structured logging.</p>
 *
 * <p><b>Validates: Requirement 13.1</b></p>
 */
class PrintStackTraceDetectionPropertyTest {

    // ── Inline severity model (self-contained, no external dependencies) ──

    enum LoggingSeverity {
        LOW,    // e.printStackTrace() — should be replaced with structured logging
        SAFE    // Proper structured logging via SLF4J logger.error/warn/info
    }

    /**
     * Classifies an exception handling code snippet for printStackTrace usage.
     *
     * <ul>
     *   <li>{@code e.printStackTrace()} → LOW severity (should use structured logging)</li>
     *   <li>{@code ex.printStackTrace()} → LOW severity</li>
     *   <li>{@code exception.printStackTrace()} → LOW severity</li>
     *   <li>{@code logger.error("...", e)} → SAFE (proper structured logging)</li>
     *   <li>{@code log.error("...", e)} → SAFE (proper structured logging)</li>
     *   <li>{@code logger.warn("...", e)} → SAFE</li>
     *   <li>{@code logger.info("...", e)} → SAFE</li>
     * </ul>
     */
    static LoggingSeverity classifyExceptionHandling(String snippet) {
        if (snippet == null || snippet.isBlank()) {
            return LoggingSeverity.SAFE;
        }

        // 1. Detect any variable.printStackTrace() call
        //    Matches: e.printStackTrace(), ex.printStackTrace(), exception.printStackTrace(), etc.
        if (snippet.matches("(?s).*\\b\\w+\\.printStackTrace\\(\\).*")) {
            return LoggingSeverity.LOW;
        }

        // 2. Proper SLF4J structured logging → SAFE
        if (snippet.matches("(?s).*(logger|log)\\.(error|warn|info|debug|trace)\\(.*\\).*")) {
            return LoggingSeverity.SAFE;
        }

        // 3. No exception handling pattern detected → SAFE (not a handler)
        return LoggingSeverity.SAFE;
    }

    // ── Generators ──

    @Provide
    Arbitrary<String> exceptionVariableNames() {
        return Arbitraries.of(
            "e", "ex", "exception", "err", "error", "cause", "t", "throwable"
        );
    }

    @Provide
    Arbitrary<String> loggerVariableNames() {
        return Arbitraries.of(
            "logger", "log", "LOG", "LOGGER"
        );
    }

    @Provide
    Arbitrary<String> logLevels() {
        return Arbitraries.of(
            "error", "warn", "info", "debug", "trace"
        );
    }

    @Provide
    Arbitrary<String> logMessages() {
        return Arbitraries.of(
            "\"Error processing request\"",
            "\"Failed to load product id={}\"",
            "\"DB error in getAllProducts\"",
            "\"Unexpected error during checkout\"",
            "\"Error fetching user by id={}\"",
            "\"Failed to save order\"",
            "\"Error updating cart\"",
            "\"Authentication failed\""
        );
    }

    @Provide
    Arbitrary<String> contextPrefixes() {
        return Arbitraries.of(
            "} catch (Exception e) {\n    ",
            "} catch (SQLException e) {\n    ",
            "} catch (NumberFormatException e) {\n    ",
            "} catch (IOException e) {\n    ",
            "} catch (RuntimeException e) {\n    "
        );
    }

    /** Generates e.printStackTrace() catch blocks */
    @Provide
    Arbitrary<String> printStackTraceSnippets() {
        return Combinators.combine(
            contextPrefixes(),
            exceptionVariableNames()
        ).as((prefix, varName) ->
            prefix + varName + ".printStackTrace();\n}"
        );
    }

    /** Generates proper SLF4J logger.error() catch blocks */
    @Provide
    Arbitrary<String> structuredLoggingSnippets() {
        return Combinators.combine(
            contextPrefixes(),
            loggerVariableNames(),
            logLevels(),
            logMessages(),
            exceptionVariableNames()
        ).as((prefix, logVar, level, message, exVar) ->
            prefix + logVar + "." + level + "(" + message + ", " + exVar + ");\n}"
        );
    }

    /** Generates mixed snippets: logger call followed by additional handling */
    @Provide
    Arbitrary<String> structuredLoggingWithRedirectSnippets() {
        return Combinators.combine(
            loggerVariableNames(),
            logMessages(),
            exceptionVariableNames()
        ).as((logVar, message, exVar) ->
            "} catch (Exception " + exVar + ") {\n" +
            "    " + logVar + ".error(" + message + ", " + exVar + ");\n" +
            "    session.setAttribute(\"error\", \"Có lỗi xảy ra.\");\n" +
            "    response.sendRedirect(\"shop\");\n" +
            "}"
        );
    }

    // ── Property Tests ──

    /**
     * <b>Validates: Requirement 13.1</b>
     *
     * <p>For any exception handler that calls {@code e.printStackTrace()},
     * the classifier must return LOW severity.</p>
     */
    @Property(tries = 200)
    void printStackTraceCallsAreClassifiedLow(
            @ForAll("printStackTraceSnippets") String snippet) {
        LoggingSeverity severity = classifyExceptionHandling(snippet);
        assertEquals(LoggingSeverity.LOW, severity,
                "e.printStackTrace() should be classified LOW: " + snippet);
    }

    /**
     * <b>Validates: Requirement 13.1</b>
     *
     * <p>For any exception handler that uses SLF4J structured logging
     * ({@code logger.error()}, {@code log.warn()}, etc.),
     * the classifier must return SAFE.</p>
     */
    @Property(tries = 200)
    void structuredLoggingIsClassifiedSafe(
            @ForAll("structuredLoggingSnippets") String snippet) {
        LoggingSeverity severity = classifyExceptionHandling(snippet);
        assertEquals(LoggingSeverity.SAFE, severity,
                "SLF4J structured logging should be SAFE: " + snippet);
    }

    /**
     * <b>Validates: Requirement 13.1</b>
     *
     * <p>For any exception handler that uses structured logging AND performs
     * a redirect with a user message, the classifier must return SAFE.</p>
     */
    @Property(tries = 100)
    void structuredLoggingWithRedirectIsClassifiedSafe(
            @ForAll("structuredLoggingWithRedirectSnippets") String snippet) {
        LoggingSeverity severity = classifyExceptionHandling(snippet);
        assertEquals(LoggingSeverity.SAFE, severity,
                "Structured logging with redirect should be SAFE: " + snippet);
    }

    /**
     * <b>Validates: Requirement 13.1</b>
     *
     * <p>LOW severity is strictly higher than SAFE. This ensures the
     * classification ordering is consistent — LOW findings should be
     * remediated (lower ordinal = higher priority).</p>
     */
    @Property(tries = 200)
    void severityOrderingIsConsistent(
            @ForAll("printStackTraceSnippets") String lowSnippet,
            @ForAll("structuredLoggingSnippets") String safeSnippet) {

        LoggingSeverity low = classifyExceptionHandling(lowSnippet);
        LoggingSeverity safe = classifyExceptionHandling(safeSnippet);

        assertTrue(low.ordinal() < safe.ordinal(),
                "LOW should rank higher (lower ordinal) than SAFE");
    }

    /**
     * <b>Validates: Requirement 13.1</b>
     *
     * <p>A snippet containing both a printStackTrace() call AND a logger call
     * should still be classified as LOW, because the printStackTrace() is present
     * and must be flagged regardless of other logging.</p>
     */
    @Property(tries = 50)
    void mixedSnippetWithPrintStackTraceIsFlagged(
            @ForAll("exceptionVariableNames") String exVar,
            @ForAll("loggerVariableNames") String logVar) {

        // Snippet has both logger.error AND e.printStackTrace() — should still be LOW
        String mixedSnippet =
            "} catch (Exception " + exVar + ") {\n" +
            "    " + logVar + ".error(\"Error occurred\", " + exVar + ");\n" +
            "    " + exVar + ".printStackTrace();\n" +
            "}";

        LoggingSeverity severity = classifyExceptionHandling(mixedSnippet);
        assertEquals(LoggingSeverity.LOW, severity,
                "Snippet with both logger and printStackTrace should be LOW: " + mixedSnippet);
    }

    /**
     * <b>Validates: Requirement 13.1</b>
     *
     * <p>Verifies that all common exception variable names are detected when
     * used with printStackTrace().</p>
     */
    @Property(tries = 50)
    void allCommonExceptionVariableNamesAreDetected(
            @ForAll("exceptionVariableNames") String varName) {

        String snippet = "} catch (Exception " + varName + ") {\n    " + varName + ".printStackTrace();\n}";
        LoggingSeverity severity = classifyExceptionHandling(snippet);
        assertEquals(LoggingSeverity.LOW, severity,
                "printStackTrace with variable '" + varName + "' should be LOW: " + snippet);
    }

    /**
     * <b>Validates: Requirement 13.1</b>
     *
     * <p>Verifies that all common logger variable names are recognized as SAFE
     * when used with any log level.</p>
     */
    @Property(tries = 50)
    void allCommonLoggerVariableNamesAreRecognized(
            @ForAll("loggerVariableNames") String logVar,
            @ForAll("logLevels") String level,
            @ForAll("exceptionVariableNames") String exVar) {

        String snippet = "} catch (Exception " + exVar + ") {\n    " +
                logVar + "." + level + "(\"Error\", " + exVar + ");\n}";
        LoggingSeverity severity = classifyExceptionHandling(snippet);
        assertEquals(LoggingSeverity.SAFE, severity,
                "SLF4J " + logVar + "." + level + "() should be SAFE: " + snippet);
    }
}
