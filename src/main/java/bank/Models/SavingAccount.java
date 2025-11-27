package bank.Models;

import java.time.LocalDateTime;

/**
 * Represents a saving account, which earns interest and requires maintaining
 * a minimum balance. Withdrawals are only allowed if the remaining balance
 * does not go below the set minimum balance.
 */
public class SavingAccount extends Account {

    private double minimumBalance;

    /**
     * Creates a new SavingAccount object.
     *
     * @param accountId       unique ID of the account
     * @param customerId      ID of the customer who owns the account
     * @param branchId        ID of the bank branch associated with this account
     * @param accountNumber   account number as a string
     * @param balance         current balance of the saving account
     * @param interestRate    interest rate applied to the saving account
     * @param bankCode        bank code for this account
     * @param createdAt       timestamp when the account was created
     * @param minimumBalance  minimum balance required to avoid penalties and allow withdrawals
     */
    public SavingAccount(int accountId,
                         int customerId,
                         int branchId,
                         String accountNumber,
                         double balance,
                         double interestRate,
                         String bankCode,
                         LocalDateTime createdAt,
                         double minimumBalance) {

        super(accountId,
              customerId,
              branchId,
              accountNumber,
              "SAVING",          
              balance,
              interestRate,
              null,  
              bankCode,
              createdAt);

        this.minimumBalance = minimumBalance;
    }

    /**
     * Returns the minimum balance required for this saving account.
     *
     * @return the minimum allowed balance
     */
    public double getMinimumBalance() {
        return minimumBalance;
    }

    /**
     * Sets a new minimum balance requirement for this saving account.
     *
     * @param minimumBalance the new minimum balance
     */
    public void setMinimumBalance(double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    /**
     * Prints a message indicating that savings accounts typically do not
     * support direct payment transactions.
     */
    @Override
    public void pay() {
        System.out.println("Savings accounts usually don't support direct payments.");
    }

    /**
     * Prints a message indicating a savings account receipt was generated.
     */
    @Override
    public void receipt() {
        System.out.println("Savings account receipt generated.");
    }

    /**
     * Determines whether the requested withdrawal can be made without
     * violating the minimum balance requirement.
     *
     * @param amount the amount the user wants to withdraw
     * @return true if balance after withdrawal stays above minimum balance,
     *         false otherwise
     */
    @Override
    protected boolean canWithdraw(double amount) {
        // Allow withdrawal only if balance after withdrawal >= minimum balance
        return (this.balance - amount) >= minimumBalance;
    }
}
