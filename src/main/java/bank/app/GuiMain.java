package bank.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GuiMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                GuiMain.class.getResource("/bank/views/LoginForm.fxml")
        );

        Scene scene = new Scene(loader.load(), 800, 700);

        scene.getStylesheets().add(
                GuiMain.class.getResource("/bank/css/login.css").toExternalForm()
        );

        stage.setTitle("MyBankUML Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
