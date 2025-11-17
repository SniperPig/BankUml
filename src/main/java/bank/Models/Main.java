package bank.Models;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

import bank.DB.BankDb;
import bank.DB.DbManager;
public class Main {
    public static void main(String[] args) {
        // New customer
        Customer customer = new Customer("Shayan Aminaei");
        customer.printCustomerInfo();
        System.out.println();

        // Making different accounts
        Card card = new Card(customer);
        Check check = new Check(customer);
        Saving saving = new Saving(customer);

        // Transations for each account
        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();
        Transaction t3 = new Transaction();

        card.addTransaction(t1);
        check.addTransaction(t2);
        saving.addTransaction(t3);

        // Transactions
        card.pay();
        card.receipt();
        System.out.println();

        check.pay();
        check.receipt();
        System.out.println();

        saving.pay();
        saving.receipt();
        System.out.println();

        // Bank and branches Test
        Bank bank = new Bank("National Bank");
        Branch branch1 = new Branch("Branch no1 ", bank);
        Branch branch2 = new Branch("Branch no2 ", bank);

        bank.printBankInfo();
        System.out.println();

        // Transaction's test
        System.out.println("Card   transactions count:   " + card.getTransactions().size());
        System.out.println("Check  transactions count:   " + check.getTransactions().size());
        System.out.println("Saving transactions count:   " + saving.getTransactions().size());

        // 1) Test raw connection
        try (Connection conn = DbManager.getConnection()) {
            System.out.println("Connection OK: " + conn.getMetaData().getURL());
        } catch (SQLException e) {
            System.err.println("Connection FAILED");
            e.printStackTrace();
            return; // no point continuing if we can't connect
        }

        BankDb db = new BankDb();

        // 2) Test a simple report: branch summary for branch 1
        try {
            Timestamp from = Timestamp.valueOf(LocalDateTime.now().minusDays(7));
            Timestamp to   = Timestamp.valueOf(LocalDateTime.now());

            List<Map<String, Object>> summary =
                    db.reportBranchSummary(1, from, to);

            System.out.println("\n--- Branch 1 Summary (last 7 days) ---");
            if (summary.isEmpty()) {
                System.out.println("No summary rows returned.");
            } else {
                printRows(summary);
            }
        } catch (SQLException e) {
            System.err.println("Error calling sp_report_branch_summary:");
            e.printStackTrace();
        }

        // 3) Test customer login (using your seeded Test User)
        try {
            String email = "test@mail.com";
            String passwordHash = "test123"; // for now you stored plain text

            List<Map<String, Object>> customerList =
                    db.customerLogin(email, passwordHash);

            System.out.println("\n--- Customer Login Test ---");
            if (customerList.isEmpty()) {
                System.out.println("Login returned no rows (unexpected).");
            } else {
                printRows(customerList);
            }
        } catch (SQLException e) {
            System.err.println("Error calling sp_customer_login:");
            e.printStackTrace();
        }

        System.out.println("\nSmoke test finished.");
    }

    private static void printRows(List<Map<String, Object>> rows) {
        int i = 1;
        for (Map<String, Object> row : rows) {
            System.out.println("Row " + i++ + ":");
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                System.out.println("  " + entry.getKey() + " = " + entry.getValue());
            }
        }
    }
}
