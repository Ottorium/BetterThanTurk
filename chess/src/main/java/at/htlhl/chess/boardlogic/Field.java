package at.htlhl.chess.boardlogic;

import at.htlhl.chess.boardlogic.util.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chess field/board and its state
 */
public class Field {

    /**
     * Stores the current board with each square being one byte using bit flags. To set or modify this value please use {@link PieceUtil}.
     */
    private byte[][] board;

    private boolean blackTurn;

    /**
     * Stores the current castling rights using bit flags. To set or modify this value please use {@link CastlingUtil}.
     */
    private byte castlingInformation;

    private Square possibleEnPassantSquare;

    private int playedHalfMovesSinceLastPawnMoveOrCapture;

    private int numberOfNextMove;

    private static final String INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private GameState gameState;

    private final MoveChecker moveChecker = new MoveChecker(this);

    private int pieceEvaluation = 0;
    private final List<Byte> capturedWhitePieces = new ArrayList<>();
    private final List<Byte> capturedBlackPieces = new ArrayList<>();


    private Player kingInCheck = null;

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
     *
     * @return true if reset was successful
     */
    public boolean resetBoard() {
        return trySetFEN(INITIAL_FEN);
    }

    /**
     * Executes a move on the board,  if the move is valid
     *
     * @param move The move to execute
     * @return true if the move is valid and got moved, false otherwise.
     */
    public boolean move(Move move) {
        moveChecker.validateMove(move);

        if (move.isLegal()) {
            forceMove(move);
            return true;
        }
        return false;
    }

    /**
     * Executes a move on the board. Does not check if the move is valid. Requires the move to be validated earlier
     *
     * @param move The move to execute. Undefined behaviour if the move is not valid
     */
    public void forceMove(Move move) {

        // store captured piece for material calculation and castling calculation later
        byte capturedPiece = getPieceBySquare(move.getTargetSquare());

        // move piece to target square
        setPieceOnSquare(move.getTargetSquare(), getPieceBySquare(move.getStartingSquare()));
        setPieceOnSquare(move.getStartingSquare(), PieceUtil.EMPTY);

        //En passant
        //Delete captured pawn if enPassant happened
        if (move.isEnPassantMove()) {
            Square capturedEnPassantPawn = new Square(possibleEnPassantSquare.x(), possibleEnPassantSquare.y() + (isBlackTurn() ? -1 : 1));
            capturedPiece = getPieceBySquare(capturedEnPassantPawn);
            setPieceOnSquare(capturedEnPassantPawn, PieceUtil.EMPTY);
        }
        possibleEnPassantSquare = move.getPossibleEnPassantSquare();

        // Castling
        moveRookIfCastlingMove(move);
        removeCastlingRightsIfNeeded(move, capturedPiece);

        // Promotions
        if (PieceUtil.isEmpty(move.getPromotionPiece()) == false)
            setPieceOnSquare(move.getTargetSquare(), move.getPromotionPiece());

        setKingInCheck(move.getAppearedCheck());
        calculateMaterial(capturedPiece);
        updatePlayedHalfMovesSinceLastPawnMoveOrCapture(move);
        if (blackTurn) numberOfNextMove++;
        blackTurn = !blackTurn;
        setGameState();
    }

    /**
     * Updates the number of half-moves since the last pawn move or capture.
     * This counter is incremented after each move unless a pawn is moved or a piece is captured,
     * in which case it is reset to 0.
     *
     * @param move The move that was just executed
     */
    private void updatePlayedHalfMovesSinceLastPawnMoveOrCapture(Move move) {
        byte movingPiece = getPieceBySquare(move.getTargetSquare());
        byte capturedPiece = board[move.getTargetSquare().y()][move.getTargetSquare().x()];

        if (PieceUtil.isPawn(movingPiece) ||PieceUtil.isEmpty(capturedPiece) == false) {
            playedHalfMovesSinceLastPawnMoveOrCapture = 0;
        } else {
            playedHalfMovesSinceLastPawnMoveOrCapture++;
        }
    }

    /**
     * Sets the game state to represent the current board
     * */
    private void setGameState() {
        List<Move> legalMoves = moveChecker.getAllLegalMoves();

        if (legalMoves.isEmpty() == false) {
            gameState = GameState.NOT_DECIDED;
            return;
        }

        if (kingInCheck == (blackTurn ? Player.BLACK : Player.WHITE)) {
            gameState = isBlackTurn() ? GameState.WHITE_WIN : GameState.BLACK_WIN;
        } else {
            gameState = GameState.DRAW; // no moves -> draw
        }
    }

    /**
     * Adds the Captured piece to the class variables keeping track of the current captured pieces
     *
     * @param capturedPiece the piece to add (eg. the piece that got captured in the last move)
     */
    private void calculateMaterial(byte capturedPiece) {
        if (PieceUtil.isEmpty(capturedPiece)) return;

        if (PieceUtil.isWhite(capturedPiece))
            capturedWhitePieces.add(capturedPiece);
        else
            capturedBlackPieces.add(capturedPiece);

        pieceEvaluation += PieceUtil.getRelativeValue(capturedPiece);
    }

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
     * Checks if a player is in check on a simulated board
     *
     * @param simBoard Simulated board state to check
     * @return The player in check, or null if no one is in check
     */
    private Player getCheckOnBoard(byte[][] simBoard) {
        throw new UnsupportedOperationException("getCheckOnBoard not implemented");
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


    // Getters and setters
    public boolean isBlackTurn() {
        return blackTurn;
    }

    public byte getCastlingInformation() {
        return castlingInformation;
    }

    public Square getPossibleEnPassantSquare() {
        return possibleEnPassantSquare;
    }

    public int getPlayedHalfMovesSinceLastPawnMoveOrCapture() {
        return playedHalfMovesSinceLastPawnMoveOrCapture;
    }

    public int getNumberOfNextMove() {
        return numberOfNextMove;
    }

    public Player getKingInCheck() {
        return kingInCheck;
    }

    public void setKingInCheck(Player kingInCheck) {
        this.kingInCheck = kingInCheck;
    }
}