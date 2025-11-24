package bank.Controllers;

import bank.DB.BankDb;
import bank.Models.Branch;
import bank.Models.Employee;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import bank.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.util.Map;
import java.io.IOException;
import java.util.regex.Pattern;


/**
 * Controller for CreateAccountForm.fxml.
 *
 * Handles input validation, account creation, and DB interaction.
 */
public class CreateAccountController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker dobPicker;
    @FXML private TextField govIdField;
    @FXML private TextField addressField;

    @FXML private PasswordField passwordField;

    @FXML private ComboBox<String> securityQuestionBox;
    @FXML private TextField securityAnswerField;

    @FXML private ComboBox<Branch> branchBox;

    // --- Regex patterns for form validation ---
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z ]+$");              // letters + spaces

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{10}$");                 // exactly 10 digits

    private static final Pattern GOV_ID_PATTERN =
            Pattern.compile("^[A-Za-z0-9]+$");            // letters + digits only

    private static final Pattern ADDRESS_PATTERN =
            Pattern.compile("^\\d{3,5}\\s+[A-Za-z0-9\\s]+$");
    // 3–5 digits, space, then street name

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    private static final Pattern SECURITY_ANSWER_PATTERN =
            Pattern.compile("^[A-Za-z ]+$");              // letters + spaces only

    private final BankDb db = new BankDb();

    /**
     * Initializes dropdown values when the FXML loads.
     */
    @FXML
    public void initialize() {
        // Security questions
        securityQuestionBox.getItems().addAll(
                "What is your mother's maiden name?",
                "What is your first pet's name?",
                "What city were you born in?",
                "What is your favorite teacher's name?"
        );

        // Load branches from DB
        try {
            List<Branch> branches = db.getAllBranches();
            branchBox.getItems().addAll(branches);

            // TEMP DEBUG — print branch codes to console
            for (Branch b : branches) {
                System.out.println("DEBUG → Branch loaded: " + b.getCode());
            }
        } catch (SQLException e) {
            showError("Failed to load branches from database.");
        }
    }

    /**
     * Handles the Create Account button.
     */
@FXML
private void handleCreate(javafx.event.ActionEvent event) {
    try {
        if (!validate()) return;

        // 1) Get logged-in employee from Session
        Employee currentEmployee = SessionManager.getCurrentEmployee();
        if (currentEmployee == null) {
            showError("No logged-in employee found. Please log in again.");
            return;
        }

        // Look up this employee in the DB to get IDs
        List<Map<String, Object>> empRows =
                db.employeeGetByEmail(currentEmployee.getEmail());
        if (empRows.isEmpty()) {
            showError("Logged-in employee not found in the database.");
            return;
        }

        Map<String, Object> empRow = empRows.get(0);
        int actorEmployeeId =
                ((Number) empRow.get("employee_id")).intValue();
        int employeeBranchId =
                ((Number) empRow.get("branch_id")).intValue();

        // 2) Read form fields
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        LocalDate dob = dobPicker.getValue();
        String govId = govIdField.getText();
        String address = addressField.getText();
        String password = passwordField.getText();
        String securityQ = securityQuestionBox.getValue();
        String securityA = securityAnswerField.getText();

        // 3) Branch comes from the dropdown for the customer
        Branch selectedBranch = branchBox.getValue();
        int customerBranchId = selectedBranch.getBranchId();  // numeric branch_id

        System.out.println("DEBUG → Creating customer: " + email);
        System.out.println("DEBUG → Customer branch: " + customerBranchId);
        System.out.println("DEBUG → Employee id: " + actorEmployeeId +
                           ", employee branch: " + employeeBranchId);

        // 4) CREATE CUSTOMER and get the new customer_id back
        int newCustomerId = db.customerCreate(
                actorEmployeeId,
                customerBranchId,
                name,
                email,
                phone,
                java.sql.Date.valueOf(dob),
                govId,
                address,
                password,
                securityQ,
                securityA
        );

        System.out.println("DEBUG → New customer_id = " + newCustomerId);

        // 5) OPEN TWO ACCOUNTS for this customer
        //    Branch for accounts = branch of the current employee
        db.accountOpen(
                actorEmployeeId,
                newCustomerId,
                employeeBranchId,   // branch of the employee
                "SAVING",           // make sure this matches your DB enum
                0.0,
                null,
                null,
                null
        );

        db.accountOpen(
            actorEmployeeId,
            newCustomerId,
            employeeBranchId,
            "CHECKING",
            0.0,
            null,
            null,
            null
        );

        System.out.println("DEBUG → SAVING and CHECKING accounts created.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Created");
        alert.setHeaderText(null);
        alert.setContentText("Customer and two accounts created successfully!");
        alert.showAndWait();
        switchScene(event, "/bank/Views/TellerDashboard.fxml");

    } catch (SQLException e) {
        e.printStackTrace();
        showError("Database error: " + e.getMessage());
    }
}

    
    /**
     * Cancels and closes the window.
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    /**
     * Validates all input fields.
     */
    private boolean validate() {
        // --------- Basic "not empty" checks ----------
        if (nameField.getText().isEmpty() ||
                emailField.getText().isEmpty() ||
                phoneField.getText().isEmpty() ||
                dobPicker.getValue() == null ||
                govIdField.getText().isEmpty() ||
                addressField.getText().isEmpty() ||
                passwordField.getText().isEmpty() ||
                securityQuestionBox.getValue() == null ||
                securityAnswerField.getText().isEmpty() ||
                branchBox.getValue() == null) {
            showError("All fields must be filled.");
            return false;
        }

        // Trimmed values for regex checks
        String name      = nameField.getText().trim();
        String email     = emailField.getText().trim();
        String phone     = phoneField.getText().trim();
        String govId     = govIdField.getText().trim();
        String address   = addressField.getText().trim();
        String password  = passwordField.getText();
        String secAnswer = securityAnswerField.getText().trim();

        // --------- Regex validation ----------

        // Full Name: characters only (allow spaces)
        if (!NAME_PATTERN.matcher(name).matches()) {
            showError("Full Name can only contain letters and spaces.");
            return false;
        }

        // Email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address.");
            return false;
        }

        // Phone: 10 digits
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            showError("Phone Number must be exactly 10 digits.");
            return false;
        }

        // Government ID: no special symbols (letters + digits only)
        if (!GOV_ID_PATTERN.matcher(govId).matches()) {
            showError("Government ID can only contain letters and digits (no symbols).");
            return false;
        }

        // Address: 3–5 digits then characters
        if (!ADDRESS_PATTERN.matcher(address).matches()) {
            showError("Address must start with 3–5 digits followed by the street name (e.g., '123 Main St').");
            return false;
        }

        // Password: given strong-password regex
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            showError("Password must be at least 8 characters and include:\n" +
                    "• one lowercase letter\n" +
                    "• one uppercase letter\n" +
                    "• one digit\n" +
                    "• one special character (@$!%*?&)");
            return false;
        }

        // Security Answer: characters only, no digits or symbols
        if (!SECURITY_ANSWER_PATTERN.matcher(secAnswer).matches()) {
            showError("Security Answer can only contain letters and spaces (no digits or symbols).");
            return false;
        }

        return true;
    }

        /**
     * "Back to Home" under the title – just closes this window
     * and returns the user to the Teller dashboard behind it.
     */
    @FXML
    private void handleBackToHome(ActionEvent event) {
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

    /**
     * Logout hyperlink at the bottom-right.
     * Sends the user back to the LoginForm.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clear();
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

    /**
     * Generic helper for swapping scenes, same pattern as other controllers.
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

    // UI Helpers
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.show();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }
}
