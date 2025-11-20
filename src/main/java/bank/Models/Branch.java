package bank.Models;

/**
 * Represents a physical branch of the bank.
 *
 * UML-required attributes:
 *  - branchAddress : String
 *  - bank          : Bank
 *  - branchCode    : String
 *
 * DB-required attributes (needed for MySQL):
 *  - branchId      : int
 *  - branchName    : String
 */
public class Branch {

    // === UML attributes ===
    private final String branchAddress;
    private final Bank bank;
    private final String branchCode;

    // === DB attributes ===
    private final int branchId;
    private final String branchName;

    /**
     * Constructor used when loading branches from the DATABASE.
     * This is your MAIN constructor in the real system.
     */
    public Branch(int branchId, String code, String name, String address) {
        this.branchId = branchId;
        this.branchCode = code;
        this.branchName = name;
        this.branchAddress = address;
        this.bank = null; // DB does not return Bank object
    }

    /**
     * UML constructor (kept for minimum UML compliance).
     * This is not used in DB context but required by UML.
     */
    public Branch(String code, String address, Bank bank) {
        this.branchCode = code;
        this.branchAddress = address;
        this.bank = bank;

        this.branchId = -1;      // not known
        this.branchName = null;  // not known

        if (bank != null) {
            bank.addBranch(this);
        }
    }

    // === UML required methods ===
    public String getCode() {
        return branchCode;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public Bank getBank() {
        return bank;
    }

    public void printBranchInfo() {
        System.out.println("Branch " + branchCode + " at " + branchAddress);
    }

    // === DB getters ===
    public int getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    /**
     * Used by ComboBox to show branches nicely.
     */
    @Override
    public String toString() {
        if (branchName != null) {
            return branchName + " (" + branchCode + ")";
        }
        return branchCode;
    }
}