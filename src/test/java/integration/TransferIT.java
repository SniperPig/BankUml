package integration;

import bank.DB.BankDb;
import bank.DB.DbManager;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransferIT {

    private BankDb db;
    private int customerId = -1;
    private int fromAccountId = -1;
    private int toAccountId = -1;

    @BeforeAll
    void setup() throws SQLException {
        db = new BankDb();
        // create test customer
        customerId = db.customerCreate(0, 1, "Transfer User", "transfer-user@example.com", "000-111-2222",
                new java.sql.Date(System.currentTimeMillis())
, "GOV-TR-1", "1 Transfer St", "hash", "Q", "A");

        // open two accounts for the same customer
        Map<String,Object> r1 = db.accountOpen(0, customerId, 1, "CHECKING", 300.00, null, "CHK-FROM", "BANK-IT");
        Map<String,Object> r2 = db.accountOpen(0, customerId, 1, "SAVING", 200.00, 0.05, null, "BANK-IT");

        fromAccountId = ((Number)r1.get("account_id")).intValue();
        toAccountId = ((Number)r2.get("account_id")).intValue();
    }

    @Test
    @DisplayName("ITC-04: Transfer respects overdraft and deposits to receiver")
    void testTransferAndReceiverDeposit() throws Exception {
        // transfer 250 (should succeed: 300 balance)
        db.transactionTransfer(fromAccountId, toAccountId, 250.00, "CUSTOMER", customerId);

        // read balances
        List<Map<String,Object>> fromRows = db.accountGetById(fromAccountId);
        List<Map<String,Object>> toRows = db.accountGetById(toAccountId);
        assertFalse(fromRows.isEmpty());
        assertFalse(toRows.isEmpty());

        double fromBalance = ((Number)fromRows.get(0).get("balance")).doubleValue();
        double toBalance = ((Number)toRows.get(0).get("balance")).doubleValue();

        assertEquals(50.00, fromBalance, 0.01, "Sender balance should have reduced by 250");
        assertEquals(450.00, toBalance, 0.01, "Receiver balance should have increased by 250");

        // transfer that exceeds overdraft (try 200, but suppose overdraft limit 0 for checking here)
        // If the checking account has overdraft limit configured on DB, adjust expectation.
        SQLException ex = assertThrows(SQLException.class, () ->
                db.transactionTransfer(fromAccountId, toAccountId, 500.00, "CUSTOMER", customerId)
        );
        assertNotNull(ex);
    }

    @AfterAll
    void cleanup() {
        try (Connection conn = DbManager.getConnection()) {
            if (fromAccountId > 0) {
                try (PreparedStatement p = conn.prepareStatement("DELETE FROM bank_transaction WHERE account_id = ?")) {
                    p.setInt(1, fromAccountId);
                    p.executeUpdate();
                } catch (SQLException ignored) {}
                try (PreparedStatement p = conn.prepareStatement("DELETE FROM account WHERE account_id = ?")) {
                    p.setInt(1, fromAccountId);
                    p.executeUpdate();
                } catch (SQLException ignored) {}
            }
            if (toAccountId > 0) {
                try (PreparedStatement p = conn.prepareStatement("DELETE FROM bank_transaction WHERE account_id = ?")) {
                    p.setInt(1, toAccountId);
                    p.executeUpdate();
                } catch (SQLException ignored) {}
                try (PreparedStatement p = conn.prepareStatement("DELETE FROM account WHERE account_id = ?")) {
                    p.setInt(1, toAccountId);
                    p.executeUpdate();
                } catch (SQLException ignored) {}
            }
            if (customerId > 0) {
                try (PreparedStatement p = conn.prepareStatement("DELETE FROM customer WHERE customer_id = ?")) {
                    p.setInt(1, customerId);
                    p.executeUpdate();
                } catch (SQLException ignored) {}
            }
        } catch (SQLException ignored) {}
    }
}
