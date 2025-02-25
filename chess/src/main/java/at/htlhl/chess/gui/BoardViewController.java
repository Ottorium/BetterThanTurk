package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Square;
import at.htlhl.chess.boardlogic.util.PieceUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    @FXML
    private GridPane chessBoard;

    private final Field field = new Field();

    /**
     * Initializes the chess board view when the controller is loaded.
     * Creates an empty chess board, sets the initial position using FEN, draws the pieces,
     * and sets up user interaction handling.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createEmptyChessBoard();
        field.resetBoard();
        drawPieces();

        ChessBoardInteractionHandler interactionHandler = new ChessBoardInteractionHandler(
                chessBoard,
                field,
                SQUARE_SIZE,
                unused -> drawPieces() // Update callback
        );
        interactionHandler.setupInteractions();
    }

    /**
     * Creates an empty 8x8 chess board with alternating light and dark squares.
     * Each square is represented by a {@link StackPane} containing a colored {@link Rectangle}.
     */
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

    /**
     * Draws chess pieces on the board based on the current state of the {@link Field}.
     * Removes any existing pieces and adds new ones where applicable.
     */
    private void drawPieces() {
        for (int row = 0; row < field.getBoard().length; row++) {
            for (int col = 0; col < field.getBoard()[row].length; col++) {
                StackPane square = getSquarePane(chessBoard, col, row);
                var piece = field.getBoard()[row][col];

                if (square == null) continue;

                // Remove existing piece if present
                if (square.getChildren().size() > 1)
                    square.getChildren().remove(1);

                if (!PieceUtil.isEmpty(piece)) {
                    Image img = PieceImageUtil.getImage(piece);
                    ImageView imageView = new ImageView(img);
                    imageView.setFitWidth(SQUARE_SIZE);
                    imageView.setFitHeight(SQUARE_SIZE);
                    square.getChildren().add(imageView);
                }
            }
        }
    }

    /**
     * Retrieves the {@link StackPane} representing a specific square on the chess board.
     *
     * @param board The {@link GridPane} containing the chess board squares.
     * @param col   The column index of the square (0-based).
     * @param row   The row index of the square (0-based).
     * @return The {@link StackPane} at the specified coordinates.
     * @throws RuntimeException if no square is found at the specified coordinates.
     */
    public static StackPane getSquarePane(GridPane board, int col, int row) {
        return (StackPane) board.getChildren().stream()
                .filter(node -> GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Could not find square at coordinates %d %d", col, row)));
    }
}