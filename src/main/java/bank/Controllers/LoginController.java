package bank.Controllers;

import java.io.IOException;
import bank.SessionManager;
import bank.Models.Employee;
import bank.Models.Customer;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

/**
 * This is the Controller associated with the LoginForm view.
 * It handles the login process, and can send the user to the "Reset Password" page
 * if they press "Forgot password?"
 */
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ToggleButton customerToggle;
    @FXML private ToggleButton employeeToggle;
    @FXML private ToggleGroup roleGroup;
    @FXML private Button loginButton;
    @FXML private Hyperlink forgotLink;

    // How many attempts left a user has to log in
    private int remainingAttempts = 3;

    /**
     * This function initializes the LoginForm by setting the default role to Customer
     * and to make sure clicks on buttons and hyperlinks lead to the appropriate function.
     */
    @FXML
    public void initialize() {
        // If the roleGroup & customerToggle actually exist, then set customerToggle to be default
        if (roleGroup != null && customerToggle != null) {
            roleGroup.selectToggle(customerToggle);
        }

        // If the login button is pressed, use the handleLogin function
        if (loginButton != null) {
            loginButton.setOnAction(this::handleLogin);
        }

        // If the forgotLink is pressed, use the handleForgotPassword() function
        if (forgotLink != null) {
            forgotLink.setOnAction(this::handleForgotPassword);
        }
    }

    /**
     * This function allows the GUI to switch scenes
     * 
     * @param event the event that is created by clicking a button
     * @param resourcePath the path of the new scene we want to reach
     */
    private void switchScene(ActionEvent event, String resourcePath) {
        try {
            // Load the new screen we want to go to 
            Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
            // Get the current scene & stage (window)
            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load " + resourcePath, ex);
        }
    }

    /**
     * This method attempts to fetch either a Customer or Employee (depending on which role
     * is selected) from the database in order to validate credentials.
     * If the credentials are valid, the user is sent to the appropriate dashboard.
     * 
     * @param event the event created from the user pressing the "Login" button
     */
    private void handleLogin(ActionEvent event) {
        // First retrieve the input email and password    
        String username = usernameField.getText();
        String password = passwordField.getText();

        // This will be used for error messages
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);

        // Make sure both fields are not empty
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.setContentText("Please enter both username and password.");
            alert.showAndWait();
            return;
        }

        // Then check whether customer or employee is selected
        boolean isEmployee = employeeToggle.isSelected();
        
        try {
            // if it's an employee
            if (isEmployee) {
                Employee currentEmployee = Employee.fetchEmployeeByEmail(username);
                
                // if no employee retrieved
                if (currentEmployee == null) {
                    alert.setAlertType(Alert.AlertType.WARNING);
                    alert.setContentText("Account not found. Please try a different username.");
                    alert.showAndWait();
                    return;
                }
                boolean validCredentials = currentEmployee.isPasswordValid(password);  

                // if passwords don't match
                if (!validCredentials) {
                    remainingAttempts--;

                    // If 3 attempts have already been made, block the user from trying to log in again
                    if (remainingAttempts <= 0) {
                        loginButton.setDisable(true);
                        usernameField.setDisable(true);
                        passwordField.setDisable(true);
                        forgotLink.setDisable(true);
                        alert.setAlertType(Alert.AlertType.ERROR);
                        alert.setContentText("Too many attempts. Login locked for security.");
                        alert.showAndWait();
                        return;
                    } 
                    // otherwise, tell user how many attempts are left
                    else {
                        alert.setAlertType(Alert.AlertType.WARNING);
                        alert.setContentText("Invalid password. You have " 
                            + remainingAttempts + " attempt(s) left.\nPlease try again.");
                        alert.showAndWait();
                        return;
                    }
                } 

                // if credentials are valid, then
                // 1) reset # of remaining attempts
                remainingAttempts = 3;

                // 2) save the data in SessionManager 
                SessionManager.setCurrentEmployee(currentEmployee);

                // 3) Go to the appropriate dashboard (teller dashboard in this case)
                switchScene(event, "/bank/Views/TellerDashboard.fxml");
            } 
            // if it's a customer, repeat process but with Customer object
            else {
                Customer currentCustomer = Customer.fetchCustomerByEmail(username);

                if (currentCustomer == null) {
                    alert.setAlertType(Alert.AlertType.WARNING);
                    alert.setContentText("Account not found. Please try a different username.");
                    alert.showAndWait();
                    return;
                }     
                boolean validCredentials = currentCustomer.isPasswordValid(password);

                // if passwords don't match
                if (!validCredentials) {
                    remainingAttempts--;

                    // If 3 attempts have already been made, block the user from trying to log in again
                    if (remainingAttempts <= 0) {
                        loginButton.setDisable(true);
                        usernameField.setDisable(true);
                        passwordField.setDisable(true);
                        forgotLink.setDisable(true);
                        alert.setAlertType(Alert.AlertType.ERROR);
                        alert.setContentText("Too many attempts. Login locked for security.");
                        alert.showAndWait();
                        return;
                    } 
                    // otherwise, tell user how many attempts are left
                    else {
                        alert.setAlertType(Alert.AlertType.WARNING);
                        alert.setContentText("Invalid password. You have " 
                            + remainingAttempts + " attempt(s) left.\nPlease try again.");
                        alert.showAndWait();
                        return;
                    }
                } 

                remainingAttempts = 3;
                SessionManager.setCurrentCustomer(currentCustomer);
                switchScene(event, "/bank/Views/CustomerDashboard.fxml");
            }    
        } catch (SQLException e) {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText("Database error: " + e.getMessage());
        }
    }

    /**
     * Handles clicks on the "forgot password" link. Placeholder for navigation to recovery flow.
     * 
     * @param event the event created when the user clicks "Forgot password?"
     */
    private void handleForgotPassword(ActionEvent event) {
        switchScene(event, "/bank/Views/EmailConfirmation.fxml");
    }
}