package bank.Models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class AccountCreationTest {

    @Test
    public void testAccountCreationAndRetrieval() {

        AccountStub testAccount = new AccountStub(
                1,          // accountId
                101,        // customerId
                201,        // branchId
                "A1001",    // accountNumber
                0.0         // balance
        );

        // Validate fields
        assertEquals("A1001", testAccount.getAccountNumber());
        assertEquals(0.0, testAccount.getBalance());
        assertEquals(1, testAccount.getAccountId());
        assertEquals(101, testAccount.getCustomerId());
        assertEquals("CHECKING", testAccount.getAccountType());
        assertNotNull(testAccount.getCreatedAt());

        // Boundary: Balance cannot be negative
        assertTrue(testAccount.getBalance() >= 0);
    }
}
