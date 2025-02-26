package at.htlhl.chess.boardlogic.util;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Square;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to check possible moves for different chess pieces.
 */
public class MoveChecker {
    private final Field field;

    /**
     * Constructs a MoveChecker object.
     */
    public MoveChecker(Field field) {
        this.field = field;
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
    private boolean isTargetSquarePossible(int x, int y, boolean isStartWhite) {
        return isOnBoard(x, y) && (PieceUtil.isEmpty(field.getBoard()[y][x]) || PieceUtil.isWhite(field.getBoard()[y][x]) != isStartWhite);
    }

    /**
     * Gets the possible target squares for a bishop.
     *
     * @return A list of valid squares the bishop can move to.
     */
    private List<Square> getPossibleBishopTargetSquares(Square position, boolean isStartWhite) {
        List<Square> squares = new ArrayList<>();
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                int x = position.x() + dir[0] * i;
                int y = position.y() + dir[1] * i;
                if (isTargetSquarePossible(x, y, isStartWhite)) {
                    squares.add(new Square(x, y));
                    if (!PieceUtil.isEmpty(field.getBoard()[y][x])) {
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
    private List<Square> getPossibleRookTargetSquares(Square position, boolean isStartWhite) {
        List<Square> squares = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                int x = position.x() + dir[0] * i;
                int y = position.y() + dir[1] * i;
                if (isTargetSquarePossible(x, y, isStartWhite)) {
                    squares.add(new Square(x, y));
                    if (!PieceUtil.isEmpty(field.getBoard()[y][x])) {
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
    private List<Square> getPossibleQueenTargetSquares(Square position, boolean isStartWhite) {
        List<Square> squares = new ArrayList<>();
        squares.addAll(getPossibleBishopTargetSquares(position, isStartWhite));
        squares.addAll(getPossibleRookTargetSquares(position, isStartWhite));
        return squares;
    }

    /**
     * Gets the possible target squares for a knight.
     *
     * @return A list of valid squares the knight can move to.
     */
    private List<Square> getPossibleKnightTargetSquares(Square position, boolean isStartWhite) {
        List<Square> squares = new ArrayList<>();

        int[][] knightMoves = {{1, -2}, {1, 2}, {-1, -2}, {-1, 2}, {2, -1}, {2, 1}, {-2, 1}, {-2, -1}};

        for (int[] move : knightMoves) {
            int x = position.x() + move[0];
            int y = position.y() + move[1];
            if (isTargetSquarePossible(x, y, isStartWhite)) {
                squares.add(new Square(x, y));
            }
        }
        return squares;
    }

    /**
     * Returns True if the pawn can capture other piece there or do en passant
     *
     * @param x x coordinate of target
     * @param y y coordinate of target
     * @return Either can or not
     */
    private boolean isPawnCaptureSquarePossible(int x, int y, boolean isStartWhite) {
        if (!isOnBoard(x, y)) {
            return false;
        }

        if (!PieceUtil.isEmpty(field.getBoard()[y][x]) && (PieceUtil.isWhite(field.getBoard()[y][x]) != isStartWhite)) {
            return true;
        }

        // EnPassant
        if (field.getPossibleEnPassantSquare() == null) {
            return false;
        }
        if (field.getPossibleEnPassantSquare().y() == y) {
            return field.getPossibleEnPassantSquare().x() == x;
        }
        return false;
    }

    /**
     * Checks if a given square (x, y) is a valid target for a pawn to move forward to
     *
     * @param x The x-coordinate of the square.
     * @param y The y-coordinate of the square.
     * @return true if the square is on the board and empty, false otherwise.
     */
    private boolean isPawnTargetSquarePossible(int x, int y) {
        if (isOnBoard(x, y)) {
            return PieceUtil.isEmpty(field.getBoard()[y][x]);
        }
        return false;
    }

    /**
     * Checks if this pawn moves for the first time
     *
     * @return true if yes, false otherwise
     */
    private boolean isPawnFirstMove(Square position, boolean isStartWhite) {
        if (isStartWhite) {
            return position.y() == 6;
        } else {
            return position.y() == 1;
        }
    }

    /**
     * Looks for all possible moves for this pawn
     *
     * @return ArrayList of squares where pawn can legally move
     */
    private List<Square> getPossiblePawnTargetSquares(Square position, boolean isStartWhite) {
        List<Square> squares = new ArrayList<>();

        int step = 1;
        if (!isStartWhite) {
            step = -1;
        }

        // Captures
        if (isPawnCaptureSquarePossible(position.x() - 1, position.y() - step, isStartWhite)) {
            squares.add(new Square(position.x() - 1, position.y() - step));
        }
        if (isPawnCaptureSquarePossible(position.x() + 1, position.y() - step, isStartWhite)) {
            squares.add(new Square(position.x() + 1, position.y() - step));
        }

        // Move forward
        if (isPawnTargetSquarePossible(position.x(), position.y() - step)) {
            squares.add(new Square(position.x(), position.y() - step));
            if (isPawnFirstMove(position, isStartWhite) && isPawnTargetSquarePossible(position.x(), position.y() - (step*2))) {
                squares.add(new Square(position.x(), position.y() - (step*2)));
            }
        }

        return squares;
    }

    /** Looks for all possible moves for this king
     */

    private List<Square> getPossibleKingTargetSquares(Square position, boolean isStartWhite) {
        List<Square> squares = new ArrayList<>();
        ArrayList<int[]> targets = new ArrayList<>();
        targets.add(new int[]{0, 1});
        targets.add(new int[]{0, -1});
        targets.add(new int[]{1, 1});
        targets.add(new int[]{1, -1});
        targets.add(new int[]{1, 0});
        targets.add(new int[]{-1, 1});
        targets.add(new int[]{-1, -1});
        targets.add(new int[]{-1, 0});

        byte castlingInfo = field.getCastlingInformation();
        byte kingSideFlag = isStartWhite ? CastlingUtil.WHITE_KING_SIDE : CastlingUtil.BLACK_KING_SIDE;
        byte queenSideFlag = isStartWhite ? CastlingUtil.WHITE_QUEEN_SIDE : CastlingUtil.BLACK_QUEEN_SIDE;

        // Check kingside castling - spaces between king (x) and rook (x+3) must be empty
        if (CastlingUtil.hasFlag(castlingInfo, kingSideFlag) &&
                PieceUtil.isEmpty(field.getPieceBySquare(new Square(position.x() + 1, position.y()))) &&
                PieceUtil.isEmpty(field.getPieceBySquare(new Square(position.x() + 2, position.y())))) {
            targets.add(new int[]{2, 0});
        }

        // Check queenside castling - spaces between king (x) and rook (x-4) must be empty
        if (CastlingUtil.hasFlag(castlingInfo, queenSideFlag) &&
                PieceUtil.isEmpty(field.getPieceBySquare(new Square(position.x() - 1, position.y()))) &&
                PieceUtil.isEmpty(field.getPieceBySquare(new Square(position.x() - 2, position.y()))) &&
                PieceUtil.isEmpty(field.getPieceBySquare(new Square(position.x() - 3, position.y())))) {
            targets.add(new int[]{-2, 0});
        }


        for (int[] move : targets) {
            int x = position.x() + move[0];
            int y = position.y() + move[1];
            if (isTargetSquarePossible(x, y, isStartWhite)) {
                squares.add(new Square(x, y));
            }
        }
        return squares;
    }

    /**
     * Looks what piece is on board and calls corresponding methods
     * checks are not calculated here !
     *
     * @return List of possible target squares
     */
    public List<Square> getTargetSquares(Square position, boolean isStartWhite) {
        byte piece = field.getBoard()[position.y()][position.x()];

        // TODO: replace with switch case

        if (PieceUtil.isEmpty(piece)) {
            return new ArrayList<Square>();
        }
        if (PieceUtil.isBishop(piece)) {
            return getPossibleBishopTargetSquares(position, isStartWhite);
        }
        if (PieceUtil.isRook(piece)) {
            return getPossibleRookTargetSquares(position, isStartWhite);
        }
        if (PieceUtil.isKnight(piece)) {
            return getPossibleKnightTargetSquares(position, isStartWhite);
        }
        if (PieceUtil.isQueen(piece)) {
            return getPossibleQueenTargetSquares(position, isStartWhite);
        }
        if (PieceUtil.isPawn(piece)) {
            return getPossiblePawnTargetSquares(position, isStartWhite);
        }
        if (PieceUtil.isKing(piece)) {
            return getPossibleKingTargetSquares(position, isStartWhite);
        }
        return new ArrayList<Square>();
    }

    /**
     * Checks if a given move is legal. This Method is inefficient.
     *
     * @param move The move to check.
     * @return {@code true} if the move is legal, {@code false} otherwise.
     */
    public boolean isMoveLegal(Move move) {

        if (move == null) {
            return false;
        }

        boolean isStartWhite = PieceUtil.isWhite(getPieceBySquare(move.startingSquare()));


        // check if player color is ok
        if (isStartWhite == field.isBlackTurn()) { // if colors are equal
            return false;
        }

        //Get possible targets
        List<Square> possibleTargets = getTargetSquares(move.startingSquare(), isStartWhite);
        if (possibleTargets.isEmpty()) {
            return false;
        }

        // look if target square is possible
        for (Square target : possibleTargets) {
            if (target.equals(move.targetSquare())) {
                //TODO  Check check
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieves a list of legal target squares for a piece at a given position.
     *
     * @param position The starting position of the piece.
     * @return A list of squares to which the piece can legally move.
     */
    public List<Square> getLegalTargetsSquares(Square position) {
        boolean isStartWhite = PieceUtil.isWhite(getPieceBySquare(position));
        List<Square> targets = getTargetSquares(position, isStartWhite);
        if (!targets.isEmpty()) {
            List<Square> legalTargets = new ArrayList<>();
            for (Square target : targets) {

                // We must not use an empty piece here, as a promotion to an empty piece would be illegal
                if (isMoveLegal(new Move(position, target, PieceUtil.QUEEN_MASK))) {
                    legalTargets.add(target);
                }
            }
            return legalTargets;
        }

        return new ArrayList<>();
    }

    /**
     * Determines the en passant target square produced by a pawn's double move, if applicable.
     *
     * @param move the Move object representing the pawn's movement
     * @return the Square that can be targeted for an en passant capture, or null if the move does not
     *         produce an en passant square (e.g., not a pawn, or not a double move)
     */
    public Square getEnPassantSquareProducedByPawnDoubleMove(Move move){
        byte piece = getPieceBySquare(move.targetSquare());
        if (PieceUtil.isPawn(piece)) {
            boolean isStartWhite = PieceUtil.isWhite(getPieceBySquare(move.targetSquare()));
            if (Math.abs(move.targetSquare().y() - move.startingSquare().y()) == 2) {
                return new Square(move.targetSquare().x(), move.targetSquare().y() + (isStartWhite ? 1 : -1));
            }
        }
        return null;
    }





    /**
     * Gets piece byte from board
     */
    private byte getPieceBySquare(Square square) {
        return field.getBoard()[square.y()][square.x()];
    }

    /**
     * Sets piece byte on board
     */
    private void setPieceBySquare(Square square, byte piece) {
        field.getBoard()[square.y()][square.x()] = piece;
    }

    public boolean isCastlingMove(Move move) {
        return PieceUtil.isKing(getPieceBySquare(move.targetSquare()))
                && Math.abs(move.targetSquare().x() - move.startingSquare().x()) == 2;
    }
}