package at.htlhl.chess.boardlogic;

import at.htlhl.chess.boardlogic.util.*;

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
     * @return true if moved successfully, false otherwise
     */
    public boolean move(Move move) {
        MoveChecker moveChecker = new MoveChecker(this);
        if (moveChecker.isMoveLegal(move)) {
            // move piece to target square
            setPieceBySquare(move.targetSquare(), getPieceBySquare(move.startingSquare()));
            setPieceBySquare(move.startingSquare(), PieceUtil.EMPTY);

            //En passant
            //Delete captured pawn if enPassant happened
            if (move.targetSquare().equals(possibleEnPassantSquare)
                    && PieceUtil.isPawn(getPieceBySquare(move.targetSquare()))) {
                setPieceBySquare(new Square(possibleEnPassantSquare.x(), possibleEnPassantSquare.y() + (isBlackTurn() ? -1 : 1)), PieceUtil.EMPTY);
            }


            possibleEnPassantSquare = moveChecker.getEnPassantSquareProducedByPawnDoubleMove(move);

            //TODO: Add capture material calculation
            blackTurn = !blackTurn;
            return true;
        }
        return false;
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
    private byte getPieceBySquare(Square square) {
        return board[square.y()][square.x()];
    }

    /**
     * Sets piece byte on board
     */
    private void setPieceBySquare(Square square, byte piece) {
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
}