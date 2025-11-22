package bank.Controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import bank.Models.Employee;
import bank.Models.Customer;
import java.util.List;
import bank.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class TellerDashboardController {

    private static final String[] CATEGORIES = {
            "ID", "Name", "Email", "Phone Number", "Date of Birth", "Government ID", "Address"
    };

    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchCategoryBox;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> idColumn;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> dobColumn;
    @FXML private TableColumn<Customer, String> govIdColumn;
    @FXML private TableColumn<Customer, String> addressColumn;

    private final ObservableList<Customer> masterData = FXCollections.observableArrayList();
    private final FilteredList<Customer> filteredData = new FilteredList<>(masterData, record -> true);

    
    /**
     * Initialize the Teller Dashboard
     * When the dashboard is loaded, this method sets up the welcome message,
     * search functionality, and populates the customer table with data.
     * @throws SQLException
     */
    @FXML
    private void initialize() throws SQLException{
        // This part will be changed to actual logged in employee later
        Employee employee = SessionManager.getCurrentEmployee(); 
        // Set welcome message with employee's first name
        welcomeLabel.setText("Welcome, " + employee.getName().split("\\s+")[0]);

        searchCategoryBox.setItems(FXCollections.observableArrayList(Arrays.asList(CATEGORIES)));
        searchCategoryBox.getSelectionModel().select("Name");

        idColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getCustomerID())));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(safeString(cell.getValue().getName())));
        emailColumn.setCellValueFactory(cell -> new SimpleStringProperty(safeString(cell.getValue().getEmail())));
        phoneColumn.setCellValueFactory(cell -> new SimpleStringProperty(safeString(cell.getValue().getPhoneNumber())));
        dobColumn.setCellValueFactory(cell -> {
            if (cell.getValue().getDOB() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(cell.getValue().getDOB().toString());
        });
        govIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(safeString(cell.getValue().getGovtID())));
        addressColumn.setCellValueFactory(cell -> new SimpleStringProperty(safeString(cell.getValue().getAddress())));

        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        populateSampleData();
        customerTable.setItems(filteredData);
    }
 
    /**
     * Populate the customer with sample data from the database
     * This is used to fetch all customers from the database and populate the master data list.
     * @throws SQLException
     */
    private void populateSampleData() throws SQLException {
        
        List<Customer> customers = Customer.fetchAllCustomers();
        masterData.setAll(customers);
    }

    // Handles the search functionality based on user input and selected category
    /**
     * Handles the search functionality 
     * This method filters the customer table based on the search keyword and selected category.
     */
    @FXML
    private void handleSearch() {
        final String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        final String category = searchCategoryBox.getValue();

        filteredData.setPredicate(customer -> {
            if (keyword.isBlank()) {
                return true;
            }
            final String value = recordValueByCategory(customer, category);
            return value.toLowerCase(Locale.ENGLISH).contains(keyword.toLowerCase(Locale.ENGLISH));
        });
    }

    /**
     * Records value by category
     * This method retrieves the value of the specified category from the customer record.
     * @param customer the customer record
     * @param category the category to retrieve
     * @return the value of the specified category
     */
    private String recordValueByCategory(Customer customer, String category) {
        final String defaultValue = safeString(customer.getName());
        if (category == null) {
            return defaultValue;
        }
        
        return switch (category) {
            case "ID" -> String.valueOf(customer.getCustomerID());
            case "Email" -> safeString(customer.getEmail());
            case "Phone Number" -> safeString(customer.getPhoneNumber());
            case "Date of Birth" -> customer.getDOB() == null ? "" : customer.getDOB().toString();
            case "Government ID" -> safeString(customer.getGovtID());
            case "Address" -> safeString(customer.getAddress());
            default -> defaultValue;
        };
    }

    /**
     * Handles the action of opening a selected customer's account view
     * Open account view for the selected customer in the table.
     * @param event
     */
    @FXML
    private void handleOpenAccount(ActionEvent event) {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Please select a customer.");
            alert.showAndWait();
            return;
        }

        switchToAccountView(event, selected);
    }

    /**
     * Switch to account view for the selected customer
     * @param event
     * @param customer
     */
    private void switchToAccountView(ActionEvent event, Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bank/Views/TellerAccountView.fxml"));
            Parent root = loader.load();
            TellerAccountViewController controller = loader.getController();
            controller.populateWithCustomer(customer);

            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load account view.", ex);
        }
    }

    /**
     * Handles the action of creating a new customer account
     */
    @FXML
    private void handleCreateAccount() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("Create account flow not implemented yet.");
        alert.showAndWait();
    }

    /**
     * Handles the action of updating password and security question
     * @param event
     */
    @FXML
    private void handleUpdatePassword(ActionEvent event) {
        switchScene(event, "/bank/Views/UpdatePasswordSecurityQuestion.fxml");
    }

    /**
     * Handles the logout action and switches back to the login form
     * @param event the event created when a user clicks "logout" hyperlink
     */ 
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clear();
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

    /**
     * Switches the scene to the specified FXML resource
     * @param event
     * @param resourcePath
     */
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
     * Safely returns a non-null string
     * @param value
     * @return
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
