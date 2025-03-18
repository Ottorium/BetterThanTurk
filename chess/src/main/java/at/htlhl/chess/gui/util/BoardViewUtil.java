package at.htlhl.chess.gui.util;

import javafx.scene.control.Alert;

public class BoardViewUtil {

    public BoardViewUtil (){

    }

    /**
     * Displays an error alert with the specified header and content text.
     * @param headerText the header text for the alert
     * @param contentText the content text for the alert
     */
    public void alertProblem(String headerText, String contentText){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setGraphic(null);
        alert.showAndWait();
    }
}
