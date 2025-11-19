package bank.Controllers;

import bank.Models.Customer;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ResetPasswordFormController {
    private Customer customer;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    /**
     * Handles the action to reset the password.
     * Checks if the new password and confirmation match, then updates the password.
     * Displays appropriate alerts based on the outcome.
     */
    @FXML
    private void handleResetPassword() {
        if (newPasswordField == null || confirmPasswordField == null) {
            return;
        }

        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);

        if (newPassword == null || newPassword.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.setContentText("Please enter and confirm your new password.");
        } else if (!newPassword.equals(confirmPassword)) {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.setContentText("Passwords do not match.");
        } else {
            if (updatePassword(newPassword)) {
                alert.setContentText("Password reset submitted!");
                switchSceneFromNode(newPasswordField, "/bank/Views/LoginForm.fxml");
            } else {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setContentText("Unable to reset password right now.");
            }
        }

        alert.showAndWait();
    }

    /**
     * Handles the action to go back to the login form.
     */
    @FXML
    private void handleBackToLogin(ActionEvent event) {
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

    /**
     * Sets the customer whose password is to be reset.
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /**
     * Updates the customer's password.
     */
    private boolean updatePassword(String newPassword) {
        if (customer == null) {
            return false;
        }

        customer.setPassword(newPassword);
        return true;
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
    * Switches the scene using a node reference instead of an event. 
    */
    private void switchSceneFromNode(Node node, String resourcePath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
            Scene scene = node.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load " + resourcePath, ex);
        }
    }
}
