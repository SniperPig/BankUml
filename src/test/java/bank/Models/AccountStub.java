package bank.Models;
import java.time.LocalDateTime;

/**
 * This is a stub for our Account model. Note that it must extend Account in order for the
 * Transaction constructor to accept the object.
 */
public class AccountStub extends Account {
    /**
     * This is the constructor that takes from the parent (Account)
     */
    public AccountStub(int accountId, int customerId, int branchId, String accountNumber, 
            double balance) {
        super(accountId,
              customerId,
              branchId,
              accountNumber,
              "CHECKING",   // we force a type
              balance,
              0.0,          // set the interest rate to 0 
              "CHK-STUB",   // we force a chequebookNumber
              "BANK-STUB",  // and force a bankCode
              LocalDateTime.now());
    }

    /**
     * This is to get the account's balance
     * @return balance the account's balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * These 3 functions are kept as empty, as we do not need them for the stub.
     */
    @Override public void pay() { /* keep it empty because we won't need it */ }
    @Override public void receipt() { /* keep it empty because we won't need it */ }
    @Override protected boolean canWithdraw(double amount) { return (this.balance - amount) >= 0; }
}
