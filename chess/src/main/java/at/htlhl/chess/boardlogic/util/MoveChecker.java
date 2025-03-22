package at.htlhl.chess.boardlogic.util;

import at.htlhl.chess.boardlogic.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to check possible moves for different chess pieces.
 */
public class MoveChecker {
    public static final int[][] knightMoves = {{1, -2}, {1, 2}, {-1, -2}, {-1, 2}, {2, -1}, {2, 1}, {-2, 1}, {-2, -1}};
    public static final int[][] rookDirections = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    public static final int[][] bishopDirections = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    public static final int[][] kingDirections = {{0, 1}, {0, -1}, {1, 1}, {1, -1}, {1, 0}, {-1, 1}, {-1, -1}, {-1, 0}};
    public static final int[][] slidingDirections = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    public static final byte[] whitePromotionPieces = {PieceUtil.WHITE_QUEEN, PieceUtil.WHITE_ROOK, PieceUtil.WHITE_BISHOP, PieceUtil.WHITE_KNIGHT};
    public static final byte[] blackPromotionPieces = {PieceUtil.BLACK_QUEEN, PieceUtil.BLACK_ROOK, PieceUtil.BLACK_BISHOP, PieceUtil.BLACK_KNIGHT};
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
    public boolean isOnBoard(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    /**
     * Checks if a target square is a valid move.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return True if the target square is empty or occupied by an opponent's piece.
     */
    private boolean isTargetSquarePossible(int x, int y, boolean isStartWhite, boolean captureOwnPieces) {
        if (!isOnBoard(x, y)) return false;

        if (captureOwnPieces) return true;

        byte square = field.getBoard()[y * 8 + x];
        return PieceUtil.isEmpty(square) ||
                PieceUtil.isWhite(square) != isStartWhite;
    }

    /**
     * Gets the possible target squares for a bishop.
     *
     * @return A list of valid squares the bishop can move to.
     */
    private List<Square> getPossibleBishopTargetSquares(Square position, boolean isStartWhite, boolean captureOwnPieces) {
        List<Square> squares = new ArrayList<>();

        for (int[] dir : bishopDirections) {
            for (int i = 1; i < 8; i++) {
                int x = position.x() + dir[0] * i;
                int y = position.y() + dir[1] * i;
                if (isTargetSquarePossible(x, y, isStartWhite, captureOwnPieces)) {
                    squares.add(new Square(x, y));
                    if (!PieceUtil.isEmpty(field.getBoard()[y * 8 + x])) {
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
    private List<Square> getPossibleRookTargetSquares(Square position, boolean isStartWhite, boolean captureOwnPieces) {
        List<Square> squares = new ArrayList<>();

        for (int[] dir : rookDirections) {
            for (int i = 1; i < 8; i++) {
                int x = position.x() + dir[0] * i;
                int y = position.y() + dir[1] * i;
                if (isTargetSquarePossible(x, y, isStartWhite, captureOwnPieces)) {
                    squares.add(new Square(x, y));
                    if (!PieceUtil.isEmpty(field.getBoard()[y * 8 + x])) {
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
    private List<Square> getPossibleQueenTargetSquares(Square position, boolean isStartWhite, boolean captureOwnPieces) {
        List<Square> squares = new ArrayList<>();
        squares.addAll(getPossibleBishopTargetSquares(position, isStartWhite, captureOwnPieces));
        squares.addAll(getPossibleRookTargetSquares(position, isStartWhite, captureOwnPieces));
        return squares;
    }

    /**
     * Gets the possible target squares for a knight.
     *
     * @return A list of valid squares the knight can move to.
     */
    private List<Square> getPossibleKnightTargetSquares(Square position, boolean isStartWhite, boolean captureOwnPieces) {
        List<Square> squares = new ArrayList<>();


        for (int[] move : knightMoves) {
            int x = position.x() + move[0];
            int y = position.y() + move[1];
            if (isTargetSquarePossible(x, y, isStartWhite, captureOwnPieces)) {
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

        if (!PieceUtil.isEmpty(field.getBoard()[y * 8 + x]) && (PieceUtil.isWhite(field.getBoard()[y * 8 + x]) != isStartWhite)) {
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
            return PieceUtil.isEmpty(field.getBoard()[y * 8 + x]);
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
    private List<Square> getPossiblePawnTargetSquares(Square position, boolean isStartWhite, boolean captureOwnPieces) {
        if (captureOwnPieces) throw new UnsupportedOperationException("Capture own pieces are not supported in Pawn move");

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
            if (isPawnFirstMove(position, isStartWhite) && isPawnTargetSquarePossible(position.x(), position.y() - (step * 2))) {
                squares.add(new Square(position.x(), position.y() - (step * 2)));
            }
        }

        return squares;
    }

    /**
     * Looks for all possible moves for this king
     */

    private List<Square> getPossibleKingTargetSquares(Square position, boolean isStartWhite, boolean captureOwnPieces) {
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
            // captureOwnPieces is used for attack squares, and castling is not possible if there is an enemy piece
            if (captureOwnPieces == false)
                targets.add(new int[]{2, 0});
        }

        // Check queenside castling - spaces between king (x) and rook (x-4) must be empty
        if (CastlingUtil.hasFlag(castlingInfo, queenSideFlag) &&
                PieceUtil.isEmpty(field.getPieceBySquare(new Square(position.x() - 1, position.y()))) &&
                PieceUtil.isEmpty(field.getPieceBySquare(new Square(position.x() - 2, position.y()))) &&
                PieceUtil.isEmpty(field.getPieceBySquare(new Square(position.x() - 3, position.y())))) {
            // captureOwnPieces is used for attack squares, and castling is not possible if there is an enemy piece
            if (captureOwnPieces == false)
                targets.add(new int[]{-2, 0});
        }


        for (int[] move : targets) {
            int x = position.x() + move[0];
            int y = position.y() + move[1];
            if (isTargetSquarePossible(x, y, isStartWhite, captureOwnPieces)) {
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
    public List<Square> getTargetSquares(Square position, boolean isStartWhite, byte piece) {
        return getTargetSquares(position, isStartWhite, piece, false);
    }

    public List<Square> getTargetSquares(Square position, boolean isStartWhite, byte piece, boolean captureOwnPieces) {

        // TODO: replace with switch case

        if (PieceUtil.isEmpty(piece)) {
            return new ArrayList<>();
        }
        if (PieceUtil.isBishop(piece)) {
            return getPossibleBishopTargetSquares(position, isStartWhite, captureOwnPieces);
        }
        if (PieceUtil.isRook(piece)) {
            return getPossibleRookTargetSquares(position, isStartWhite, captureOwnPieces);
        }
        if (PieceUtil.isKnight(piece)) {
            return getPossibleKnightTargetSquares(position, isStartWhite, captureOwnPieces);
        }
        if (PieceUtil.isQueen(piece)) {
            return getPossibleQueenTargetSquares(position, isStartWhite, captureOwnPieces);
        }
        if (PieceUtil.isPawn(piece)) {
            return getPossiblePawnTargetSquares(position, isStartWhite, captureOwnPieces);
        }
        if (PieceUtil.isKing(piece)) {
            return getPossibleKingTargetSquares(position, isStartWhite, captureOwnPieces);
        }
        return new ArrayList<>();
    }

    /**
     * Checks information about the move and edits it's information
     * isMoveValid, isThatEnPassant, what check will appear
     *
     * @param move The move to check.
     */
    public void validateMove(Move move) {
        var position = move.getStartingSquare();
        validateMove(move, getTargetSquares(position, PieceUtil.isWhite(getPieceBySquare(move.getStartingSquare())), field.getBoard()[position.y() * 8 + position.x()]));
    }

    private void validateMove(Move move, List<Square> possibleTargets) {

        if (move == null) {
            throw new NullPointerException("Move cannot be null");
        }

        boolean isStartWhite = PieceUtil.isWhite(getPieceBySquare(move.getStartingSquare()));


        // check if player color is ok
        if (isStartWhite == field.isBlackTurn()) { // if colors are equal
            move.setLegal(false);
            return;
        }

        if (PieceUtil.isEmpty(move.getPromotionPiece()) == false && PieceUtil.isBlack(move.getPromotionPiece()) != field.isBlackTurn()) {
            move.setLegal(false);
            return;
        }

        if (possibleTargets.isEmpty()) {
            move.setLegal(false);
            return;
        }

        // look for move type
        gatherMoveInfo(move);

        // look if target square is possible
        for (Square target : possibleTargets) {
            if (target.equals(move.getTargetSquare())) {


                //Looks for checks between castling
                if (move.isCastlingMove()) {
                    if (field.getPlayerInCheck() != null) {
                        move.setLegal(false);
                        return;
                    }
                    if (isThereNoChecksOnCastlingPath(move) == false) {
                        move.setLegal(false);
                        return;
                    }
                }
                // Look for check
                List<Player> appearedChecks = lookForChecksInMove(move);
                if (isCheckLegal(appearedChecks)) {
                    move.setLegal(true);

                    move.setCapturedPiece(field.getPieceBySquare(move.getTargetSquare()));
                    return;
                }
            }
        }

        move.setLegal(false);
    }

    /**
     * Looks for all checks on castling path
     *
     * @param move
     * @return true, if there is no check, false, if there is a check
     */
    private boolean isThereNoChecksOnCastlingPath(Move move) {
        // get direction
        // Determine castling direction and rook starting position
        int kingMoveDistance = move.getTargetSquare().x() - move.getStartingSquare().x();
        int yRank = move.getStartingSquare().y();
        Square currentKingPosition = move.getStartingSquare();
        byte king = getPieceBySquare(currentKingPosition);

        boolean noChecks = true;

        if (kingMoveDistance > 0) {
            // try one square
            Square kingSquare = new Square(move.getStartingSquare().x() + 1, yRank);
            setPieceBySquare(move.getStartingSquare(), PieceUtil.EMPTY);
            setPieceBySquare(kingSquare, king);
            if (isKingChecked(kingSquare)) {
                noChecks = false;
            }
            setPieceBySquare(kingSquare, PieceUtil.EMPTY);
            setPieceBySquare(move.getStartingSquare(), king);
        } else {
            // try first square
            Square kingSquare = new Square(move.getStartingSquare().x() - 1, yRank);
            setPieceBySquare(move.getStartingSquare(), PieceUtil.EMPTY);
            setPieceBySquare(kingSquare, king);
            if (isKingChecked(kingSquare)) {
                noChecks = false;
            }
            setPieceBySquare(kingSquare, PieceUtil.EMPTY);

            // try second square
            kingSquare = new Square(move.getStartingSquare().x() - 2, yRank);
            setPieceBySquare(kingSquare, king);
            if (isKingChecked(kingSquare)) {
                noChecks = false;
            }
            setPieceBySquare(kingSquare, PieceUtil.EMPTY);
            setPieceBySquare(move.getStartingSquare(), king);
        }

        return noChecks;
    }

    /**
     * Looks for checks produced by EnPassant move
     *
     * @param move to look at
     * @return appeared Check. It must be legal
     */

    private List<Player> lookForChecksInEnPassant(Move move) {
        Square possibleEnPassantSquare = field.getPossibleEnPassantSquare();
        Square deletedPawn = field.isBlackTurn() ? new Square(possibleEnPassantSquare.x(), possibleEnPassantSquare.y() - 1) : new Square(possibleEnPassantSquare.x(), possibleEnPassantSquare.y() + 1);
        byte startingPawn = getPieceBySquare(move.getStartingSquare());
        byte opponentPawn = getPieceBySquare(deletedPawn);
        // simulating move
        setPieceBySquare(move.getStartingSquare(), PieceUtil.EMPTY);
        setPieceBySquare(move.getTargetSquare(), startingPawn);
        setPieceBySquare(deletedPawn, PieceUtil.EMPTY);

        // check if checks are legal
        List<Player> appearedChecks = lookForChecksOnBoard();

        // move everything back
        setPieceBySquare(move.getStartingSquare(), startingPawn);
        setPieceBySquare(move.getTargetSquare(), PieceUtil.EMPTY);
        setPieceBySquare(deletedPawn, opponentPawn);

        return appearedChecks;
    }

    /**
     * Adds Catling and enPassant information to move
     *
     * @param move
     */
    private void gatherMoveInfo(Move move) {
        move.setCapturedPiece(getPieceBySquare(move.getTargetSquare()));
        // Castling
        if (isCastlingMove(move)) {
            move.setCastlingMove(true);
        } else {

            //En passant move is move when pawn does capture
            if (move.getTargetSquare().equals(field.getPossibleEnPassantSquare())
                    && PieceUtil.isPawn(getPieceBySquare(move.getStartingSquare()))) {
                move.setEnPassantMove(true);
            } else {

                // EnPassant possible square
                move.setPossibleEnPassantSquare(getEnPassantSquareProducedByPawnDoubleMove(move));
            }
        }
    }

    /**
     * Retrieves a list of legal target squares for a piece at a given position.
     *
     * @param position The starting position of the piece.
     * @return A list of squares to which the piece can legally move.
     */
    public List<Square> getLegalTargetsSquares(Square position) {
        return getAllLegalMoves().stream().filter(move -> move.getStartingSquare().equals(position)).map(Move::getTargetSquare).collect(Collectors.toList());
    }

    /**
     * Determines the en passant target square produced by a pawn's double move, if applicable.
     *
     * @param move the Move object representing the pawn's movement
     * @return the Square that can be targeted for an en passant capture, or null if the move does not
     * produce an en passant square (e.g., not a pawn, or not a double move)
     */
    private Square getEnPassantSquareProducedByPawnDoubleMove(Move move) {
        byte piece = getPieceBySquare(move.getStartingSquare());
        if (PieceUtil.isPawn(piece)) {
            boolean isStartWhite = PieceUtil.isWhite(getPieceBySquare(move.getStartingSquare()));
            if (Math.abs(move.getTargetSquare().y() - move.getStartingSquare().y()) == 2) {
                return new Square(move.getTargetSquare().x(), move.getTargetSquare().y() + (isStartWhite ? 1 : -1));
            }
        }
        return null;
    }

    /**
     * Search from king position to determine if it's in check
     *
     * @param position King position
     * @return true if king is checked, false otherwise
     */
    public boolean isKingChecked(Square position) {
        boolean isStartWhite = PieceUtil.isWhite(getPieceBySquare(position));
        byte[] board = field.getBoard();
        int kingX = position.x();
        int kingY = position.y();

        for (int[] move : knightMoves) {
            int x = kingX + move[0];
            int y = kingY + move[1];
            if (isOnBoard(x, y)) {
                byte piece = board[y * 8 + x];
                if (PieceUtil.isKnight(piece) && PieceUtil.isWhite(piece) != isStartWhite) {
                    return true;
                }
            }
        }


        for (int[] dir : slidingDirections) {
            for (int i = 1; i < 8; i++) {
                int x = kingX + dir[0] * i;
                int y = kingY + dir[1] * i;
                if (!isOnBoard(x, y)) break;

                byte piece = board[y * 8 + x];
                if (PieceUtil.isEmpty(piece)) continue;

                boolean isOpponent = PieceUtil.isWhite(piece) != isStartWhite;
                if (!isOpponent) break;

                if ((dir[0] != 0 && dir[1] != 0) &&  // Diagonal
                        (PieceUtil.isBishop(piece) || PieceUtil.isQueen(piece))) {
                    return true;
                } else if ((dir[0] == 0 || dir[1] == 0) &&  // Straight
                        (PieceUtil.isRook(piece) || PieceUtil.isQueen(piece))) {
                    return true;
                }
                break; // Blocked by another piece
            }
        }

        int pawnDir = isStartWhite ? -1 : 1;  // White pawns attack up, black down
        int[] pawnXOffsets = {-1, 1};
        for (int xOffset : pawnXOffsets) {
            int x = kingX + xOffset;
            int y = kingY + pawnDir;
            if (isOnBoard(x, y)) {
                byte piece = board[y * 8 + x];
                if (PieceUtil.isPawn(piece) && PieceUtil.isWhite(piece) != isStartWhite) {
                    return true;
                }
            }
        }

        for (int[] dir : kingDirections) {
            int x = kingX + dir[0];
            int y = kingY + dir[1];
            if (isOnBoard(x, y)) {
                byte piece = board[y * 8 + x];
                if (PieceUtil.isKing(piece) && PieceUtil.isWhite(piece) != isStartWhite) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Simulates move and looks if checks appeared. Edge cases like castling are not handled here now
     *
     * @param move that will be simulated
     * @return List<Player> of appeared checks
     */

    private List<Player> lookForChecksInMove(Move move) {

        // If move is en passant, there is another method
        if (move.isEnPassantMove()) {
            return lookForChecksInEnPassant(move);
        }

        //save pieces to revert move
        byte savedTargetPiece = getPieceBySquare(move.getTargetSquare());
        byte savedStartingPiece = getPieceBySquare(move.getStartingSquare());

        ArrayList<Square> kingsBefore = field.getCachedKingPositions();
        if (PieceUtil.isKing(savedStartingPiece))
            field.setCachedKingPositions(null);

        // make move, because it must be legal
        setPieceBySquare(move.getTargetSquare(), savedStartingPiece);
        setPieceBySquare(move.getStartingSquare(), PieceUtil.EMPTY);

        List<Player> checkedPlayers = lookForChecksOnBoard();

        // fix the board
        setPieceBySquare(move.getTargetSquare(), savedTargetPiece);
        setPieceBySquare(move.getStartingSquare(), savedStartingPiece);

        field.setCachedKingPositions(kingsBefore);

        return checkedPlayers;
    }

    /**
     * Looks for current checks On Board
     *
     * @return List<Player> that are checked
     */

    public List<Player> lookForChecksOnBoard() {
        List<Player> checkedPlayers = new ArrayList<>();
        // look for checks
        ArrayList<Square> kings = field.getCachedKingPositions() == null ? findKings() : field.getCachedKingPositions();
        for (Square king : kings) {
            boolean isWhite = PieceUtil.isWhite(getPieceBySquare(king));
            if (isKingChecked(king)) {
                if (isWhite) {
                    checkedPlayers.add(Player.WHITE);
                } else {
                    checkedPlayers.add(Player.BLACK);
                }
            }
        }
        return checkedPlayers;
    }

    ;

    /**
     * Finds both kings on board
     *
     * @return Square array of king's positions
     */
    public ArrayList<Square> findKings() {
        var kings = new ArrayList<Square>();
        var board = field.getBoard();
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (PieceUtil.isKing(board[i * 8 + j])) {
                    kings.add(new Square(j, i));
                    if (kings.size() >= 2)
                        return kings;
                }
        return kings;
    }

    ;

    /**
     * Looks if the check is legal in such way
     * For example, if only one check is on board, or if the check appeared after your move
     *
     * @return true if the check is legal
     */
    private boolean isCheckLegal(List<Player> possibleChecks) {
        if (possibleChecks.size() > 1) {
            return false;
        }
        if (possibleChecks.isEmpty()) {
            return true;
        }


        //if your color check appeared after your move
        if (field.getPlayerInCheck() == null) {
            if (field.isBlackTurn()) {
                return !possibleChecks.getFirst().equals(Player.BLACK);
            } else {
                return !possibleChecks.getFirst().equals(Player.WHITE);
            }
        }

        //if your check did not disappear
        if (field.getPlayerInCheck().equals(possibleChecks.getFirst())) {
            return false;
        }
        return true;
    }


    /**
     * Gets piece byte from board
     */
    private byte getPieceBySquare(Square square) {
        return field.getBoard()[square.y() * 8 + square.x()];
    }

    /**
     * Sets piece byte on board
     */
    private void setPieceBySquare(Square square, byte piece) {
        field.getBoard()[square.y() * 8 + square.x()] = piece;
    }

    private boolean isCastlingMove(Move move) {
        return PieceUtil.isKing(getPieceBySquare(move.getStartingSquare()))
                && Math.abs(move.getTargetSquare().x() - move.getStartingSquare().x()) == 2;
    }

    /**
     * Gets all legal moves for the current player's turn
     *
     * @return List of all legal moves possible in the current position
     */
    public ArrayList<Move> getAllLegalMoves() {
        ArrayList<Move> legalMoves = new ArrayList<>();

        if (field.getGameState() != GameState.NOT_DECIDED)
            return legalMoves;

        // Iterate through all squares on the board
        // this is not very efficient, but to do it different, we would need to save the board piece centric, which... well, its work...
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                Square start = new Square(x, y);
                byte piece = getPieceBySquare(start);

                if (PieceUtil.isEmpty(piece) || PieceUtil.isWhite(piece) == field.isBlackTurn())
                    continue;
                List<Square> possibleTargets = getTargetSquares(start, PieceUtil.isWhite(piece), field.getBoard()[start.y() * 8 + start.x()]);
                for (Square target : possibleTargets) {
                    if (PieceUtil.isPawn(piece) && (target.y() == 0 || target.y() == 7))
                        for (byte promotionPiece : field.isBlackTurn() ? blackPromotionPieces : whitePromotionPieces) {
                            Move move = new Move(start, target);
                            move.setPromotionPiece(promotionPiece);
                            validateMove(move, possibleTargets);  // This sets the move's legal status and other properties
                            if (move.isLegal()) legalMoves.add(move);
                        }
                    else {
                        Move move = new Move(start, target);
                        validateMove(move, possibleTargets);  // This sets the move's legal status and other properties
                        if (move.isLegal()) legalMoves.add(move);
                    }
                }
            }
        }

        return legalMoves;
    }
}