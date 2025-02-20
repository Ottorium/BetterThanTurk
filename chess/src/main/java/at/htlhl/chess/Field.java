package at.htlhl.chess;

import at.htlhl.chess.util.*;

import java.util.List;

/**
 * Represents a chess field/board and its state
 */
public class Field {

    /**
     * Stores the current board with each square being one byte using bit flags. To set or modify this value please use {@link at.htlhl.chess.util.PieceUtil}.
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
        throw new UnsupportedOperationException("resetBoard not implemented");
    }

    /**
     * Validates if a move is legal
     *
     * @param move The move to validate
     * @return true if the move is valid, false otherwise
     */
    public boolean validateMove(Move move) {
        if (move == null) {
            return false;
        }

        // check if player color is ok
        if (PieceUtil.isBlack(getPieceBySquare(move.startingSquare())) ^ blackTurn) { // if colors are not equal
            return false;
        }

        // Get possible targets
        List<Square> possibleTargets = getMovesForPiece(move.startingSquare());
        if (possibleTargets.isEmpty()) {
            return false;
        }

        // look if target square is possible
        for (Square target : possibleTargets) {
            if (target.equals(move.targetSquare())){
                //TODO  Check check
                return true;
            }
        }

        return false;
    }

    /**
     * Executes a move on the board
     *
     * @param move The move to execute
     */
    public void move(Move move) {
        if (validateMove(move)) {
            // move piece to target square
            setPieceBySquare(move.targetSquare(), getPieceBySquare(move.startingSquare()));
            setPieceBySquare(move.startingSquare(), PieceUtil.EMPTY);

            //TODO: Add capture material calculation
        }
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
     * Gets all possible moves for a piece at a given position
     *
     * @param position The square containing the piece
     * @return List of possible target squares for the piece
     */
    private List<Square> getMovesForPiece(Square position) {
        MoveChecker moveChecker = new MoveChecker(board, position, possibleEnPassantSquare);
        return moveChecker.getMoves();
    }

    /**
     * Gets piece byte from board
     *
     * @param square position of piece
     * @return piece value
     */
    private byte getPieceBySquare(Square square) {
        return board[square.y()][square.x()];
    }

    /**
     * Sets piece byte on board
     *
     * @param square
     * @param piece
     */
    private void setPieceBySquare(Square square, byte piece) {
        board[square.y()][square.x()] = piece;
    }
}