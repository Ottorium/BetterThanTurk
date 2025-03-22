package at.htlhl.chess.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

public class ChessApplication extends Application {

    public static final String CONFIG_DIRECTORY = System.getProperty("user.home") + "/.itp3b-chess";
    public static final String CONFIG_FILENAME = "app.properties";
    public static final String CONFIG_FILEPATH = CONFIG_DIRECTORY + "/" + CONFIG_FILENAME;

    public static Properties prop;
    /*
        Current existing properties:
        stockfish_path
     */

    BoardViewController boardViewController;

    public static void main(String[] args) {
        // initializing config
        createConfigDirectory();
        initProperties();

        // look for missing properties
        requestProperties();

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

    @Override
    public void stop() {
        boardViewController.shutdown();
        saveProperties();
    }

    private static void initProperties() {
        prop = new Properties();
        File configFile = new File(CONFIG_FILEPATH);
        InputStream in = null;
        try {
            in = new FileInputStream(configFile);
        } catch (FileNotFoundException e) {
            System.err.println(configFile + " not found");
        }
        if (in != null) {
            try {
                prop.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves properties to the file
     */
    public static void saveProperties() {
        try {
            prop.store(new FileOutputStream(CONFIG_FILEPATH), "This is the properties file");
        } catch (IOException e) {
            System.err.println("Could not save properties to " + CONFIG_FILEPATH);
        }
    }

    private static void requestProperties() {
        // adding stockfish
        if (prop.stringPropertyNames().contains("stockfish_path") == false) {
            getStockfishPath();
        }
        // other properties
    }

    private static void getStockfishPath() {
        System.out.print("Please enter stockfish_path: ");
        Scanner scanner = new Scanner(System.in);
        String stockfishPath = scanner.nextLine();
        prop.setProperty("stockfish_path", stockfishPath);
    }

    private static void createConfigDirectory(){
        File configDir = new File(CONFIG_DIRECTORY);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }
}