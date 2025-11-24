package bank.Controllers;

import bank.Models.Account;
import bank.Models.Transaction;
import bank.DB.BankDb;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TransferController {

    @FXML private TextField amountField;
    @FXML private TextField destinationAccountField;
    @FXML private Button confirmButton;
    @FXML private Hyperlink backLink;
    @FXML private Label notificationLabel;

    private Account fromAccount;
    private BankDb db;
    private String parentPage; // "customer" or "teller"

    public void setDependencies(Account fromAccount, BankDb db) {
        this.fromAccount = fromAccount;
        this.db = db;
    }

    public void setParentPage(String parentPage) {
        this.parentPage = parentPage;
    }

    @FXML
    public void initialize() {
        if (notificationLabel != null) notificationLabel.setText("");
        if (confirmButton != null) confirmButton.setOnAction(this::handleTransfer);
        if (backLink != null) backLink.setOnAction(this::handleBack);
    }

    @FXML
    private void handleTransfer(ActionEvent event) {
        double amount;
        int toAccountId;

        try {
            amount = Double.parseDouble(amountField.getText());
            toAccountId = Integer.parseInt(destinationAccountField.getText());
        } catch (NumberFormatException e) {
            notificationLabel.setStyle("-fx-text-fill: red;");
            notificationLabel.setText("Please enter valid numbers for account and amount.");
            return;
        }

        if (amount <= 0) {
            notificationLabel.setStyle("-fx-text-fill: red;");
            notificationLabel.setText("Transfer amount must be positive.");
            return;
        }

        if (amount > fromAccount.getBalance()) {
            notificationLabel.setStyle("-fx-text-fill: red;");
            notificationLabel.setText("Insufficient balance for this transfer.");
            return;
        }

        try {
            db.transactionTransfer(fromAccount.getAccountId(), toAccountId, amount, "CUSTOMER", fromAccount.getCustomerId());

            // Update local account
            fromAccount.setBalance(fromAccount.getBalance() - amount);

            List<Map<String, Object>> lastTx = db.transactionGetRecentByAccount(fromAccount.getAccountId(), 1);
            long transactionId = (Long) lastTx.get(0).get("transaction_id");

            Transaction transaction = new Transaction(
                    (int) transactionId,
                    fromAccount,
                    amount,
                    "TRANSFER",
                    "COMPLETED",
                    "CUSTOMER",
                    LocalDateTime.now()
            );
            fromAccount.addTransaction(transaction);

            notificationLabel.setStyle("-fx-text-fill: green;");
            notificationLabel.setText("Transfer successful!");
            amountField.clear();
            destinationAccountField.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            notificationLabel.setStyle("-fx-text-fill: red;");
            notificationLabel.setText("Transfer failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            String path = "customer".equals(parentPage) ?
                    "/bank/Views/CustomerDashboard.fxml" :
                    "/bank/Views/TellerDashboard.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();

        } catch (Exception e) {
            e.printStackTrace();
            notificationLabel.setStyle("-fx-text-fill: red;");
            notificationLabel.setText("Failed to navigate back.");
        }
    }
}
