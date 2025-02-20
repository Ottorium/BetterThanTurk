package at.htlhl.chess;

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

public class ChessBoardInteractionHandler {
    private static final double DRAG_SCALE = 1.1;
    private static final Color HIGHLIGHT_COLOR = Color.rgb(100, 100, 100, 0.5);

    private final GridPane chessBoard;
    private final Field field;
    private final int squareSize;
    private final Consumer<Void> onBoardUpdate;

    private Square selectedSquare = null;
    private List<Square> highlightedSquares = null;

    public ChessBoardInteractionHandler(GridPane chessBoard, Field field, int squareSize, Consumer<Void> onBoardUpdate) {
        this.chessBoard = chessBoard;
        this.field = field;
        this.squareSize = squareSize;
        this.onBoardUpdate = onBoardUpdate;
    }

    private void updateBoard() {
        onBoardUpdate.accept(null);
    }


    public void setupInteractions() {
        setupClickHandlers();
        setupDragAndDrop();
    }

    private void setupClickHandlers() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof StackPane square) {
                square.setOnMouseClicked(event -> handleSquareClick(square));
            }
        }
    }

    private void handleSquareClick(StackPane square) {
        Square clickedSquare = (Square) square.getUserData();

        if (selectedSquare == null && hasPiece(square)) {
            selectSquare(clickedSquare);
        } else if (selectedSquare != null && isHighlightedSquare(clickedSquare)) {
            makeMove(new Move(selectedSquare, clickedSquare));
            clearSelection();
            updateBoard();
        } else {
            clearSelection();
            if (hasPiece(square)) {
                selectSquare(clickedSquare);
            }
        }
    }

    private void selectSquare(Square square) {
        selectedSquare = square;
        highlightPossibleMoves(square);
    }

    private void clearSelection() {
        clearHighlights();
        selectedSquare = null;
    }

    private void highlightPossibleMoves(Square square) {
        clearHighlights();
        highlightedSquares = field.getMovesForPiece(square);
        highlightedSquares.forEach(this::highlightSquare);
    }

    private void highlightSquare(Square square) {
        StackPane squarePane = getSquarePane(square.x(), square.y());

        Circle highlight = new Circle(squareSize / 6.0);
        highlight.setFill(HIGHLIGHT_COLOR);
        highlight.setMouseTransparent(true);

        squarePane.getChildren().add(highlight);
    }

    private void clearHighlights() {
        if (highlightedSquares != null) {
            highlightedSquares.forEach(square -> {
                StackPane squarePane = getSquarePane(square.x(), square.y());
                if (squarePane.getChildren().size() > 1) {
                    squarePane.getChildren().removeLast();
                }
            });
            highlightedSquares = null;
        }
    }

    private boolean isHighlightedSquare(Square square) {
        return highlightedSquares != null && highlightedSquares.contains(square);
    }

    private void setupDragAndDrop() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof StackPane square) {
                setupDragHandlers(square);
                setupDropHandlers(square);
            }
        }
    }

    private void setupDragHandlers(StackPane square) {
        square.setOnDragDetected(event -> {
            if (!hasPiece(square)) return;

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

    private void setupDragView(Dragboard db, ImageView piece) {
        double dragSize = squareSize * DRAG_SCALE;
        ImageView dragView = new ImageView(piece.getImage());
        dragView.setFitWidth(dragSize);
        dragView.setFitHeight(dragSize);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        db.setDragView(dragView.snapshot(params, null), dragSize / 2, dragSize / 2);
    }

    private void setupDropHandlers(StackPane square) {
        square.setOnDragOver(event -> {
            if (event.getGestureSource() != square && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        square.setOnDragDropped(event -> handleDrop(event, square));
    }

    private void handleDrop(javafx.scene.input.DragEvent event, StackPane square) {
        Dragboard db = event.getDragboard();
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
                if (field.validateMove(move)) {
                    makeMove(move);
                    success = true;
                }
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    private void makeMove(Move move) {
        if (field.validateMove(move)) {
            field.move(move);
            updateBoard();
        }
    }

    private boolean hasPiece(StackPane square) {
        return square.getChildren().size() > 1;
    }

    private StackPane getSquarePane(int col, int row) {
        return (StackPane) chessBoard.getChildren().stream()
                .filter(node -> GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Could not find square at coordinates %d %d", col, row)));
    }
}