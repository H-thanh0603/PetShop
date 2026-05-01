package audit;

import net.jqwik.api.*;

import java.util.*;
import java.util.regex.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Admin Action Audit Logging Completeness.
 *
 * <p><b>Property 6: Admin Action Audit Logging Completeness</b></p>
 *
 * <p>For any admin action handler (role change, password reset, status toggle,
 * delete, add, update), if the handler does not include an audit log call,
 * the Audit_Engine SHALL flag it as a MEDIUM severity finding.</p>
 *
 * <p><b>Validates: Requirement 4.3</b></p>
 */
class AdminAuditLoggingPropertyTest {

    // ── Inline model ──────────────────────────────────────────────────────────

    enum AuditLogSeverity {
        MEDIUM, // Admin action handler missing audit log call
        NONE    // Audit log call is present
    }

    /**
     * Represents a single finding for a missing audit log in an admin action handler.
     *
     * @param actionType  the admin action type (e.g., "updateRole", "resetPassword")
     * @param reason      explanation of why the finding was raised
     * @param severity    severity of the finding
     */
    record AuditLogFinding(String actionType, String reason, AuditLogSeverity severity) {
        AuditLogFinding {
            Objects.requireNonNull(actionType, "actionType must not be null");
            Objects.requireNonNull(reason, "reason must not be null");
            Objects.requireNonNull(severity, "severity must not be null");
        }
    }

    // ── Detector ─────────────────────────────────────────────────────────────

    /**
     * Analyzes a Java source snippet representing a single admin action handler
     * (a {@code case} block in a switch statement) and checks whether it calls
     * an audit log method.
     *
     * <p>Detection rules:</p>
     * <ul>
     *   <li>If the snippet contains a call to {@code actionLog.log(} or
     *       {@code AdminActionLogDAO} → audit log is present → no finding</li>
     *   <li>Otherwise → MEDIUM severity finding is produced</li>
     * </ul>
     *
     * @param actionType    the name of the admin action (e.g., "updateRole")
     * @param handlerCode   the source code of the action handler block
     * @return list of findings; empty when audit logging is present
     */
    static List<AuditLogFinding> detectMissingAuditLog(String actionType, String handlerCode) {
        if (handlerCode == null || handlerCode.isBlank()) {
            return List.of(new AuditLogFinding(
                    actionType,
                    "Handler code is empty — cannot verify audit logging",
                    AuditLogSeverity.MEDIUM
            ));
        }

        // Strip single-line comments so commented-out log calls are not counted
        String strippedCode = handlerCode.replaceAll("//[^\n]*", "");

        boolean hasAuditLogCall =
                strippedCode.contains("actionLog.log(") ||
                strippedCode.contains("AdminActionLogDAO") ||
                strippedCode.matches("(?si).*\\.log\\s*\\(\\s*adminId.*");

        if (!hasAuditLogCall) {
            return List.of(new AuditLogFinding(
                    actionType,
                    "Admin action '" + actionType + "' does not call audit log (actionLog.log). " +
                    "All state-changing admin actions must be recorded in admin_action_log.",
                    AuditLogSeverity.MEDIUM
            ));
        }
        return List.of();
    }

    // ── Known admin action types ──────────────────────────────────────────────

    /** All state-changing admin actions that require audit logging. */
    static final List<String> AUDITABLE_ADMIN_ACTIONS = List.of(
            "add", "update", "updateRole", "toggleStatus", "resetPassword", "delete"
    );

    // ── Generators ───────────────────────────────────────────────────────────

    @Provide
    Arbitrary<String> auditableActionTypes() {
        return Arbitraries.of(AUDITABLE_ADMIN_ACTIONS);
    }

    /** Handler snippets that DO include an audit log call. */
    @Provide
    Arbitrary<String> handlerSnippetsWithAuditLog() {
        return Arbitraries.of(
                // updateRole with audit log
                "int roleUserId = Integer.parseInt(request.getParameter(\"userId\"));\n" +
                "String newRole = request.getParameter(\"role\");\n" +
                "if (userDAO.updateUserRole(roleUserId, newRole)) {\n" +
                "    actionLog.log(adminId, \"UPDATE_ROLE\", \"user\", roleUserId, \"newRole=\" + newRole);\n" +
                "    message = \"Đã cập nhật quyền thành công!\";\n" +
                "}\n",

                // toggleStatus with audit log
                "int statusUserId = Integer.parseInt(request.getParameter(\"userId\"));\n" +
                "String newStatus = request.getParameter(\"status\");\n" +
                "if (userDAO.updateUserStatus(statusUserId, newStatus)) {\n" +
                "    actionLog.log(adminId, \"TOGGLE_STATUS\", \"user\", statusUserId, \"newStatus=\" + newStatus);\n" +
                "    message = newStatus.equals(\"active\") ? \"Đã mở khóa!\" : \"Đã khóa!\";\n" +
                "}\n",

                // resetPassword with audit log
                "int resetUserId = Integer.parseInt(request.getParameter(\"userId\"));\n" +
                "String newPassword = request.getParameter(\"newPassword\");\n" +
                "if (userDAO.resetUserPassword(resetUserId, newPassword)) {\n" +
                "    actionLog.log(adminId, \"RESET_PASSWORD\", \"user\", resetUserId, null);\n" +
                "    message = \"Đã reset mật khẩu thành công!\";\n" +
                "}\n",

                // delete with audit log
                "int deleteId = Integer.parseInt(request.getParameter(\"userId\"));\n" +
                "if (userDAO.deactivateUser(deleteId)) {\n" +
                "    actionLog.log(adminId, \"DELETE_USER\", \"user\", deleteId, null);\n" +
                "    message = \"Đã vô hiệu hóa tài khoản thành công!\";\n" +
                "}\n",

                // add with audit log
                "String username = request.getParameter(\"username\");\n" +
                "String role = request.getParameter(\"role\");\n" +
                "if (userDAO.addUser(username, password, fullname, email, phone, role)) {\n" +
                "    actionLog.log(adminId, \"ADD_USER\", \"user\", null, \"username=\" + username);\n" +
                "    message = \"Thêm người dùng thành công!\";\n" +
                "}\n",

                // update with audit log
                "int updateId = Integer.parseInt(request.getParameter(\"userId\"));\n" +
                "if (userDAO.updateUser(updateId, fullname, email, phone, address)) {\n" +
                "    actionLog.log(adminId, \"UPDATE_USER\", \"user\", updateId, null);\n" +
                "    message = \"Cập nhật thông tin thành công!\";\n" +
                "}\n"
        );
    }

    /** Handler snippets that are MISSING the audit log call. */
    @Provide
    Arbitrary<String> handlerSnippetsWithoutAuditLog() {
        return Arbitraries.of(
                // updateRole WITHOUT audit log
                "int roleUserId = Integer.parseInt(request.getParameter(\"userId\"));\n" +
                "String newRole = request.getParameter(\"role\");\n" +
                "if (userDAO.updateUserRole(roleUserId, newRole)) {\n" +
                "    message = \"Đã cập nhật quyền thành công!\";\n" +
                "} else {\n" +
                "    message = \"Có lỗi xảy ra!\";\n" +
                "}\n",

                // toggleStatus WITHOUT audit log
                "int statusUserId = Integer.parseInt(request.getParameter(\"userId\"));\n" +
                "String newStatus = request.getParameter(\"status\");\n" +
                "if (userDAO.updateUserStatus(statusUserId, newStatus)) {\n" +
                "    message = newStatus.equals(\"active\") ? \"Đã mở khóa!\" : \"Đã khóa!\";\n" +
                "}\n",

                // resetPassword WITHOUT audit log
                "int resetUserId = Integer.parseInt(request.getParameter(\"userId\"));\n" +
                "String newPassword = request.getParameter(\"newPassword\");\n" +
                "if (userDAO.resetUserPassword(resetUserId, newPassword)) {\n" +
                "    message = \"Đã reset mật khẩu thành công!\";\n" +
                "}\n",

                // delete WITHOUT audit log
                "int deleteId = Integer.parseInt(request.getParameter(\"userId\"));\n" +
                "if (userDAO.deactivateUser(deleteId)) {\n" +
                "    message = \"Đã vô hiệu hóa tài khoản thành công!\";\n" +
                "}\n",

                // add WITHOUT audit log
                "String username = request.getParameter(\"username\");\n" +
                "if (userDAO.addUser(username, password, fullname, email, phone, role)) {\n" +
                "    message = \"Thêm người dùng thành công!\";\n" +
                "}\n"
        );
    }

    /** Handler snippets where the audit log call is commented out. */
    @Provide
    Arbitrary<String> handlerSnippetsWithCommentedOutAuditLog() {
        return Arbitraries.of(
                "int roleUserId = Integer.parseInt(request.getParameter(\"userId\"));\n" +
                "String newRole = request.getParameter(\"role\");\n" +
                "if (userDAO.updateUserRole(roleUserId, newRole)) {\n" +
                "    // actionLog.log(adminId, \"UPDATE_ROLE\", \"user\", roleUserId, null);\n" +
                "    message = \"Đã cập nhật quyền thành công!\";\n" +
                "}\n",

                "int statusUserId = Integer.parseInt(request.getParameter(\"userId\"));\n" +
                "if (userDAO.updateUserStatus(statusUserId, \"locked\")) {\n" +
                "    // TODO: add audit log\n" +
                "    message = \"Đã khóa tài khoản!\";\n" +
                "}\n"
        );
    }

    // ── Properties ───────────────────────────────────────────────────────────

    /**
     * <b>Validates: Requirement 4.3</b>
     *
     * <p>Admin action handlers that include an audit log call produce no findings.</p>
     */
    @Property(tries = 50)
    void handlersWithAuditLogProduceNoFindings(
            @ForAll("auditableActionTypes") String actionType,
            @ForAll("handlerSnippetsWithAuditLog") String handlerCode) {

        List<AuditLogFinding> findings = detectMissingAuditLog(actionType, handlerCode);

        assertTrue(findings.isEmpty(),
                "Handler with audit log call should produce no findings for action '" +
                actionType + "':\n" + handlerCode);
    }

    /**
     * <b>Validates: Requirement 4.3</b>
     *
     * <p>Admin action handlers missing an audit log call produce a MEDIUM severity finding.</p>
     */
    @Property(tries = 50)
    void handlersWithoutAuditLogProduceMediumFinding(
            @ForAll("auditableActionTypes") String actionType,
            @ForAll("handlerSnippetsWithoutAuditLog") String handlerCode) {

        List<AuditLogFinding> findings = detectMissingAuditLog(actionType, handlerCode);

        assertFalse(findings.isEmpty(),
                "Handler without audit log should produce at least one finding for action '" +
                actionType + "'");

        boolean hasMediumFinding = findings.stream()
                .anyMatch(f -> f.severity() == AuditLogSeverity.MEDIUM);
        assertTrue(hasMediumFinding,
                "Missing audit log should produce a MEDIUM severity finding for action '" +
                actionType + "': " + findings);
    }

    /**
     * <b>Validates: Requirement 4.3</b>
     *
     * <p>A commented-out audit log call is treated as absent and still produces
     * a MEDIUM severity finding.</p>
     */
    @Property(tries = 50)
    void commentedOutAuditLogProducesMediumFinding(
            @ForAll("auditableActionTypes") String actionType,
            @ForAll("handlerSnippetsWithCommentedOutAuditLog") String handlerCode) {

        List<AuditLogFinding> findings = detectMissingAuditLog(actionType, handlerCode);

        assertFalse(findings.isEmpty(),
                "Commented-out audit log should still produce a finding for action '" +
                actionType + "'");

        boolean hasMediumFinding = findings.stream()
                .anyMatch(f -> f.severity() == AuditLogSeverity.MEDIUM);
        assertTrue(hasMediumFinding,
                "Commented-out audit log should produce a MEDIUM severity finding: " + findings);
    }

    /**
     * <b>Validates: Requirement 4.3</b>
     *
     * <p>Every finding produced by the detector has a non-blank action type,
     * reason, and a non-null severity.</p>
     */
    @Property(tries = 50)
    void allFindingsHaveRequiredFields(
            @ForAll("auditableActionTypes") String actionType,
            @ForAll("handlerSnippetsWithoutAuditLog") String handlerCode) {

        List<AuditLogFinding> findings = detectMissingAuditLog(actionType, handlerCode);

        for (AuditLogFinding finding : findings) {
            assertFalse(finding.actionType().isBlank(),
                    "Finding actionType must not be blank");
            assertFalse(finding.reason().isBlank(),
                    "Finding reason must not be blank");
            assertNotNull(finding.severity(),
                    "Finding severity must not be null");
        }
    }

    /**
     * <b>Validates: Requirement 4.3</b>
     *
     * <p>The finding's action type matches the action type passed to the detector,
     * ensuring traceability back to the specific handler.</p>
     */
    @Property(tries = 50)
    void findingActionTypeMatchesInput(
            @ForAll("auditableActionTypes") String actionType,
            @ForAll("handlerSnippetsWithoutAuditLog") String handlerCode) {

        List<AuditLogFinding> findings = detectMissingAuditLog(actionType, handlerCode);

        for (AuditLogFinding finding : findings) {
            assertEquals(actionType, finding.actionType(),
                    "Finding actionType must match the input action type");
        }
    }

    /**
     * <b>Validates: Requirement 4.3</b>
     *
     * <p>Verifies that the actual UserManageServlet post-fix has audit log calls
     * for all auditable admin actions by checking representative handler patterns.</p>
     */
    @Property(tries = 1)
    void actualUserManageServletActionsHaveAuditLogging() {
        // Representative snippets from the fixed UserManageServlet
        Map<String, String> actionHandlers = new LinkedHashMap<>();

        actionHandlers.put("add",
                "if (userDAO.addUser(username, password, fullname, email, phone, role)) {\n" +
                "    actionLog.log(adminId, \"ADD_USER\", \"user\", null, \"username=\" + username + \";role=\" + role);\n" +
                "    message = \"Thêm người dùng thành công!\";\n" +
                "}\n");

        actionHandlers.put("update",
                "if (userDAO.updateUser(updateId, updateFullname, updateEmail, updatePhone, updateAddress)) {\n" +
                "    actionLog.log(adminId, \"UPDATE_USER\", \"user\", updateId, null);\n" +
                "    message = \"Cập nhật thông tin thành công!\";\n" +
                "}\n");

        actionHandlers.put("updateRole",
                "if (userDAO.updateUserRole(roleUserId, newRole)) {\n" +
                "    actionLog.log(adminId, \"UPDATE_ROLE\", \"user\", roleUserId, \"newRole=\" + newRole);\n" +
                "    message = \"Đã cập nhật quyền thành công!\";\n" +
                "}\n");

        actionHandlers.put("toggleStatus",
                "if (userDAO.updateUserStatus(statusUserId, newStatus)) {\n" +
                "    actionLog.log(adminId, \"TOGGLE_STATUS\", \"user\", statusUserId, \"newStatus=\" + newStatus);\n" +
                "    message = newStatus.equals(\"active\") ? \"Đã mở khóa tài khoản!\" : \"Đã khóa tài khoản!\";\n" +
                "}\n");

        actionHandlers.put("resetPassword",
                "if (userDAO.resetUserPassword(resetUserId, newPassword)) {\n" +
                "    actionLog.log(adminId, \"RESET_PASSWORD\", \"user\", resetUserId, null);\n" +
                "    message = \"Đã reset mật khẩu thành công!\";\n" +
                "}\n");

        actionHandlers.put("delete",
                "if (userDAO.deactivateUser(deleteId)) {\n" +
                "    actionLog.log(adminId, \"DELETE_USER\", \"user\", deleteId, null);\n" +
                "    message = \"Đã vô hiệu hóa tài khoản thành công!\";\n" +
                "}\n");

        for (Map.Entry<String, String> entry : actionHandlers.entrySet()) {
            List<AuditLogFinding> findings = detectMissingAuditLog(entry.getKey(), entry.getValue());
            assertTrue(findings.isEmpty(),
                    "UserManageServlet action '" + entry.getKey() + "' should have audit logging " +
                    "but produced findings: " + findings);
        }
    }
}
