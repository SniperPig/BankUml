package bank.Models;

/**
 * Represents a physical branch of the bank.
 *
 * UML attributes:
 *  - branchAddress : String
 *  - bank          : Bank
 *  - branchCode    : String
 *
 * UML operations:
 *  + Branch(code: String, address: String, bank: Bank)
 *  + getCode() : String
 *  + getBranchAddress() : String
 *  + getBank() : Bank
 *  + printBranchInfo() : void
 */
public class Branch {

    // === Attributes ===
    private final String branchAddress;
    private final Bank bank;
    private final String branchCode;

    // === Constructor ===
    public Branch(String code, String address, Bank bank) {
        this.branchCode = code;
        this.branchAddress = address;
        this.bank = bank;

        // Old behavior: automatically register branch in its bank
        if (bank != null) {
            bank.addBranch(this);
        }
    }

    // === UML Methods ===

    /** Returns the branch code. */
    public String getCode() {
        return branchCode;
    }

    /** Returns the branch address. */
    public String getBranchAddress() {
        return branchAddress;
    }

    /** Returns the bank this branch belongs to. */
    public Bank getBank() {
        return bank;
    }

    /** Prints formatted branch information. */
    public void printBranchInfo() {
        String bankName = (bank != null) ? bank.getBankName() : "<no bank>";
        System.out.println("Branch " + branchCode +
                " at " + branchAddress +
                " from Bank " + bankName);
    }

    // === Backwards compatibility (original code used getAddress()) ===
    public String getAddress() {
        return branchAddress;
    }
}