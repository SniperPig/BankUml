package bank.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Bank in the system.
 *
 * UML attributes:
 *  - bankName : String
 *  - branches : List<Branch>
 *  - customers : List<Customer>
 *  - accounts : List<Account>
 *
 * UML operations:
 *  + Bank(name: String)
 *  + addBranch(branch: Branch) : void
 *  + getBankName() : String
 *  + getBranches() : List<Branch>
 *  + printBankInfo() : void
 */
public class Bank {

    // === Attributes (from UML) ===
    private final String bankName;
    private final List<Branch> branches;
    private final List<Customer> customers;
    private final List<Account> accounts;

    // === Constructor ===
    public Bank(String bankName) {
        this.bankName = bankName;
        this.branches = new ArrayList<>();
        this.customers = new ArrayList<>();
        this.accounts = new ArrayList<>();
    }

    // === UML: getBankName() ===
    public String getBankName() {
        return bankName;
    }

    // === Compatibility: existing code uses getName() ===
    public String getName() {
        return bankName;
    }

    // === UML: addBranch() ===
    public void addBranch(Branch branch) {
        if (branch != null && !branches.contains(branch)) {
            branches.add(branch);
        }
    }

    // === UML: getBranches() ===
    public List<Branch> getBranches() {
        return branches;
    }

    // === UML: printBankInfo() ===
    public void printBankInfo() {
        System.out.println("Bank: " + bankName);
        for (Branch branch : branches) {
            System.out.println(" - Branch: " + branch.getBranchAddress());
        }
    }

    // === Optional getters (not required by UML but useful) ===
    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}