package bank.Models;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import bank.DB.BankDb;

/**
 * Abstract representation of a bank account.
 * <p>
 * This class stores shared fields and behavior between different types
 * of accounts such as checking and saving accounts. 
 * Concrete subclasses must implement {@link #pay()}, {@link #receipt()},
 * and {@link #canWithdraw(double)} to define account-specific logic.
 * </p>
 */
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

    /**
     * Creates a new Account model instance.
     *
     * @param accountId         unique ID of the account
     * @param customerId        ID of the customer who owns the account
     * @param branchId          ID of the bank branch
     * @param accountNumber     account number as a string
     * @param accountType       type of account (e.g., CHECKING, SAVING)
     * @param balance           current account balance
     * @param interestRate      interest rate associated with the account
     * @param chequebookNumber  chequebook number (if applicable)
     * @param bankCode          bank code for the account
     * @param createdAt         date and time the account was created
     */
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

    /** @return the unique ID of the account */
    public int getAccountId() { return accountId; }

    /** @return the ID of the customer who owns the account */
    public int getCustomerId() { return customerId; }

    /** @return the ID of the bank branch where this account belongs */
    public int getBranchId() { return branchId; }

    /** @return the account number as a string */
    public String getAccountNumber() { return accountNumber; }

    /** @return the account type (e.g., "CHECKING", "SAVING") */
    public String getAccountType() { return accountType; }

    /** @return the current account balance */
    public double getBalance() { return balance; }

    /**
     * Sets the account balance.
     *
     * @param balance the new balance to set
     */
    public void setBalance(double balance) { this.balance = balance; }

    /** @return the interest rate assigned to the account */
    public double getInterestRate() { return interestRate; }

    /**
     * Sets the interest rate for the account.
     *
     * @param interestRate the new interest rate
     */
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    /** @return the chequebook number associated with the account */
    public String getChequebookNumber() { return chequebookNumber; }

    /**
     * Sets the chequebook number for the account.
     *
     * @param chequebookNumber the new chequebook number
     */
    public void setChequebookNumber(String chequebookNumber) { this.chequebookNumber = chequebookNumber; }

    /** @return the bank code for the account */
    public String getBankCode() { return bankCode; }

    /** @return the timestamp when the account was created */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @return the transaction history associated with the account */
    public List<Transaction> getTransactions() { return transactions; }

    /**
     * Adds a transaction to the account's transaction history.
     *
     * @param transaction the transaction to add
     */
    public void addTransaction(Transaction transaction) {
        if (transaction != null) {
            transactions.add(transaction);
        }
    }

    /**
     * Abstract method for payment action.
     * Subclasses must define how the account handles payments.
     */
    public abstract void pay();

    /**
     * Abstract method for generating a receipt.
     * Subclasses must define how account receipts are handled.
     */
    public abstract void receipt();

    /**
     * Determines whether the account allows withdrawal of a given amount.
     *
     * @param amount the amount to check
     * @return true if the withdrawal is permitted, false otherwise
     */
    protected abstract boolean canWithdraw(double amount);

    /**
     * Fetches all accounts belonging to a specific customer from the database.
     *
     * @param customerId the ID of the customer
     * @return a list of {@link Account} objects
     * @throws SQLException if a database error occurs
     */
    public static List<Account> fetchAccountsByCustomer(int customerId) throws SQLException {
        BankDb db = new BankDb();
        List<Map<String, Object>> rows = db.accountListByCustomer(customerId);

        return rows.stream()
                   .map(Account::mapRowToAccount)
                   .filter(Objects::nonNull)
                   .toList();
    }

    /**
     * Maps a database row to the appropriate Account subclass.
     *
     * @param row a map representing a database row
     * @return an Account instance (Checking or Saving), or null if type is unknown
     */
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

    /** Converts an object to an int. */
    private static int toInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    /** Converts an object to a double. */
    private static double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return v == null ? 0.0 : Double.parseDouble(v.toString());
    }

    /** Converts an object to a String. */
    private static String str(Object v) {
        return v == null ? null : v.toString();
    }

    /** Converts an object to a LocalDateTime. */
    private static LocalDateTime toDate(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime dt) return dt;
        if (v instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return LocalDateTime.parse(v.toString());
    }

}
