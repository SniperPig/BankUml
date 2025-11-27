package bank.Models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-03: Test creating a Branch object.
 *
 * Objective:
 * Ensure that a user can successfully create a Branch object
 * with valid branchCode, branchAddress, and Bank association.
 *
 * Boundary:
 * Valid fields → No exceptions raised.
 */
public class CreateBranch_TC03 {

    /**
     * TC-03
     * Steps:
     * 1. Create a Bank object (bankName = "RBC")
     * 2. Create a Branch:
     *      branchAddress = "20 Grove Pk"
     *      branchCode = "B2"
     *      bank = currentBank
     * 3. Verify constructor fields
     */
    @Test
    public void testCreateBranch() {

        // Step 1 — Create bank
        Bank currentBank = new Bank("RBC");

        // Step 2 — Create branch
        Branch currentBranch = new Branch(
                "B2",             // branchCode
                "20 Grove Pk",    // branchAddress
                currentBank       // bank
        );

        // Step 3 — Validate branch fields
        assertNotNull(currentBranch, "Branch object should not be null.");

        assertEquals("B2", currentBranch.getCode(),
                "Branch code should match the constructor input.");

        assertEquals("20 Grove Pk", currentBranch.getBranchAddress(),
                "Branch address should match the constructor input.");

        assertEquals(currentBank, currentBranch.getBank(),
                "Branch should reference the bank passed in the constructor.");

        // Bonus check: adding branch to bank should work
        assertTrue(currentBank.getBranches().contains(currentBranch),
                "Bank should contain the new branch in its branch list.");
    }
}