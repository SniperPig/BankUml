package bank.Models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class CheckingWithdrawalTest {

    @Test
    public void testOverdraftPrevention() {

        CheckingAccount account = new CheckingAccount(
                2,              // accountId
                102,            // customerId
                202,            // branchId
                "A1002",        // number
                100.0,          // balance
                "CHK-001",
                "BANK-01",
                LocalDateTime.now(),
                0.0             // overdraft limit = zero = STRICT
        );

        // Attempt to withdraw MORE than balance
        Exception ex = assertThrows(RuntimeException.class, () -> {
            if (!account.canWithdraw(150.0)) {
                throw new RuntimeException("Insufficient funds");
            }
            account.setBalance(account.getBalance() - 150.0);
        });

        assertTrue(ex.getMessage().contains("Insufficient"));

        // Balance remains unchanged
        assertEquals(100.0, account.getBalance());
    }
}
