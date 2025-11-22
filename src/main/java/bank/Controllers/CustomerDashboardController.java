package bank.Controllers;

import bank.Models.Account;
import bank.Models.Customer;
import bank.Models.Transaction;
import bank.SessionManager;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class CustomerDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TableView<Account> accountsTable;
    @FXML private TableColumn<Account, String> accountTypeColumn;
    @FXML private TableColumn<Account, String> accountNumberColumn;
    @FXML private TableColumn<Account, String> accountBalanceColumn;
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> transactionDateColumn;
    @FXML private TableColumn<Transaction, String> transactionDescriptionColumn;
    @FXML private TableColumn<Transaction, String> transactionAmountColumn;
    @FXML private TableColumn<Transaction, String> transactionStatusColumn;
    @FXML private TableColumn<Transaction, String> transactionBalanceColumn;

    // Tables in FXML require an observable list, so we'll transfer the data to these ObservableLists
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    @FXML
    /**
     * This function initializes the CustomerDashboard using the current active Customer.
     * It gives the user a simple greeting, and configures the two tables for the user.
     */
    private void initialize() {
        Customer customer = SessionManager.getCurrentCustomer();
        welcomeLabel.setText("Welcome, " + customer.getName().split("\\s+")[0]);

        // configure the tables
        configureTables();

        // fill the tables from JavaFX
        loadAccountsForCustomer();
        accountsTable.setItems(accounts);
        transactionsTable.setItems(transactions);

        // and lastly, update the transactions table if the customer selects a different account
        accountsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldAcc, newAcc) -> {
            if (newAcc != null) {
                loadTransactionsForAccount(newAcc);
            } else {
                // If the row is empty, there should be no tarnsactions visible
                transactions.clear();
            }
        });
    }

    /**
     * This function allows the GUI to switch scenes.
     * 
     * @param event the event that is created by clicking a button
     * @param resourcePath the path of the new scene we want to reach
     */
    private void switchScene(ActionEvent event, String resourcePath) {
        try {
            // Load the new screen we want to go to 
            Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
            // Get the current scene & stage (window)
            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load " + resourcePath, ex);
        }
    }

    @FXML
    /**
     * This function is invoked when the customer clicks "Update Password".
     * It navigates the user to the update password process.
     * 
     * @param event the event created by clicking "Update Password" hyperlink
     */
    private void handleUpdatePassword(ActionEvent event) {
        switchScene(event, "/bank/Views/UpdatePasswordSecurityQuestion.fxml");
    }

    @FXML
    /** 
     * This function is invoked when the customer presses the "Withdraw" button.
     * It navigates the user to the Withdraw page.
     * 
     * @param event the event created by clicking "Withdraw" button
     */
    private void handleWithdraw(ActionEvent event) {
        switchScene(event, "/bank/Views/WithdrawForm.fxml");
    }

    @FXML
    /** 
     * This function is invoked when the customer presses the "Deposit" button.
     * It navigates the user to the Deposit page.
     * 
     * @param event the event created by clicking "Deposit" button
     */
    private void handleDeposit(ActionEvent event) {
        switchScene(event, "/bank/Views/DepositForm.fxml");
    }

    @FXML
    /** 
     * This function is invoked when the customer presses the "Transfer" button.
     * It navigates the user to the Transfer page.
     * 
     * @param event the event created by clicking "Transfer" button
     */
    private void handleTransfer(ActionEvent event) {
        switchScene(event, "/bank/Views/TransferForm.fxml");
    }

    @FXML
    /** 
     * This function is invoked when the customer presses the "Logout" hyperlink.
     * It navigates the user back to the Login page.
     * 
     * @param event the event created by clicking "Logout" hyperlink
     */
    private void handleLogout(ActionEvent event) {
        SessionManager.clear();
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

    /*
     * This function retrieves the accounts associated with the logged in user in order
     * to display them.
     */
    private void loadAccountsForCustomer() {
        // This will be used for error messages
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);

        try {
            // First retrieve the customerID 
            Customer customer = SessionManager.getCurrentCustomer();
            int customerID = customer.getCustomerID();

            // Then fetch the associated accounts with that customer
            List<Account> associatedAccounts = Account.fetchAccountsByCustomer(customerID);
            // And transfer them to the ObservableList
            accounts.setAll(associatedAccounts);

            if (!associatedAccounts.isEmpty()) {
                // Retrieve the first account in the list to set it as default in the selection
                Account first = associatedAccounts.get(0);
                accountsTable.getSelectionModel().select(first);

                // And lastly, load the transactions for that selected account
                loadTransactionsForAccount(first);
            } else {
                // If no account selected
                transactions.clear();
            }
        } catch (SQLException e) {  
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText("Database error: " + e.getMessage());
            alert.showAndWait();
            accounts.clear();
            transactions.clear();
        }
    }

    /**
     * This function retrieves the transactions associated with the selected account of the 
     * logged in user in order to display them.
     * It performs math to determine what the balance was after each transaction.
     * 
     * @param account the selected account of the logged in user
     */
    private void loadTransactionsForAccount(Account account) {
        // This will be used for error messages
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);

        try {
            // We retrieve the first 15 transactions associated with the selected account
            List<Transaction> associatedTransactions =
                    Transaction.fetchRecentTransactionsByAccount(account.getAccountId(), 15);

            // We need to store the balance after each transaction was done
            // We begin with the current balance, because that's the balance after the most recent transaction
            double balanceAtTheTime = account.getBalance();

            for (Transaction transaction : associatedTransactions) {
                transaction.setBalanceAfter(balanceAtTheTime);

                // If the transaction had failed, skip this iteration
                if ("FAILED".equalsIgnoreCase(transaction.getStatus())) {
                    continue;
                }

                // the amount used in the transaction
                double amountOfTrans = transaction.getAmount();
                String typeOfTrans = transaction.getTransactionType();

                // Depending on the transaction type, do different operations
                switch (typeOfTrans) {
                    case "DEPOSIT":
                        // For the next most recent transaction, remove the amount that we deposited
                        balanceAtTheTime -= amountOfTrans;   
                        break;

                    case "WITHDRAWAL":
                        // For the next most recent transaction, add the amount that we withdrew
                        balanceAtTheTime += amountOfTrans;
                        break;

                    case "TRANSFER":
                        // This is similar to a withdrawal; we removed money from account
                        // So for next most recent transaction, add the money back
                        balanceAtTheTime += amountOfTrans;   
                        break;

                    default:
                        break;
                }
            }

            // And then transfer the data to the ObservableList "transactions"
            transactions.setAll(associatedTransactions);
        } catch (SQLException e) {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText("Database error: " + e.getMessage());
            alert.showAndWait();
            transactions.clear();
        }
    }

    /**
     * This function will be used in the table configuration to ensure that if any fields are null, they
     * show up as an empty string
     * 
     * @param value the value we are checking (to see if it's null)
     * @return value a string that is either empty, or has a value
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }

    /**
     * This function will be used in the table configuration to format
     * currency.
     * 
     * @param amount the amount to be formatted
     * @return amount the amount formatted for currency
     */
    private String formatCurrency(double amount) {
        return String.format("$%,.2f", amount);
    }

    /**
     * This function will be used in the table configuration to format
     * dates.
     * 
     * @param dateTime the date to be formatted
     * @return dateTime the datetime formatted as a string
     */
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(dateTime);
    }

    /**
     * This function is used to actually set up the tables and fill them with the relevant information.
     * We have two tables: one for Accounts, and one for Transactions of the selected account.
     */
    private void configureTables() {
        // Note that for each column, the process is similar
        //      1) convert the value to SimpleStringProperty (because that's what JavaFX requires)
        //      2) when applicable, applies either the safeString, formatCurrency, or formatDate function 
        //          on the value 

        // First taking care of the account table 
        accountTypeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getAccountType())));
        accountNumberColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getAccountNumber())));
        accountBalanceColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatCurrency(cell.getValue().getBalance())));

        // And now repeating the process for the transaction table
        transactionDateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatDate(cell.getValue().getCreatedAt())));
        transactionDescriptionColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getTransactionType())));
        transactionAmountColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatCurrency(cell.getValue().getAmount())));
        transactionStatusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getStatus())));
        transactionBalanceColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatCurrency(cell.getValue().getBalanceAfter())));

        // Since there aren't that many fields, we can force the horizontal width to remain the same
        // But it's still scrollable vertically
        accountsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}