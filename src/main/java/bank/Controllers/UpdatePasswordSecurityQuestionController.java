package bank.Controllers;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UpdatePasswordSecurityQuestionController {

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
            switchScene(event, "/bank/Views/UpdatePasswordForm.fxml");
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Incorrect answer. Please try again.");
            alert.showAndWait();
        }
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
