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
import bank.DB.BankDb;
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

    @FXML private Label accountHeading;
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
        transactionStatusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getStatus())));
        transactionBalanceColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatCurrency(cell.getValue().getBalanceAfter())));

        accountsTable.setItems(accounts);
        transactionsTable.setItems(transactions);

        accountsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

    @FXML
private void handleWithdraw(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/bank/Views/WithdrawForm.fxml"));
        Parent root = loader.load();

        WithdrawController controller = loader.getController();

        Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            System.out.println("No account selected");
            return;
        }

        controller.setDependencies(selectedAccount, new BankDb());
        controller.setParentPage("teller");

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.sizeToScene();

    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Failed to open WithdrawForm");
    }
}

@FXML
private void handleDeposit(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/bank/Views/DepositForm.fxml"));
        Parent root = loader.load();

        DepositController controller = loader.getController();

        Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            System.out.println("No account selected");
            return;
        }

        controller.setDependencies(selectedAccount, new BankDb());
        controller.setParentPage("teller");

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.sizeToScene();

    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Failed to open DepositForm");
    }
}


@FXML
private void handleTransfer(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/bank/Views/TransferForm.fxml"));
        Parent root = loader.load();

        TransferController controller = loader.getController();

        Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            System.out.println("No account selected");
            return;
        }

        controller.setDependencies(selectedAccount, new BankDb());
        controller.setParentPage("teller");

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.sizeToScene();

    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Failed to open TransferForm");
    }
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

            transactions.setAll(associatedTransactions);
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
