package at.htlhl.chess.gui.util;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.engine.EvaluatedMove;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

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

    /**
     * Creates a HBox containing a move name, styled based on payer color
     * @param move to show
     * @return built HBox
     */
    public HBox buildMoveBox(Move move, Player playerThatMoved){
        // Create the HBox container
        HBox box = new HBox();
        Text moveText = new Text(move.toString());

        // Base styling for all move boxes
        box.setPadding(new Insets(5, 10, 5, 10)); // Consistent padding: top, right, bottom, left
        box.setStyle(
                "-fx-border-color: #b0b0b0;" + // Softer gray border
                        "-fx-border-width: 2;" +       // Thinner, elegant border
                        "-fx-border-radius: 4;" +      // Rounded corners for a modern look
                        "-fx-background-radius: 4;"    // Match background to border radius
        );
        box.setMinWidth(100); // Ensure consistent sizing (adjust as needed)
        box.setAlignment(Pos.CENTER_LEFT); // Align text neatly

        // Customize based on player
        if (playerThatMoved == Player.BLACK) {
            box.setStyle(
                    "-fx-background-color: #2c2c2c;" + // Dark charcoal, less harsh than pure black
                            "-fx-border-color: #b0b0b0;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 4;" +
                            "-fx-background-radius: 4;"
            );
            moveText.setFill(Color.WHITE);
        } else { // Assuming Player.WHITE or other players
            box.setStyle(
                    "-fx-background-color: #f5f5f5;" + // Light grayish-white for contrast
                            "-fx-border-color: #b0b0b0;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 4;" +
                            "-fx-background-radius: 4;"
            );
            moveText.setFill(Color.BLACK);
        }

        // Enhance text styling
        moveText.setFont(Font.font("Arial", FontWeight.BOLD, 15)); // Clean, readable font (adjust size as needed)

        // Add the text to the box
        box.getChildren().add(moveText);
        return box;
    }

    /**
     * Creates a HBox with move and evaluation
     * @param move to build from
     * @return built box
     */
    public HBox buildMoveBox(EvaluatedMove move, Player playerThatMoved){
        HBox box = buildMoveBox(move.move(), playerThatMoved);
        Text evaluation = new Text();

        // Enhance text styling
        evaluation.setFont(Font.font("Arial", FontWeight.BOLD, 15)); // Clean, readable font (adjust size as needed)
        evaluation.setText("\tEval: " + move.evaluation());
        if (playerThatMoved == Player.BLACK) {
            evaluation.setFill(Color.WHITE);
        } else {
            evaluation.setFill(Color.BLACK);
        }


        box.getChildren().add(evaluation);
        return box;
    }
}
