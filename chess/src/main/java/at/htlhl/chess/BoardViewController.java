package at.htlhl.chess;

import at.htlhl.chess.util.PieceUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class BoardViewController implements Initializable {
    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_SIZE = 60;
    private static final Color LIGHT_SQUARE_COLOR = Color.rgb(242, 226, 190);
    private static final Color DARK_SQUARE_COLOR = Color.rgb(176, 136, 104);
    private static final String INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @FXML
    private GridPane chessBoard;

    private final Field field = new Field();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createEmptyChessBoard();
        field.trySetFEN(INITIAL_FEN);
        drawPieces();

        ChessBoardInteractionHandler interactionHandler = new ChessBoardInteractionHandler(
                chessBoard,
                field,
                SQUARE_SIZE,
                unused -> drawPieces() // Update callback
        );
        interactionHandler.setupInteractions();
    }

    private void createEmptyChessBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {

                StackPane squarePane = new StackPane();

                Rectangle square = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                square.setFill((row + col) % 2 == 0 ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR);

                squarePane.getChildren().add(square);
                squarePane.setUserData(new Square(col, row));

                chessBoard.add(squarePane, col, row);
            }
        }
    }


    private void drawPieces() {
        for (int row = 0; row < field.getBoard().length; row++) {
            for (int col = 0; col < field.getBoard()[row].length; col++) {

                StackPane square = (StackPane) getNodeFromGridPane(chessBoard, col, row);
                var piece = field.getBoard()[row][col];

                if (square == null) continue;

                // remove existing piece if there
                if (square.getChildren().size() > 1)
                    square.getChildren().remove(1);

                if (!PieceUtil.isEmpty(piece)) {
                    Image img = PieceUtil.getImage(piece);
                    ImageView imageView = new ImageView(img);
                    imageView.setFitWidth(SQUARE_SIZE);
                    imageView.setFitHeight(SQUARE_SIZE);
                    square.getChildren().add(imageView);
                }
            }
        }
    }

    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        throw new RuntimeException("Could not find Node at position " + col + ", " + row);
    }
}