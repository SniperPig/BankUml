package bank.Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    
    protected List<Transaction> transactions = new ArrayList<>();



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
    }



    public int getAccountId() { return accountId; }
    public int getCustomerId() { return customerId; }
    public int getBranchId() { return branchId; }

    public String getAccountNumber() { return accountNumber; }
    public String getAccountType() { return accountType; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    public String getChequebookNumber() { return chequebookNumber; }
    public void setChequebookNumber(String chequebookNumber) { this.chequebookNumber = chequebookNumber; }

    public String getBankCode() { return bankCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<Transaction> getTransactions() { return transactions; }

    public void addTransaction(Transaction transaction) {
        if (transaction != null) {
            transactions.add(transaction);
        }
    }



    public abstract void pay();
    public abstract void receipt();



    public static Account mapRowToAccount(Map<String, Object> row) {
        if (row == null) return null;

        int accountId = toInt(row.get("account_id"));
        int customerId = toInt(row.get("customer_id"));
        int branchId = toInt(row.get("branch_id"));

        String accountNumber = str(row.get("account_number"));
        String accountType = str(row.get("account_type"));
        String bankCode = str(row.get("bank_code"));
        String chequebookNumber = str(row.get("chequebook_number"));

        double balance = toDouble(row.get("balance"));
        double interestRate = toDouble(row.get("interest_rate"));

        LocalDateTime createdAt = toDate(row.get("created_at"));

    
        if ("CHECKING".equalsIgnoreCase(accountType)) {
            double overdraftLimit = toDouble(row.get("overdraft_limit"));
            return new CheckingAccount(accountId, customerId, branchId, accountNumber,
                    balance, chequebookNumber, bankCode, createdAt, overdraftLimit);
        }

        if ("SAVING".equalsIgnoreCase(accountType)) {
            double minBalance = toDouble(row.get("minimum_balance"));
            return new SavingAccount(accountId, customerId, branchId, accountNumber,
                    balance, interestRate, bankCode, createdAt, minBalance);
        }

        return null;
    }



    private static int toInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    private static double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return v == null ? 0.0 : Double.parseDouble(v.toString());
    }

    private static String str(Object v) {
        return v == null ? null : v.toString();
    }

    private static LocalDateTime toDate(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime dt) return dt;
        if (v instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return LocalDateTime.parse(v.toString());
    }

}
