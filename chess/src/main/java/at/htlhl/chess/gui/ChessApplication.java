package at.htlhl.chess.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChessApplication extends Application {

    BoardViewController boardViewController;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChessApplication.class.getResource("board-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1600, 900);
        boardViewController = fxmlLoader.getController();
        stage.setTitle("Chess");
        stage.setScene(scene);
        stage.show();
    }
}