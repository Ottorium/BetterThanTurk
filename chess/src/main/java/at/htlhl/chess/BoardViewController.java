package at.htlhl.chess;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createEmptyChessBoard();
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

    private void drawPieces(Piece[][] board) {
        throw new UnsupportedOperationException("drawPieces not implemented");
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