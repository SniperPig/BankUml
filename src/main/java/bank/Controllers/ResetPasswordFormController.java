package bank.Controllers;

import bank.Models.PasswordResettable;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ResetPasswordFormController {
    private PasswordResettable account;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField newPasswordVisibleField;

    @FXML
    private TextField confirmPasswordVisibleField;

    @FXML
    private ToggleButton newPasswordToggle;

    @FXML
    private ToggleButton confirmPasswordToggle;

    @FXML
    private void initialize() {
        setupVisibilityToggle(newPasswordField, newPasswordVisibleField, newPasswordToggle);
        setupVisibilityToggle(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);
    }

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
        
        // Password must be at least 8 characters, contain at least one uppercase letter,
        // one lowercase letter, one digit, and one special character.
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        

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
            if (newPassword.matches(regex) && updatePassword(newPassword)) {
                alert.setContentText("Password reset submitted!");
                switchSceneFromNode(newPasswordField, "/bank/Views/LoginForm.fxml");
            } else if (!newPassword.matches(regex)) {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setContentText("Password must be\n • at least 8 characters \n • contain at least one uppercase letter \n • one lowercase letter \n • one digit \n • and one special character.");
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
     * Sets the account whose password is to be reset.
     */
    public void setAccount(PasswordResettable account) {
        this.account = account;
    }

    /**
     * Updates the account password.
     * 
     * @param newPassword the new password for the account
     * @return boolean true if the account password was updated, false otherwise
     */
    private boolean updatePassword(String newPassword) {
        if (account == null) {
            return false;
        }

        account.setPassword(newPassword);
        return true;
    }

    /**
     * Toggles the visibility of the new password field.
     */
    @FXML
    private void toggleNewPasswordVisibility() {
        toggleFieldVisibility(newPasswordField, newPasswordVisibleField, newPasswordToggle);
    }

    /**
     * Toggles the visibility of the confirm password field.
     */
    @FXML
    private void toggleConfirmPasswordVisibility() {
        toggleFieldVisibility(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);
    }

    /**
     * Sets up the visibility toggle for a password field.
     * @param hiddenField the PasswordField that is initially hidden
     * @param visibleField the TextField that shows the password in plain text
     * @param toggleButton the ToggleButton to switch visibility
     */
    private void setupVisibilityToggle(PasswordField hiddenField,
                                       TextField visibleField,
                                       ToggleButton toggleButton) {
        if (hiddenField == null || visibleField == null) {
            return;
        }

        visibleField.setManaged(false);
        visibleField.setVisible(false);
        visibleField.textProperty().bindBidirectional(hiddenField.textProperty());

        if (toggleButton != null) {
            toggleButton.setText(null);
            toggleButton.setGraphic(createEyeIcon(false));
            toggleButton.setOnAction(e -> toggleFieldVisibility(hiddenField, visibleField, toggleButton));
        }
    }

    /**
     * Toggles the visibility of the password field.
     * @param hiddenField the PasswordField that is initially hidden
     * @param visibleField the TextField that shows the password in plain text
     * @param toggleButton the ToggleButton to switch visibility
     */
    private void toggleFieldVisibility(PasswordField hiddenField,
                                       TextField visibleField,
                                       ToggleButton toggleButton) {
        if (hiddenField == null || visibleField == null || toggleButton == null) {
            return;
        }

        boolean show = toggleButton.isSelected();
        visibleField.setManaged(show);
        visibleField.setVisible(show);
        hiddenField.setManaged(!show);
        hiddenField.setVisible(!show);

        toggleButton.setGraphic(createEyeIcon(show));
    }

    /**
     * Creates an eye icon for the visibility toggle button.
     * @param open whether the eye icon should appear open or closed
     * @return a Node representing the eye icon
     */
    private Node createEyeIcon(boolean open) {
        SVGPath path = new SVGPath();
        // This is the eye icon path data for open and closed states
        path.setContent(open
                ? "M1 12c2.5-4.5 6.5-7 11-7s8.5 2.5 11 7c-2.5 4.5-6.5 7-11 7S3.5 16.5 1 12zm11 4a4 4 0 1 0 0-8 4 4 0 0 0 0 8z"
                : "M2 3.5 3.5 2l18.5 18.5L20.5 22l-2.9-2.9c-1.4.6-3 1-4.6 1-4.5 0-8.5-2.5-11-7 1.1-1.9 2.5-3.4 4.1-4.6L2 3.5zm6 6L6.6 8c-1.2.9-2.3 2.1-3.2 3.6 2.1 3.5 5.1 5.4 8.7 5.4 1.2 0 2.3-.2 3.4-.6l-1.6-1.6a4 4 0 0 1-5-5zm3.7-5c4.5 0 8.5 2.5 11 7-.9 1.5-2 2.8-3.3 3.8l-1.4-1.4c1-.8 1.9-1.9 2.6-3.2-2.1-3.5-5.1-5.4-8.9-5.4-.8 0-1.5.1-2.2.2L8.8 4.3c1-.5 2.1-.8 3.9-.8z");
        path.setFill(Color.web("#444444"));
        // This ensures the icon scales properly
        path.setScaleX(1.0);
        path.setScaleY(1.0);
        return path;
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
