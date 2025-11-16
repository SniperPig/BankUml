package bank.Controllers;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ResetPasswordSecurityQuestionController {

    private static final String EXPECTED_ANSWER = "mittens";

    @FXML
    private TextField answerField;

    @FXML
    private void handleConfirmAnswer(ActionEvent event) {
        if (answerField == null) {
            return;
        }

        String provided = answerField.getText();
        if (provided != null && provided.trim().equalsIgnoreCase(EXPECTED_ANSWER)) {
            switchScene(event, "/bank/Views/ResetPasswordForm.fxml");
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Incorrect answer. Please try again.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

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
}
