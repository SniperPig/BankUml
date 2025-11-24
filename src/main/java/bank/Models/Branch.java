package bank.Models;

/**
 * Represents a physical branch of the bank.
 */
public class Branch {

    private final String branchAddress;
    private final Bank bank;
    private final String branchCode;

    private final int branchId;
    private final String branchName;

    public Branch(int branchId, String code, String name, String address) {
        this.branchId = branchId;
        this.branchCode = code;
        this.branchName = name;
        this.branchAddress = address;
        this.bank = null; // DB does not return Bank object
    }

    public Branch(String code, String address, Bank bank) {
        this.branchCode = code;
        this.branchAddress = address;
        this.bank = bank;

        this.branchId = -1;      
        this.branchName = null;  

        if (bank != null) {
            bank.addBranch(this);
        }
    }

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

    public int getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    @Override
    public String toString() {
        if (branchName != null) {
            return branchName + " (" + branchCode + ")";
        }
        return branchCode;
    }
}