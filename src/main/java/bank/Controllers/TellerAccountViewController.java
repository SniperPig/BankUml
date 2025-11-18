package bank.Controllers;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class TellerAccountViewController {

    @FXML
    private Label accountHeading;

    @FXML
    private TableView<AccountRow> accountsTable;

    @FXML
    private TableColumn<AccountRow, String> accountTypeColumn;

    @FXML
    private TableColumn<AccountRow, String> accountNumberColumn;

    @FXML
    private TableColumn<AccountRow, String> accountBalanceColumn;

    @FXML
    private TableView<TransactionRow> transactionsTable;

    @FXML
    private TableColumn<TransactionRow, String> transactionDateColumn;

    @FXML
    private TableColumn<TransactionRow, String> transactionDescriptionColumn;

    @FXML
    private TableColumn<TransactionRow, String> transactionAmountColumn;

    @FXML
    private TableColumn<TransactionRow, String> transactionBalanceColumn;

    private final ObservableList<AccountRow> accounts = FXCollections.observableArrayList();
    private final ObservableList<TransactionRow> transactions = FXCollections.observableArrayList();

    // Initialize the Teller Account View 
    @FXML
    private void initialize() {
        accountTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        accountNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        accountBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        accountsTable.setItems(accounts);
        transactionsTable.setItems(transactions);
    }

    // Populate the view with data from database 
    public void populateWithCustomer(TellerDashboardController.CustomerRecord record) {
        accountHeading.setText(record.getName() + "'s Account");

        accounts.setAll(
                new AccountRow("Savings", "132902442", "$10,042"),
                new AccountRow("Checking", "222233334", "$2,950")
        );

        transactions.setAll(
                new TransactionRow("01/04/2025", "Withdrawal from account.", "$100", "$10,042"),
                new TransactionRow("28/03/2025", "Deposit received.", "$500", "$10,142"),
                new TransactionRow("20/03/2025", "Transfer to Checking.", "$300", "$9,642")
        );
    }

    // Handles the back button to return to teller's dashboard
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        switchScene(event, "/bank/Views/TellerDashboard.fxml");
    }

    @FXML
    private void handleWithdraw() {
        showActionAlert("Withdraw");
    }

    @FXML
    private void handleDeposit() {
        showActionAlert("Deposit");
    }

    @FXML
    private void handleTransfer() {
        showActionAlert("Transfer");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

    // Show alert for unimplemented actions for now
    // Later to be replaced with actual implementations
    private void showActionAlert(String action) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(action + " flow not implemented yet.");
        alert.showAndWait();
    }

    // Switches the scene to the specified FXML resource
    private void switchScene(ActionEvent event, String resourcePath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load " + resourcePath, ex);
        }
    }

    // This is only a placeholder class for customer data
    // to be replaced with actual Customer model from database later
    public static class AccountRow {
        private final String type;
        private final String number;
        private final String balance;

        public AccountRow(String type, String number, String balance) {
            this.type = type;
            this.number = number;
            this.balance = balance;
        }

        public String getType() {
            return type;
        }

        public String getNumber() {
            return number;
        }

        public String getBalance() {
            return balance;
        }
    }

     // This is only a placeholder class for customer data
    // to be replaced with actual Customer model from database later
    public static class TransactionRow {
        private final String date;
        private final String description;
        private final String amount;
        private final String balance;

        public TransactionRow(String date, String description, String amount, String balance) {
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.balance = balance;
        }

        public String getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public String getAmount() {
            return amount;
        }

        public String getBalance() {
            return balance;
        }
    }
}
