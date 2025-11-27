package bank.Models;

import java.time.LocalDateTime;

/**
 * Represents a checking account, which allows payments and supports
 * overdraft up to a specified limit. Checking accounts typically do not
 * earn interest.
 */
public class CheckingAccount extends Account {

    private double overdraftLimit;

    /**
     * Creates a new CheckingAccount object.
     *
     * @param accountId        unique ID of the account
     * @param customerId       ID of the customer who owns the account
     * @param branchId         ID of the associated bank branch
     * @param accountNumber    account number as a string
     * @param balance          current balance of the checking account
     * @param chequebookNumber chequebook number linked to this account
     * @param bankCode         the bank identification code
     * @param createdAt        timestamp of account creation
     * @param overdraftLimit   maximum overdraft allowed (positive value)
     */
    public CheckingAccount(int accountId,
                           int customerId,
                           int branchId,
                           String accountNumber,
                           double balance,
                           String chequebookNumber,
                           String bankCode,
                           LocalDateTime createdAt,
                           double overdraftLimit) {

        super(accountId,
              customerId,
              branchId,
              accountNumber,
              "CHECKING",       
              balance,
              0.0,  
              chequebookNumber,
              bankCode,
              createdAt);

        this.overdraftLimit = overdraftLimit;
    }

    /**
     * Returns the overdraft limit for this checking account.
     *
     * @return the maximum overdraft amount allowed
     */
    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    /**
     * Sets the overdraft limit for this checking account.
     *
     * @param overdraftLimit the new overdraft limit
     */
    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    /**
     * Prints a message confirming a payment transaction from the checking account.
     */
    @Override
    public void pay() {
        System.out.println("Payment processed from checking account.");
    }

    /**
     * Prints a message confirming that a receipt was generated.
     */
    @Override
    public void receipt() {
        System.out.println("Checking account receipt generated.");
    }

    /**
     * Determines whether a withdrawal is allowed based on the current balance
     * and overdraft limit.
     *
     * The withdrawal is permitted if:
     * <pre>
     *     balance - amount >= -overdraftLimit
     * </pre>
     *
     * @param amount the withdrawal amount requested
     * @return true if the account remains within overdraft limits, false otherwise
     */
    @Override
    protected boolean canWithdraw(double amount) {
        return (this.balance - amount) >= -overdraftLimit;
    }
}
