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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class CustomerDashboardController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML private Label welcomeLabel;
    @FXML private TableView<Account> accountsTable;
    @FXML private TableColumn<Account, String> accountTypeColumn;
    @FXML private TableColumn<Account, String> accountNumberColumn;
    @FXML private TableColumn<Account, String> accountBalanceColumn;
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> transactionDateColumn;
    @FXML private TableColumn<Transaction, String> transactionDescriptionColumn;
    @FXML private TableColumn<Transaction, String> transactionAmountColumn;
    @FXML private TableColumn<Transaction, String> transactionBalanceColumn;

    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    @FXML
    /**
     * This function initializes the CustomerDashboard using the current active Customer.
     * It gives the user a simple greeting.
     */
    private void initialize() {
        Customer customer = SessionManager.getCurrentCustomer();
        welcomeLabel.setText("Welcome, " + customer.getName().split("\\s+")[0]);
    }

    /**
     * This function allows the GUI to switch scenes
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
     * This function is invoked when the customer clicks "Update Password"
     * It navigates the user to the update password process
     * 
     * @param event the event created by clicking "Update Password" hyperlink
     */
    private void handleUpdatePassword(ActionEvent event) {
        switchScene(event, "/bank/Views/UpdatePasswordSecurityQuestion.fxml");
    }

    @FXML
    /** 
     * This function is invoked when the customer presses the "Withdraw" button
     * It navigates the user to the Withdraw page
     * 
     * @param event the event created by clicking "Withdraw" button
     */
    private void handleWithdraw(ActionEvent event) {
        switchScene(event, "/bank/Views/WithdrawForm.fxml");
    }

    @FXML
    /** 
     * This function is invoked when the customer presses the "Deposit" button
     * It navigates the user to the Deposit page
     * 
     * @param event the event created by clicking "Deposit" button
     */
    private void handleDeposit(ActionEvent event) {
        switchScene(event, "/bank/Views/DepositForm.fxml");
    }

    @FXML
    /** 
     * This function is invoked when the customer presses the "Transfer" button
     * It navigates the user to the Transfer page
     * 
     * @param event the event created by clicking "Transfers" button
     */
    private void handleTransfer(ActionEvent event) {
        switchScene(event, "/bank/Views/TransferForm.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {

    }
}







    // @FXML
    // private void initialize() {
    //     configureTables();

    //     accountsTable.setItems(accounts);
    //     transactionsTable.setItems(transactions);

    //     accountsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldAcc, newAcc) -> {
    //         if (newAcc != null) {
    //             loadTransactionsForAccount(newAcc);
    //         } else {
    //             transactions.clear();
    //         }
    //     });

    //     currentCustomer = SessionManager.getCurrentCustomer();
    //     if (currentCustomer != null) {
    //         setWelcomeMessage(currentCustomer);
    //         loadAccountsForCustomer(currentCustomer.getCustomerID());
    //     } else {
    //         welcomeLabel.setText("Welcome");
    //     }
    // }

    // private void configureTables() {
    //     accountTypeColumn.setCellValueFactory(cell ->
    //             new SimpleStringProperty(safeString(cell.getValue().getAccountType())));
    //     accountNumberColumn.setCellValueFactory(cell ->
    //             new SimpleStringProperty(safeString(cell.getValue().getAccountNumber())));
    //     accountBalanceColumn.setCellValueFactory(cell ->
    //             new SimpleStringProperty(formatCurrency(cell.getValue().getBalance())));

    //     transactionDateColumn.setCellValueFactory(cell ->
    //             new SimpleStringProperty(formatDate(cell.getValue().getCreatedAt())));
    //     transactionDescriptionColumn.setCellValueFactory(cell ->
    //             new SimpleStringProperty(safeString(cell.getValue().getTransactionType())));
    //     transactionAmountColumn.setCellValueFactory(cell ->
    //             new SimpleStringProperty(formatCurrency(cell.getValue().getAmount())));
    //     transactionBalanceColumn.setCellValueFactory(cell -> {
    //         Account account = cell.getValue().getAccount();
    //         if (account == null) {
    //             return new SimpleStringProperty("");
    //         }
    //         return new SimpleStringProperty(formatCurrency(account.getBalance()));
    //     });

    //     accountsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    //     transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    // }

    // private void setWelcomeMessage(Customer customer) {
    //     String name = safeString(customer.getName());
    //     String firstName = name.isBlank() ? "Customer" : name.split("\\s+")[0];
    //     welcomeLabel.setText("Welcome, " + firstName);
    // }

    // private void loadAccountsForCustomer(int customerId) {
    //     try {
    //         List<Account> fetchedAccounts = Account.fetchAccountsByCustomer(customerId);
    //         accounts.setAll(fetchedAccounts);
    //         if (!fetchedAccounts.isEmpty()) {
    //             Account first = fetchedAccounts.get(0);
    //             accountsTable.getSelectionModel().select(first);
    //             loadTransactionsForAccount(first);
    //         } else {
    //             transactions.clear();
    //         }
    //     } catch (SQLException e) {
    //         showError("Unable to load accounts.", e);
    //         accounts.clear();
    //         transactions.clear();
    //     }
    // }

    // private void loadTransactionsForAccount(Account account) {
    //     try {
    //         List<Transaction> fetchedTransactions =
    //                 Transaction.fetchRecentTransactionsByAccount(account.getAccountId(), 15);
    //         transactions.setAll(fetchedTransactions);
    //     } catch (SQLException e) {
    //         showError("Unable to load transactions.", e);
    //         transactions.clear();
    //     }
    // }

    // @FXML
    // private void handleWithdraw() {
    //     showActionAlert("Withdraw");
    // }

    // @FXML
    // private void handleDeposit() {
    //     showActionAlert("Deposit");
    // }

    // @FXML
    // private void handleTransfer() {
    //     showActionAlert("Transfer");
    // }

    // @FXML
    // private void handleUpdatePassword(ActionEvent event) {
    //     switchScene(event, "/bank/Views/UpdatePasswordSecurityQuestion.fxml");
    // }

    // @FXML
    // private void handleLogout(ActionEvent event) {
    //     SessionManager.clear();
    //     switchScene(event, "/bank/Views/LoginForm.fxml");
    // }

    // private void showActionAlert(String action) {
    //     Alert alert = new Alert(Alert.AlertType.INFORMATION);
    //     alert.setHeaderText(null);
    //     String target = currentCustomer == null ? "your account" : currentCustomer.getName();
    //     alert.setContentText(action + " flow not implemented yet for " + target + ".");
    //     alert.showAndWait();
    // }

    // private void switchScene(ActionEvent event, String resourcePath) {
    //     try {
    //         Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
    //         Scene scene = ((Node) event.getSource()).getScene();
    //         Stage stage = (Stage) scene.getWindow();
    //         scene.setRoot(root);
    //         stage.sizeToScene();
    //     } catch (IOException ex) {
    //         throw new RuntimeException("Unable to load " + resourcePath, ex);
    //     }
    // }

    // private String formatCurrency(double amount) {
    //     return String.format("$%,.2f", amount);
    // }

    // private String formatDate(LocalDateTime dateTime) {
    //     if (dateTime == null) {
    //         return "";
    //     }
    //     return DATE_FORMATTER.format(dateTime);
    // }

    // private String safeString(String value) {
    //     return value == null ? "" : value;
    // }

    // private void showError(String message, Exception e) {
    //     Alert alert = new Alert(Alert.AlertType.ERROR);
    //     alert.setHeaderText(message);
    //     alert.setContentText(e.getMessage());
    //     alert.showAndWait();
    // }