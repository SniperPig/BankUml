package bank.Controllers;

import java.io.IOException;

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
            loginButton.setOnAction(e -> handleLogin());
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
     * 
     */
    private void handleLogin() {
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
