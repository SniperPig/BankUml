package bank.Models;

import java.time.LocalDateTime;

public class CheckingAccount extends Account {

    private double overdraftLimit;

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

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void pay() {
        System.out.println("Payment processed from checking account.");
    }

    @Override
    public void receipt() {
        System.out.println("Checking account receipt generated.");
    }

    @Override
    protected boolean canWithdraw(double amount) {
        // withdrawal if balance after withdrawal >= -overdraftLimit
        return (this.balance - amount) >= -overdraftLimit;
    }
}
