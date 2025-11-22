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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Label;

/**
 * This class is the first of two steps to update a user's password.
 * It validates that the active user is the right one by asking a security question
 * and validating the answer. 
 */
public class UpdatePasswordSecurityQuestionController {

    @FXML private TextField answerField;
    @FXML private Label securityQuestion;

    /**
     * Edit the security question based on the current user
     */
    @FXML
    public void initialize() {
        // Determine which type of user it is
        Employee currentEmployee = SessionManager.getCurrentEmployee();
        Customer currentCustomer = SessionManager.getCurrentCustomer();

        if (currentCustomer != null) {
            // If it's a customer, use their security question
            securityQuestion.setText(currentCustomer.getSecurityQuestion());
        } else {
            // If it's an employee, use their security question
            securityQuestion.setText(currentEmployee.getSecurityQuestion());
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
     * AdministratorDashboard, or CustomerDashboard) when they click "Back to Home"
     * 
     * @param event the event created when a user clicks "Back to Home" hyperlink
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

    @FXML 
    /**
     * Verify that the answer provided by the user is the correct one.
     * If it is, take the user to the next page of the update password process.
     * If not, show an error. 
     */
    private void handleConfirmAnswer(ActionEvent event) {
        if (answerField == null) {
            return;
        }

        // Answer provided by user
        String provided = answerField.getText();
        // Answer that is stored in DB
        String expected;

        // Determine what type of user it is
        Employee currentEmployee = SessionManager.getCurrentEmployee();
        Customer currentCustomer = SessionManager.getCurrentCustomer();

        // And based on user, retrieve security answer
        if (currentCustomer != null) {
            // If it's a customer
            expected = currentCustomer.getSecurityAnswer();

        } else {
            // If it's an employee 
            expected = currentEmployee.getSecurityAnswer();
        }

        // If the answers match, go to the next page of the process
        if (provided != null && provided.trim().equalsIgnoreCase(expected)) {
            switchScene(event, "/bank/Views/UpdatePasswordForm.fxml");
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Incorrect answer. Please try again.");
            alert.showAndWait();
        }
    }
}