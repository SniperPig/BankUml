package bank.Controllers;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import bank.DB.BankDb;
import bank.Models.Account;
import bank.Models.Transaction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller responsible for handling money transfers between two accounts.
 * Works for both customer and teller views.
 */
public class TransferController {

    @FXML private TextField amountField;
    @FXML private TextField destinationAccountField;
    @FXML private Button confirmButton;
    @FXML private Hyperlink backLink;
    @FXML private Label notificationLabel;

    private Account fromAccount;
    private BankDb db;
    private String parentPage; // "customer" or "teller"

    /**
     * Injects required dependencies.
     *
     * @param fromAccount the account transferring funds
     * @param db the database access object
     */
    public void setDependencies(Account fromAccount, BankDb db) {
        this.fromAccount = fromAccount;
        this.db = db;
    }

    /**
     * Sets the page to return to after completing the transfer.
     *
     * @param parentPage either "customer" or "teller"
     */
    public void setParentPage(String parentPage) {
        this.parentPage = parentPage;
    }

    /**
     * Initializes UI components and event handlers.
     */
    @FXML
    public void initialize() {
        if (notificationLabel != null) notificationLabel.setText("");
        if (confirmButton != null) confirmButton.setOnAction(this::handleTransfer);
        if (backLink != null) backLink.setOnAction(this::handleBack);
    }

    /**
     * Handles the logic for transferring funds from one account to another.
     *
     * @param event the event triggered when the user clicks the confirm button
     */
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
            // 1) Perform transfer in DB
            db.transactionTransfer(fromAccount.getAccountId(), toAccountId, amount,
                    "CUSTOMER", fromAccount.getCustomerId());

            // 2) Update sender balance locally
            fromAccount.setBalance(fromAccount.getBalance() - amount);

            // 3) Fetch receiver info
            List<Map<String, Object>> receiverData = db.accountGetById(toAccountId);
            if (!receiverData.isEmpty()) {
                Map<String, Object> receiverMap = receiverData.get(0);
                double receiverBalance =
                        ((java.math.BigDecimal) receiverMap.get("balance")).doubleValue();
                System.out.println("Receiver new balance: " + receiverBalance);
            }

            // 4) Add sender transaction locally
            List<Map<String, Object>> lastTx =
                    db.transactionGetRecentByAccount(fromAccount.getAccountId(), 1);

            long transactionId = (Long) lastTx.get(0).get("transaction_id");

            Transaction transaction = new Transaction(
                    (int) transactionId,
                    fromAccount,
                    amount,
                    "TRANSFER",
                    "COMPLETED",
                    "CUSTOMER",
                    java.time.LocalDateTime.now()
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

    /**
     * Navigates back to the appropriate dashboard (customer or teller).
     *
     * @param event the event triggered when clicking the back link
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            String path = "customer".equals(parentPage)
                    ? "/bank/Views/CustomerDashboard.fxml"
                    : "/bank/Views/TellerDashboard.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();

        } catch (Exception e) {
            e.printStackTrace();
            notificationLabel.setStyle("-fx-text-fill: red;");
            notificationLabel.setText("Failed to navigate back.");
        }
    }
}
