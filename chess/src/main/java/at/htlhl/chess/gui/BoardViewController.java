package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Square;
import at.htlhl.chess.boardlogic.util.PieceUtil;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class BoardViewController implements Initializable {

    private static final int BOARD_SIZE = 8;
    private static final int INITIAL_SQUARE_SIZE = 60;
    private static final Color LIGHT_SQUARE_COLOR = Color.rgb(242, 226, 190);
    private static final Color DARK_SQUARE_COLOR = Color.rgb(176, 136, 104);
    private static final Color LAST_MOVE_HIGHLIGHT_COLOR = Color.rgb(255, 255, 0, 0.4);
    private static final Color KING_CHECK_COLOR = Color.rgb(255, 0, 0);
    public static final Color ARROW_COLOR = Color.rgb(110, 110, 110);
    private final Field field = new Field();
    @FXML
    private GridPane chessBoard;
    private DoubleBinding squareSizeBinding;
    private Pane arrowPane;
    private Square arrowStartSquare;
    private Square arrowEndSquare;
    private byte arrowPromotionPiece;
    private EngineConnector engineConnector;

    private static Rectangle getCheckHighlight() {
        Rectangle checkHighlight = new Rectangle(INITIAL_SQUARE_SIZE, INITIAL_SQUARE_SIZE);

        // Create a radial gradient: center is bright red, edges fade to transparent
        RadialGradient gradient = new RadialGradient(
                0,           // focus angle
                0.1,         // focus distance
                0.5,         // center X
                0.5,         // center Y
                0.7,         // radius of the highlight
                true,        // proportional (coordinates are relative to the shape's bounds)
                CycleMethod.NO_CYCLE,
                new Stop(0.0, KING_CHECK_COLOR), // Center: bright red
                new Stop(1.0, Color.TRANSPARENT) // Edges: fully transparent
        );
        checkHighlight.setFill(gradient);
        return checkHighlight;
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

        arrowPane = new Pane();
        arrowPane.setMouseTransparent(true);
        chessBoard.add(arrowPane, 0, 0);
        GridPane.setColumnSpan(arrowPane, 8);
        GridPane.setRowSpan(arrowPane, 8);

        Platform.runLater(() -> {
            setUpScalability();
            engineConnector = new EngineConnector(field, this::drawArrow);
            drawPieces(null, null);
        });
        setUpInteractions();
    }

    /**
     * Configures scalability for the chess board, ensuring it resizes dynamically with the window.
     */
    private void setUpScalability() {
        Pane boardPane = (Pane) chessBoard.getParent();
        squareSizeBinding = (DoubleBinding) Bindings.min(
                boardPane.widthProperty().divide(BOARD_SIZE),
                boardPane.heightProperty().divide(BOARD_SIZE)
        );

        chessBoard.setMinSize(BOARD_SIZE * INITIAL_SQUARE_SIZE, BOARD_SIZE * INITIAL_SQUARE_SIZE);
        chessBoard.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane squarePane = getSquarePane(chessBoard, col, row);
                Rectangle square = (Rectangle) squarePane.getChildren().getFirst();

                // Bind rectangle dimensions to the calculated size
                square.widthProperty().bind(squareSizeBinding);
                square.heightProperty().bind(squareSizeBinding);
             }
        }
    }

    /**
     * Draws an arrow from square s1 to square s2 on the chess board.
     * Stores the squares for later redrawing when the board resizes.
     *
     * @param move The move to draw
     */
    public void drawArrow(Move move) {
        arrowStartSquare = (move != null) ? move.getStartingSquare() : null;
        arrowEndSquare = (move != null) ? move.getTargetSquare() : null;
        arrowPromotionPiece = (move != null) ? move.getPromotionPiece() : PieceUtil.EMPTY;
        updateArrow();
    }

    /**
     * Updates the arrow based on the current square size and stored squares.
     * This method creates and binds arrow components to ensure they update with the board.
     */
    private void updateArrow() {
        arrowPane.getChildren().clear();
        if (arrowStartSquare == null || arrowEndSquare == null) return;

        Line arrowBody = new Line();
        Polygon arrowHead = new Polygon();

        // Set up basic bindings for start and end points
        DoubleBinding startX = squareSizeBinding.multiply(arrowStartSquare.x() + 0.5);
        DoubleBinding startY = squareSizeBinding.multiply(arrowStartSquare.y() + 0.5);
        DoubleBinding endX = squareSizeBinding.multiply(arrowEndSquare.x() + 0.5);
        DoubleBinding endY = squareSizeBinding.multiply(arrowEndSquare.y() + 0.5);

        // Calculate direction vector and derived values as bindings
        DoubleBinding dx = endX.subtract(startX);
        DoubleBinding dy = endY.subtract(startY);
        DoubleBinding length = Bindings.createDoubleBinding(() -> Math.sqrt(dx.get() * dx.get() + dy.get() * dy.get()), dx, dy);
        DoubleBinding dirX = Bindings.createDoubleBinding(() -> length.get() > 0 ? dx.get() / length.get() : 0, dx, length);
        DoubleBinding dirY = Bindings.createDoubleBinding(() -> length.get() > 0 ? dy.get() / length.get() : 0, dy, length);
        DoubleBinding perpX = Bindings.createDoubleBinding(() -> -dirY.get(), dirY);
        DoubleBinding perpY = Bindings.createDoubleBinding(() -> dirX.get(), dirX);

        // Arrow dimensions
        DoubleBinding thickness = squareSizeBinding.multiply(0.1);
        DoubleBinding arrowHeadLength = squareSizeBinding.multiply(0.3);
        DoubleBinding arrowHeadWidth = squareSizeBinding.multiply(0.2);
        DoubleBinding bodyEndX = Bindings.createDoubleBinding(() -> endX.get() - dirX.get() * arrowHeadLength.get(), endX, dirX, arrowHeadLength);
        DoubleBinding bodyEndY = Bindings.createDoubleBinding(() -> endY.get() - dirY.get() * arrowHeadLength.get(), endY, dirY, arrowHeadLength);

        // Configure arrow body
        arrowBody.startXProperty().bind(startX);
        arrowBody.startYProperty().bind(startY);
        arrowBody.endXProperty().bind(bodyEndX);
        arrowBody.endYProperty().bind(bodyEndY);
        arrowBody.setStroke(ARROW_COLOR);
        arrowBody.strokeWidthProperty().bind(thickness);

        // Set up arrow head points updater
        InvalidationListener pointsUpdater = obs -> {
            arrowHead.getPoints().clear();
            arrowHead.getPoints().addAll(
                    endX.get(), endY.get(),
                    bodyEndX.get() + perpX.get() * arrowHeadWidth.get(), bodyEndY.get() + perpY.get() * arrowHeadWidth.get(),
                    bodyEndX.get() - perpX.get() * arrowHeadWidth.get(), bodyEndY.get() - perpY.get() * arrowHeadWidth.get()
            );
        };

        // Register listeners and initialize
        endX.addListener(pointsUpdater);
        endY.addListener(pointsUpdater);
        bodyEndX.addListener(pointsUpdater);
        bodyEndY.addListener(pointsUpdater);
        perpX.addListener(pointsUpdater);
        perpY.addListener(pointsUpdater);
        arrowHeadWidth.addListener(pointsUpdater);
        pointsUpdater.invalidated(null);

        arrowHead.setFill(ARROW_COLOR);

        arrowPane.getChildren().addAll(arrowBody, arrowHead);

        if (PieceUtil.isEmpty(arrowPromotionPiece) == false)
            drawPiece(arrowPromotionPiece, arrowEndSquare, 40);
    }

    /**
     * Initializes user interaction handling for the chess board.
     * Sets up a {@link ChessBoardInteractionHandler} with the current square size to manage clicks,
     * drag-and-drop, and other interactions, updating the board display as needed.
     */
    private void setUpInteractions() {
        double currentSquareSize = (squareSizeBinding != null && squareSizeBinding.isValid())
                ? squareSizeBinding.get()
                : INITIAL_SQUARE_SIZE;

        ChessBoardInteractionHandler interactionHandler = new ChessBoardInteractionHandler(
                chessBoard,
                field,
                currentSquareSize,
                this::drawPieces // Update callback
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

                Rectangle square = new Rectangle(INITIAL_SQUARE_SIZE, INITIAL_SQUARE_SIZE);
                square.setFill((row + col) % 2 == 0 ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR);

                squarePane.getChildren().addFirst(square);
                squarePane.setUserData(new Square(col, row));

                chessBoard.add(squarePane, col, row);
            }
        }
    }

    /**
     * Draws chess pieces on the board based on the current state of the {@link Field}.
     * Removes any existing pieces and adds new ones where applicable. Highlights the last move
     * with a yellow background and the king in check with a red radial gradient background.
     *
     * @param moveToHighlight    The move to highlight with a yellow background (source and target squares).
     * @param kingCheckHighlight The square containing the king in check, to highlight with a red radial gradient.
     */
    private void drawPieces(Move moveToHighlight, Square kingCheckHighlight) {
        for (int row = 0; row < field.getBoard().length; row++) {
            for (int col = 0; col < field.getBoard()[row].length; col++) {
                StackPane square = getSquarePane(chessBoard, col, row);
                var piece = field.getBoard()[row][col];

                if (square == null) continue;

                // Remove everything except for the color
                while (square.getChildren().size() > 1)
                    square.getChildren().remove(1);

                // Add highlight for last move
                if (moveToHighlight != null
                        && ((col == moveToHighlight.getStartingSquare().x() && row == moveToHighlight.getStartingSquare().y())
                        || (col == moveToHighlight.getTargetSquare().x() && row == moveToHighlight.getTargetSquare().y()))) {
                    Rectangle highlight = new Rectangle(INITIAL_SQUARE_SIZE, INITIAL_SQUARE_SIZE);
                    highlight.setFill(LAST_MOVE_HIGHLIGHT_COLOR);
                    highlight.widthProperty().bind(((Rectangle) square.getChildren().getFirst()).widthProperty());
                    highlight.heightProperty().bind(((Rectangle) square.getChildren().getFirst()).heightProperty());
                    square.getChildren().add(1, highlight); // Add highlight just above the base square
                }

                // Add highlight for king in check
                if (kingCheckHighlight != null
                        && col == kingCheckHighlight.x() && row == kingCheckHighlight.y()) {
                    Rectangle checkHighlight = getCheckHighlight();

                    checkHighlight.widthProperty().bind(((Rectangle) square.getChildren().getFirst()).widthProperty());
                    checkHighlight.heightProperty().bind(((Rectangle) square.getChildren().getFirst()).heightProperty());
                    // Add the check highlight just above the last move highlight (or base square if no last move highlight)
                    // This could happen if the last move highlight isn't actually used as a last move highlight
                    square.getChildren().add(square.getChildren().size() > 1 ? 2 : 1, checkHighlight);
                }

                // Add piece if present
                if (!PieceUtil.isEmpty(piece)) {
                    drawPiece(piece, new Square(col, row), 100);
                }
            }
        }
        engineConnector.stopCurrentExecutions();
        engineConnector.getEngine().setField(field);
        engineConnector.drawBestMove();
    }

    private void drawPiece(byte piece, Square square, int opacity) {
        var stackpane = getSquarePane(chessBoard, square.x(), square.y());
        Image img = PieceImageUtil.getImage(piece);
        ImageView imageView = new ImageView(img);
        imageView.setOpacity(((double) opacity) / 100);

        // Bind image size to square size
        imageView.fitWidthProperty().bind(((Rectangle) stackpane.getChildren().getFirst()).widthProperty());
        imageView.fitHeightProperty().bind(((Rectangle) stackpane.getChildren().getFirst()).heightProperty());

        stackpane.getChildren().add(imageView);
    }
}