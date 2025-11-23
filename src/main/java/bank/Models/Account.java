package bank.Models;

import bank.DB.BankDb;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Account {
    private static final BankDb BANK_DB = new BankDb();

    protected final int accountId;
    protected final int customerId;
    protected final int branchId;

    protected final String accountNumber;
    protected final String accountType;

    protected double balance;
    protected final double interestRate;

    protected final String chequebookNumber;
    protected final String bankCode;

    protected final LocalDateTime createdAt;

    protected final List<Transaction> transactions;

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

        this.transactions = new CopyOnWriteArrayList<>();
    }


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
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }

    public void deposit(double amount, String performedByUserId) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        this.balance += amount;

        Transaction t = new Transaction(
                0, this, amount, "DEPOSIT", "SUCCESS", performedByUserId, LocalDateTime.now()
        );
        this.transactions.add(t);
    }

    public void withdraw(double amount, String performedByUserId) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");

        if (!canWithdraw(amount)) {
            Transaction t = new Transaction(
                    0, this, -amount, "WITHDRAWAL", "FAILED", performedByUserId, LocalDateTime.now()
            );
            this.transactions.add(t);
            throw new IllegalArgumentException("Insufficient funds or account constraints.");
        }

        this.balance -= amount;
        Transaction t = new Transaction(
                0, this, -amount, "WITHDRAWAL", "SUCCESS", performedByUserId, LocalDateTime.now()
        );
        this.transactions.add(t);
    }

    public void transfer(Account destination, double amount, String performedByUserId) {
        if (destination == null) throw new IllegalArgumentException("Destination account cannot be null.");
        if (amount <= 0) throw new IllegalArgumentException("Transfer amount must be positive.");

        try {
            this.withdraw(amount, performedByUserId); // may throw if insufficient funds
            destination.deposit(amount, performedByUserId);

            Transaction t = new Transaction(
                    0, this, -amount, "TRANSFER", "SUCCESS", performedByUserId, LocalDateTime.now()
            );
            this.transactions.add(t);
        } catch (IllegalArgumentException e) {
            Transaction t = new Transaction(
                    0, this, -amount, "TRANSFER", "FAILED", performedByUserId, LocalDateTime.now()
            );
            this.transactions.add(t);
            throw e;
        }
    }


    protected abstract boolean canWithdraw(double amount);

    public abstract void pay();
    public abstract void receipt();

    public static List<Account> fetchAccountsByCustomer(int customerId) throws SQLException {
        List<Map<String, Object>> rows = BANK_DB.accountListByCustomer(customerId);
        List<Account> accounts = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Account account = mapRowToAccount(row);
            if (account != null) accounts.add(account);
        }
        return accounts;
    }

    public static Account mapRowToAccount(Map<String, Object> row) {
        if (row == null) return null;

        int accountId = toInt(row.get("account_id"));
        int customerId = toInt(row.get("customer_id"));
        int branchId = toInt(row.get("branch_id"));
        String accountNumber = Objects.toString(row.get("account_number"), null);
        String accountType = Objects.toString(row.get("account_type"), "").toUpperCase(Locale.ROOT);
        double balance = toDouble(row.get("balance"));
        Double interestRate = toNullableDouble(row.get("interest_rate"));
        String chequebookNumber = Objects.toString(row.get("chequebook_number"), null);
        String bankCode = Objects.toString(row.get("bank_code"), null);
        LocalDateTime createdAt = toLocalDateTime(row.get("created_at"));

        return switch (accountType) {
            case "SAVING" -> new SavingAccount(
                    accountId,
                    customerId,
                    branchId,
                    accountNumber,
                    balance,
                    interestRate != null ? interestRate : 0.0,
                    bankCode,
                    createdAt,
                    0.0
            );
            case "CHECKING" -> new CheckingAccount(
                    accountId,
                    customerId,
                    branchId,
                    accountNumber,
                    balance,
                    chequebookNumber,
                    bankCode,
                    createdAt,
                    0.0
            );
            default -> throw new IllegalArgumentException("Unknown account type: " + accountType);
        };
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) return number.intValue();
        if (value == null) return 0;
        return Integer.parseInt(value.toString());
    }

    private static double toDouble(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        if (value == null || (value instanceof String str && str.isEmpty())) return 0.0;
        return Double.parseDouble(value.toString());
    }

    private static Double toNullableDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.doubleValue();
        if (value instanceof String str && str.isBlank()) return null;
        return Double.parseDouble(value.toString());
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime localDateTime) return localDateTime;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return LocalDateTime.parse(value.toString());
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", customerId=" + customerId +
                ", branchId=" + branchId +
                ", accountNumber='" + accountNumber + '\'' +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                ", interestRate=" + interestRate +
                ", chequebookNumber='" + chequebookNumber + '\'' +
                ", bankCode='" + bankCode + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return accountId == account.accountId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }
}
