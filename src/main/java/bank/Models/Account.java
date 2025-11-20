package bank.Models;

import bank.DB.BankDb;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class Account {
    private static final BankDb BANK_DB = new BankDb();

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

    //  ---------- METHODS TellerAccountView ----------

    /**
     * Fetches accounts for the specified customer ID
     * @param customerId the ID of the customer
     * @return a list of accounts belonging to the customer
     * @throws SQLException if a database access error occurs
     */
    public static List<Account> fetchAccountsByCustomer(int customerId) throws SQLException {
        List<Map<String, Object>> rows = BANK_DB.accountListByCustomer(customerId);
        List<Account> accounts = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Account account = mapRowToAccount(row);
            if (account != null) {
                accounts.add(account);
            }
        }
        return accounts;
    }

    /**
     * Maps a database row to an Account object (either SavingAccount or CheckingAccount).
     * @param row the database row as a map
     * @return the mapped Account object
     */
    public static Account mapRowToAccount(Map<String, Object> row) {
        if (row == null) {
            return null;
        }

        int accountId = toInt(row.get("account_id"));
        int customerId = toInt(row.get("customer_id"));
        int branchId = toInt(row.get("branch_id"));
        String accountNumber = Objects.toString(row.get("account_number"), null);
        String accountType = Objects.toString(row.get("account_type"), "");
        double balance = toDouble(row.get("balance"));
        Double interestRate = toNullableDouble(row.get("interest_rate"));
        String chequebookNumber = Objects.toString(row.get("chequebook_number"), null);
        String bankCode = Objects.toString(row.get("bank_code"), null);
        LocalDateTime createdAt = toLocalDateTime(row.get("created_at"));

        // Determine account type and create appropriate Account subclass
        if ("SAVING".equalsIgnoreCase(accountType)) {
            return new SavingAccount(
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
        }

        // Return CheckingAccount by default
        return new CheckingAccount(
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
    }

    /**
     * Converts an object to an integer.
     * @param value the object to convert
     * @return the integer value
     */
    private static int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * Converts an object to a double.
     * @param value the object to convert
     * @return the double value
     */
    private static double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0.0;
        }
        if (value instanceof String str && str.isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(value.toString());
    }

    /**
     * Converts an object to a nullable Double.
     * @param value the object to convert
     * @return the Double value or null if the input is null or blank
     */
    private static Double toNullableDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str && str.isBlank()) {
            return null;
        }
        return Double.parseDouble(value.toString());
    }

    /**
     * Converts an object to a LocalDateTime
     * @param value the object to convert
     * @return the LocalDateTime value
     */
    private static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        return LocalDateTime.parse(value.toString());
    }
}
