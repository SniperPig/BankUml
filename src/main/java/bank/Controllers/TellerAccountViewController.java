package bank.Controllers;

import bank.Models.Account;
import bank.Models.Customer;
import bank.Models.Transaction;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import bank.SessionManager;

import javafx.beans.property.SimpleStringProperty;
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
import javafx.stage.Stage;

public class TellerAccountViewController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private Label accountHeading;

    @FXML
    private TableView<Account> accountsTable;

    @FXML
    private TableColumn<Account, String> accountTypeColumn;

    @FXML
    private TableColumn<Account, String> accountNumberColumn;

    @FXML
    private TableColumn<Account, String> accountBalanceColumn;

    @FXML
    private TableView<Transaction> transactionsTable;

    @FXML
    private TableColumn<Transaction, String> transactionDateColumn;

    @FXML
    private TableColumn<Transaction, String> transactionDescriptionColumn;

    @FXML
    private TableColumn<Transaction, String> transactionAmountColumn;

    @FXML
    private TableColumn<Transaction, String> transactionBalanceColumn;

    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private Customer currentCustomer;

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        accountTypeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAccountType()));
        accountNumberColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAccountNumber()));
        accountBalanceColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatCurrency(cell.getValue().getBalance())));

        transactionDateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatDate(cell.getValue().getCreatedAt())));
        transactionDescriptionColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getTransactionType())));
        transactionAmountColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatCurrency(cell.getValue().getAmount())));
        transactionBalanceColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getStatus())));

        accountsTable.setItems(accounts);
        transactionsTable.setItems(transactions);

        accountsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldAcc, newAcc) -> {
            if (newAcc != null) {
                loadTransactionsForAccount(newAcc);
            } else {
                transactions.clear();
            }
        });
    }

    /**
     * Populates the view with the specified customer's information
     * @param customer the customer whose information is to be displayed
     */
    public void populateWithCustomer(Customer customer) {
        this.currentCustomer = customer;
        // Set heading to customer's first name
        accountHeading.setText(customer.getName().split("\\s+")[0] + "'s Accounts");
        loadAccountsForCustomer(customer.getCustomerID());
    }

    /**
     * Handles the action of going back to the dashboard
     * @param event the action event triggered by the back button
     */
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        switchScene(event, "/bank/Views/TellerDashboard.fxml");
    }

    /**
     * Handles the withdraw action
     * 
     * @param event the action event triggered by the withdraw button
     */
    @FXML
    private void handleWithdraw(ActionEvent event) {
        switchScene(event, "/bank/Views/WithdrawForm.fxml");
    }

    /**
     * Handles the deposit action
     * 
     * @param event the action event triggered by the deposit button
     */
    @FXML
    private void handleDeposit(ActionEvent event) {
        switchScene(event, "/bank/Views/DepositForm.fxml");
    }

    /**
     * Handles the transfer action
     * 
     * @param event the action event triggered by the transfer button
     */
    @FXML
    private void handleTransfer(ActionEvent event) {
        switchScene(event, "/bank/Views/TransferForm.fxml");
    }

    /**
     * Handles the logout action and switches back to the login form
     * @param event the action event triggered by the logout button
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clear();
        switchScene(event, "/bank/Views/LoginForm.fxml");
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

    /**
     * Loads accounts for the specified customer ID
     * @param customerId the ID of the customer
     */
    private void loadAccountsForCustomer(int customerId) {
        try {
            List<Account> fetchedAccounts = Account.fetchAccountsByCustomer(customerId);
            accounts.setAll(fetchedAccounts);
            if (!fetchedAccounts.isEmpty()) {
                Account first = fetchedAccounts.get(0);
                accountsTable.getSelectionModel().select(first);
                loadTransactionsForAccount(first);
            } else {
                transactions.clear();
            }
        } catch (SQLException e) {
            showError("Unable to load accounts.", e);
            accounts.clear();
            transactions.clear();
        }
    }

    /**
     * Loads transactions for the specified account
     * @param account the account for which to load transactions
     */
    private void loadTransactionsForAccount(Account account) {
        try {
            List<Transaction> fetchedTransactions =
                    Transaction.fetchRecentTransactionsByAccount(account.getAccountId(), 15);
            transactions.setAll(fetchedTransactions);
        } catch (SQLException e) {
            showError("Unable to load transactions.", e);
            transactions.clear();
        }
    }

    /**
     * Shows an error alert with the specified message and exception details
     * @param message the error message to display
     * @param e the exception that occurred
     */
    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    /**
     * Formats a double value as currency
     * @param amount the amount to format
     * @return the formatted currency string
     */
    private String formatCurrency(double amount) {
        return String.format("$%,.2f", amount);
    }

    /**
     * Formats a LocalDateTime object as a string
     * @param dateTime the date and time to format
     * @return the formatted date string
     */
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return DATE_FORMATTER.format(dateTime);
    }

    /**
     * Returns a safe string, replacing null with an empty string
     * @param value the string value to check
     * @return a non-null string
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
