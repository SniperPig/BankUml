package bank.Controllers;

import bank.Models.Customer;
import bank.Models.Employee;
import bank.SessionManager;
import bank.Controllers.ManageUserRoleController;


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
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Controller for the Administrator Dashboard.
 * Shows a combined list of customers and employees and allows searching.
 */
public class AdminDashboardController {

    private static final String[] CATEGORIES = {
            "ID",
            "Type",
            "Name",
            "Email",
            "Phone Number",
            "Date of Birth",
            "Address",
            "Role",
            "Branch"
    };

    @FXML private Label welcomeLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchCategoryBox;

    @FXML private TableView<UserRecord> userTable;
    @FXML private TableColumn<UserRecord, String> idColumn;
    @FXML private TableColumn<UserRecord, String> typeColumn;
    @FXML private TableColumn<UserRecord, String> nameColumn;
    @FXML private TableColumn<UserRecord, String> emailColumn;
    @FXML private TableColumn<UserRecord, String> phoneColumn;
    @FXML private TableColumn<UserRecord, String> dobColumn;
    @FXML private TableColumn<UserRecord, String> addressColumn;
    @FXML private TableColumn<UserRecord, String> roleColumn;
    @FXML private TableColumn<UserRecord, String> branchColumn;

    private final ObservableList<UserRecord> masterData =
            FXCollections.observableArrayList();
    private final FilteredList<UserRecord> filteredData =
            new FilteredList<>(masterData, record -> true);

    /**
     * JavaFX initialize hook. Sets up the table and loads data.
     * Called automatically after FXML is loaded.
     *
     * @throws SQLException if loading customers or employees fails
     */
    @FXML
    private void initialize() throws SQLException {
        Employee admin = SessionManager.getCurrentEmployee();
        if (admin != null && admin.getName() != null) {
            String firstName = admin.getName().split("\\s+")[0];
            welcomeLabel.setText("Welcome, " + firstName);
        }

        // Setup category combo box
        searchCategoryBox.setItems(FXCollections.observableArrayList(Arrays.asList(CATEGORIES)));
        searchCategoryBox.getSelectionModel().select("Name");

        // Setup table columns
        idColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getUserId())));
        typeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getUserType())));
        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getName())));
        emailColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getEmail())));
        phoneColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getPhoneNumber())));
        dobColumn.setCellValueFactory(cell -> {
            LocalDate dob = cell.getValue().getDob();
            return new SimpleStringProperty(dob == null ? "" : dob.toString());
        });
        addressColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getAddress())));
        roleColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getRole())));
        branchColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeString(cell.getValue().getBranchName())));

        // Load initial data
        populateData();

        userTable.setItems(filteredData);
    }

    /**
     * Loads all customers & employees into the master list.
     * Replace the fetchAll* calls with whatever you actually use to load them.
     *
     * @throws SQLException if fetching customers or employees fails
     */
    private void populateData() throws SQLException {
        masterData.clear();

        // Customers
        List<Customer> customers = Customer.fetchAllCustomers(); // adjust if needed
        for (Customer c : customers) {
            UserRecord rec = UserRecord.fromCustomer(c);
            masterData.add(rec);
        }

        // Employees
        List<Employee> employees = Employee.fetchAllEmployees(); // adjust if needed
        for (Employee e : employees) {
            UserRecord rec = UserRecord.fromEmployee(e);
            masterData.add(rec);
        }
    }

    /**
     * Search/filter handler – similar idea to TellerDashboard.
     * <p>
     * Reads the current search text and selected category and applies
     * a predicate to the {@link #filteredData} list to update the table view.
     */
    @FXML
    private void handleSearch() {
        final String keyword = searchField.getText() == null
                ? ""
                : searchField.getText().trim();
        final String category = searchCategoryBox.getValue();

        filteredData.setPredicate(record -> {
            if (keyword.isBlank()) {
                return true;
            }
            final String value = recordValueByCategory(record, category);
            return value.toLowerCase(Locale.ENGLISH)
                    .contains(keyword.toLowerCase(Locale.ENGLISH));
        });
    }

    /**
     * Maps a row + category name to a string we can search on.
     *
     * @param record   the user record representing a row in the table
     * @param category the search category (e.g. "Name", "Email")
     * @return the string value from the record that corresponds to the category;
     *         empty string if no match
     */
    private String recordValueByCategory(UserRecord record, String category) {
        if (record == null || category == null) {
            return "";
        }

        return switch (category) {
            case "ID" -> String.valueOf(record.getUserId());
            case "Type" -> safeString(record.getUserType());
            case "Name" -> safeString(record.getName());
            case "Email" -> safeString(record.getEmail());
            case "Phone Number" -> safeString(record.getPhoneNumber());
            case "Date of Birth" -> {
                LocalDate dob = record.getDob();
                yield dob == null ? "" : dob.toString();
            }
            case "Address" -> safeString(record.getAddress());
            case "Role" -> safeString(record.getRole());
            case "Branch" -> safeString(record.getBranchName());
            default -> "";
        };
    }

    /**
     * Handles the "Manage User" action.
     * <p>
     * Ensures a user is selected in the table, then loads the
     * {@code ManageUserRole.fxml} view and passes the selected {@link UserRecord}
     * to {@link ManageUserRoleController#initializeData(AdminDashboardController.UserRecord)}.
     *
     * @param event the action event triggered by the "Manage User" control
     */
    @FXML
    private void handleManageUser(ActionEvent event) {
        UserRecord selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a user from the table first.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/bank/Views/ManageUserRole.fxml"));
            Parent root = loader.load();

            ManageUserRoleController controller = loader.getController();
            controller.initializeData(selected); // pass the selected user

            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unable to open Manage User Role");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Handles the "View Audit Log" action.
     * <p>
     * Loads the {@code AuditLog.fxml} view and replaces the current scene content.
     * If loading fails, shows an error dialog.
     *
     * @param event the action event triggered by the "View Audit Log" control
     */
    @FXML
    private void handleViewAuditLog(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/bank/Views/AuditLog.fxml"));
            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unable to open Audit Log");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Handles navigation to the "Update Password & Security Question" screen.
     *
     * @param event the action event triggered by the corresponding control
     */
    @FXML
    private void handleUpdatePassword(ActionEvent event) {
        switchScene(event, "/bank/Views/UpdatePasswordSecurityQuestion.fxml");
    }

    /**
     * Handles logout from the admin dashboard.
     * <p>
     * Clears the current session via {@link SessionManager#clear()} and
     * returns to the login form.
     *
     * @param event the action event triggered by the Logout control
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clear();
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

    /**
     * Utility method to switch scenes to another FXML view.
     *
     * @param event        the originating action event, used to access the current stage
     * @param resourcePath the classpath to the FXML resource to load
     * @throws RuntimeException if the FXML resource cannot be loaded
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
     * Returns an empty string if the provided value is {@code null}, otherwise returns it unchanged.
     *
     * @param value the input string which may be {@code null}
     * @return an empty string if {@code value} is {@code null}; otherwise {@code value}
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }

    /**
     * Row model for the admin dashboard table.
     * Backed either by a Customer or an Employee.
     */
    public static class UserRecord {
        private final int userId;
        private final String userType; // "CUSTOMER" or "EMPLOYEE"
        private final String name;
        private final String email;
        private final String phoneNumber;
        private final LocalDate dob;
        private final String address;
        private final String role;
        private final String branchName;

        private final Integer customerId;
        private final Integer employeeId;

        /**
         * Constructs a {@code UserRecord} representing either a customer or an employee.
         *
         * @param userId      unique ID used in the table (customer or employee ID)
         * @param userType    "CUSTOMER" or "EMPLOYEE"
         * @param name        full name
         * @param email       email address
         * @param phoneNumber phone number
         * @param dob         date of birth
         * @param address     postal address
         * @param role        employee role; empty string for customers
         * @param branchName  employee branch name; empty string for customers
         * @param customerId  underlying customer ID, or {@code null} if this represents an employee
         * @param employeeId  underlying employee ID, or {@code null} if this represents a customer
         */
        private UserRecord(int userId,
                           String userType,
                           String name,
                           String email,
                           String phoneNumber,
                           LocalDate dob,
                           String address,
                           String role,
                           String branchName,
                           Integer customerId,
                           Integer employeeId) {
            this.userId = userId;
            this.userType = userType;
            this.name = name;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.dob = dob;
            this.address = address;
            this.role = role;
            this.branchName = branchName;
            this.customerId = customerId;
            this.employeeId = employeeId;
        }

        /**
         * Creates a {@code UserRecord} from a {@link Customer} model.
         *
         * @param c the customer instance
         * @return a new {@code UserRecord} representing the customer
         */
        public static UserRecord fromCustomer(Customer c) {
            return new UserRecord(
                    c.getCustomerID(),              // adjust getter names if needed
                    "CUSTOMER",
                    c.getName(),
                    c.getEmail(),
                    c.getPhoneNumber(),
                    c.getDOB(),
                    c.getAddress(),
                    "",                             // customers don't have an employee role
                    "",                             // no branch for customers
                    c.getCustomerID(),
                    null
            );
        }

        /**
         * Creates a {@code UserRecord} from an {@link Employee} model.
         *
         * @param e the employee instance
         * @return a new {@code UserRecord} representing the employee
         */
        public static UserRecord fromEmployee(Employee e) {
            String branchName = (e.getBranch() == null)
                    ? ""
                    : e.getBranch().getBranchName(); // adjust getter
            return new UserRecord(
                    e.getEmployeeID(),
                    "EMPLOYEE",
                    e.getName(),
                    e.getEmail(),
                    e.getPhoneNumber(),
                    e.getDOB(),
                    e.getAddress(),
                    e.getRole(),
                    branchName,
                    null,
                    e.getEmployeeID()
            );
        }

        /**
         * Returns the table user ID (customer or employee ID).
         *
         * @return the user ID
         */
        public int getUserId() {
            return userId;
        }

        /**
         * Returns the user type ("CUSTOMER" or "EMPLOYEE").
         *
         * @return the user type
         */
        public String getUserType() {
            return userType;
        }

        /**
         * Returns the full name of the user.
         *
         * @return the user name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the email address of the user.
         *
         * @return the email
         */
        public String getEmail() {
            return email;
        }

        /**
         * Returns the phone number of the user.
         *
         * @return the phone number
         */
        public String getPhoneNumber() {
            return phoneNumber;
        }

        /**
         * Returns the date of birth of the user.
         *
         * @return the date of birth, or {@code null} if not set
         */
        public LocalDate getDob() {
            return dob;
        }

        /**
         * Returns the address of the user.
         *
         * @return the address
         */
        public String getAddress() {
            return address;
        }

        /**
         * Returns the role of the user if an employee.
         *
         * @return the role, or empty string if this record represents a customer
         */
        public String getRole() {
            return role;
        }

        /**
         * Returns the branch name for the user if an employee.
         *
         * @return the branch name, or empty string if this record represents a customer
         */
        public String getBranchName() {
            return branchName;
        }

        /**
         * Returns the underlying customer ID, if this record represents a customer.
         *
         * @return the customer ID, or {@code null} if not a customer record
         */
        public Integer getCustomerId() {
            return customerId;
        }

        /**
         * Returns the underlying employee ID, if this record represents an employee.
         *
         * @return the employee ID, or {@code null} if not an employee record
         */
        public Integer getEmployeeId() {
            return employeeId;
        }
    }
}
