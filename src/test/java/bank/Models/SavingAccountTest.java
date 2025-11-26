package bank.Models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class SavingAccountTest {

    @Test
    public void testSavingAccountInterestAndWithdrawals() {

        SavingAccount savingAcc = new SavingAccount(
                3,              // accountId
                103,            // customerId
                203,            // branchId
                "S1001",        // account number
                1000.0,         // initial balance
                0.05,           // interest rate
                "BANK-01",      // bank code
                LocalDateTime.now(),
                0.0             // minimumBalance
        );

        // Step 1 — calculate interest manually since no method exists
        double interest = savingAcc.getBalance() * savingAcc.getInterestRate();
        assertEquals(50.0, interest);

        // Apply interest
        savingAcc.setBalance(savingAcc.getBalance() + interest);
        assertEquals(1050.0, savingAcc.getBalance());

        // Step 2 — valid withdrawal
        assertTrue(savingAcc.canWithdraw(200.0));
        savingAcc.setBalance(savingAcc.getBalance() - 200.0);
        assertEquals(850.0, savingAcc.getBalance());

        // Step 3 — invalid withdrawal
        Exception ex = assertThrows(RuntimeException.class, () -> {
            if (!savingAcc.canWithdraw(900.0)) {
                throw new RuntimeException("Insufficient funds");
            }
        });

        assertTrue(ex.getMessage().contains("Insufficient"));

        // Final checks
        assertEquals("S1001", savingAcc.getAccountNumber());
        assertEquals(0.05, savingAcc.getInterestRate());
        assertEquals(850.0, savingAcc.getBalance());
    }
}
