package bank.Models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-01: Unit tests for the Bank class — creation test.
 *
 * Objective:
 * Verify that a Bank is created with the given name and empty lists
 * of branches, customers, and accounts.
 */
public class BankCreateTest {

    /**
     * TC-01
     * Test create Bank object.
     *
     * Steps:
     * 1. Create a Bank object with name = "Concordia Bank"
     * 2. Verify all internal lists are empty and name is set.
     *
     * Boundary:
     * Valid input → No exceptions should occur.
     */
    @Test
    public void testBankConstructor() {

        String bankName = "Concordia Bank";
        Bank currentBank = new Bank(bankName);


        assertNotNull(currentBank, "Bank object should not be null.");


        assertEquals(bankName, currentBank.getBankName(),
                "Bank name should match the constructor input.");


        assertNotNull(currentBank.getBranches(), "Branches list should not be null.");
        assertTrue(currentBank.getBranches().isEmpty(), "Branches list should be empty.");

        assertNotNull(currentBank.getCustomers(), "Customers list should not be null.");
        assertTrue(currentBank.getCustomers().isEmpty(), "Customers list should be empty.");

        assertNotNull(currentBank.getAccounts(), "Accounts list should not be null.");
        assertTrue(currentBank.getAccounts().isEmpty(), "Accounts list should be empty.");
    }
}