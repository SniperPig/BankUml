package bank.Models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * TC-02: Test retrieving all branches associated with a Bank.
 *
 * Objective:
 * Validate that the getBranches() method of Bank correctly returns
 * all branches that are associated with the Bank object.
 *
 * Boundary:
 * A Bank with no branches should return an empty list.
 */
public class RetrieveAllBranch_TC02 {

    /**
     * TC-02
     * Steps:
     * 1. Create a Bank object named "Concordia Bank"
     * 2. Create two Branch objects:
     *      - branchAddress = "123 Test Ave.", branchCode = "CB1"
     *      - branchAddress = "456 Test Ave.", branchCode = "CB2"
     *    Both referencing the same bank.
     * 3. Add both branches to the Bank via addBranch()
     * 4. Retrieve list using getBranches()
     * 5. Validate size, ordering, and field values
     */
    @Test
    public void testGetBranches() {


        Bank currentBank = new Bank("Concordia Bank");


        Branch currentBranch = new Branch(
                "123 Test Ave.",     // branchAddress
                "CB1",               // branchCode
                currentBank          // bank
        );

        Branch currentBranch2 = new Branch(
                "456 Test Ave.",     // branchAddress
                "CB2",               // branchCode
                currentBank
        );


        currentBank.addBranch(currentBranch);
        currentBank.addBranch(currentBranch2);


        List<Branch> listOfBranches = currentBank.getBranches();


        assertNotNull(listOfBranches, "Branch list should not be null.");
        assertEquals(2, listOfBranches.size(), "Bank should have exactly 2 branches.");

        Branch firstBranch = listOfBranches.get(0);
        Branch secondBranch = listOfBranches.get(1);


        assertEquals("123 Test Ave.", firstBranch.getBranchAddress(), "First branch address mismatch.");
        assertEquals("CB1", firstBranch.getCode(), "First branch code mismatch.");
        assertEquals(currentBank, firstBranch.getBank(), "First branch should reference the same bank.");


        assertEquals("456 Test Ave.", secondBranch.getBranchAddress(), "Second branch address mismatch.");
        assertEquals("CB2", secondBranch.getCode(), "Second branch code mismatch.");
        assertEquals(currentBank, secondBranch.getBank(), "Second branch should reference the same bank.");
    }

    /**
     * Boundary Test:
     * Bank with no branches → return empty list
     */
    @Test
    public void testGetBranches_EmptyBank() {
        Bank emptyBank = new Bank("Empty Bank");

        List<Branch> branches = emptyBank.getBranches();

        assertNotNull(branches, "Branch list should not be null.");
        assertTrue(branches.isEmpty(), "Branch list should be empty for a new bank.");
    }
}