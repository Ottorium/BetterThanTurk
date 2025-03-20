package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Square;
import at.htlhl.chess.boardlogic.util.PieceUtil;
import at.htlhl.chess.gui.util.BoardViewUtil;
import at.htlhl.chess.gui.util.ChessBoardInteractionHandler;
import at.htlhl.chess.gui.util.EngineConnector;
import at.htlhl.chess.gui.util.PieceImageUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class BoardViewController implements Initializable {

    public static final Color ARROW_COLOR = Color.rgb(110, 110, 110);
    private static final int BOARD_SIZE = 8;
    private static final int INITIAL_SQUARE_SIZE = 60;
    private static final Color LIGHT_SQUARE_COLOR = Color.rgb(242, 226, 190);
    private static final Color DARK_SQUARE_COLOR = Color.rgb(176, 136, 104);
    private static final Color LAST_MOVE_HIGHLIGHT_COLOR = Color.rgb(255, 255, 0, 0.4);
    private static final Color KING_CHECK_COLOR = Color.rgb(255, 0, 0);
    private final Field field = new Field();

    @FXML
    ToolBar toolBar;
    @FXML
    private FlowPane capturedWhitePieces;
    @FXML
    private FlowPane capturedBlackPieces;
    @FXML
    private TextArea FENTextArea;
    @FXML
    private GridPane chessBoard;
    private DoubleBinding squareSizeBinding;
    private Pane arrowPane;
    private EngineConnector engineConnector;
    private BoardViewUtil boardViewUtil = new BoardViewUtil();
    private List<Arrow> arrowsToDraw = new ArrayList<>(); // Will be reset after each move


    private boolean updatingBoardListeners = false; // Guard flag to prevent chaining

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

    public Pane getArrowPane() {
        return arrowPane;
    }

    public DoubleBinding squareSizeBindingProperty() {
        return squareSizeBinding;
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
            engineConnector = new EngineConnector(field, this::addArrow);
            updateUI(null, null);
        });
        setUpInteractions();
        initFENTextArea();
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

        // Panes above and below of board
        Pane capturedBlackPiecesPane = (Pane) capturedBlackPieces.getParent();
        capturedBlackPiecesPane.prefWidthProperty().bind(boardPane.widthProperty());
        Pane capturedWhitePiecesPane = (Pane) capturedWhitePieces.getParent();
        capturedWhitePiecesPane.prefWidthProperty().bind(boardPane.widthProperty());
        capturedWhitePiecesPane.prefHeightProperty().bind(boardPane.getScene().heightProperty().subtract(toolBar.heightProperty()).subtract(boardPane.widthProperty()).divide(2));
        capturedBlackPiecesPane.prefHeightProperty().bind(capturedWhitePiecesPane.prefHeightProperty());
        capturedBlackPieces.prefWidthProperty().bind(capturedWhitePiecesPane.prefWidthProperty());
        capturedWhitePieces.prefWidthProperty().bind(capturedWhitePiecesPane.prefWidthProperty());

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
     * Adds a new arrow to Arrows to draw list
     * @param move
     */
    public void addArrow(Move move) {
        if (move == null) {
            return;
        }
        Square arrowStartSquare = move.getStartingSquare();
        Square arrowEndSquare = move.getTargetSquare();
        byte arrowPromotionPiece = move.getPromotionPiece();
        Arrow arrow = new Arrow(arrowStartSquare, arrowEndSquare);
        arrow.setPromotionPiece(arrowPromotionPiece);
        arrow.setColor(ARROW_COLOR);
        arrowsToDraw.add(arrow);
        updateArrows();
    }

    private void updateArrows() {
        arrowPane.getChildren().clear();
        for (Arrow arrow : arrowsToDraw) {
            arrow.draw(this);
        }
    }

    private void clearArrows() {
        arrowsToDraw.clear();
        updateArrows();
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
                this::updateUI // Update callback
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
     * calls all ui update methods, like drawPieces or updateFEN
     *
     * @param moveToHighlight    for drawPieces
     * @param kingCheckHighlight for drawPieces
     */
    private void updateUI(Move moveToHighlight, Square kingCheckHighlight) {
        drawPieces(moveToHighlight, kingCheckHighlight);
        updateFENinFENTextArea();
        updateCapturedPieces();
        clearArrows();
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
        tmpConnectEngine();
    }

    private void tmpConnectEngine (){
        // TODO refactor
        engineConnector.stopCurrentExecutions();
        engineConnector = new EngineConnector(field, this::addArrow);
        engineConnector.drawBestMove();
    }

    /**
     * draws a Piece Image on the board. Does not clear images, only adds ImageVies
     *
     * @param piece   byte to draw
     * @param square  where to draw
     * @param opacity of this piece
     */
    public void drawPiece(byte piece, Square square, int opacity) {
        var stackpane = getSquarePane(chessBoard, square.x(), square.y());
        Image img = PieceImageUtil.getImage(piece);
        ImageView imageView = new ImageView(img);
        imageView.setOpacity(((double) opacity) / 100);

        // Bind image size to square size
        imageView.fitWidthProperty().bind(((Rectangle) stackpane.getChildren().getFirst()).widthProperty());
        imageView.fitHeightProperty().bind(((Rectangle) stackpane.getChildren().getFirst()).heightProperty());

        stackpane.getChildren().add(imageView);
    }


    // not board UI

    /**
     * Initializes the FEN text area, enabling text wrapping and handling Enter key presses to update the board.
     */
    private void initFENTextArea() {
        FENTextArea.setWrapText(true);
        FENTextArea.setOnKeyPressed(keyEvent -> {

            // remove new Line
            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.ENTER) {
                FENTextArea.setText(FENTextArea.getText().replace("\n", ""));
                setBoardByFEN();
            }
        });
    }

    /**
     * Updates the FEN text area with the current board's FEN string. Shows an error if FEN retrieval is unsupported.
     */
    private void updateFENinFENTextArea() {
        try {
            FENTextArea.setText(field.getFEN());
        } catch (UnsupportedOperationException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Sets the chess board based on the FEN string in the text area. Alerts if the FEN is invalid.
     */
    private void setBoardByFEN() {
        if (!field.trySetFEN(FENTextArea.getText())) {
            boardViewUtil.alertProblem("Invalid FEN!", "Check if your input is correct");
        }
        updateUI(null, null);
    }

    private void updateCapturedPieces() {
        refillCapturedPieceBox(capturedWhitePieces, field.getCapturedWhitePieces());
        refillCapturedPieceBox(capturedBlackPieces, field.getCapturedBlackPieces());
    }

    private void refillCapturedPieceBox(FlowPane box, List<Byte> pieces) {
        box.getChildren().clear();
        for (Byte piece : pieces) {
            ImageView pieceImageView = new ImageView(PieceImageUtil.getImage(piece));
            pieceImageView.setPreserveRatio(true);
            pieceImageView.setFitHeight((double) INITIAL_SQUARE_SIZE / 2);
            pieceImageView.setFitWidth((double) INITIAL_SQUARE_SIZE / 2);
            box.getChildren().add(pieceImageView);
        }
    }

    public void shutdown() {
        engineConnector.shutdown();
    }
}