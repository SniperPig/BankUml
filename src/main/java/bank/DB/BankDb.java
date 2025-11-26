package bank.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import bank.Models.Branch;

public class BankDb {


    

    /* =========================================================
       A) SECURITY & AUTHENTICATION
       ========================================================= */

    public List<Map<String, Object>> customerLogin(String email, String passwordHash) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_customer_login(?,?)}")) {

            stmt.setString(1, email);
            stmt.setString(2, passwordHash);

            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> employeeLogin(String email, String passwordHash) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_employee_login(?,?)}")) {

            stmt.setString(1, email);
            stmt.setString(2, passwordHash);

            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    public void customerChangePassword(int customerId, String oldPasswordHash, String newPasswordHash)
            throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_customer_change_password(?,?,?)}")) {

            stmt.setInt(1, customerId);
            stmt.setString(2, oldPasswordHash);
            stmt.setString(3, newPasswordHash);
            stmt.execute();
        }
    }

    public void employeeChangePassword(int employeeId, String oldPasswordHash, String newPasswordHash)
            throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_employee_change_password(?,?,?)}")) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, oldPasswordHash);
            stmt.setString(3, newPasswordHash);
            stmt.execute();
        }
    }

    public void customerResetPassword(int customerId, String securityAnswerHash, String newPasswordHash)
            throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_customer_reset_password(?,?,?)}")) {

            stmt.setInt(1, customerId);
            stmt.setString(2, securityAnswerHash);
            stmt.setString(3, newPasswordHash);
            stmt.execute();
        }
    }

    public void employeeResetPassword(int employeeId, String securityAnswerHash, String newPasswordHash)
            throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_employee_reset_password(?,?,?)}")) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, securityAnswerHash);
            stmt.setString(3, newPasswordHash);
            stmt.execute();
        }
    }


    /* =========================================================
       B) CUSTOMER MANAGEMENT
       ========================================================= */

    public int customerCreate(
            int actorEmployeeId,
            int branchId,
            String name,
            String email,
            String phone,
            Date dob,
            String governmentId,
            String address,
            String passwordHash,
            String secQuestion,
            String secAnswerHash
    ) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_customer_create(?,?,?,?,?,?,?,?,?,?,?)}")) {

            stmt.setInt(1, actorEmployeeId);
            stmt.setInt(2, branchId);
            stmt.setString(3, name);
            stmt.setString(4, email);
            stmt.setString(5, phone);
            stmt.setDate(6, new java.sql.Date(dob.getTime()));
            stmt.setString(7, governmentId);
            stmt.setString(8, address);
            stmt.setString(9, passwordHash);
            stmt.setString(10, secQuestion);
            stmt.setString(11, secAnswerHash);

            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        return rs.getInt("customer_id");
                    }
                }
            }
            throw new SQLException("sp_customer_create did not return a customer_id");
        }
    }

    public void customerUpdate(
            int actorEmployeeId,
            int customerId,
            int branchId,
            String name,
            String email,
            String phone,
            Date dob,
            String governmentId,
            String address
    ) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_customer_update(?,?,?,?,?,?,?,?,?)}")) {

            stmt.setInt(1, actorEmployeeId);
            stmt.setInt(2, customerId);
            stmt.setInt(3, branchId);
            stmt.setString(4, name);
            stmt.setString(5, email);
            stmt.setString(6, phone);
            stmt.setDate(7, new java.sql.Date(dob.getTime()));
            stmt.setString(8, governmentId);
            stmt.setString(9, address);
            stmt.execute();
        }
    }

    public void customerDeactivate(int actorEmployeeId, int customerId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_customer_deactivate(?,?)}")) {

            stmt.setInt(1, actorEmployeeId);
            stmt.setInt(2, customerId);
            stmt.execute();
        }
    }

    public void customerReactivate(int actorEmployeeId, int customerId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_customer_reactivate(?,?)}")) {

            stmt.setInt(1, actorEmployeeId);
            stmt.setInt(2, customerId);
            stmt.execute();
        }
    }

    public List<Map<String, Object>> customerGetById(int customerId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_customer_get_by_id(?)}")) {

            stmt.setInt(1, customerId);
            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }


    /**
     * Fetches customers by email. Returns empty list if none found.
     */
    public List<Map<String, Object>> customerGetByEmail(String email) throws SQLException {
        String sql = """
            SELECT customer_id, customer_name, customer_email, customer_phone,
                dob, government_id, customer_address, password_hash,
                security_question, security_answer_hash
            FROM customer
            WHERE customer_email = ?
        """;

        try (Connection conn = DbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return ResultSetUtils.toList(rs);
            }
        }
    }

    /**
     * Fetches all customers in the database no matter the branch.
     * Returns empty list if none found.
     */ 
    public List<Map<String, Object>> fetchAllCustomers() throws SQLException {
        String sql = """
            SELECT customer_id, customer_name, customer_email, customer_phone,
                dob, government_id, customer_address, password_hash,
                security_question, security_answer_hash
            FROM customer
        """;

        try (Connection conn = DbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            return ResultSetUtils.toList(rs);
        }
    }

    /**
     * Fetches all customers matching the search query across all branches.
     * Returns Empty list if none found.
     * Otherwise returns list of rows with customer info.
     */
    public List<Map<String, Object>> customerSearchAll(String query) throws SQLException {
        try (Connection conn = DbManager.getConnection();
            CallableStatement stmt = conn.prepareCall("{CALL sp_customer_search_all(?)}")) {

            stmt.setString(1, query == null ? "" : query.trim());
            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }   


    public List<Map<String, Object>> customerSearch(int branchId, String query) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_customer_search(?,?)}")) {

            stmt.setInt(1, branchId);
            stmt.setString(2, query);
            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }


    /* =========================================================
       C) EMPLOYEE & ROLE MANAGEMENT
       ========================================================= */

    public int employeeCreate(
            int actorAdminId,
            int branchId,
            String name,
            String email,
            String phone,
            Date dob,
            String address,
            String role, // "TELLER","MANAGER","ADMIN"
            String passwordHash,
            String secQuestion,
            String secAnswerHash,
            String adminSecondaryPassword // null if not ADMIN
    ) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_employee_create(?,?,?,?,?,?,?,?,?,?,?,?)}")) {

            stmt.setInt(1, actorAdminId);
            stmt.setInt(2, branchId);
            stmt.setString(3, name);
            stmt.setString(4, email);
            stmt.setString(5, phone);
            stmt.setDate(6, new java.sql.Date(dob.getTime()));
            stmt.setString(7, address);
            stmt.setString(8, role);
            stmt.setString(9, passwordHash);
            stmt.setString(10, secQuestion);
            stmt.setString(11, secAnswerHash);
            stmt.setString(12, adminSecondaryPassword);

            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        return rs.getInt("employee_id");
                    }
                }
            }
            throw new SQLException("sp_employee_create did not return an employee_id");
        }
    }

    public void employeeUpdate(
            int actorAdminId,
            int employeeId,
            int branchId,
            String name,
            String email,
            String phone,
            Date dob,
            String address,
            String role
    ) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_employee_update(?,?,?,?,?,?,?,?,?)}")) {

            stmt.setInt(1, actorAdminId);
            stmt.setInt(2, employeeId);
            stmt.setInt(3, branchId);
            stmt.setString(4, name);
            stmt.setString(5, email);
            stmt.setString(6, phone);
            stmt.setDate(7, new java.sql.Date(dob.getTime()));
            stmt.setString(8, address);
            stmt.setString(9, role);
            stmt.execute();
        }
    }

    public void employeeChangeRole(int actorAdminId, int employeeId, String newRole) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_employee_change_role(?,?,?)}")) {

            stmt.setInt(1, actorAdminId);
            stmt.setInt(2, employeeId);
            stmt.setString(3, newRole);
            stmt.execute();
        }
    }

    public void employeeDeactivate(int actorAdminId, int employeeId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_employee_deactivate(?,?)}")) {

            stmt.setInt(1, actorAdminId);
            stmt.setInt(2, employeeId);
            stmt.execute();
        }
    }

    public void employeeReactivate(int actorAdminId, int employeeId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_employee_reactivate(?,?)}")) {

            stmt.setInt(1, actorAdminId);
            stmt.setInt(2, employeeId);
            stmt.execute();
        }
    }

    /**
     * Fetches the employee from the database with the given employeeId.
     * @param employeeId the ID of the employee to fetch
     * @return List of rows with employee info.
     * @throws SQLException if database error occurs.
     */
    public List<Map<String, Object>> employeeGetById(int employeeId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_employee_get_by_id(?)}")) {

            stmt.setInt(1, employeeId);
            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    /**
     * Fetches the employee from the database with the given email.
     * @param email the email of the employee to fetch
     * @return List of rows with employee info.
     * @throws SQLException if database error occurs.
     */
    public List<Map<String, Object>> employeeGetByEmail(String email) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM employee WHERE employee_email = ?")) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return ResultSetUtils.toList(rs);
            }
        }
    }

    public List<Map<String, Object>> employeeListByBranch(int branchId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_employee_list_by_branch(?)}")) {

            stmt.setInt(1, branchId);
            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    /**
     * Fetches all employees in the database no matter the branch.  
     * @return List of rows with employee info. 
     * @throws SQLException if database error occurs.
     */
    public List<Map<String, Object>> getAllEmployee() throws SQLException {
        String sql = """
            SELECT employee_id, employee_name, employee_email, employee_phone,
                dob, employee_address, role, branch_id
            FROM employee
        """;

        try (Connection conn = DbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            return ResultSetUtils.toList(rs);
        }
    }


    /* =========================================================
       D) ACCOUNT MANAGEMENT
       ========================================================= */

    public Map<String, Object> accountOpen(
            int actorEmployeeId,
            int customerId,
            int branchId,
            String accountType, // "SAVING" or "CHECKING"
            double initialBalance,
            Double interestRate,
            String chequebookNumber,
            String bankCode
    ) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_account_open(?,?,?,?,?,?,?,?)}")) {

            stmt.setInt(1, actorEmployeeId);
            stmt.setInt(2, customerId);
            stmt.setInt(3, branchId);
            stmt.setString(4, accountType);
            stmt.setBigDecimal(5, java.math.BigDecimal.valueOf(initialBalance));

            if (interestRate != null) {
                stmt.setBigDecimal(6, java.math.BigDecimal.valueOf(interestRate));
            } else {
                stmt.setNull(6, Types.DECIMAL);
            }

            if (chequebookNumber != null) {
                stmt.setString(7, chequebookNumber);
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }

            if (bankCode != null) {
                stmt.setString(8, bankCode);
            } else {
                stmt.setNull(8, Types.VARCHAR);
            }

            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    List<Map<String, Object>> rows = ResultSetUtils.toList(rs);
                    if (!rows.isEmpty()) {
                        return rows.get(0); // contains account_id, account_number
                    }
                }
            }
            throw new SQLException("sp_account_open did not return account info");
        }
    }

    public List<Map<String, Object>> accountGetById(int accountId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_account_get_by_id(?)}")) {

            stmt.setInt(1, accountId);
            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> accountListByCustomer(int customerId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_account_list_by_customer(?)}")) {

            stmt.setInt(1, customerId);
            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> accountOverviewForCustomer(int customerId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_account_get_overview_for_customer(?)}")) {

            stmt.setInt(1, customerId);
            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }


    /* =========================================================
       E) TRANSACTION MANAGEMENT
       (deposit/withdraw/transfer + read/reverse)
       ========================================================= */

    public void transactionDeposit(int accountId, double amount, String actorType, int actorId)
            throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_transaction_deposit(?,?,?,?)}")) {

            stmt.setInt(1, accountId);
            stmt.setBigDecimal(2, java.math.BigDecimal.valueOf(amount));
            stmt.setString(3, actorType); // "CUSTOMER" or "EMPLOYEE"
            stmt.setInt(4, actorId);
            stmt.execute();
        }
    }

    public void transactionWithdraw(int accountId, double amount, String actorType, int actorId)
            throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_transaction_withdraw(?,?,?,?)}")) {

            stmt.setInt(1, accountId);
            stmt.setBigDecimal(2, java.math.BigDecimal.valueOf(amount));
            stmt.setString(3, actorType);
            stmt.setInt(4, actorId);
            stmt.execute();
        }
    }

    public void transactionTransfer(int fromAccountId, int toAccountId, double amount,
                                    String actorType, int actorId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_transaction_transfer(?,?,?,?,?)}")) {

            stmt.setInt(1, fromAccountId);
            stmt.setInt(2, toAccountId);
            stmt.setBigDecimal(3, java.math.BigDecimal.valueOf(amount));
            stmt.setString(4, actorType);
            stmt.setInt(5, actorId);
            stmt.execute();
        }
    }

    public List<Map<String, Object>> transactionGetRecentByAccount(int accountId, int limit)
            throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_transaction_get_recent_by_account(?,?)}")) {

            stmt.setInt(1, accountId);
            stmt.setInt(2, limit);
            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> transactionGetById(long transactionId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_transaction_get_by_id(?)}")) {

            stmt.setLong(1, transactionId);
            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    public void transactionReverse(int actorEmployeeId, long transactionId, String reason)
            throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_transaction_reverse(?,?,?)}")) {

            stmt.setInt(1, actorEmployeeId);
            stmt.setLong(2, transactionId);
            stmt.setString(3, reason);
            stmt.execute();
        }
    }


    /* =========================================================
       F) AUDIT & MONITORING
       ========================================================= */

    public List<Map<String, Object>> auditGetByBranch(int branchId, Timestamp from, Timestamp to)
            throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_audit_get_by_branch(?,?,?)}")) {

            stmt.setInt(1, branchId);
            stmt.setTimestamp(2, from);
            stmt.setTimestamp(3, to);

            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> auditGetForActor(String actorType, int actorId,
                                                      Timestamp from, Timestamp to)
            throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_audit_get_for_actor(?,?,?,?)}")) {

            stmt.setString(1, actorType); // "CUSTOMER" or "EMPLOYEE"
            stmt.setInt(2, actorId);
            stmt.setTimestamp(3, from);
            stmt.setTimestamp(4, to);

            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> auditGetForTarget(String targetType, int targetId,
                                                       Timestamp from, Timestamp to)
            throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_audit_get_for_target(?,?,?,?)}")) {

            stmt.setString(1, targetType);
            stmt.setInt(2, targetId);
            stmt.setTimestamp(3, from);
            stmt.setTimestamp(4, to);

            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }


    /* =========================================================
       G) REPORTING / ADMIN EXTRAS
       ========================================================= */

    public List<Map<String, Object>> reportBranchSummary(int branchId,
                                                         Timestamp from,
                                                         Timestamp to) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_report_branch_summary(?,?,?)}")) {

            stmt.setInt(1, branchId);
            stmt.setTimestamp(2, from);
            stmt.setTimestamp(3, to);

            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> reportCustomerActivity(int customerId,
                                                            Timestamp from,
                                                            Timestamp to) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_report_customer_activity(?,?,?)}")) {

            stmt.setInt(1, customerId);
            stmt.setTimestamp(2, from);
            stmt.setTimestamp(3, to);

            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return ResultSetUtils.toList(rs);
                }
            }
            return Collections.emptyList();
        }
    }

    public void applyMonthlyInterest(int actorEmployeeId, int branchId) throws SQLException {
        try (Connection conn = DbManager.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_apply_monthly_interest(?,?)}")) {

            stmt.setInt(1, actorEmployeeId);
            stmt.setInt(2, branchId);
            stmt.execute();
        }
    }

/* =========================================================
   H) BRANCH LOOKUP (needed for Create Account)
   ========================================================= */

/**
 * Returns all branches from the database as Branch model objects.
 * Uses a direct SELECT on the branch table.
 */
    public List<Branch> getAllBranches() throws SQLException {

        String sql = "SELECT branch_id, branch_code, branch_name, branch_address FROM branch";

        try (Connection conn = DbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            List<Branch> result = new ArrayList<>();

            while (rs.next()) {
                int branchId   = rs.getInt("branch_id");
                String code    = rs.getString("branch_code");
                String name    = rs.getString("branch_name");
                String address = rs.getString("branch_address");

                Branch b = new Branch(branchId, code, name, address);
                result.add(b);
            }

            return result;
        }
    }

    /**
     * Allows an admin to update an employee's role (and optionally branch),
     * while validating the admin's secondary password and writing to the audit log.
     *
     * This method relies on a stored procedure:
     *
     *   sp_admin_update_user_role(
     *       IN p_actor_admin_id INT,
     *       IN p_target_employee_id INT,
     *       IN p_new_role VARCHAR(50),
     *       IN p_new_branch_id INT,
     *       IN p_admin_secondary_password_plain VARCHAR(255),
     *       IN p_new_admin_secondary_password_plain VARCHAR(255)
     *   )
     *
     * The stored procedure should:
     *  - Verify the actor admin's secondary password.
     *  - Update the target employee's role and branch.
     *  - If p_new_admin_secondary_password_plain is not NULL and new role is ADMIN,
     *    hash it and store it in admin_secondary_password_hash.
     *  - Insert an entry in the audit log with the branch.
     */
    public void adminUpdateUserRole(
            int actorAdminId,
            int targetEmployeeId,
            String newRole,
            Integer newBranchId,
            String adminSecondaryPasswordPlain,
            String newAdminSecondaryPasswordPlain
    ) throws SQLException {

        try (Connection conn = DbManager.getConnection();
                CallableStatement stmt =
                        conn.prepareCall("{CALL sp_admin_update_user_role(?,?,?,?,?,?)}")) {

            stmt.setInt(1, actorAdminId);
            stmt.setInt(2, targetEmployeeId);
            stmt.setString(3, newRole);

            if (newBranchId != null) {
                stmt.setInt(4, newBranchId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setString(5, adminSecondaryPasswordPlain);
            stmt.setString(6, newAdminSecondaryPasswordPlain);

            stmt.execute();
        }
    }

    // Audit Log
public List<Map<String, Object>> getAuditLog(
        Integer branchId,
        String actorType,        // "CUSTOMER", "EMPLOYEE" or null
        Integer actorId,
        Timestamp from,
        Timestamp to
) throws SQLException {

    List<Map<String, Object>> result = Collections.emptyList();

    try (Connection conn = DbManager.getConnection();
         CallableStatement stmt =
                 conn.prepareCall("{CALL sp_audit_log_search(?,?,?,?,?)}")) {

        if (branchId == null) {
            stmt.setNull(1, Types.INTEGER);
        } else {
            stmt.setInt(1, branchId);
        }

        if (actorType == null || actorType.isBlank()) {
            stmt.setNull(2, Types.VARCHAR);
        } else {
            stmt.setString(2, actorType);
        }

        if (actorId == null) {
            stmt.setNull(3, Types.INTEGER);
        } else {
            stmt.setInt(3, actorId);
        }

        if (from == null) {
            stmt.setNull(4, Types.TIMESTAMP);
        } else {
            stmt.setTimestamp(4, from);
        }

        if (to == null) {
            stmt.setNull(5, Types.TIMESTAMP);
        } else {
            stmt.setTimestamp(5, to);
        }

        boolean hasResult = stmt.execute();
        if (hasResult) {
            try (ResultSet rs = stmt.getResultSet()) {
                result = ResultSetUtils.toList(rs);
            }
        }
    }

    return result;
}



}
