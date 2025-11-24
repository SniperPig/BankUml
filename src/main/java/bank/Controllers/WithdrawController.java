package bank.Controllers;

import bank.Models.Account;
import bank.Models.Transaction;
import bank.DB.BankDb;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class WithdrawController {

    @FXML private TextField amountField;
    @FXML private Button confirmButton;

    private Account account;
    private BankDb db;

    public void setDependencies(Account account, BankDb db) {
        this.account = account;
        this.db = db;
    }

    @FXML
    public void initialize() {
        if (confirmButton != null) {
            confirmButton.setOnAction(this::handleWithdraw);
        }
    }

    private void handleWithdraw(ActionEvent event) {
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid number for withdrawal.");
            return;
        }

        if (amount <= 0) {
            showAlert("Withdrawal amount must be positive.");
            return;
        }

        if (amount > account.getBalance()) {
            showAlert("Insufficient balance for this withdrawal.");
            return;
        }

        try {

            db.transactionWithdraw(account.getAccountId(), amount, "CUSTOMER", account.getCustomerId());

          
            List<Map<String, Object>> lastTx = db.transactionGetRecentByAccount(account.getAccountId(), 1);
            long transactionId = (Long) lastTx.get(0).get("transaction_id");

          
            account.setBalance(account.getBalance() - amount);

     
           Transaction transaction = new Transaction(
                    (int) transactionId,
                    account,                  // pass the Account object itself
                    amount,                   // the transaction amount
                    "WITHDRAWAL",             // transaction type
                    "COMPLETED",              // transaction status
                    "CUSTOMER",               // performedByUserId
                    LocalDateTime.now()       // createdAt
            );
            account.addTransaction(transaction);

            showAlert("Withdrawal successful!");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Withdrawal failed: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
