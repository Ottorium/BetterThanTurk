package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Square;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.function.Consumer;

/**
 * Handles user interactions with the chess board, including clicking and drag-and-drop functionality.
 * Manages piece movement, square selection, and highlighting of legal moves.
 */
public class ChessBoardInteractionHandler {
    /** The scale factor applied to pieces during drag operations. For some reason, this does not work on some linux distros*/
    private static final double DRAG_SCALE = 1.1;

    /** The color used to highlight legal move targets on the board. */
    private static final Color HIGHLIGHT_COLOR = Color.rgb(100, 100, 100, 0.5);

    /** The {@link GridPane} representing the chess board UI. */
    private final GridPane chessBoard;

    /** The logical representation of the chess board and its state as a {@link Field}. */
    private final Field field;

    /** The size of each square on the board in pixels. */
    private final int squareSize;

    /** Callback function to update the board UI after a move is made. */
    private final Consumer<Void> onBoardUpdate;

    /** The currently selected square, or null if no square is selected. */
    private Square selectedSquare = null;

    /** List of squares highlighted as legal move targets, or null if no highlights are active. */
    private List<Square> highlightedSquares = null;

    /**
     * Constructs a new interaction handler for the chess board.
     *
     * @param chessBoard    The {@link GridPane} representing the chess board UI.
     * @param field         The logical {@link Field} managing the board state.
     * @param squareSize    The size of each square in pixels.
     * @param onBoardUpdate Callback to refresh the board UI after a move.
     */
    public ChessBoardInteractionHandler(GridPane chessBoard, Field field, int squareSize, Consumer<Void> onBoardUpdate) {
        this.chessBoard = chessBoard;
        this.field = field;
        this.squareSize = squareSize;
        this.onBoardUpdate = onBoardUpdate;
    }

    /**
     * Triggers the board update callback to refresh the UI.
     */
    private void updateBoard() {
        onBoardUpdate.accept(null);
    }

    /**
     * Sets up all user interaction handlers for the chess board, including clicks and drag-and-drop.
     */
    public void setupInteractions() {
        setupClickHandlers();
        setupDragAndDrop();
    }

    /**
     * Configures click event handlers for all squares on the board.
     */
    private void setupClickHandlers() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof StackPane square) {
                square.setOnMouseClicked(event -> handleSquareClick(square));
            }
        }
    }

    /**
     * Handles a mouse click on a square, allowing piece selection or movement.
     *
     * @param square The clicked {@link StackPane} representing a board square.
     */
    private void handleSquareClick(StackPane square) {
        Square clickedSquare = (Square) square.getUserData();

        if (selectedSquare == null && hasPiece(square)) {
            selectSquare(clickedSquare);
        } else if (selectedSquare != null && isHighlightedSquare(clickedSquare)) {
            field.move(new Move(selectedSquare, clickedSquare));
            clearSelection();
            updateBoard();
        } else {
            clearSelection();
            if (hasPiece(square)) {
                selectSquare(clickedSquare);
            }
        }
    }

    /**
     * Selects a square and highlights its possible legal moves.
     *
     * @param square The {@link Square} to select.
     */
    private void selectSquare(Square square) {
        selectedSquare = square;
        highlightPossibleMoves(square);
    }

    /**
     * Clears the current selection and removes all highlights from the board.
     */
    private void clearSelection() {
        clearHighlights();
        selectedSquare = null;
    }

    /**
     * Highlights all legal move targets for the piece on the given square.
     *
     * @param square The {@link Square} containing the piece to evaluate.
     */
    private void highlightPossibleMoves(Square square) {
        clearHighlights();
        highlightedSquares = field.getLegalTargetsForSquare(square);
        highlightedSquares.forEach(this::highlightSquare);
    }

    /**
     * Adds a visual highlight to the specified square.
     *
     * @param square The {@link Square} to highlight.
     */
    private void highlightSquare(Square square) {
        StackPane squarePane = BoardViewController.getSquarePane(chessBoard, square.x(), square.y());

        Circle highlight = new Circle(squareSize / 6.0);
        highlight.setFill(HIGHLIGHT_COLOR);
        highlight.setMouseTransparent(true);

        squarePane.getChildren().add(highlight);
    }

    /**
     * Removes all highlights from the board.
     */
    private void clearHighlights() {
        if (highlightedSquares != null) {
            highlightedSquares.forEach(square -> {
                StackPane squarePane = BoardViewController.getSquarePane(chessBoard, square.x(), square.y());
                if (squarePane.getChildren().size() > 1) {
                    squarePane.getChildren().removeLast();
                }
            });
            highlightedSquares = null;
        }
    }

    /**
     * Checks if the given square is among the highlighted legal move targets.
     *
     * @param square The {@link Square} to check.
     * @return true if the square is highlighted, false otherwise.
     */
    private boolean isHighlightedSquare(Square square) {
        return highlightedSquares != null && highlightedSquares.contains(square);
    }

    /**
     * Configures drag-and-drop event handlers for all squares on the board.
     */
    private void setupDragAndDrop() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof StackPane square) {
                setupDragHandlers(square);
                setupDropHandlers(square);
            }
        }
    }

    /**
     * Sets up drag initiation handlers for a square.
     *
     * @param square The {@link StackPane} to configure for dragging.
     */
    private void setupDragHandlers(StackPane square) {
        square.setOnDragDetected(event -> {
            if (!hasPiece(square)) return;

            selectSquare((Square) square.getUserData());

            ImageView piece = (ImageView) square.getChildren().get(1);
            Square sourceSquare = (Square) square.getUserData();
            Dragboard db = piece.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();
            content.putString(sourceSquare.x() + "," + sourceSquare.y());
            db.setContent(content);

            setupDragView(db, piece);

            event.consume();
        });
    }

    /**
     * Configures the visual representation of a piece during a drag operation.
     *
     * @param db    The {@link Dragboard} managing the drag operation.
     * @param piece The {@link ImageView} of the piece being dragged.
     */
    private void setupDragView(Dragboard db, ImageView piece) {
        double dragSize = squareSize * DRAG_SCALE;
        ImageView dragView = new ImageView(piece.getImage());
        dragView.setFitWidth(dragSize);
        dragView.setFitHeight(dragSize);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        db.setDragView(dragView.snapshot(params, null), dragSize / 2, dragSize / 2);
    }

    /**
     * Sets up drop target handlers for a square.
     *
     * @param square The {@link StackPane} to configure as a drop target.
     */
    private void setupDropHandlers(StackPane square) {
        square.setOnDragOver(event -> {
            if (event.getGestureSource() != square && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        square.setOnDragDropped(event -> handleDrop(event, square));
    }

    /**
     * Handles the drop event when a piece is dragged and dropped onto a square.
     *
     * @param event  The {@link javafx.scene.input.DragEvent} containing drop details.
     * @param square The {@link StackPane} where the piece was dropped.
     */
    private void handleDrop(javafx.scene.input.DragEvent event, StackPane square) {
        Dragboard db = event.getDragboard();

        clearSelection();

        boolean success = false;

        if (db.hasString()) {
            String[] coords = db.getString().split(",");
            Square sourceSquare = new Square(
                    Integer.parseInt(coords[0]),
                    Integer.parseInt(coords[1])
            );
            Square targetSquare = (Square) square.getUserData();

            if (!sourceSquare.equals(targetSquare)) {
                Move move = new Move(sourceSquare, targetSquare);
                success = field.move(move);
                if (success) updateBoard();
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Checks if the given square contains a chess piece.
     *
     * @param square The {@link StackPane} to check.
     * @return true if the square has a piece, false otherwise.
     */
    private boolean hasPiece(StackPane square) {
        return square.getChildren().size() > 1;
    }
}