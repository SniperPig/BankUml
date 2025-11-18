package bank.Models;

import java.time.LocalDateTime;

public class SavingAccount extends Account {

    private double minimumBalance;

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

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    @Override
    public void pay() {
        System.out.println("Savings accounts usually don't support direct payments.");
    }

    @Override
    public void receipt() {
        System.out.println("Savings account receipt generated.");
    }
}
