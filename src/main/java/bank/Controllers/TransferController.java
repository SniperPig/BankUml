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

public class TransferController {

    @FXML private TextField toAccountField;
    @FXML private TextField amountField;
    @FXML private Button confirmButton;

    private Account fromAccount;
    private BankDb db;

    public void setDependencies(Account fromAccount, BankDb db) {
        this.fromAccount = fromAccount;
        this.db = db;
    }

    @FXML
    public void initialize() {
        if (confirmButton != null) {
            confirmButton.setOnAction(this::handleTransfer);
        }
    }

    private void handleTransfer(ActionEvent event) {
        double amount;
        int toAccountId;

        try {
            amount = Double.parseDouble(amountField.getText());
            toAccountId = Integer.parseInt(toAccountField.getText());
        } catch (NumberFormatException e) {
            showAlert("Please enter valid numbers for account and amount.");
            return;
        }

        if (amount <= 0) {
            showAlert("Transfer amount must be positive.");
            return;
        }

        if (amount > fromAccount.getBalance()) {
            showAlert("Insufficient balance for this transfer.");
            return;
        }

        try {
        
            db.transactionTransfer(fromAccount.getAccountId(), toAccountId, amount, "CUSTOMER", fromAccount.getCustomerId());

            List<Map<String, Object>> lastTx = db.transactionGetRecentByAccount(fromAccount.getAccountId(), 1);
            long transactionId = (Long) lastTx.get(0).get("transaction_id");

            
            fromAccount.setBalance(fromAccount.getBalance() - amount);

          
            Transaction transaction = new Transaction(
                    (int) transactionId,
                    fromAccount.getAccountId(),
                    LocalDateTime.now(),
                    "TRANSFER",
                    amount,
                    "CUSTOMER",
                    fromAccount.getCustomerId()
            );
            fromAccount.addTransaction(transaction);

            showAlert("Transfer successful!");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Transfer failed: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
