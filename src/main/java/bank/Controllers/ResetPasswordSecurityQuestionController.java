package bank.Controllers;

import java.io.IOException;
import bank.Models.Customer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ResetPasswordSecurityQuestionController {
    private String emailAddress;
    private Customer customer; 

    @FXML
    private TextField answerField;

    @FXML
    private Label emailContextLabel;

    @FXML
    private Label securityQuestionLabel;

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        if (emailContextLabel != null) {
            emailContextLabel.setText("");
        }
    }

    /**
     * Sets the email address to be displayed in the context label.
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        updateEmailContext();
    }

    /**
     * Sets the customer whose security question is to be displayed.
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
        updateEmailContext();
         if (securityQuestionLabel != null && customer.getSecurityQuestion() != null) {
            securityQuestionLabel.setText(customer.getSecurityQuestion());
        }
    }
    
    /**
     * Updates the email context label based on the current email address or customer.
     */
    private void updateEmailContext() {
        if (emailContextLabel == null) {
            return;
        }

        String emailToDisplay = emailAddress;
        if ((emailToDisplay == null || emailToDisplay.isBlank()) && customer != null) {
            emailToDisplay = customer.getEmail();
        }

        if (emailToDisplay == null || emailToDisplay.isBlank()) {
            emailContextLabel.setText("");
        } else {
            emailContextLabel.setText("Resetting password for " + emailToDisplay);
        }
    }

    /**
     * Handles the "Confirm Answer" button action.
     */
    @FXML
    private void handleConfirmAnswer(ActionEvent event) {
        if (answerField == null) {
            return;
        }

        String provided = answerField.getText();
        if (customer == null) {
            showError("Unable to validate answer because no customer context is available.");
            return;
        }

        if (provided != null && provided.trim().equalsIgnoreCase(customer.getSecurityAnswer())) {
            goToResetForm(event);
        } else {
            showError("Incorrect answer. Please try again.");
        }
    }

    /**
     * Loads the reset password form 
     * Also passes the customer object to the ResetPasswordFormController .
     */
    private void goToResetForm(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/bank/Views/ResetPasswordForm.fxml"));
        Parent root = loader.load();

        ResetPasswordFormController controller = loader.getController();
        controller.setCustomer(customer);   // add this setter in the form controller

        Scene scene = ((Node) event.getSource()).getScene();
        Stage stage = (Stage) scene.getWindow();
        scene.setRoot(root);
        stage.sizeToScene();
    } catch (IOException ex) {
        showError("Unable to load reset form: " + ex.getMessage());
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
     * Handles the "Back to Email Entry" button action.
     */
    @FXML
    private void handleBackToEmailEntry(ActionEvent event) {
        switchScene(event, "/bank/Views/EmailConfirmation.fxml");
    }

    /**
     * Switches the scene to the specified FXML resource.
     */
    private void switchScene(ActionEvent event, String resourcePath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();
            currentScene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load " + resourcePath, ex);
        }
    }

    /**
     * Displays an error alert with the specified message.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
