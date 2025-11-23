package bank.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Bank in the system.
 */
public class Bank {

    private final String bankName;
    private final List<Branch> branches;
    private final List<Customer> customers;
    private final List<Account> accounts;

    public Bank(String bankName) {
        this.bankName = bankName;
        this.branches = new ArrayList<>();
        this.customers = new ArrayList<>();
        this.accounts = new ArrayList<>();
    }

    public String getBankName() {
        return bankName;
    }

    public String getName() {
        return bankName;
    }

    public void addBranch(Branch branch) {
        if (branch != null && !branches.contains(branch)) {
            branches.add(branch);
        }
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void printBankInfo() {
        System.out.println("Bank: " + bankName);
        for (Branch branch : branches) {
            System.out.println(" - Branch: " + branch.getBranchAddress());
        }
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}