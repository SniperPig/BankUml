package bank.Controllers;

import bank.DB.BankDb;
import bank.Models.Branch;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

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
    private void handleCreate() {
        try {
            if (!validate()) return;

            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            LocalDate dob = dobPicker.getValue();
            String govId = govIdField.getText();
            String address = addressField.getText();
            String password = passwordField.getText();
            String securityQ = securityQuestionBox.getValue();
            String securityA = securityAnswerField.getText();

            Branch selectedBranch = branchBox.getValue();
            int branchId = selectedBranch.getBranchId();   // ✅ get numeric ID from Branch

            System.out.println("DEBUG → Attempting to CREATE account for: " + email);
            System.out.println("DEBUG → Using branch: " + selectedBranch + " (id=" + branchId + ")");

            db.customerCreate(
                    1,                 // TEMP: actor employee ID (e.g. admin)
                    branchId,          // ✅ real branch_id from DB
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

            System.out.println("DEBUG → ACCOUNT CREATED successfully.");

            showInfo("Account successfully created!");
            closeWindow();

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
        if (nameField.getText().isEmpty() ||
                emailField.getText().isEmpty() ||
                phoneField.getText().isEmpty() ||
                dobPicker.getValue() == null ||
                govIdField.getText().isEmpty() ||
                addressField.getText().isEmpty() ||
                passwordField.getText().isEmpty() ||
                securityQuestionBox.getValue() == null ||
                securityAnswerField.getText().isEmpty() ||
                branchBox.getValue() == null
        ) {
            showError("All fields must be filled.");
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