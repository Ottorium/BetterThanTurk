package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Square;
import at.htlhl.chess.boardlogic.util.PieceUtil;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.function.BiConsumer;

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
    private final double squareSize;

    /** Callback function to update the board UI after a move is made. */
    private final BiConsumer<Move, Square> onBoardUpdate;

    /** The currently selected square, or null if no square is selected. */
    private Square selectedSquare = null;

    /** List of squares highlighted as legal move targets, or null if no highlights are active. */
    private List<Square> highlightedSquares = null;

    private boolean autoQueen = true;

    /**
     * Constructs a new interaction handler for the chess board.
     *
     * @param chessBoard    The {@link GridPane} representing the chess board UI.
     * @param field         The logical {@link Field} managing the board state.
     * @param squareSize    The size of each square in pixels.
     * @param onBoardUpdate Callback to refresh the board UI after a move.
     */
    public ChessBoardInteractionHandler(GridPane chessBoard, Field field, double squareSize, BiConsumer<Move, Square> onBoardUpdate) {
        this.chessBoard = chessBoard;
        this.field = field;
        this.squareSize = squareSize;
        this.onBoardUpdate = onBoardUpdate;
    }

    /**
     * Triggers the board update callback to refresh the UI.
     */
    private void updateBoard(Move move, Square kingCheckHighlight) {
        onBoardUpdate.accept(move, kingCheckHighlight);
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
                square.setOnMouseClicked(event -> {
                    autoQueen = event.getButton() == MouseButton.PRIMARY;
                    handleSquareClick(square);
                });
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
            Move move = new Move(selectedSquare, clickedSquare);
            move.setPromotionPiece(getPromotionPiece(selectedSquare, clickedSquare));
            boolean success = field.move(move);
            clearSelection();
            if (success) updateBoard(move, field.getSquareOfCheck());
        } else {
            clearSelection();
            if (hasPiece(square)) {
                selectSquare(clickedSquare);
            }
        }
    }

    /**
     * Determines if a pawn is being promoted and prompts the user to choose a piece via an image-based dialog.
     *
     * @param startSquare  The starting {@link Square} of the move.
     * @param targetSquare The target {@link Square} of the move.
     * @return The byte value of the chosen promotion piece, or PieceUtil.EMPTY if the move is not a promotion.
     */
    private byte getPromotionPiece(Square startSquare, Square targetSquare) {
        if (PieceUtil.isPawn(field.getPieceBySquare(startSquare)) == false)
            return PieceUtil.EMPTY;

        if (targetSquare.y() != (field.isBlackTurn() ? 7 : 0))
            return PieceUtil.EMPTY;

        if (autoQueen)
            return field.isBlackTurn() ? PieceUtil.BLACK_QUEEN : PieceUtil.WHITE_QUEEN;


        // Create a custom dialog for promotion selection
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Pawn Promotion");

        HBox hbox = new HBox(0); // Spacing between images
        hbox.setStyle("-fx-background-color: #ffffff; -fx-padding: 0; -fx-alignment: center;");

        ImageView queenView = new ImageView(field.isBlackTurn() ? PieceImageUtil.BLACK_QUEEN_IMAGE : PieceImageUtil.WHITE_QUEEN_IMAGE);
        ImageView rookView = new ImageView(field.isBlackTurn() ? PieceImageUtil.BLACK_ROOK_IMAGE : PieceImageUtil.WHITE_ROOK_IMAGE);
        ImageView bishopView = new ImageView(field.isBlackTurn() ? PieceImageUtil.BLACK_BISHOP_IMAGE : PieceImageUtil.WHITE_BISHOP_IMAGE);
        ImageView knightView = new ImageView(field.isBlackTurn() ? PieceImageUtil.BLACK_KNIGHT_IMAGE : PieceImageUtil.WHITE_KNIGHT_IMAGE);

        double imageSize = squareSize;
        for (ImageView view : List.of(queenView, rookView, bishopView, knightView)) {
            view.setFitWidth(imageSize);
            view.setFitHeight(imageSize);
            view.setPreserveRatio(true);
        }

        final byte[] chosenPiece = {field.isBlackTurn() ? PieceUtil.BLACK_QUEEN : PieceUtil.WHITE_QUEEN};

        queenView.setOnMouseClicked(event -> {
            chosenPiece[0] = field.isBlackTurn() ? PieceUtil.BLACK_QUEEN : PieceUtil.WHITE_QUEEN;
            dialog.close();
        });
        rookView.setOnMouseClicked(event -> {
            chosenPiece[0] = field.isBlackTurn() ? PieceUtil.BLACK_ROOK : PieceUtil.WHITE_ROOK;
            dialog.close();
        });
        bishopView.setOnMouseClicked(event -> {
            chosenPiece[0] = field.isBlackTurn() ? PieceUtil.BLACK_BISHOP : PieceUtil.WHITE_BISHOP;
            dialog.close();
        });
        knightView.setOnMouseClicked(event -> {
            chosenPiece[0] = field.isBlackTurn() ? PieceUtil.BLACK_KNIGHT : PieceUtil.WHITE_KNIGHT;
            dialog.close();
        });

        hbox.getChildren().addAll(queenView, rookView, bishopView, knightView);

        Scene scene = new Scene(hbox);
        dialog.setScene(scene);
        dialog.showAndWait();

        return chosenPiece[0];
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
        Rectangle squareRect = (Rectangle) squarePane.getChildren().getFirst();

        Circle highlight = new Circle();
        highlight.radiusProperty().bind(squareRect.widthProperty().divide(6.0));
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

            autoQueen = event.getButton() == MouseButton.PRIMARY;

            selectSquare((Square) square.getUserData());

            ImageView piece = (ImageView) square.getChildren().getLast();
            Square sourceSquare = (Square) square.getUserData();
            Dragboard db = piece.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();
            content.putString(sourceSquare.x() + "," + sourceSquare.y());
            db.setContent(content);

            setupDragView(db, piece, square);

            event.consume();
        });
    }

    /**
     * Configures the visual representation of a piece during a drag operation.
     *
     * @param db    The {@link Dragboard} managing the drag operation.
     * @param piece The {@link ImageView} of the piece being dragged.
     */
    private void setupDragView(Dragboard db, ImageView piece, StackPane square) {
        Rectangle squareRect = (Rectangle) square.getChildren().getFirst();

        ImageView dragView = new ImageView(piece.getImage());
        dragView.fitWidthProperty().bind(squareRect.widthProperty().multiply(DRAG_SCALE));
        dragView.fitHeightProperty().bind(squareRect.heightProperty().multiply(DRAG_SCALE));

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        // Use current value for the snapshot since bindings don't work directly with snapshot
        double dragWidth = squareRect.getWidth() * DRAG_SCALE;

        db.setDragView(dragView.snapshot(params, null), dragWidth / 2, dragWidth / 2);
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
                move.setPromotionPiece(getPromotionPiece(sourceSquare,targetSquare));
                success = field.move(move);
                if (success) updateBoard(move, field.getSquareOfCheck());
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