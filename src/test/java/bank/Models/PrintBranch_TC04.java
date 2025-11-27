package bank.Models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * TC-04: Test printBranchInfo output.
 *
 * Objective:
 * Ensure that printBranchInfo() prints the properly formatted line containing:
 * - branch code
 * - branch address
 * - bank name
 *
 * Boundary:
 * Valid branch with non-empty fields → should print correctly with no exceptions.
 */
public class PrintBranch_TC04 {

    /**
     * TC-04
     * Steps:
     * 1. Create Bank object with name "RBC"
     * 2. Create Branch with:
     *      branchCode = "B2"
     *      branchAddress = "20 Grove Pk"
     *      bank = currentBank
     * 3. Capture console output of printBranchInfo()
     * 4. Compare expected output
     */
    @Test
    public void testPrintBranchInfo() {

        Bank currentBank = new Bank("RBC");


        Branch currentBranch = new Branch(
                "B2",
                "20 Grove Pk",
                currentBank
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            currentBranch.printBranchInfo();
        } finally {
            System.setOut(originalOut);    // Restore System.out
        }


        String output = outputStream.toString().trim();


        String expected = "Branch B2 at 20 Grove Pk";


        assertEquals(expected, output,
                "printBranchInfo() should output the correct formatted branch info.");
    }
}