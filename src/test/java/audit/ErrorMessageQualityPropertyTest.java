package audit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Error Message Quality in User-Facing Responses.
 *
 * <p><b>Property 20: Error Message Quality in User-Facing Responses</b></p>
 *
 * <p>For any error response message returned to the user, the message SHALL not contain
 * internal system details (Java class names, stack traces, SQL error codes, or file paths),
 * and if a servlet catches a NumberFormatException from user input without returning a
 * descriptive error, the Audit_Engine SHALL flag it.</p>
 *
 * <p><b>Validates: Requirements 13.3, 13.4</b></p>
 */
class ErrorMessageQualityPropertyTest {

    // ── Inline classifier (self-contained, no external dependencies) ──

    enum ErrorMessageQuality {
        SAFE,       // User-friendly message with no internal details
        FLAGGED     // Contains internal system details — must not be shown to users
    }

    /**
     * Classifies an error message for internal system detail leakage.
     *
     * <p>A message is FLAGGED if it contains any of:</p>
     * <ul>
     *   <li>Java class names (e.g., {@code java.lang.NullPointerException},
     *       {@code com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException})</li>
     *   <li>Stack trace indicators (e.g., {@code at com.}, {@code at java.},
     *       {@code Caused by:})</li>
     *   <li>SQL error codes or SQL keywords in error context
     *       (e.g., {@code SQLState:}, {@code Error Code: 1064})</li>
     *   <li>File system paths (e.g., {@code /WEB-INF/}, {@code C:\}, {@code /home/})</li>
     *   <li>Raw exception messages that expose internal state
     *       (e.g., {@code e.getMessage()} patterns like
     *       {@code Column 'x' cannot be null})</li>
     * </ul>
     */
    static ErrorMessageQuality classifyErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return ErrorMessageQuality.SAFE;
        }

        // 1. Java fully-qualified class names (package.ClassName pattern)
        if (message.matches("(?s).*\\b(java|javax|jakarta|com|org|net)\\.[a-zA-Z]+(\\.[a-zA-Z]+)+.*")) {
            return ErrorMessageQuality.FLAGGED;
        }

        // 2. Stack trace indicators
        if (message.matches("(?s).*\\bat\\s+(java|javax|jakarta|com|org|net)\\.[a-zA-Z].*")) {
            return ErrorMessageQuality.FLAGGED;
        }
        if (message.contains("Caused by:")) {
            return ErrorMessageQuality.FLAGGED;
        }

        // 3. SQL error codes / SQL state
        if (message.matches("(?si).*SQLState\\s*:.*") ||
            message.matches("(?si).*Error\\s+Code\\s*:\\s*\\d+.*") ||
            message.matches("(?si).*You have an error in your SQL syntax.*") ||
            message.matches("(?si).*Unknown column.*in.*field list.*")) {
            return ErrorMessageQuality.FLAGGED;
        }

        // 4. File system paths
        if (message.matches("(?s).*((/WEB-INF/|/src/main/|/classes/|C:\\\\|/home/|/var/|/usr/))[^\\s]*.*")) {
            return ErrorMessageQuality.FLAGGED;
        }

        // 5. Raw exception class name without package (e.g., "NullPointerException")
        if (message.matches("(?s).*(NullPointerException|ClassCastException|ArrayIndexOutOfBoundsException" +
                "|StackOverflowError|OutOfMemoryError|IllegalArgumentException" +
                "|IllegalStateException|UnsupportedOperationException).*")) {
            return ErrorMessageQuality.FLAGGED;
        }

        return ErrorMessageQuality.SAFE;
    }

    /**
     * Classifies a servlet catch block for NumberFormatException handling quality.
     *
     * <p>A catch block is FLAGGED if it catches NumberFormatException (or a broad
     * Exception that would include it) but does NOT set a descriptive user-facing
     * error message (i.e., no session.setAttribute("error"/"toastMessage") or
     * response.getWriter().write() with a user message).</p>
     */
    enum NumberFormatHandling {
        DESCRIPTIVE,    // Catch block sets a user-friendly error message
        SILENT          // Catch block swallows the exception without user notification
    }

    static NumberFormatHandling classifyNumberFormatHandling(String catchBlockCode) {
        if (catchBlockCode == null || catchBlockCode.isBlank()) {
            return NumberFormatHandling.SILENT;
        }

        // Check for user-facing error message patterns
        boolean hasUserMessage =
            catchBlockCode.contains("session.setAttribute(\"error\"") ||
            catchBlockCode.contains("session.setAttribute(\"toastMessage\"") ||
            catchBlockCode.contains("result.put(\"message\"") ||
            catchBlockCode.contains("writeJson(response, false") ||
            catchBlockCode.contains("response.getWriter().write(") ||
            catchBlockCode.contains("request.setAttribute(\"error\"") ||
            catchBlockCode.contains("session.setAttribute(\"message\"");

        return hasUserMessage ? NumberFormatHandling.DESCRIPTIVE : NumberFormatHandling.SILENT;
    }

    // ── Generators ──

    @Provide
    Arbitrary<String> javaExceptionClassNames() {
        return Arbitraries.of(
            "java.lang.NullPointerException",
            "java.sql.SQLException: Column 'user_id' cannot be null",
            "com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException",
            "jakarta.servlet.ServletException: java.lang.RuntimeException",
            "org.springframework.dao.DataAccessException",
            "java.io.FileNotFoundException: /WEB-INF/config.properties"
        );
    }

    @Provide
    Arbitrary<String> stackTraceSnippets() {
        return Arbitraries.of(
            "at java.lang.Integer.parseInt(Integer.java:652)",
            "at com.petshop.controller.CheckoutServlet.doPost(CheckoutServlet.java:145)",
            "Caused by: java.sql.SQLIntegrityConstraintViolationException",
            "at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:590)"
        );
    }

    @Provide
    Arbitrary<String> sqlErrorMessages() {
        return Arbitraries.of(
            "SQLState: 42000, Error Code: 1064",
            "You have an error in your SQL syntax near 'WHERE'",
            "Unknown column 'user_name' in 'field list'",
            "Error Code: 1452 - Cannot add or update a child row"
        );
    }

    @Provide
    Arbitrary<String> filePathMessages() {
        return Arbitraries.of(
            "File not found: /WEB-INF/web.xml",
            "Cannot read: C:\\PetShop\\src\\main\\resources\\db.properties",
            "Error loading: /home/ubuntu/petshop/classes/config.properties",
            "Missing: /var/lib/tomcat/webapps/PetShop/WEB-INF/lib"
        );
    }

    @Provide
    Arbitrary<String> rawExceptionNames() {
        return Arbitraries.of(
            "NullPointerException occurred",
            "ClassCastException: cannot cast User to Admin",
            "ArrayIndexOutOfBoundsException at index 5",
            "IllegalArgumentException: invalid parameter"
        );
    }

    @Provide
    Arbitrary<String> userFriendlyMessages() {
        return Arbitraries.of(
            "Có lỗi xảy ra. Vui lòng thử lại.",
            "Mã sản phẩm không hợp lệ.",
            "Không thể tải thông tin sản phẩm. Vui lòng thử lại.",
            "Đã có lỗi xảy ra. Vui lòng thử lại.",
            "Mã đơn hàng không hợp lệ.",
            "Sản phẩm không tồn tại hoặc đã bị xóa.",
            "Không thể gửi đánh giá.",
            "Đăng nhập Google thất bại. Kiểm tra lại cấu hình OAuth.",
            "Lỗi server khi upload file",
            "Không thể hủy đơn hàng này.",
            "Invalid input. Please provide a valid ID.",
            "An error occurred. Please try again."
        );
    }

    @Provide
    Arbitrary<String> descriptiveCatchBlocks() {
        return Arbitraries.of(
            "} catch (NumberFormatException e) {\n    session.setAttribute(\"error\", \"Mã sản phẩm không hợp lệ.\");\n    response.sendRedirect(\"shop\");\n}",
            "} catch (NumberFormatException e) {\n    session.setAttribute(\"toastMessage\", \"Invalid product ID.\");\n    session.setAttribute(\"toastType\", \"error\");\n    response.sendRedirect(contextPath + \"/cart\");\n}",
            "} catch (NumberFormatException e) {\n    result.put(\"message\", \"Invalid order ID.\");\n    writeJson(response, false, \"Invalid input\");\n}",
            "} catch (NumberFormatException e) {\n    session.setAttribute(\"message\", \"Mã đơn hàng không hợp lệ.\");\n    session.setAttribute(\"messageType\", \"error\");\n    response.sendRedirect(contextPath + \"/admin/orders\");\n}"
        );
    }

    @Provide
    Arbitrary<String> silentCatchBlocks() {
        return Arbitraries.of(
            "} catch (NumberFormatException e) {\n    response.sendRedirect(\"shop\");\n}",
            "} catch (NumberFormatException e) {\n    // ignore\n}",
            "} catch (NumberFormatException e) {\n    logger.error(\"Invalid id\", e);\n    response.sendRedirect(\"home\");\n}",
            "} catch (Exception e) {\n    logger.error(\"Error\", e);\n    response.sendRedirect(\"error\");\n}"
        );
    }

    // ── Property Tests ──

    /**
     * <b>Validates: Requirement 13.3</b>
     *
     * <p>For any error message containing a Java exception class name (fully qualified),
     * the classifier must return FLAGGED.</p>
     */
    @Property(tries = 50)
    void javaExceptionClassNamesAreFlagged(
            @ForAll("javaExceptionClassNames") String message) {
        ErrorMessageQuality quality = classifyErrorMessage(message);
        assertEquals(ErrorMessageQuality.FLAGGED, quality,
                "Java exception class name in error message should be FLAGGED: " + message);
    }

    /**
     * <b>Validates: Requirement 13.3</b>
     *
     * <p>For any error message containing a stack trace snippet,
     * the classifier must return FLAGGED.</p>
     */
    @Property(tries = 50)
    void stackTraceSnippetsAreFlagged(
            @ForAll("stackTraceSnippets") String message) {
        ErrorMessageQuality quality = classifyErrorMessage(message);
        assertEquals(ErrorMessageQuality.FLAGGED, quality,
                "Stack trace in error message should be FLAGGED: " + message);
    }

    /**
     * <b>Validates: Requirement 13.3</b>
     *
     * <p>For any error message containing SQL error codes or SQL syntax errors,
     * the classifier must return FLAGGED.</p>
     */
    @Property(tries = 50)
    void sqlErrorMessagesAreFlagged(
            @ForAll("sqlErrorMessages") String message) {
        ErrorMessageQuality quality = classifyErrorMessage(message);
        assertEquals(ErrorMessageQuality.FLAGGED, quality,
                "SQL error details in error message should be FLAGGED: " + message);
    }

    /**
     * <b>Validates: Requirement 13.3</b>
     *
     * <p>For any error message containing file system paths,
     * the classifier must return FLAGGED.</p>
     */
    @Property(tries = 50)
    void filePathMessagesAreFlagged(
            @ForAll("filePathMessages") String message) {
        ErrorMessageQuality quality = classifyErrorMessage(message);
        assertEquals(ErrorMessageQuality.FLAGGED, quality,
                "File path in error message should be FLAGGED: " + message);
    }

    /**
     * <b>Validates: Requirement 13.3</b>
     *
     * <p>For any error message containing raw exception class names (without package),
     * the classifier must return FLAGGED.</p>
     */
    @Property(tries = 50)
    void rawExceptionNamesAreFlagged(
            @ForAll("rawExceptionNames") String message) {
        ErrorMessageQuality quality = classifyErrorMessage(message);
        assertEquals(ErrorMessageQuality.FLAGGED, quality,
                "Raw exception name in error message should be FLAGGED: " + message);
    }

    /**
     * <b>Validates: Requirements 13.3, 13.4</b>
     *
     * <p>For any user-friendly error message (no internal details),
     * the classifier must return SAFE.</p>
     */
    @Property(tries = 50)
    void userFriendlyMessagesAreSafe(
            @ForAll("userFriendlyMessages") String message) {
        ErrorMessageQuality quality = classifyErrorMessage(message);
        assertEquals(ErrorMessageQuality.SAFE, quality,
                "User-friendly error message should be SAFE: " + message);
    }

    /**
     * <b>Validates: Requirement 13.4</b>
     *
     * <p>For any catch block that catches NumberFormatException and sets a
     * user-facing error message, the handler must be classified as DESCRIPTIVE.</p>
     */
    @Property(tries = 50)
    void descriptiveCatchBlocksAreClassifiedDescriptive(
            @ForAll("descriptiveCatchBlocks") String catchBlock) {
        NumberFormatHandling handling = classifyNumberFormatHandling(catchBlock);
        assertEquals(NumberFormatHandling.DESCRIPTIVE, handling,
                "Catch block with user message should be DESCRIPTIVE: " + catchBlock);
    }

    /**
     * <b>Validates: Requirement 13.4</b>
     *
     * <p>For any catch block that catches NumberFormatException but does NOT set
     * a user-facing error message, the handler must be classified as SILENT.</p>
     */
    @Property(tries = 50)
    void silentCatchBlocksAreClassifiedSilent(
            @ForAll("silentCatchBlocks") String catchBlock) {
        NumberFormatHandling handling = classifyNumberFormatHandling(catchBlock);
        assertEquals(NumberFormatHandling.SILENT, handling,
                "Catch block without user message should be SILENT: " + catchBlock);
    }

    /**
     * <b>Validates: Requirements 13.3, 13.4</b>
     *
     * <p>FLAGGED severity is strictly higher than SAFE. This ensures the
     * classification ordering is consistent.</p>
     */
    @Property(tries = 50)
    void severityOrderingIsConsistent(
            @ForAll("javaExceptionClassNames") String flaggedMessage,
            @ForAll("userFriendlyMessages") String safeMessage) {

        ErrorMessageQuality flagged = classifyErrorMessage(flaggedMessage);
        ErrorMessageQuality safe = classifyErrorMessage(safeMessage);

        assertTrue(flagged.ordinal() > safe.ordinal(),
                "FLAGGED should rank higher (higher ordinal) than SAFE");
    }

    /**
     * <b>Validates: Requirement 13.4</b>
     *
     * <p>DESCRIPTIVE handling is strictly better than SILENT. This ensures the
     * classification ordering is consistent.</p>
     */
    @Property(tries = 50)
    void handlingOrderingIsConsistent(
            @ForAll("descriptiveCatchBlocks") String descriptiveBlock,
            @ForAll("silentCatchBlocks") String silentBlock) {

        NumberFormatHandling descriptive = classifyNumberFormatHandling(descriptiveBlock);
        NumberFormatHandling silent = classifyNumberFormatHandling(silentBlock);

        assertTrue(descriptive.ordinal() < silent.ordinal(),
                "DESCRIPTIVE should rank lower ordinal (better) than SILENT");
    }
}
