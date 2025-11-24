package bank.Models;

// For unit testing
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
// For assertions
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is used to perform JUnit testing on the Transaction model
 */
class TransactionTest {

    @Test
    /**
     * This function reflects the first transaction unit test from Assignment 03.
     * It tests whether the constructor for Transaction objects works as specified.
     */
    void testCreateTransactionObject() {
        // First create a customer using the stub
        CustomerStub currentCustomer = new CustomerStub(
                1,
                "Test Customer"
        );

        // Then create an account using the stub
        AccountStub currentAccount = new AccountStub(
                1,                    
                currentCustomer.getId(),
                0,                   // branchId - not needed
                "",                 // accountNo - not needed
                1000.00
        );

        LocalDateTime createdAt = LocalDateTime.now();

        // Then create a transaction 
        Transaction currentTransaction = new Transaction(
                1,
                currentAccount,
                100.00,
                "DEPOSIT",
                "SUCCESS",
                String.valueOf(currentCustomer.getId()),
                createdAt
        );

        // And verify that the values are all correct 
        assertNotNull(currentTransaction);
        assertEquals(1, currentTransaction.getTransactionID());
        assertSame(currentAccount, currentTransaction.getAccount());
        assertEquals(100.00, currentTransaction.getAmount());
        assertEquals("DEPOSIT", currentTransaction.getTransactionType());
        assertEquals("SUCCESS", currentTransaction.getStatus());
        assertEquals("1", currentTransaction.getPerformedByUserId());
        assertNotNull(currentTransaction.getCreatedAt());

        // Ensure that just creating a transaction doesn't change the balance yet
        assertEquals(1000.00, currentAccount.getBalance());
    }

    @Test
    /**
     * This function reflects the second transaction unit test from Assignment 03.
     * It tests whether we can retrieve an ID properly.
     */
    void testRetrieveTransactionId() {

        // First create a customer using the stub
        CustomerStub currentCustomer = new CustomerStub(
                1,
                "Test Customer"
        );

        // Then create an account using the stub
        AccountStub currentAccount = new AccountStub(
                1,                    
                currentCustomer.getId(),
                0,                   // branchId - not needed
                "",                 // accountNo - not needed
                1000.00
        );

        LocalDateTime createdAt = LocalDateTime.now();

        // And create the transaction object
        Transaction currentTransaction = new Transaction(
                1,
                currentAccount,
                100.00,
                "DEPOSIT",
                "SUCCESS",
                String.valueOf(currentCustomer.getId()),
                createdAt
        );

        // Then retrieve the ID of the object
        int returnedId = currentTransaction.getTransactionID();

        // And check that the ID is the same as what's expected
        assertEquals(1, returnedId);

        // Also check that all other fields of the constructor are fine
        assertSame(currentAccount, currentTransaction.getAccount());
        assertEquals(100.00, currentTransaction.getAmount());
        assertEquals("DEPOSIT", currentTransaction.getTransactionType());
        assertEquals("SUCCESS", currentTransaction.getStatus());
        assertEquals("1", currentTransaction.getPerformedByUserId());
        assertEquals(createdAt, currentTransaction.getCreatedAt());

        // And that the balance didn't change
        assertEquals(1000.00, currentAccount.getBalance());
    }
}