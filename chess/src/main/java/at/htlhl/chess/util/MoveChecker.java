package at.htlhl.chess.util;

import at.htlhl.chess.Square;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to check possible moves for different chess pieces.
 */
public class MoveChecker {

    private final byte[][] board;
    private final Square position;
    private final Square enPassantSquare;
    private final boolean isWhite;

    /**
     * Constructs a MoveChecker object.
     *
     * @param board           The current state of the chessboard.
     * @param position        The position of the piece to check moves for.
     * @param enPassantSquare The square available for en passant.
     */
    public MoveChecker(byte[][] board, Square position, Square enPassantSquare) {
        this.board = board;
        this.position = position;
        this.enPassantSquare = enPassantSquare;
        this.isWhite = PieceUtil.isWhite(board[position.y()][position.x()]);
    }

    /**
     * Checks if the given position is on the board.
     *
     * @param position The square to check.
     * @return True if the position is on the board, false otherwise.
     */
    private boolean isOnBoard(Square position) {
        return position.x() >= 0 && position.y() >= 0 && position.x() < 8 && position.y() < 8;
    }

    /**
     * Checks if the given coordinates are on the board.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return True if the coordinates are within board boundaries, false otherwise.
     */
    private boolean isOnBoard(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    /**
     * Checks if a target square is a valid move.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return True if the target square is empty or occupied by an opponent's piece.
     */
    private boolean isTargetSquarePossible(int x, int y) {
        return isOnBoard(x, y) && (PieceUtil.isEmpty(board[y][x]) || PieceUtil.isWhite(board[y][x]) != isWhite);
    }

    /**
     * Gets the possible target squares for a bishop.
     *
     * @return A list of valid squares the bishop can move to.
     */
    private List<Square> getPossibleBishopTargetSquares() {
        List<Square> squares = new ArrayList<>();
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                int x = position.x() + dir[0] * i;
                int y = position.y() + dir[1] * i;
                if (isTargetSquarePossible(x, y)) {
                    squares.add(new Square(x, y));
                    if (!PieceUtil.isEmpty(board[y][x])) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return squares;
    }

    /**
     * Gets the possible target squares for a rook.
     *
     * @return A list of valid squares the rook can move to.
     */
    private List<Square> getPossibleRookTargetSquares() {
        List<Square> squares = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                int x = position.x() + dir[0] * i;
                int y = position.y() + dir[1] * i;
                if (isTargetSquarePossible(x, y)) {
                    squares.add(new Square(x, y));
                    if (!PieceUtil.isEmpty(board[y][x])) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return squares;
    }

    /**
     * Gets the possible target squares for a queen.
     *
     * @return A list of valid squares the queen can move to.
     */
    private List<Square> getPossibleQueenTargetSquares() {
        List<Square> squares = new ArrayList<>();
        squares.addAll(getPossibleBishopTargetSquares());
        squares.addAll(getPossibleRookTargetSquares());
        return squares;
    }

    /**
     * Gets the possible target squares for a knight.
     *
     * @return A list of valid squares the knight can move to.
     */
    private List<Square> getPossibleKnightTargetSquares() {
        boolean isWhite = PieceUtil.isWhite(board[position.y()][position.x()]);
        List<Square> squares = new ArrayList<>();

        int[][] knightMoves = {{1, -2}, {1, 2}, {-1, -2}, {-1, 2}, {2, -1}, {2, 1}, {-2, 1}, {-2, -1}};

        for (int[] move : knightMoves) {
            int x = position.x() + move[0];
            int y = position.y() + move[1];
            if (isTargetSquarePossible(x, y)) {
                squares.add(new Square(x, y));
            }
        }
        return squares;
    }

    /**
     * Returns True if the pawn can capture other piece there or do en passant
     * @param x x coordinate of target
     * @param y y coordinate of target
     * @return Either can or not
     */
    private boolean isPawnCaptureSquarePossible(int x, int y) {
        if (!isOnBoard(x, y)) {
            return false;
        }

        if (!PieceUtil.isEmpty(board[y][x]) && (PieceUtil.isWhite(board[y][x]) ^ isWhite)) {
            return true;
        }

        // EnPassant
        if (isWhite && enPassantSquare.y() == y) {
            return enPassantSquare.x() == x;
        }
        if (!isWhite && enPassantSquare.y() == y) {
            return enPassantSquare.x() == x;
        }
        return false;
    }

    /**
     * Checks if a given square (x, y) is a valid target for a pawn to move to.
     *
     * @param x The x-coordinate of the square.
     * @param y The y-coordinate of the square.
     * @return true if the square is on the board and empty, false otherwise.
     */
    private boolean isPawnTargetSquarePossible(int x, int y) {
        if (isOnBoard(x, y)) {
            return PieceUtil.isEmpty(board[y][x]);
        }
        return false;
    }

    /**
     * Checks if this pawn moves for the first time
     * @return true if yes, false otherwise
     */
    private boolean isPawnFirstMove() {
        if (isWhite) {
            return position.y() == 6;
        } else {
            return position.y() == 1;
        }
    }

    /**
     * Looks for all possible moves for this pawn
     * @return ArrayList of squares where pawn can legaly move
     */
    private List<Square> getPossiblePawnTargetSquares() {
        List<Square> squares = new ArrayList<>();

        // Captures
        if (isWhite) {
            if (isPawnCaptureSquarePossible(position.x() - 1, position.y() - 1)) {
                squares.add(new Square(position.x() - 1, position.y()));
            }
            if (isPawnCaptureSquarePossible(position.x() + 1, position.y() - 1)) {
                squares.add(new Square(position.x() - 1, position.y()));
            }
        } else {
            if (isPawnCaptureSquarePossible(position.x() - 1, position.y() + 1)) {
                squares.add(new Square(position.x() - 1, position.y()));
            }
            if (isPawnCaptureSquarePossible(position.x() + 1, position.y() + 1)) {
                squares.add(new Square(position.x() - 1, position.y()));
            }
        }

        // Move forward

        if (isWhite) {
            if (isPawnTargetSquarePossible(position.x(), position.y() - 1)) {
                squares.add(new Square(position.x(), position.y() - 1));
            }
            if (isPawnFirstMove() && isPawnCaptureSquarePossible(position.x(), position.y() - 2)) {
                squares.add(new Square(position.x(), position.y() - 2));
            }
        } else {
            if (isPawnTargetSquarePossible(position.x(), position.y() + 1)) {
                squares.add(new Square(position.x(), position.y() + 1));
            }
            if (isPawnFirstMove() && isPawnCaptureSquarePossible(position.x(), position.y() + 2)) {
                squares.add(new Square(position.x(), position.y() + 2));
            }
        }
        return squares;
    }

    /**
     * Looks what piece is on board and calls corresponding methods
     * checks are not calculated here !
     * @return List of possible target squares
     */
    public List<Square> getMoves(){
        byte piece = board[position.y()][position.x()];
        if (PieceUtil.isEmpty(piece)) {
            return new ArrayList<Square>();
        }
        if (PieceUtil.isBishop(piece)) {
            return getPossibleBishopTargetSquares();
        }
        if (PieceUtil.isRook(piece)) {
            return getPossibleRookTargetSquares();
        }
        if (PieceUtil.isKnight(piece)) {
            return getPossibleKnightTargetSquares();
        }
        if (PieceUtil.isQueen(piece)) {
            return getPossibleQueenTargetSquares();
        }
        if (PieceUtil.isPawn(piece)) {
            return getPossiblePawnTargetSquares();
        }
        if (PieceUtil.isKing(piece)) {
            // TODO: King moves
        }
        return new ArrayList<Square>();
    }

}