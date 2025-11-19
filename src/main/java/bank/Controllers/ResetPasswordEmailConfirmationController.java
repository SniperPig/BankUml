package bank.Controllers;

import bank.Models.Customer;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ResetPasswordEmailConfirmationController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    @FXML
    private TextField emailField;

    /**
     * Handles the "Continue" button action.
     * Validates the email and loads the security question view if valid.
     * Otherwise shows an error alert.
     */
    @FXML
    private void handleContinue(ActionEvent event) {
        String email = emailField.getText().trim();

        Customer customer;
        try {
            customer = Customer.fetchCustomerByEmail(email);
        } catch (SQLException ex) {
            // showError("Unable to look up that email right now. Please try again later.");
            throw new RuntimeException(ex);
        }

        if (customer == null || customer.getEmail() == null) {
            showError("The email address you entered does not exist in our records.");
            return;
        }

        loadSecurityQuestion(event, customer);
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
     * Passes the customer object to the next controller (ResetPasswordSecurityQuestionController).
     *
     */
    private void loadSecurityQuestion(ActionEvent event, Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bank/Views/ResetPasswordSecurityQuestion.fxml"));
            Parent root = loader.load();
            ResetPasswordSecurityQuestionController controller = loader.getController();
            // controller.setEmailAddress(customer.getEmail());
            controller.setCustomer(customer);

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
}
