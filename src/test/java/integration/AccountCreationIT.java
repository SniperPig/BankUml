package integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import bank.DB.BankDb;
import bank.DB.DbManager;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountCreationIT {

    private BankDb db;
    private int createdCustomerId = -1;
    private int createdAccountId = -1;

    @BeforeAll
    void setup() {
        db = new BankDb();
    }

    @Test
    @DisplayName("ITC-03: Create account (valid) and reject invalid inputs")
    void testAccountCreateAndValidation() throws Exception {
        // create a test customer (actorEmployeeId = 0 for tests)
        createdCustomerId = db.customerCreate(0, 1, "IT Test User", "it-test@example.com", "000-000-0000",
                new java.sql.Date(System.currentTimeMillis())
, "GOV-IT-1", "123 Test St", "testhash", "Q", "A");

        // open an account using stored proc - accountOpen returns map with account_id
        Map<String, Object> accRow = db.accountOpen(0, createdCustomerId, 1, "CHECKING", 100.00, null, "CHK-TEST", "BANK-IT");

        assertNotNull(accRow);
        assertTrue(accRow.containsKey("account_id"));
        createdAccountId = ((Number) accRow.get("account_id")).intValue();

        // fetch the account directly to validate initial balance
        List<Map<String, Object>> rows = db.accountGetById(createdAccountId);
        assertFalse(rows.isEmpty(), "accountGetById should return a row for the created account");
        Map<String, Object> row = rows.get(0);

        double balance = ((Number)row.get("balance")).doubleValue();
        assertEquals(100.00, balance, 0.001);

        // Negative test: attempt to create account with negative initial balance - depends on stored proc validation
        // We expect stored proc to either throw SQLException or return an error. We'll call and expect SQLException.
        SQLException negativeEx = assertThrows(SQLException.class, () ->
                db.accountOpen(0, createdCustomerId, 1, "CHECKING", -50.00, null, "CHK-TEST2", "BANK-IT")
        );

        // pass if it threw (if your stored proc allows creation of negative balances, adapt expectation)
        assertNotNull(negativeEx);
    }

    @AfterAll
    void cleanup() {
        // remove created account and customer (if any)
        try (Connection conn = DbManager.getConnection()) {
            if (createdAccountId > 0) {
                try (PreparedStatement p = conn.prepareStatement("DELETE FROM bank_transaction WHERE account_id = ?")) {
                    p.setInt(1, createdAccountId);
                    p.executeUpdate();
                } catch (SQLException e) {
                    // ignore
                }
                try (PreparedStatement p = conn.prepareStatement("DELETE FROM account WHERE account_id = ?")) {
                    p.setInt(1, createdAccountId);
                    p.executeUpdate();
                } catch (SQLException e) {
                    // ignore
                }
            }
            if (createdCustomerId > 0) {
                try (PreparedStatement p = conn.prepareStatement("DELETE FROM customer WHERE customer_id = ?")) {
                    p.setInt(1, createdCustomerId);
                    p.executeUpdate();
                } catch (SQLException e) {
                    // ignore
                }
            }
        } catch (SQLException ignored) {}
    }
}
