package bank.Controllers;

import java.io.IOException;
import bank.SessionManager;
import bank.Models.Customer;
import bank.Models.Employee;

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
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

/**
 * This class is the second of two steps to update a user's password.
 * It asks the user to choose a new password and validates that the new password
 * follows the security rules. 
 */
public class UpdatePasswordFormController {

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField newPasswordVisibleField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private ToggleButton newPasswordToggle;
    @FXML private ToggleButton confirmPasswordToggle;

    @FXML
    /**
     * This initializes the second update password form by setting both passwords fields to not visible at first.
     * If a user wishes to change the visibility, they can press the eye button.
     */
    private void initialize() {
        setupVisibilityToggle(newPasswordField, newPasswordVisibleField, newPasswordToggle);
        setupVisibilityToggle(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);
    }

    /**
     * This function allows the GUI to switch scenes
     * 
     * @param event the event that is created by clicking a button
     * @param resourcePath the path of the new scene we want to reach
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

    @FXML
    /**
     * Take the user back to their appropriate home page (either TellerDashboard, 
     * AdministratorDashboard, or CustomerDashboard) when they click "Back to Account Page"
     * 
     * @param event the event created when a user clicks "Back to Account Page" hyperlink
     */
    private void handleBackToAccount(ActionEvent event) {
        Employee currentEmployee = SessionManager.getCurrentEmployee();
        Customer currentCustomer = SessionManager.getCurrentCustomer();

        // First check if we have a customer or employee
        if (currentCustomer != null) {
            // If customer, go back to CustomerDashboard
            switchScene(event, "/bank/Views/CustomerDashboard.fxml");   
        } else {
            // If employee, check if it's admin or teller
            if (currentEmployee.getRole().equalsIgnoreCase("ADMIN")) {
                // If admin, go back to AdminDashboard
                switchScene(event, "/bank/Views/AdminDashboard.fxml");  
            } else {
                // If teller, go back to TellerDasboard
                switchScene(event, "/bank/Views/TellerDashboard.fxml");  
            }
        }
    }

    @FXML
    /**
     * Handles the logout action and switches back to the login form
     * @param event the event created when a user clicks "logout" hyperlink
     */ 
    private void handleLogout(ActionEvent event) {
        SessionManager.clear();
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

    
    /**
     * Updates the account password. Gets called in handleUpdatePassword().
     * 
     * @param newPassword the new password for the account
     * @return boolean true if the account password was updated, false otherwise
     */
    private boolean updatePassword(String newPassword) {
        // First check which user it is
        Employee currentEmployee = SessionManager.getCurrentEmployee();
        Customer currentCustomer = SessionManager.getCurrentCustomer();

        // If it's a customer
        if (currentCustomer != null) {
            currentCustomer.setPassword(newPassword);
            return true;
        } 
        // if it's an employee
        else if (currentEmployee != null) {
            currentEmployee.setPassword(newPassword);
            return true;
        } else {
            return false;
        }
    }

    @FXML
    /**
     * This function is invoked when the user confirms their new password. 
     * It checks that the password is valid per our requirements, and that both passwords match.
     * It then updates the DB with the new password.
     * 
     * @param event the event when user presses "Confirm" button
     */
    private void handleUpdatePassword(ActionEvent event) {
        // First make sure fields exist
        if (newPasswordField == null || confirmPasswordField == null) {
            return;
        }

        // Then retrieve the text from both fields
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // This will be used for alerts
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);

        // If either field is empty, show an alert to the user
        if (newPassword == null || newPassword.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.setContentText("Please enter and confirm your new password.");
            alert.showAndWait();
            return;
        } 

        // Password must be at least 8 characters, contain at least one uppercase letter,
        // one lowercase letter, one digit, and one special character.
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!newPassword.matches(regex)) {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.setContentText("Password must be\n • at least 8 characters \n • contain" + 
                    " at least one uppercase letter \n • one lowercase letter \n • one" + 
                    " digit \n • and one special character.");
            alert.showAndWait();
            return;
        }
        
        // If the passwords are not the same, show an alert
        if (!newPassword.equals(confirmPassword)) {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.setContentText("Passwords do not match.");
            alert.showAndWait();
            return;
        } 

        // Otherwise, confirm the password 
        if (updatePassword(newPassword)) {
            alert.setContentText("Password update submitted!");
            alert.showAndWait();
            handleBackToAccount(event);
        }
    }

    /**
    * Creates an eye icon using an SVGPath, used to toggle whether the password is visible or not.
    * 
    * @param open whether the eye is open or not
    * @return a SVGPath to show the appropriate eye icon
    */
    private SVGPath createEyeIcon(boolean open) {
        SVGPath path = new SVGPath();
        path.setContent(open
                ? "M1 12c2.5-4.5 6.5-7 11-7s8.5 2.5 11 7c-2.5 4.5-6.5 7-11 7S3.5 16.5 1 12zm11 4a4 4 0 1 0 0-8 4 4 0 0 0 0 8z"
                : "M2 3.5 3.5 2l18.5 18.5L20.5 22l-2.9-2.9c-1.4.6-3 1-4.6 1-4.5 0-8.5-2.5-11-7 1.1-1.9 2.5-3.4 4.1-4.6L2 3.5zm6 6L6.6 8c-1.2.9-2.3 2.1-3.2 3.6 2.1 3.5 5.1 5.4 8.7 5.4 1.2 0 2.3-.2 3.4-.6l-1.6-1.6a4 4 0 0 1-5-5zm3.7-5c4.5 0 8.5 2.5 11 7-.9 1.5-2 2.8-3.3 3.8l-1.4-1.4c1-.8 1.9-1.9 2.6-3.2-2.1-3.5-5.1-5.4-8.9-5.4-.8 0-1.5.1-2.2.2L8.8 4.3c1-.5 2.1-.8 3.9-.8z");
        path.setFill(Color.web("#444444"));
        return path;
    }

    /**
     * Toggles the visibility of a password field.
     *
     * @param hiddenField the PasswordField that hides the plain text 
     * @param visibleField the TextField that shows the password in plain text
     * @param toggleButton the button controlling whether the password is visible
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
     * This function uses previous functions to set everything up regarding the visibility of passwords in one place.
     * 
     * @param hiddenField the PasswordField that hides the plain text 
     * @param visibleField the TextField that shows the password in plain text
     * @param toggleButton the button controlling whether the password is visible
     */
    private void setupVisibilityToggle(PasswordField hiddenField,
                                       TextField visibleField,
                                       ToggleButton toggleButton) {

        // First make sure we have the two fields 
        if (hiddenField == null || visibleField == null) {
            return;
        }

        // This basically excludes the item from VBox
        visibleField.setManaged(false);
        // We start by having it not visible
        visibleField.setVisible(false);
        // Link the text of both in the same direction
        visibleField.textProperty().bindBidirectional(hiddenField.textProperty());

        // This is just to design our button to look like the eye graphic 
        if (toggleButton != null) {
            toggleButton.setText(null);
            toggleButton.setGraphic(createEyeIcon(false));
            // and this is when we want to toggle the button
            toggleButton.setOnAction(e -> toggleFieldVisibility(hiddenField, visibleField, toggleButton));
        }
    }

    @FXML
    /**
     * This is what allows the user to toggle the visibility of the new password specifically.
     * It calls toggleFieldVisibility.
     */
    private void toggleNewPasswordVisibility() {
        toggleFieldVisibility(newPasswordField, newPasswordVisibleField, newPasswordToggle);
    }

    @FXML
    /**
     * This is what allows the user to toggle the visibility of the confirmed new password specifically.
     * It calls toggleFieldVisibility.
     */
    private void toggleConfirmPasswordVisibility() {
        toggleFieldVisibility(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);
    }
}