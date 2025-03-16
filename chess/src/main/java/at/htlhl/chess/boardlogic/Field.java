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
    private byte[][] board;

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
     * Stores the Player that is currently in check.
     */
    private Player kingInCheck = null;

    /**
     * The {@link MoveChecker} used to validate moves.
     */
    private MoveChecker moveChecker = new MoveChecker(this);

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
        moveChecker = new MoveChecker(this);
        positionCounts.clear();
        Player currentPlayer = isBlackTurn() ? Player.BLACK : Player.WHITE;
        kingInCheck = moveChecker.lookForChecksOnBoard().contains(currentPlayer) ? currentPlayer : null;
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
        throw new UnsupportedOperationException("getFEN not implemented");
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
            setPieceOnSquare(capturedEnPassantPawn, PieceUtil.EMPTY);
            changesInLastMove.add(new FieldChange("board", undo -> setPieceOnSquare(capturedEnPassantPawn, finalCapturedPiece)));
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
        var kingInCheckBefore = kingInCheck;
        kingInCheck = move.getAppearedCheck();
        if (kingInCheckBefore != kingInCheck)
            changesInLastMove.add(new FieldChange("kingInCheck", undo -> kingInCheck = kingInCheckBefore));

        calculateMaterial(capturedPiece);
        updatePlayedHalfMovesSinceLastPawnMoveOrCapture(move);

        if (blackTurn) {
            numberOfNextMove++;
            changesInLastMove.add(new FieldChange("numberOfNextMove", undo -> numberOfNextMove--));
        }

        blackTurn = !blackTurn;
        changesInLastMove.add(new FieldChange("blackTurn", undo -> blackTurn = !blackTurn));

        var legalMovesBefore = new ArrayList<Move>(legalMoves.size());
        for (Move legalMove : legalMoves) legalMovesBefore.add(legalMove.clone());
        legalMoves = moveChecker.getAllLegalMoves();
        changesInLastMove.add(new FieldChange("legalMoves", undo -> legalMoves = legalMovesBefore));

        var gameStateBefore = gameState;
        gameState = computeGameState();
        changesInLastMove.add(new FieldChange("gameState", undo -> gameState = gameStateBefore));

        var lastMoveBefore = lastMove;
        lastMove = move;
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
        byte capturedPiece = board[move.getTargetSquare().y()][move.getTargetSquare().x()];

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


        byte[] flatBoard = getFlattenedBoard();
        int current = Arrays.hashCode(flatBoard);

        int count = positionCounts.getOrDefault(current, 0) + 1;
        var before = (HashMap<Integer, Integer>) positionCounts.clone();
        positionCounts.put(current, count);
        changesInLastMove.add(new FieldChange("positionCounts", undo -> positionCounts = before));

        if (count >= 3) {
            return GameState.DRAW; // Threefold repetition
        }


        boolean insufficient = true;
        int numberOfPieces = 0;
        for (byte piece : flatBoard) {
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

        if (kingInCheck == (blackTurn ? Player.BLACK : Player.WHITE)) {
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
    private void calculateMaterial(byte capturedPiece) {
        if (PieceUtil.isEmpty(capturedPiece)) return;

        if (PieceUtil.isWhite(capturedPiece)) {
            capturedWhitePieces.add(capturedPiece);
            changesInLastMove.add(new FieldChange("capturedWhitePieces", undo -> capturedWhitePieces.removeLast()));
        } else {
            capturedBlackPieces.add(capturedPiece);
            changesInLastMove.add(new FieldChange("capturedBlackPieces", undo -> capturedBlackPieces.removeLast()));
        }
        var before = pieceEvaluation;
        pieceEvaluation += PieceUtil.getRelativeValue(capturedPiece);
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

            castlingInformation = CastlingUtil.removeCastlingRights(castlingInformation, blackTurn ? Player.BLACK : Player.WHITE);
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
    public byte[][] getBoard() {
        return board;
    }

    /**
     * Gets the current board state as a 1d Array
     *
     * @return an array flattened to 1D representing the board
     */
    public byte[] getFlattenedBoard() {
        byte[] flatArray = new byte[64];
        int index = 0;
        for (byte[] row : board)
            for (byte b : row)
                flatArray[index++] = b;

        return flatArray;
    }

    /**
     * Gets the position of the King in Check
     *
     * @return Position of the king in check, null if the current player is not in check
     */
    public Square getSquareOfCheck() {
        if (kingInCheck != (blackTurn ? Player.BLACK : Player.WHITE))
            return null;

        for (int row = 0; row < board.length; row++)
            for (int col = 0; col < board[row].length; col++)
                if (board[row][col] == (blackTurn ? PieceUtil.BLACK_KING : PieceUtil.WHITE_KING))
                    return new Square(col, row);

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
        return board[square.y()][square.x()];
    }

    /**
     * Sets piece byte on board
     */
    private void setPieceOnSquare(Square square, byte piece) {
        board[square.y()][square.x()] = piece;
    }


    // Getters and setters (do not just add some setters, pay attention to move undoing!!!)
    public boolean isBlackTurn() {
        return blackTurn;
    }

    public byte getCastlingInformation() {
        return castlingInformation;
    }

    public Square getPossibleEnPassantSquare() {
        return possibleEnPassantSquare;
    }

    public Player getKingInCheck() {
        return kingInCheck;
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

        clone.board = new byte[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, clone.board[i], 0, board[i].length);
        }

        clone.blackTurn = this.blackTurn;
        clone.castlingInformation = this.castlingInformation;
        clone.playedHalfMovesSinceLastPawnMoveOrCapture = this.playedHalfMovesSinceLastPawnMoveOrCapture;
        clone.numberOfNextMove = this.numberOfNextMove;
        clone.gameState = this.gameState;
        clone.pieceEvaluation = this.pieceEvaluation;
        clone.kingInCheck = this.kingInCheck;

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

        var legalMovesClone = new ArrayList<Move>(legalMoves.size());
        for (Move legalMove : legalMoves) legalMovesClone.add(legalMove.clone());
        clone.legalMoves = legalMovesClone;

        return clone;
    }
}