package bank.Controllers;

import bank.Models.Customer;
import bank.Models.Employee;
import bank.Models.PasswordResettable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class ResetPasswordEmailConfirmationController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    @FXML
    private TextField emailField;

    @FXML
    private ToggleGroup accountTypeToggle;

    @FXML
    private ToggleButton customerRadio;

    @FXML
    private ToggleButton employeeRadio;

    @FXML
    private void initialize() {
        if (customerRadio != null) {
            customerRadio.setUserData(AccountType.CUSTOMER);
        }
        if (employeeRadio != null) {
            employeeRadio.setUserData(AccountType.EMPLOYEE);
        }
        if (accountTypeToggle != null && accountTypeToggle.getSelectedToggle() == null && customerRadio != null) {
            customerRadio.setSelected(true);
        }
    }

    /**
     * Handles the "Continue" button action.
     * Validates the email and loads the security question view if valid.
     * Otherwise shows an error alert.
     */
    @FXML
    private void handleContinue(ActionEvent event) {
        String email = emailField.getText().trim();

        PasswordResettable account = lookupAccountByType(email);

        if (account == null) {
            return; // lookupAccountByType already surfaced errors
        }

        loadSecurityQuestion(event, account);
    }

    /**
     * Looks up the account by email and type.
     * @param email of the account that wants to reset password
     * @return the PasswordResettable account, or null if not found
     * PasswordResettable could be either Customer or Employee based on selection
     * PasswordResettable is an interface implemented by both Customer and Employee for password reset functionality
     */
    private PasswordResettable lookupAccountByType(String email) {
        if (accountTypeToggle == null || accountTypeToggle.getSelectedToggle() == null) {
            showError("Please select Customer or Employee.");
            return null;
        }

        Object selected = accountTypeToggle.getSelectedToggle().getUserData();
        AccountType type;
        if (selected instanceof AccountType at) {
            type = at;
        } else if (selected instanceof String s) {
            type = AccountType.valueOf(s);
        } else {
            showError("Unknown account type selection.");
            return null;
        }

        try {
            PasswordResettable account = switch (type) {
                case CUSTOMER -> Customer.fetchCustomerByEmail(email);
                case EMPLOYEE -> Employee.fetchEmployeeByEmail(email);
            };

            if (account == null || account.getEmail() == null) {
                showError("The email address you entered does not exist in our records.");
                return null;
            }

            return account;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Handles the "Back to Login" button action.
     */
    @FXML
    private void handleBackToLogin(ActionEvent event) {
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

    /**
     * Loads the security question view for password reset.
     * Passes the account object to the next controller (ResetPasswordSecurityQuestionController).
     *
     */
    private void loadSecurityQuestion(ActionEvent event, PasswordResettable account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bank/Views/ResetPasswordSecurityQuestion.fxml"));
            Parent root = loader.load();
            ResetPasswordSecurityQuestionController controller = loader.getController();
            controller.setAccount(account);

            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load reset password security question view.", ex);
        }
    }

    /**
     * Switches the scene to the specified FXML resource.
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
     * Displays an error alert with the specified message.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private enum AccountType {
        CUSTOMER,
        EMPLOYEE
    }
}
