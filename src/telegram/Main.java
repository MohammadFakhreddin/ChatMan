package telegram;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import telegram.controllers.LoginPageController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("views/LoginPageView.fxml"));
        Parent root = loader.load();
//        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("ChatMan");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        LoginPageController controller = loader.getController();
        controller.setStage(primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
