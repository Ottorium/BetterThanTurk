package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.*;
import at.htlhl.chess.boardlogic.util.PieceUtil;
import at.htlhl.chess.engine.EvaluatedMove;
import at.htlhl.chess.entities.BotEntity;
import at.htlhl.chess.entities.PlayerEntity;
import at.htlhl.chess.entities.PlayingEntity;
import at.htlhl.chess.entities.StockfishEntity;
import at.htlhl.chess.gui.util.*;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
import javafx.util.Duration;
import org.controlsfx.control.ToggleSwitch;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class BoardViewController implements Initializable {

    public static final Color ARROW_COLOR = Color.rgb(110, 210, 110);
    private static final int BOARD_SIZE = 8;
    private static final int INITIAL_SQUARE_SIZE = 60;
    private static final Color LIGHT_SQUARE_COLOR = Color.rgb(242, 226, 190);
    private static final Color DARK_SQUARE_COLOR = Color.rgb(176, 136, 104);
    private static final Color LAST_MOVE_HIGHLIGHT_COLOR = Color.rgb(255, 255, 0, 0.4);
    private static final Color KING_CHECK_COLOR = Color.rgb(255, 0, 0);
    private final Field field = new Field();
    private final BoardViewUtil boardViewUtil = new BoardViewUtil();
    private final List<Arrow> arrowsToDraw = new ArrayList<>(); // Will be reset after each move
    public boolean playAnimations = true;
    @FXML
    public Button newGameButton;
    @FXML
    public ChoiceBox whitePlayerChoiceBox;
    @FXML
    public ChoiceBox blackPlayerChoiceBox;
    @FXML
    public Button clearSettingsButton;
    @FXML
    public VBox moveSuggestionsVBox;
    @FXML
    public ToggleSwitch engineToggleSwitch;
    @FXML
    public ChoiceBox engineForSuggChoiceBox;
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
    @FXML
    private Button undoButton;
    private DoubleBinding squareSizeBinding;
    private Pane arrowPane;
    private Pane animationPane;
    private PlayingEntity blackPlayingEntity;
    private PlayingEntity whitePlayingEntity;
    private EngineConnector connector;

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
        initArrowPane();
        initAnimationPane();
        initFENTextArea();
        initMenu();
        initPlayers(); // must be after initMenu
        initWithRunLater();
    }

    private void initWithRunLater() {
        Platform.runLater(() -> {
            setUpScalability();
            updateUI(null);
        });
    }

    private void initArrowPane() {
        arrowPane = new Pane();
        arrowPane.setMouseTransparent(true);
        chessBoard.add(arrowPane, 0, 0);
        GridPane.setColumnSpan(arrowPane, 8);
        GridPane.setRowSpan(arrowPane, 8);
    }

    private void initAnimationPane() {
        animationPane = new Pane();
        animationPane.setMouseTransparent(true);
        chessBoard.add(animationPane, 0, 0);
        GridPane.setColumnSpan(animationPane, 8);
        GridPane.setRowSpan(animationPane, 8);
    }

    private void initMenu() {
        newGameButton.setOnAction(l -> newGame());
        // add on toggle
        engineToggleSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                updateSuggestions();
            }
        });

        engineForSuggChoiceBox.setOnAction(l -> updateSuggestions());
        clearSettingsButton.setOnAction(l -> clearSettings());
        fillChoiceBoxes();

        undoButton.setOnAction(l -> undoMove());
    }

    private void undoMove() {
        field.undoMove();
        updateUI();
        updateMoveOrder();
    }

    private void fillChoiceBoxes() {
        blackPlayerChoiceBox.getItems().clear();
        whitePlayerChoiceBox.getItems().clear();
        engineForSuggChoiceBox.getItems().clear();
        blackPlayerChoiceBox.getItems().addAll(PlayingEntity.Type.values());
        blackPlayerChoiceBox.setValue(PlayingEntity.Type.values()[0]);
        whitePlayerChoiceBox.getItems().addAll(PlayingEntity.Type.values());
        whitePlayerChoiceBox.setValue(PlayingEntity.Type.values()[0]);
        engineForSuggChoiceBox.getItems().addAll(EngineConnector.Type.values());
        engineForSuggChoiceBox.setValue(EngineConnector.Type.values()[0]);

        // switch toggles
        engineToggleSwitch.setSelected(false);
    }

    private void newGame() {
        removeSquareListeners();
        field.resetBoard();
        initPlayers();
        updateUI();
    }

    private void removeSquareListeners() {
        blackPlayingEntity.shutdown();
        whitePlayingEntity.shutdown();
    }

    private void initPlayers() {
        switch (blackPlayerChoiceBox.getValue()) {
            case PlayingEntity.Type.PLAYER:
                blackPlayingEntity = new PlayerEntity(Player.BLACK, this);
                break;
            case PlayingEntity.Type.CUSTOM_BOT:
                blackPlayingEntity = new BotEntity(Player.BLACK, this);
                break;
            case PlayingEntity.Type.STOCKFISH:
                blackPlayingEntity = new StockfishEntity(Player.BLACK, this);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + blackPlayerChoiceBox.getValue());
        }
        switch (whitePlayerChoiceBox.getValue()) {
            case PlayingEntity.Type.PLAYER:
                whitePlayingEntity = new PlayerEntity(Player.WHITE, this);
                break;
            case PlayingEntity.Type.CUSTOM_BOT:
                whitePlayingEntity = new BotEntity(Player.WHITE, this);
                break;
            case PlayingEntity.Type.STOCKFISH:
                whitePlayingEntity = new StockfishEntity(Player.WHITE, this);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + whitePlayerChoiceBox.getValue());
        }
        updateMoveOrder();
    }

    private void updateMoveOrder() {
        if (field.isBlackTurn()) {
            blackPlayingEntity.allowMove();
        } else {
            whitePlayingEntity.allowMove();
        }
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
     */
    public void addArrow(Move move) {
        addArrow(move, 1);
    }

    /**
     * Adds a new arrow to Arrows to draw list with scale parameter
     */
    public void addArrow(Move move, double modifier) {
        if (move == null) {
            return;
        }
        Square arrowStartSquare = move.getStartingSquare();
        Square arrowEndSquare = move.getTargetSquare();
        byte arrowPromotionPiece = move.getPromotionPiece();
        Arrow arrow = new Arrow(arrowStartSquare, arrowEndSquare, modifier);
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
     * Is used to make move and update UI. For some reason calling updateUI does not update field on first call, so the piece is missing.
     *
     * @param move that has been made
     */
    public boolean makeMove(Move move) {
        boolean success = field.move(move);
        if (success) {
            processMoveInGUI(move);
        }
        return success;
    }

    /**
     * calls all ui update methods, like drawPieces or updateFEN
     *
     * @param squaresToHighlight for drawPieces
     */
    private void updateUI(ArrayList<Square> squaresToHighlight) {
        updateFENinFENTextArea();
        updateCapturedPieces();
        updateSuggestions();
        // I think clear arrows and draw pieces MUST be the last one
        clearArrows();
        drawPieces(squaresToHighlight, field.getSquareOfCheck());
        updateGameState();
    }

    /**
     * calls all ui update methods, like drawPieces or updateFEN
     */
    public void updateUI() {
        updateUI(null);
    }

    private void updateGameState(){
        if (field.getGameState().equals(GameState.NOT_DECIDED) == false) {
            boardViewUtil.alertWithAction("Game ended", String.valueOf(field.getGameState()), "New game", this::newGame);
        }
    }
    private void playMoveSound(Move move) {
        SoundEffect soundEffect;
        if (move.isCapture()) {
            if (PieceUtil.isRook(move.getCapturedPiece())) {
                soundEffect = SoundEffect.THE_ROOK;
            } else {
                soundEffect = SoundEffect.CAPTURE;
            }
        } else {
            soundEffect = SoundEffect.MOVE;
        }
        soundEffect.playSound();
    }

    /**
     * Draws chess pieces on the board based on the current state of the {@link Field}.
     * Removes any existing pieces and adds new ones where applicable. Highlights specific squares
     * with a yellow background and the king in check with a red radial gradient background.
     *
     * @param squaresToHighlight List of squares to highlight with a yellow background.
     * @param kingCheckHighlight The square containing the king in check, to highlight with a red radial gradient.
     */
    private void drawPieces(ArrayList<Square> squaresToHighlight, Square kingCheckHighlight) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = getSquarePane(chessBoard, col, row);
                var piece = field.getBoard()[row * 8 + col];

                if (square == null) continue;

                // Remove everything except for the color
                while (square.getChildren().size() > 1)
                    square.getChildren().remove(1);

                // Add highlight for specified squares
                if (squaresToHighlight != null && squaresToHighlight.contains(new Square(col, row))) {
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

    private void processMoveInGUI(Move move) {
        //play animation and then call updates
        ImageView movingPiece = null;
        for (Node node : getSquarePane(chessBoard, move.getStartingSquare().x(), move.getStartingSquare().y()).getChildren()) {
            if (node instanceof ImageView) {
                movingPiece = (ImageView) node;
                break;
            }
        }
        StackPane targetPane = getSquarePane(chessBoard, move.getTargetSquare().x(), move.getTargetSquare().y());
        if (movingPiece == null || targetPane == null) return;

        ((StackPane) movingPiece.getParent()).getChildren().remove(movingPiece);
        //create transition
        double startingX = squareSizeBinding.get() * move.getStartingSquare().x();
        double startingY = squareSizeBinding.get() * move.getStartingSquare().y();
        double targetX = squareSizeBinding.get() * move.getTargetSquare().x();
        double targetY = squareSizeBinding.get() * move.getTargetSquare().y();
        double deltaX = targetX - startingX;
        double deltaY = targetY - startingY;

        //clone image
        ImageView clone = new ImageView(movingPiece.getImage()); // Copy the image
        clone.setFitWidth(movingPiece.getFitWidth());           // Copy width
        clone.setFitHeight(movingPiece.getFitHeight());         // Copy height
        clone.setPreserveRatio(movingPiece.isPreserveRatio());  // Copy ratio setting

        animationPane.getChildren().add(clone);
        clone.setLayoutX(startingX);
        clone.setLayoutY(startingY);

        TranslateTransition transition = new TranslateTransition(Duration.seconds(playAnimations ? 0.1 : 0));
        transition.setNode(clone);
        transition.setToX(deltaX);
        transition.setToY(deltaY);
        transition.setInterpolator(Interpolator.EASE_OUT);

        //remove piece from pane
        transition.setOnFinished(event -> {
            animationPane.getChildren().remove(clone);
            Platform.runLater(() -> updateUI(new ArrayList<>(List.of(move.getStartingSquare(), move.getTargetSquare()))));
            updateMoveOrder();
            playMoveSound(move);
        });

        transition.play();
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
        updateUI(null);
        updateMoveOrder();
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

    public GridPane getChessBoard() {
        return this.chessBoard;
    }

    public Field getField() {
        return this.field;
    }

    public double getSquareSize() {
        return (squareSizeBinding != null && squareSizeBinding.isValid())
                ? squareSizeBinding.get()
                : INITIAL_SQUARE_SIZE;
    }

    /**
     * is used to stop engine threads
     */
    public void shutdown() {
        blackPlayingEntity.shutdown();
        whitePlayingEntity.shutdown();
        if (connector != null) {
            connector.shutdown();
            connector = null;
        }
    }

    private void clearSettings() {
        ChessApplication.prop.clear();
        ChessApplication.saveProperties();
        System.exit(0);
    }

    /**
     * Shows alert and starts a new game with default settings
     *
     * @param headerText  alert header
     * @param contentText alert content
     */
    public void alertWithNewGame(String headerText, String contentText) {
        boardViewUtil.alertProblem(headerText, contentText);
        fillChoiceBoxes();
        newGame();
    }

    private void updateSuggestions() {
        moveSuggestionsVBox.getChildren().clear();
        if (connector != null) connector.shutdown();
        clearArrows();
        if (engineToggleSwitch.isSelected() == false) return;

        // run engine in background and then coll fillMoveSuggestions
        switch (engineForSuggChoiceBox.getValue()) {
            case EngineConnector.Type.CUSTOM:
                connector = new CustomEngineConnector(getField());
                break;
            case EngineConnector.Type.STOCKFISH:
                connector = new StockfishConnector(this);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + engineForSuggChoiceBox.getValue());
        }
        connector.suggestMoves(this::fillMoveSuggestions);
    }

    private void fillMoveSuggestions(List<EvaluatedMove> moves) {
        if (moves == null) return;
        double i = 0;
        for (EvaluatedMove move : moves) {
            if (move == null) continue;
            if (i < 3) {
                addArrow(move.move(), (1.7 - (i * 0.5)));
                i++;
            }
            moveSuggestionsVBox.getChildren().add(boardViewUtil.buildMoveBox(move, getPlayerThatWillMove()));
        }
    }

    /**
     * Returns the player that already made the move, so if the isBlackTurn is true, the player is white
     *
     * @return Player color that moved
     */
    public Player getPlayerThatMoved() {
        return field.isBlackTurn() ? Player.WHITE : Player.BLACK;
    }

    /**
     * Returns the player that will make a move now
     *
     * @return player
     */
    public Player getPlayerThatWillMove() {
        return field.isBlackTurn() ? Player.BLACK : Player.WHITE;
    }
}