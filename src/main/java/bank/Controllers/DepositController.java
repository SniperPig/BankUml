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

public class DepositController {

    @FXML private TextField amountField;
    @FXML private Button confirmButton;
    @FXML private Hyperlink backLink;
    @FXML private Label notificationLabel;

    private Account account;
    private BankDb db;
    private String parentPage; // "customer" or "teller"

    public void setDependencies(Account account, BankDb db) {
        this.account = account;
        this.db = db;
    }

    public void setParentPage(String parentPage) {
        this.parentPage = parentPage;
    }

    @FXML
    public void initialize() {
        if (notificationLabel != null) notificationLabel.setText("");
        if (confirmButton != null) confirmButton.setOnAction(this::handleDeposit);
        if (backLink != null) backLink.setOnAction(this::handleBack);
    }

    @FXML
    private void handleDeposit(ActionEvent event) {
        double amount;

        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            notificationLabel.setStyle("-fx-text-fill: red;");
            notificationLabel.setText("Please enter a valid number.");
            return;
        }

        if (amount <= 0) {
            notificationLabel.setStyle("-fx-text-fill: red;");
            notificationLabel.setText("Deposit amount must be positive.");
            return;
        }

        try {
            db.transactionDeposit(account.getAccountId(), amount, "CUSTOMER", account.getCustomerId());

            // Get last transaction ID
            List<Map<String, Object>> lastTx = db.transactionGetRecentByAccount(account.getAccountId(), 1);
            long transactionId = (Long) lastTx.get(0).get("transaction_id");

            account.setBalance(account.getBalance() + amount);

            Transaction transaction = new Transaction(
                    (int) transactionId,
                    account,
                    amount,
                    "DEPOSIT",
                    "COMPLETED",
                    "CUSTOMER",
                    LocalDateTime.now()
            );
            account.addTransaction(transaction);

            notificationLabel.setStyle("-fx-text-fill: green;");
            notificationLabel.setText("Deposit successful!");
            amountField.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            notificationLabel.setStyle("-fx-text-fill: red;");
            notificationLabel.setText("Deposit failed: " + e.getMessage());
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
