package bank.Models;

import bank.DB.BankDb;
import bank.DB.DbManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit / integration tests focused on the Audit Log implementation.
 *
 * These tests:
 *  - insert rows into audit_log directly (using DbManager)
 *  - call BankDb.getAuditLog(branchId, actorType, actorId, from, to)
 *  - verify that filtering behaves correctly
 *
 * Assumptions:
 *  - DbManager is configured to point to your test database (via db.properties).
 *  - The audit_log table has the columns:
 *      audit_log_id, branch_id, actor_type, actor_id, action,
 *      target_type, target_id, action_time, details
 *  - BankDb has:
 *      List<Map<String, Object>> getAuditLog(
 *          Integer branchId,
 *          String actorType,
 *          Integer actorId,
 *          Timestamp from,
 *          Timestamp to
 *      )
 */
public class AuditLogDbTest {

    BankDb bankDb = new BankDb();

    @BeforeEach
    void cleanAuditLog() throws Exception {
        try (Connection conn = DbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM audit_log");
            stmt.executeUpdate("ALTER TABLE audit_log AUTO_INCREMENT = 1");
        }
    }

    /** Helper: insert one audit_log row and return its generated ID. */
    private long insertAuditLogRow(
            int branchId,
            String actorType,
            int actorId,
            String action,
            String targetType,
            Integer targetId,
            Timestamp actionTime,
            String details
    ) throws Exception {

        String sql = """
            INSERT INTO audit_log (
                branch_id, actor_type, actor_id, action,
                target_type, target_id, action_time, details
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, branchId);
            ps.setString(2, actorType);
            ps.setInt(3, actorId);
            ps.setString(4, action);
            ps.setString(5, targetType);
            if (targetId != null) {
                ps.setInt(6, targetId);
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setTimestamp(7, actionTime);
            ps.setString(8, details);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        throw new SQLException("Failed to insert audit_log row");
    }

    // =====================================================================
    // 1) No filters → returns all rows in range
    // =====================================================================
    @Test
    @DisplayName("getAuditLog with all filters null returns all matching rows")
    void getAuditLog_noFilters_returnsAllRows() throws Exception {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        insertAuditLogRow(1, "CUSTOMER", 10, "LOGIN",
                "CUSTOMER", 10, now, "Customer 10 login");
        insertAuditLogRow(2, "EMPLOYEE", 20, "EMPLOYEE_CREATE",
                "EMPLOYEE", 20, now, "Created employee 20");

        Timestamp from = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp to   = Timestamp.valueOf(LocalDateTime.now().plusDays(1));

        // All filters null → any branch, any actor, within date range
        List<Map<String, Object>> rows =
                bankDb.getAuditLog(null, null, null, from, to);

        assertEquals(2, rows.size(), "Should return all two inserted rows");
    }

    // =====================================================================
    // 2) Branch filter
    // =====================================================================
    @Test
    @DisplayName("getAuditLog filters correctly by branchId")
    void getAuditLog_filtersByBranch() throws Exception {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        // branch 1
        insertAuditLogRow(1, "CUSTOMER", 10, "LOGIN",
                "CUSTOMER", 10, now, "Customer 10 login (branch 1)");
        insertAuditLogRow(1, "EMPLOYEE", 11, "ACCOUNT_OPEN",
                "ACCOUNT", 1001, now, "Opened account in branch 1");

        // branch 2
        insertAuditLogRow(2, "EMPLOYEE", 12, "EMPLOYEE_CREATE",
                "EMPLOYEE", 200, now, "New employee in branch 2");

        Timestamp from = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp to   = Timestamp.valueOf(LocalDateTime.now().plusDays(1));

        List<Map<String, Object>> rowsBranch1 =
                bankDb.getAuditLog(1, null, null, from, to);

        assertFalse(rowsBranch1.isEmpty(), "Expected some logs for branch 1");
        for (Map<String, Object> row : rowsBranch1) {
            int branchId = ((Number) row.get("branch_id")).intValue();
            assertEquals(1, branchId, "All returned rows must be from branch 1");
        }
    }

    // =====================================================================
    // 3) Actor type + actor ID filter
    // =====================================================================
    @Test
    @DisplayName("getAuditLog filters by actorType and actorId")
    void getAuditLog_filtersByActorTypeAndId() throws Exception {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        // Logs for CUSTOMER 10
        insertAuditLogRow(1, "CUSTOMER", 10, "LOGIN",
                "CUSTOMER", 10, now, "Customer 10 login");
        insertAuditLogRow(1, "CUSTOMER", 10, "TXN_DEPOSIT",
                "ACCOUNT", 1001, now, "Customer 10 deposit");

        // Logs for EMPLOYEE 20
        insertAuditLogRow(1, "EMPLOYEE", 20, "EMPLOYEE_CREATE",
                "EMPLOYEE", 21, now, "Employee 20 created employee 21");

        Timestamp from = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp to   = Timestamp.valueOf(LocalDateTime.now().plusDays(1));

        // Filter specifically for EMPLOYEE 20
        List<Map<String, Object>> rows =
                bankDb.getAuditLog(null, "EMPLOYEE", 20, from, to);

        assertEquals(1, rows.size(), "Expected exactly one log for EMPLOYEE 20");

        Map<String, Object> row = rows.get(0);
        assertEquals("EMPLOYEE", row.get("actor_type"));
        assertEquals(20, ((Number) row.get("actor_id")).intValue());
        assertEquals("EMPLOYEE_CREATE", row.get("action"));
    }

    // =====================================================================
    // 4) From date filter (exclude older entries)
    // =====================================================================
    @Test
    @DisplayName("getAuditLog respects from timestamp (excludes older logs)")
    void getAuditLog_respectsFromTimestamp() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Timestamp oldTs   = Timestamp.valueOf(now.minusDays(5));
        Timestamp midTs   = Timestamp.valueOf(now.minusHours(1));

        // Old log (5 days ago)
        insertAuditLogRow(1, "CUSTOMER", 10, "LOGIN",
                "CUSTOMER", 10, oldTs, "Old login");

        // Newer log (1 hour ago)
        insertAuditLogRow(1, "CUSTOMER", 10, "TXN_DEPOSIT",
                "ACCOUNT", 1001, midTs, "Recent deposit");

        Timestamp from = Timestamp.valueOf(now.minusDays(1));  // 1 day ago
        Timestamp to   = Timestamp.valueOf(now.plusDays(1));   // tomorrow

        List<Map<String, Object>> rows =
                bankDb.getAuditLog(1, null, null, from, to);

        assertEquals(1, rows.size(), "Only the recent log should be returned");
        assertEquals("TXN_DEPOSIT", rows.get(0).get("action"));
    }

    // =====================================================================
    // 5) To date filter (exclude future entries)
    // =====================================================================
    @Test
    @DisplayName("getAuditLog respects to timestamp (excludes future logs)")
    void getAuditLog_respectsToTimestamp() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Timestamp nowTs     = Timestamp.valueOf(now);
        Timestamp futureTs  = Timestamp.valueOf(now.plusDays(2));

        // Now
        insertAuditLogRow(1, "CUSTOMER", 10, "LOGIN",
                "CUSTOMER", 10, nowTs, "Current login");

        // Future log (2 days later)
        insertAuditLogRow(1, "CUSTOMER", 10, "TXN_DEPOSIT",
                "ACCOUNT", 1001, futureTs, "Future deposit (should be excluded)");

        Timestamp from = Timestamp.valueOf(now.minusDays(1));
        Timestamp to   = Timestamp.valueOf(now.plusHours(1));  // up to shortly after "now"

        List<Map<String, Object>> rows =
                bankDb.getAuditLog(1, null, null, from, to);

        assertEquals(1, rows.size(), "Future log should be excluded by 'to' filter");
        assertEquals("LOGIN", rows.get(0).get("action"));
    }
}
