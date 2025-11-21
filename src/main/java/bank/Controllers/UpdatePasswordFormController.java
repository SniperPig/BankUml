package bank.Controllers;

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
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class UpdatePasswordFormController {

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

    @FXML
    private void handleUpdatePassword() {
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
            alert.setContentText("Password updated!");
        }

        alert.showAndWait();
    }

    @FXML
    private void toggleNewPasswordVisibility() {
        toggleFieldVisibility(newPasswordField, newPasswordVisibleField, newPasswordToggle);
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        toggleFieldVisibility(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);
    }

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

    private SVGPath createEyeIcon(boolean open) {
        SVGPath path = new SVGPath();
        path.setContent(open
                ? "M1 12c2.5-4.5 6.5-7 11-7s8.5 2.5 11 7c-2.5 4.5-6.5 7-11 7S3.5 16.5 1 12zm11 4a4 4 0 1 0 0-8 4 4 0 0 0 0 8z"
                : "M2 3.5 3.5 2l18.5 18.5L20.5 22l-2.9-2.9c-1.4.6-3 1-4.6 1-4.5 0-8.5-2.5-11-7 1.1-1.9 2.5-3.4 4.1-4.6L2 3.5zm6 6L6.6 8c-1.2.9-2.3 2.1-3.2 3.6 2.1 3.5 5.1 5.4 8.7 5.4 1.2 0 2.3-.2 3.4-.6l-1.6-1.6a4 4 0 0 1-5-5zm3.7-5c4.5 0 8.5 2.5 11 7-.9 1.5-2 2.8-3.3 3.8l-1.4-1.4c1-.8 1.9-1.9 2.6-3.2-2.1-3.5-5.1-5.4-8.9-5.4-.8 0-1.5.1-2.2.2L8.8 4.3c1-.5 2.1-.8 3.9-.8z");
        path.setFill(Color.web("#444444"));
        return path;
    }

    @FXML
    private void handleBackToAccount(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("Account page not implemented yet.");
        alert.showAndWait();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

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
}
