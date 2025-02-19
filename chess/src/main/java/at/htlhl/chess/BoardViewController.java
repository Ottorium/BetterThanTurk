package at.htlhl.chess;

import at.htlhl.chess.util.PieceUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class BoardViewController implements Initializable {

    @FXML
    private GridPane chessBoard;

    private static final int BOARD_SIZE = 8;
    private static final int INITIAL_SQUARE_SIZE = 60;

    private Field field;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createEmptyChessBoard();
        field = new Field();
        field.trySetFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        drawPieces();
    }

    private void createEmptyChessBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane squarePane = new StackPane();
                Rectangle square = new Rectangle(INITIAL_SQUARE_SIZE, INITIAL_SQUARE_SIZE);

                if ((row + col) % 2 == 0)
                    square.setFill(Color.rgb(242, 226, 190)); // Light beige/cream for light squares
                else
                    square.setFill(Color.rgb(176, 136, 104)); // Medium brown for dark squares

                squarePane.getChildren().add(square);
                chessBoard.add(squarePane, col, row);
            }
        }
    }

    private Square getChosenSquare() {
        throw new UnsupportedOperationException("getChosenSquare not implemented");
    }

    private void drawPieces() {
        for (int row = 0; row < field.getBoard().length; row++) {
            for (int col = 0; col < field.getBoard()[row].length; col++) {
                StackPane squarePane = (StackPane) getNodeFromGridPane(chessBoard, col, row);

                // Remove any existing piece
                if (squarePane != null) {
                    squarePane.getChildren().removeIf(node -> node instanceof javafx.scene.image.ImageView);
                } else
                    throw new RuntimeException("Board is in an invalid state: either board is too large or the GridPane is not fully initialized");

                Image pieceImage = PieceUtil.getImage(field.getBoard()[row][col]);

                if (pieceImage != null) {
                    javafx.scene.image.ImageView pieceView = new javafx.scene.image.ImageView(pieceImage);

                    pieceView.setFitWidth(INITIAL_SQUARE_SIZE - 10);
                    pieceView.setFitHeight(INITIAL_SQUARE_SIZE - 10);

                    // Preserve ratio and use better quality filtering
                    pieceView.setPreserveRatio(true);
                    pieceView.setSmooth(true);

                    squarePane.getChildren().add(pieceView);
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
        return null;
    }

    private void drawHighlights() {
        throw new UnsupportedOperationException("drawHighlights not implemented");
    }

    private void drawPossibleMoves() {
        throw new UnsupportedOperationException("drawPossibleMoves not implemented");
    }

    private void handleMove() {
        throw new UnsupportedOperationException("handleMove not implemented");
    }

    private void chooseSquare() {
        throw new UnsupportedOperationException("chooseSquare not implemented");
    }
}