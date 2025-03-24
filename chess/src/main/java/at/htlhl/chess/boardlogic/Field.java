package at.htlhl.chess.boardlogic;

import at.htlhl.chess.boardlogic.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a chess field/board and its state
 */
public class Field {

    private static final String INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    /**
     * A List of all the pieces that black captured
     */
    private final List<Byte> capturedWhitePieces = new ArrayList<>();

    /**
     * A List of all the pieces that white captured
     */
    private final List<Byte> capturedBlackPieces = new ArrayList<>();

    /**
     * Stores the positions that have accured and how often they have accured.
     * the accured position is stored in as an Integer (a hashcode of the board)
     * Do not use to get all the positions, as it might not have all of them because of performance.
     */
    private HashMap<Integer, Integer> positionCounts = new HashMap<>();

    /**
     * Stores the current board with each square being one byte using bit flags. To set or modify this value please use {@link PieceUtil}.
     */
    private byte[] board;

    /**
     * true if it is blacks turn in the current position, otherwise false.
     */
    private boolean blackTurn;

    /**
     * Stores the current castling rights using bit flags. To set or modify this value please use {@link CastlingUtil}.
     */
    private byte castlingInformation;

    /**
     * Stores the square the another pawn can move to to capture en passant. (see FEN-Notation)
     */
    private Square possibleEnPassantSquare;

    /**
     * Stores the played half moves since the last event that changes the position permanently (this is used for the 50-move rule)
     * see FEN-Notation
     */
    private int playedHalfMovesSinceLastPawnMoveOrCapture;

    /**
     * The number of the next move in full moves
     */
    private int numberOfNextMove;

    /**
     * The current game state
     */
    private GameState gameState = GameState.NOT_DECIDED;

    /**
     * Stores the current Piece evaluation. Positive if white is up material and negative if black is up material.
     */
    private int pieceEvaluation = 0;

    /**
     * The last executed move.
     */
    private Move lastMove;

    /**
     * The changes that accured in the last move, this is for undoing.
     */
    private ArrayList<FieldChange> changesInLastMove = new ArrayList<>();

    /**
     * The full list of moves that are legal in the current position.
     */
    private ArrayList<Move> legalMoves = new ArrayList<>();

    /**
     * The {@link MoveChecker} used to validate moves.
     */
    private MoveChecker moveChecker = new MoveChecker(this);

    /**
     * Stores the current position of both kings (for faster lookup in move checking)
     */
    private ArrayList<Square> cachedKingPositions;

    /**
     * Stores the Squares that black is currently attacking and how often it is attacked
     */
    private HashMap<Square, Integer> blackAttackSquares;

    /**
     * Stores the Squares that white is currently attacking and how often it is attacked
     */
    private HashMap<Square, Integer> whiteAttackSquares;

    /**
     * Stored the Pieces that are currently pinned
     */
    private ArrayList<Pin> pins;

    /**
     * Stores the current check, if there is one
     */
    private Check check;

    /**
     * Util for finding and updating the white and black attack squares
     */
    private AttackedSquaresUtil attackedSquaresUtil;

    /**
     * Makes a new Field and initialized it with the starting chess position.
     */
    public Field() {
        this.resetBoard();
    }

    /**
     * Attempts to set the board state using FEN notation
     *
     * @param fen The FEN string to parse
     * @return true if FEN was valid and set successfully, false otherwise
     */
    public boolean trySetFEN(String fen) {
        try {
            var parser = new FENParser(fen);

            board = parser.parseBoard();
            blackTurn = parser.parseIsBlacksTurn();
            castlingInformation = parser.parseCastlingInformation();
            possibleEnPassantSquare = parser.parsePossibleEnPassantMove();
            playedHalfMovesSinceLastPawnMoveOrCapture = parser.parsePlayedHalfMovesSinceLastPawnMoveOrCapture();
            numberOfNextMove = parser.parseNumberOfNextMove();

        } catch (InvalidFENException e) {
            return false;
        }
        gameState = GameState.NOT_DECIDED;
        pins = new ArrayList<>();
        moveChecker = new MoveChecker(this);
        positionCounts.clear();
        cachedKingPositions = moveChecker.findKings();
        attackedSquaresUtil = new AttackedSquaresUtil(this);
        blackAttackSquares = attackedSquaresUtil.findAttackedSquares(Player.BLACK);
        whiteAttackSquares = attackedSquaresUtil.findAttackedSquares(Player.WHITE);
        pins = attackedSquaresUtil.lookForPins(blackTurn ? Player.BLACK : Player.WHITE);
        check = attackedSquaresUtil.lookForCheck(blackTurn ? Player.BLACK : Player.WHITE);
        legalMoves = moveChecker.getAllLegalMoves();
        gameState = computeGameState();
        return true;
    }

    /**
     * Gets the current board state in FEN notation
     *
     * @return Current board state as FEN string
     */
    public String getFEN() {
        return FENParser.exportToFEN(board, blackTurn, castlingInformation, possibleEnPassantSquare, playedHalfMovesSinceLastPawnMoveOrCapture, numberOfNextMove);
    }

    /**
     * Resets the board to initial state
     */
    public void resetBoard() {
        trySetFEN(INITIAL_FEN);
    }

    /**
     * Executes a move on the board,  if the move is valid
     *
     * @param move The move to execute
     * @return true if the move is valid and got moved, false otherwise.
     */
    public boolean move(Move move) {

        if (getGameState() != GameState.NOT_DECIDED)
            return false;

        moveChecker.validateMove(move);

        if (move.isLegal()) {
            forceMove(move, true);
            return true;
        }
        return false;
    }

    /**
     * Executes a move on the board. Does not check if the move is valid. Requires the move to be validated earlier
     *
     * @param move The move to execute. Undefined behaviour if the move is not valid
     */
    public void forceMove(Move move, boolean verbose) {
        var changesInLastMoveBefore = (ArrayList<FieldChange>) changesInLastMove.clone();
        changesInLastMove.clear();
        changesInLastMove.add(new FieldChange("changesInLastMove", undo -> changesInLastMove = changesInLastMoveBefore));


        // store captured piece for material calculation and castling calculation later
        byte capturedPiece = getPieceBySquare(move.getTargetSquare());

        // move piece to target square
        setPieceOnSquare(move.getTargetSquare(), getPieceBySquare(move.getStartingSquare()));
        setPieceOnSquare(move.getStartingSquare(), PieceUtil.EMPTY);
        byte finalCapturedPiece = capturedPiece;
        changesInLastMove.add(new FieldChange("board", undo -> {
            setPieceOnSquare(move.getStartingSquare(), getPieceBySquare(move.getTargetSquare()));
            setPieceOnSquare(move.getTargetSquare(), finalCapturedPiece);
        }));

        //En passant
        //Delete captured pawn if enPassant happened
        if (move.isEnPassantMove()) {
            Square capturedEnPassantPawn = new Square(possibleEnPassantSquare.x(), possibleEnPassantSquare.y() + (isBlackTurn() ? -1 : 1));
            capturedPiece = getPieceBySquare(capturedEnPassantPawn);
            byte capturedPawn = getPieceBySquare(capturedEnPassantPawn);
            setPieceOnSquare(capturedEnPassantPawn, PieceUtil.EMPTY);
            changesInLastMove.add(new FieldChange("board", undo -> setPieceOnSquare(capturedEnPassantPawn, capturedPawn)));
        }
        var before = possibleEnPassantSquare;
        possibleEnPassantSquare = move.getPossibleEnPassantSquare();
        if (before != possibleEnPassantSquare)
            changesInLastMove.add(new FieldChange("possibleEnPassantSquare", undo -> possibleEnPassantSquare = before));

        // Castling
        moveRookIfCastlingMove(move);
        removeCastlingRightsIfNeeded(move, capturedPiece);

        // Promotions
        if (PieceUtil.isEmpty(move.getPromotionPiece()) == false) {
            setPieceOnSquare(move.getTargetSquare(), move.getPromotionPiece());
            changesInLastMove.add(new FieldChange("board",
                    unused ->
                            setPieceOnSquare(move.getStartingSquare(),
                                    blackTurn ?
                                            PieceUtil.BLACK_PAWN
                                            : PieceUtil.WHITE_PAWN)));
        }

        calculateMaterial(capturedPiece, move);
        updatePlayedHalfMovesSinceLastPawnMoveOrCapture(move);

        if (blackTurn) {
            numberOfNextMove++;
            changesInLastMove.add(new FieldChange("numberOfNextMove", undo -> numberOfNextMove--));
        }

        blackTurn = !blackTurn;
        changesInLastMove.add(new FieldChange("blackTurn", undo -> blackTurn = !blackTurn));

        if (PieceUtil.isKing(getPieceBySquare(move.getTargetSquare()))) {
            var cachedKingPositionsBefore = cachedKingPositions;
            cachedKingPositions = moveChecker.findKings();
            changesInLastMove.add(new FieldChange("cachedKingPositions", undo -> {
                cachedKingPositions = cachedKingPositionsBefore;
            }));
        }

        var blackAttackSquaresBefore = (HashMap<Square, Integer>) blackAttackSquares.clone();
        var whiteAttackSquaresBefore = (HashMap<Square, Integer>) whiteAttackSquares.clone();
        attackedSquaresUtil.updateCachedAttackSquares(move);
        changesInLastMove.add(new FieldChange("blackAttackSquares", undo -> blackAttackSquares = blackAttackSquaresBefore));
        changesInLastMove.add(new FieldChange("whiteAttackSquares", undo -> whiteAttackSquares = whiteAttackSquaresBefore));

        var pinsBefore = pins;
        pins = attackedSquaresUtil.lookForPins(blackTurn ? Player.BLACK : Player.WHITE);
        changesInLastMove.add(new FieldChange("pinsBefore", undo -> pins = pinsBefore));

        var checkBefore = check;
        check = attackedSquaresUtil.lookForCheck(blackTurn ? Player.BLACK : Player.WHITE);
        changesInLastMove.add(new FieldChange("checkBefore", undo -> check = checkBefore));

        var legalMovesBefore = new ArrayList<Move>(legalMoves.size());
        for (Move legalMove : legalMoves) legalMovesBefore.add(legalMove.clone());
        legalMoves = moveChecker.getAllLegalMoves();
        changesInLastMove.add(new FieldChange("legalMoves", undo -> legalMoves = legalMovesBefore));

        var gameStateBefore = gameState;
        gameState = computeGameState();
        changesInLastMove.add(new FieldChange("gameState", undo -> gameState = gameStateBefore));

        var lastMoveBefore = lastMove;
        lastMove = move.clone();
        changesInLastMove.add(new FieldChange("lastMove", undo -> lastMove = lastMoveBefore));

        if (verbose) System.out.println("Game state: " + gameState);
    }

    /**
     * Updates the number of half-moves since the last pawn move or capture.
     * This counter is incremented after each move unless a pawn is moved or a piece is captured,
     * in which case it is reset to 0.
     *
     * @param move The move that was just executed
     */
    private void updatePlayedHalfMovesSinceLastPawnMoveOrCapture(Move move) {
        var before = playedHalfMovesSinceLastPawnMoveOrCapture;
        byte movingPiece = getPieceBySquare(move.getTargetSquare());
        byte capturedPiece = board[move.getTargetSquare().y() * 8 + move.getTargetSquare().x()];

        if (PieceUtil.isPawn(movingPiece) || PieceUtil.isEmpty(capturedPiece) == false) {
            playedHalfMovesSinceLastPawnMoveOrCapture = 0;
        } else {
            playedHalfMovesSinceLastPawnMoveOrCapture++;
        }
        changesInLastMove.add(new FieldChange("playedHalfMovesSinceLastPawnMoveOrCapture", undo -> playedHalfMovesSinceLastPawnMoveOrCapture = before));
    }

    /**
     * Computes a gameState the represents the current board
     *
     * @return the current gameState computed from the position of the board
     */
    private GameState computeGameState() {
        if (playedHalfMovesSinceLastPawnMoveOrCapture >= 50) {
            return GameState.DRAW;
        }


        int current = Arrays.hashCode(board);

        int count = positionCounts.getOrDefault(current, 0) + 1;
        var before = (HashMap<Integer, Integer>) positionCounts.clone();
        positionCounts.put(current, count);
        changesInLastMove.add(new FieldChange("positionCounts", undo -> positionCounts = before));

        if (count >= 3) {
            return GameState.DRAW; // Threefold repetition
        }


        boolean insufficient = true;
        int numberOfPieces = 0;
        for (byte piece : board) {
            if (piece == PieceUtil.EMPTY) continue;

            if (++numberOfPieces >= 4
                    || PieceUtil.isPawn(piece)
                    || PieceUtil.isRook(piece)
                    || PieceUtil.isQueen(piece)) {
                insufficient = false;
                break;
            }

        }
        if (insufficient) return GameState.DRAW;


        if (legalMoves.isEmpty() == false)
            return GameState.NOT_DECIDED;

        if (getPlayerInCheck() == (blackTurn ? Player.BLACK : Player.WHITE)) {
            return isBlackTurn() ? GameState.WHITE_WIN : GameState.BLACK_WIN;
        } else {
            return GameState.DRAW; // Stalemate
        }
    }


    /**
     * Adds the Captured piece to the class variables keeping track of the current captured pieces
     *
     * @param capturedPiece the piece to add (eg. the piece that got captured in the last move)
     */
    private void calculateMaterial(byte capturedPiece, Move move) {
        if (PieceUtil.isEmpty(capturedPiece)) return;

        if (PieceUtil.isWhite(capturedPiece)) {
            capturedWhitePieces.add(capturedPiece);
            changesInLastMove.add(new FieldChange("capturedWhitePieces", undo -> capturedWhitePieces.removeLast()));
        } else {
            capturedBlackPieces.add(capturedPiece);
            changesInLastMove.add(new FieldChange("capturedBlackPieces", undo -> capturedBlackPieces.removeLast()));
        }
        var before = pieceEvaluation;
        pieceEvaluation += PieceUtil.getRelativeValue(capturedPiece) - PieceUtil.getRelativeValue(move.getPromotionPiece());
        if (before != pieceEvaluation)
            changesInLastMove.add(new FieldChange("pieceEvaluation", undo -> pieceEvaluation = before));
    }

    /**
     * Moves the Rook involved in a castling move if needed
     */
    private void moveRookIfCastlingMove(Move move) {
        if (move.isCastlingMove()) {
            // Determine castling direction and rook starting position
            int kingMoveDistance = move.getTargetSquare().x() - move.getStartingSquare().x();
            int rookStartX = (kingMoveDistance > 0) ? 7 : 0;  // Kingside: h-file, Queenside: a-file
            int rookTargetX = move.getStartingSquare().x() + (kingMoveDistance / 2);
            int yRank = move.getStartingSquare().y();

            // Move the rook
            Square rookStart = new Square(rookStartX, yRank);
            Square rookTarget = new Square(rookTargetX, yRank);
            setPieceOnSquare(rookTarget, getPieceBySquare(rookStart));
            setPieceOnSquare(rookStart, PieceUtil.EMPTY);
            changesInLastMove.add(new FieldChange("board", undo -> {
                setPieceOnSquare(rookStart, getPieceBySquare(rookTarget));
                setPieceOnSquare(rookTarget, PieceUtil.EMPTY);
            }));

            byte castlingInformationBefore = castlingInformation;
            castlingInformation = CastlingUtil.removeCastlingRights(castlingInformation, blackTurn ? Player.BLACK : Player.WHITE);
            changesInLastMove.add(new FieldChange("castlingInformation", undo -> {
                castlingInformation = castlingInformationBefore;
            }));
        }
    }

    /**
     * Updates the castling rights based on the move just played.
     * This method examines the move and determines if any castling rights
     * should be revoked
     *
     * @param move          The move that was just played
     * @param capturedPiece The piece (if any) that was captured by this move.
     */
    private void removeCastlingRightsIfNeeded(Move move, byte capturedPiece) {
        var rightsBefore = castlingInformation;
        Square start = move.getStartingSquare();
        Square target = move.getTargetSquare();
        byte movingPiece = getPieceBySquare(target);

        int homeRank = blackTurn ? 0 : 7;
        int opponentHomeRank = blackTurn ? 7 : 0;

        // If king moves, remove all castling rights for that side
        if (PieceUtil.isKing(movingPiece)) {
            castlingInformation = CastlingUtil.removeCastlingRights(castlingInformation, blackTurn ? Player.BLACK : Player.WHITE);
            return;
        }

        // If rook moves from its starting position, remove that sides castling right
        if (PieceUtil.isRook(movingPiece) && start.y() == homeRank)
            if (start.x() == 0)
                castlingInformation = CastlingUtil.remove(castlingInformation,
                        blackTurn ? CastlingUtil.BLACK_QUEEN_SIDE : CastlingUtil.WHITE_QUEEN_SIDE);
            else if (start.x() == 7)
                castlingInformation = CastlingUtil.remove(castlingInformation,
                        blackTurn ? CastlingUtil.BLACK_KING_SIDE : CastlingUtil.WHITE_KING_SIDE);


        // If rook is captured, remove that sides castling right
        if (PieceUtil.isRook(capturedPiece) && target.y() == opponentHomeRank)
            if (target.x() == 0)
                castlingInformation = CastlingUtil.remove(castlingInformation,
                        blackTurn ? CastlingUtil.WHITE_QUEEN_SIDE : CastlingUtil.BLACK_QUEEN_SIDE);
            else if (target.x() == 7)
                castlingInformation = CastlingUtil.remove(castlingInformation,
                        blackTurn ? CastlingUtil.WHITE_KING_SIDE : CastlingUtil.BLACK_KING_SIDE);

        if (rightsBefore != castlingInformation) {
            changesInLastMove.add(new FieldChange("castlingInformation", undo -> castlingInformation = rightsBefore));
        }
    }

    /**
     * Undoes the last move.
     */
    public void undoMove() {
        changesInLastMove.forEach(FieldChange::undo);
    }

    /**
     * Gets the current board state
     *
     * @return 2D array representing the board
     */
    public byte[] getBoard() {
        return board;
    }

    /**
     * Gets the position of the King in Check
     *
     * @return Position of the king in check, null if the current player is not in check
     */
    public Square getSquareOfCheck() {
        if (getPlayerInCheck() != (blackTurn ? Player.BLACK : Player.WHITE))
            return null;

        for (int i = 0; i < board.length; i++)
            if (board[i] == (blackTurn ? PieceUtil.BLACK_KING : PieceUtil.WHITE_KING))
                return Square.parseBoardIndex(i);

        return null;
    }

    /**
     * Gets all legal targets for a piece at a given position
     *
     * @param position The square containing the piece
     * @return List of possible target squares for the piece
     */
    public List<Square> getLegalTargetsForSquare(Square position) {
        MoveChecker moveChecker = new MoveChecker(this);
        return moveChecker.getLegalTargetsSquares(position);
    }

    /**
     * Gets piece byte from board
     */
    public byte getPieceBySquare(Square square) {
        return board[square.y() * 8 + square.x()];
    }

    /**
     * Sets piece byte on board
     */
    private void setPieceOnSquare(Square square, byte piece) {
        board[square.y() * 8 + square.x()] = piece;
    }


    // Getters and setters (do not just add some setters, pay attention to move undoing!!!)
    public boolean isBlackTurn() {
        return blackTurn;
    }

    public void setBlackTurn(boolean blackTurn) {
        this.blackTurn = blackTurn;
    }

    public byte getCastlingInformation() {
        return castlingInformation;
    }

    public Square getPossibleEnPassantSquare() {
        return possibleEnPassantSquare;
    }

    public Player getPlayerInCheck() {
        return check == null ? null : check.getPlayerInCheck();
    }

    public GameState getGameState() {
        return gameState;
    }

    public ArrayList<Move> getLegalMoves() {
        return legalMoves;
    }

    public int getPieceEvaluation() {
        return pieceEvaluation;
    }

    /**
     * Creates a deep copy of the current Field instance
     *
     * @return A new Field instance with identical state to the current one
     */
    public Field clone() {
        Field clone = new Field();

        clone.board = new byte[board.length];
        System.arraycopy(board, 0, clone.board, 0, board.length);


        clone.blackTurn = this.blackTurn;
        clone.castlingInformation = this.castlingInformation;
        clone.playedHalfMovesSinceLastPawnMoveOrCapture = this.playedHalfMovesSinceLastPawnMoveOrCapture;
        clone.numberOfNextMove = this.numberOfNextMove;
        clone.gameState = this.gameState;
        clone.pieceEvaluation = this.pieceEvaluation;

        clone.cachedKingPositions = (ArrayList<Square>) this.cachedKingPositions.clone();
        clone.blackAttackSquares = (HashMap<Square, Integer>) this.blackAttackSquares.clone();
        clone.whiteAttackSquares = (HashMap<Square, Integer>) this.whiteAttackSquares.clone();
        clone.pins = new ArrayList<>(this.pins.stream().map(Pin::clone).toList());
        clone.check = check == null ? null : this.check.clone();

        clone.possibleEnPassantSquare = this.possibleEnPassantSquare != null
                ? new Square(this.possibleEnPassantSquare.x(), this.possibleEnPassantSquare.y())
                : null;

        clone.positionCounts.clear();
        clone.positionCounts.putAll(this.positionCounts);

        clone.capturedWhitePieces.clear();
        clone.capturedWhitePieces.addAll(this.capturedWhitePieces);

        clone.capturedBlackPieces.clear();
        clone.capturedBlackPieces.addAll(this.capturedBlackPieces);

        clone.moveChecker = new MoveChecker(clone);
        clone.attackedSquaresUtil = new AttackedSquaresUtil(clone);

        var legalMovesClone = new ArrayList<Move>(legalMoves.size());
        for (Move legalMove : legalMoves) legalMovesClone.add(legalMove.clone());
        clone.legalMoves = legalMovesClone;

        return clone;
    }

    public List<Byte> getCapturedWhitePieces() {
        return capturedWhitePieces;
    }

    public List<Byte> getCapturedBlackPieces() {
        return capturedBlackPieces;
    }

    public ArrayList<Square> getCachedKingPositions() {
        return cachedKingPositions;
    }

    public void setCachedKingPositions(ArrayList<Square> kingPositions) {
        cachedKingPositions = kingPositions;
    }

    public HashMap<Square, Integer> getWhiteAttackSquares() {
        return whiteAttackSquares;
    }

    public void setWhiteAttackSquares(HashMap<Square, Integer> value) {
        whiteAttackSquares = value;
    }

    public HashMap<Square, Integer> getBlackAttackSquares() {
        return blackAttackSquares;
    }

    public void setBlackAttackSquares(HashMap<Square, Integer> value) {
        blackAttackSquares = value;
    }

    /**
     * Gets all squares currently attacked by the current player's pieces
     *
     * @return ArrayList of Squares attacked by the current player
     */
    public ArrayList<Square> getCurrentPlayerAttackSquares() {
        return blackTurn ? new ArrayList<>(blackAttackSquares.keySet()) : new ArrayList<>(whiteAttackSquares.keySet());
    }

    /**
     * Gets all squares currently attacked by the current player's pieces
     *
     * @return ArrayList of Squares attacked by the current player
     */
    public ArrayList<Square> getPassivePlayerAttackSquares() {
        return blackTurn ? new ArrayList<>(whiteAttackSquares.keySet()) : new ArrayList<>(blackAttackSquares.keySet());
    }

    public MoveChecker getMoveChecker() {
        return moveChecker;
    }

    public ArrayList<FieldChange> getChangesInLastMove() {
        return changesInLastMove;
    }

    public ArrayList<Pin> getPins() {
        return pins;
    }

    public Check getCheck() {
        return check;
    }
}
