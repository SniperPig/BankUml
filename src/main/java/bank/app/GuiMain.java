package bank.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GuiMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                GuiMain.class.getResource("/bank/Views/UpdatePasswordForm.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 700);

        scene.getStylesheets().add(
                GuiMain.class.getResource("/bank/css/CreateAccount.css").toExternalForm()
        );

        stage.setTitle("MyBankUML Create Account");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
