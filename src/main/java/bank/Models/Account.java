package bank.Models;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Account {

    protected int accountId;
    protected int customerId;
    protected int branchId;

    protected String accountNumber;
    protected String accountType;  

    protected double balance;
    protected double interestRate;

    protected String chequebookNumber;
    protected String bankCode;

    protected LocalDateTime createdAt;

    protected List<Transaction> transactions;

    public Account(int accountId,
                   int customerId,
                   int branchId,
                   String accountNumber,
                   String accountType,
                   double balance,
                   double interestRate,
                   String chequebookNumber,
                   String bankCode,
                   LocalDateTime createdAt) {

        this.accountId = accountId;
        this.customerId = customerId;
        this.branchId = branchId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.interestRate = interestRate;
        this.chequebookNumber = chequebookNumber;
        this.bankCode = bankCode;
        this.createdAt = createdAt;

        this.transactions = new ArrayList<>();
    }

    // ---------- GETTERS ----------
    public int getAccountId() { return accountId; }
    public int getCustomerId() { return customerId; }
    public int getBranchId() { return branchId; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountType() { return accountType; }
    public double getBalance() { return balance; }
    public double getInterestRate() { return interestRate; }
    public String getChequebookNumber() { return chequebookNumber; }
    public String getBankCode() { return bankCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<Transaction> getTransactions() { return transactions; }

    // ---------- METHODS ----------
    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public void updateBalance(double amount) {
        this.balance += amount;
    }

    public abstract void pay();
    public abstract void receipt();
}
