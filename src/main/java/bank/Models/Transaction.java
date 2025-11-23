package bank.Models;

import bank.DB.BankDb;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Transaction {
    private static final BankDb BANK_DB = new BankDb();
    int transactionID;
    Account account;
    double amount;
    String transactionType; 
    String status; 
    String performedByUserId;
    LocalDateTime createdAt; 

    // This attribute is not added to the DB, it's purely for mathematical computations
    // We will use it in Customer and Teller dashboards when viewing recent transactions
    private transient double balanceAfter;

    /**
     * Constructor for Transaction 
     * @param transactionID the unique identifier for the transaction
     * @param account the account associated with the transaction
     * @param amount the amount involved in the transaction
     * @param transactionType the type of transaction (e.g., deposit, withdrawal)
     * @param status the current status of the transaction
     * @param performedByUserId the ID of the user who performed the transaction
     * @param createdAt the date and time when the transaction was created
     */
    public Transaction(int transactionID, Account account, double amount, String transactionType, String status, 
            String performedByUserId, LocalDateTime createdAt) {
        this.transactionID = transactionID;
        this.account = account;
        this.amount = amount;
        this.transactionType = transactionType;
        this.status = status;
        this.performedByUserId = performedByUserId;
        this.createdAt = createdAt;
    }

    /**
     * Get the transaction ID
     * @return transactionID
     */
    public int getTransactionID() {
        return transactionID;
    }

    /**
     * Set the transaction ID
     * @param transactionID the unique identifier for the transaction
     */
    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    /**
     * Get the account associated with the transaction
     * @return account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Set the account associated with the transaction
     * @param account the account to set
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * Get the amount involved in the transaction
     * @return amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Set the amount involved in the transaction
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Get the transaction type
     * @return transactionType
     */
    public String getTransactionType() {
        return transactionType;
    }

    /**
     * Set the transaction type
     * @param transactionType the type of transaction to set
     */
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * Get the status of the transaction
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the status of the transaction
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get the ID of the user who performed the transaction
     * @return performedByUserId
     */
    public String getPerformedByUserId() {
        return performedByUserId;
    }

    /**
     * Set the ID of the user who performed the transaction
     * @param performedByUserId the user ID to set
     */
    public void setPerformedByUserId(String performedByUserId) {
        this.performedByUserId = performedByUserId;
    }

    /**
     * Get the date and time when the transaction was created
     * @return createdAt
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the date and time when the transaction was created
     * @param createdAt the date and time to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the balance after a transaction is done
     * @return balanceAfter the balance after the transaction is done
     */
    public double getBalanceAfter() {
        return balanceAfter;
    }

    /**
     * Set the balance after a transaction is done
     * @param balanceAfter the balance after a transaction is done
     */
    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    /**
     * Fetch transactions by account ID
     * @param accountId the ID of the account
     * @return a list of transactions for the specified account
     * @throws SQLException if a database access error occurs
     */
    public List<Transaction> fetchTransactionsByAccountId(int accountId) throws SQLException {
        return fetchRecentTransactionsByAccount(accountId, 25);
    }

    /**
     * Fetch recent transactions by account ID with a limit
     * @param accountId the ID of the account
     * @param limit the maximum number of transactions to fetch
     * @return a list of recent transactions for the specified account
     * @throws SQLException if a database access error occurs
     */ 
    public static List<Transaction> fetchRecentTransactionsByAccount(int accountId, int limit) throws SQLException {
        return BANK_DB.transactionGetRecentByAccount(accountId, limit).stream()
                .map(Transaction::mapRowToTransaction)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Creates a Transaction object from a database row 
     * @param row the database row as a map
     * @return the mapped Transaction object
     */
    public static Transaction mapRowToTransaction(Map<String, Object> row) {
        if (row == null) {
            return null;
        }
        int transactionID = toInt(row.get("transaction_id"));
        Account account = Account.mapRowToAccount(row);
        double amount = toDouble(row.get("amount"));
        String transactionType = Objects.toString(row.get("transaction_type"), null);
        String status = Objects.toString(row.get("status"), null);
        String performedByUserId = Objects.toString(row.get("performed_by_user_id"), null);
        LocalDateTime createdAt = toLocalDateTime(row.get("created_at"));

        return new Transaction(transactionID, account, amount, transactionType, status, performedByUserId, createdAt);
    }

    /**
     * Converts an object to an integer
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
     * Converts an object to a double
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
