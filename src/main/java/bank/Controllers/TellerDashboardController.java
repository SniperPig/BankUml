package bank.Controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class TellerDashboardController {

    private static final String[] CATEGORIES = {
            "ID", "Name", "Email", "Phone Number", "Date of Birth", "Government ID", "Address"
    };

    @FXML
    private Label welcomeLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> searchCategoryBox;

    @FXML
    private TableView<CustomerRecord> customerTable;

    @FXML
    private TableColumn<CustomerRecord, String> idColumn;

    @FXML
    private TableColumn<CustomerRecord, String> nameColumn;

    @FXML
    private TableColumn<CustomerRecord, String> emailColumn;

    @FXML
    private TableColumn<CustomerRecord, String> phoneColumn;

    @FXML
    private TableColumn<CustomerRecord, String> dobColumn;

    @FXML
    private TableColumn<CustomerRecord, String> govIdColumn;

    @FXML
    private TableColumn<CustomerRecord, String> addressColumn;

    private final ObservableList<CustomerRecord> masterData = FXCollections.observableArrayList();
    private final FilteredList<CustomerRecord> filteredData = new FilteredList<>(masterData, record -> true);

    // Initializes the controller class. This method is automatically called
    // after the fxml file has been loaded.
    @FXML
    private void initialize() {
        welcomeLabel.setText("Welcome, Teller");

        searchCategoryBox.setItems(FXCollections.observableArrayList(Arrays.asList(CATEGORIES)));
        searchCategoryBox.getSelectionModel().select("Name");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        govIdColumn.setCellValueFactory(new PropertyValueFactory<>("governmentId"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        populateSampleData();
        customerTable.setItems(filteredData);
    }


    // Populate with sample data for demonstration purposes for now 
    // This  is used to populate the table with the customer records 
    private void populateSampleData() {
        // Sample data for now till we populate from database
        masterData.addAll(
                new CustomerRecord("000001", "Michael Scott", "mc@mail.com",
                        "514 514 5115", "28-03-1999", "HLSN2399", "Scranton, PA"),
                new CustomerRecord("000002", "Pam Beesly", "pam@mail.com",
                        "514 513 2334", "07-03-1990", "ABE9933", "Scranton, PA"),
                new CustomerRecord("000003", "Jim Halpert", "jim@mail.com",
                        "514 445 1982", "14-10-1989", "HHH1200", "Austin, TX")
        );
    }

    // Handles the search functionality based on user input and selected category
    @FXML
    private void handleSearch() {
        final String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        final String category = searchCategoryBox.getValue();

        filteredData.setPredicate(record -> {
            if (keyword.isBlank()) {
                return true;
            }
            final String value = recordValueByCategory(record, category);
            return value.toLowerCase(Locale.ENGLISH).contains(keyword.toLowerCase(Locale.ENGLISH));
        });
    }

    // Returns the value of the specified category from the customer record
    private String recordValueByCategory(CustomerRecord record, String category) {
        if (category == null) {
            return record.getName();
        }
        
        // Return the value based on the selected category for filtering
        return switch (category) {
            case "ID" -> record.getId();
            case "Email" -> record.getEmail();
            case "Phone Number" -> record.getPhone();
            case "Date of Birth" -> record.getDateOfBirth();
            case "Government ID" -> record.getGovernmentId();
            case "Address" -> record.getAddress();
            default -> record.getName();
        };
    }

    // Handles the action of opening an account for the selected customer
    @FXML
    private void handleOpenAccount(ActionEvent event) {
        CustomerRecord selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Please select an account to open.");
            alert.showAndWait();
            return;
        }

        switchToAccountView(event, selected);
    }

    private void switchToAccountView(ActionEvent event, CustomerRecord record) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bank/Views/TellerAccountView.fxml"));
            Parent root = loader.load();
            TellerAccountViewController controller = loader.getController();
            controller.populateWithCustomer(record);

            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load account view.", ex);
        }
    }


    // Handles the action of creating a new account
    @FXML
    private void handleCreateAccount() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("Create account flow not implemented yet.");
        alert.showAndWait();
    }

    // Handles the action of updating password and security question
    @FXML
    private void handleUpdatePassword(ActionEvent event) {
        switchScene(event, "/bank/Views/UpdatePasswordSecurityQuestion.fxml");
    }

    // Handles the action of logging out
    @FXML
    private void handleLogout(ActionEvent event) {
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


    // Need to changed to actual Customer model from database later
    public static class CustomerRecord {
        private final String id;
        private final String name;
        private final String email;
        private final String phone;
        private final String dateOfBirth;
        private final String governmentId;
        private final String address;

        public CustomerRecord(String id, String name, String email,
                              String phone, String dateOfBirth,
                              String governmentId, String address) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.dateOfBirth = dateOfBirth;
            this.governmentId = governmentId;
            this.address = address;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public String getGovernmentId() {
            return governmentId;
        }

        public String getAddress() {
            return address;
        }
    }
}
