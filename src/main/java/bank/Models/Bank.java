package bank.Models;

import java.util.ArrayList;
import java.util.List;

public class Bank {
    private final String name;
    private final List<Branch> branches;

    public Bank(String name) {
        this.name = name;
        this.branches = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void addBranch(Branch branch) {
        branches.add(branch);
    }

    public void printBankInfo() {
        System.out.println("Bank: " + name);
        for (Branch branch : branches) {
            System.out.println("Branch: " + branch.getAddress());
        }
    }
}
