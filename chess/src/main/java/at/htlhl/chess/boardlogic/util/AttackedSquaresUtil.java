package at.htlhl.chess.boardlogic.util;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.boardlogic.Square;

import java.util.*;
import java.util.stream.IntStream;

public class AttackedSquaresUtil {

    MoveChecker moveChecker;
    byte[] board;
    private Field field;

    public AttackedSquaresUtil(Field field) {
        this.field = field;
        moveChecker = field.getMoveChecker();
        board = field.getBoard();
    }

    /**
     * Updates the cached Attack squares
     */
    public void updateCachedAttackSquares(Move move) {
        Square startingSquare = move.getStartingSquare();
        Square targetSquare = move.getTargetSquare();
        byte movedPiece = field.getPieceBySquare(targetSquare);
        boolean isWhite = PieceUtil.isWhite(movedPiece);

        // update the piece that got moved
        int boardIndexOfTargetSquare = targetSquare.y() * 8 + targetSquare.x();
        board[boardIndexOfTargetSquare] = move.getCapturedPiece();
        List<Square> targetSquaresToRemove = getAttackedSquares(startingSquare, isWhite, movedPiece);
        board[boardIndexOfTargetSquare] = movedPiece;

        List<Square> targetSquaresToAdd = getAttackedSquares(targetSquare, isWhite, movedPiece);
        removeAttackSquares(targetSquaresToRemove, isWhite);
        addAttackSquares(targetSquaresToAdd, isWhite);

        // remove the captured piece's attack squares
        if (move.isCapture()) {
            byte capturedPiece = move.getCapturedPiece();
            List<Square> targetSquaresToRemove2 = getAttackedSquares(targetSquare, isWhite == false, capturedPiece);
            removeAttackSquares(targetSquaresToRemove2, isWhite == false);
        }

        // go out from starting square and update the sliding pieces that now have more vision
        HashMap<int[], Boolean> directionsToAddTargetSquaresIn = getDirectionsOfChangedAttackSquares(startingSquare, isWhite, move.getTargetSquare());
        for (int[] dir : directionsToAddTargetSquaresIn.keySet()) {
            ArrayList<Square> targetSquaresToAddBecauseMovedPieceGaveMoreVision = new ArrayList<>();
            boolean isOpponent = directionsToAddTargetSquaresIn.get(dir);
            for (int i = 1; i < 8; i++) {
                int x = startingSquare.x() + dir[0] * i;
                int y = startingSquare.y() + dir[1] * i;
                if (!moveChecker.isOnBoard(x, y)) break;
                byte piece = board[y * 8 + x];
                if (PieceUtil.isEmpty(piece) == false) {
                    targetSquaresToAddBecauseMovedPieceGaveMoreVision.add(new Square(x, y));
                    break;
                }
                targetSquaresToAddBecauseMovedPieceGaveMoreVision.add(new Square(x, y));
            }
            addAttackSquares(targetSquaresToAddBecauseMovedPieceGaveMoreVision, isOpponent != isWhite);
        }

        // go out from target square and update the sliding pieces that got blocked
        if (move.isCapture() == false) {
            HashMap<int[], Boolean> directionsToRemoveTargetSquaresIn = getDirectionsOfChangedAttackSquares(targetSquare, isWhite, null);
            for (int[] dir : directionsToRemoveTargetSquaresIn.keySet()) {
                ArrayList<Square> targetSquaresToRemoveBecauseMovedPieceBlockedVision = new ArrayList<>();
                boolean isOpponent = directionsToRemoveTargetSquaresIn.get(dir);
                for (int i = 1; i < 8; i++) {
                    int x = targetSquare.x() + dir[0] * i;
                    int y = targetSquare.y() + dir[1] * i;
                    if (!moveChecker.isOnBoard(x, y)) break;
                    byte piece = board[y * 8 + x];
                    if (PieceUtil.isEmpty(piece) == false) {
                        targetSquaresToRemoveBecauseMovedPieceBlockedVision.add(new Square(x, y));
                        break;
                    }
                    targetSquaresToRemoveBecauseMovedPieceBlockedVision.add(new Square(x, y));
                }
                removeAttackSquares(targetSquaresToRemoveBecauseMovedPieceBlockedVision, isOpponent != isWhite);
            }
        }

        // lazy approach: calculate everything new, as castling and en passant moves don't occur that often.
        if (move.isCastlingMove() || move.isEnPassantMove()) {
            var whiteBefore = (HashMap<Square, Integer>) field.getWhiteAttackSquares().clone();
            field.setWhiteAttackSquares(findAttackedSquares(Player.WHITE));
            field.getChangesInLastMove().add(new FieldChange("whiteAttackSquares", undo -> field.setWhiteAttackSquares(whiteBefore)));

            var blackBefore = (HashMap<Square, Integer>) field.getBlackAttackSquares().clone();
            field.setBlackAttackSquares(findAttackedSquares(Player.BLACK));
            field.getChangesInLastMove().add(new FieldChange("blackAttackSquares", undo -> field.setBlackAttackSquares(blackBefore)));
        }


        // if it is a promotion, add the attacks of the promoted piece
        if (PieceUtil.isEmpty(move.getPromotionPiece()) == false) {
            addAttackSquares(getAttackedSquares(targetSquare, isWhite, move.getPromotionPiece()), isWhite);
        }
    }

    private HashMap<int[], Boolean> getDirectionsOfChangedAttackSquares(Square changedSquare, boolean isWhite, Square executedMoveTargetSquare) {
        var directions = new HashMap<int[], Boolean>();
        for (int[] dir : MoveChecker.slidingDirections) {
            for (int i = 1; i < 8; i++) {
                int x = changedSquare.x() + dir[0] * i;
                int y = changedSquare.y() + dir[1] * i;
                if (!moveChecker.isOnBoard(x, y)) break;
                if (Objects.equals(executedMoveTargetSquare, new Square(x, y))) break;

                byte piece = field.getBoard()[y * 8 + x];
                if (PieceUtil.isEmpty(piece)) continue;

                boolean isOpponent = PieceUtil.isWhite(piece) != isWhite;

                boolean isDiagonal = (dir[0] != 0 && dir[1] != 0);

                boolean pieceIsAPieceThatHasChangedAttackSquares = isDiagonal ?
                        PieceUtil.isQueen(piece) || PieceUtil.isBishop(piece) :
                        PieceUtil.isQueen(piece) || PieceUtil.isRook(piece);


                if (pieceIsAPieceThatHasChangedAttackSquares)
                    directions.put(new int[]{dir[0] * -1, dir[1] * -1}, isOpponent);
                break;
            }
        }
        return directions;
    }

    private List<Square> getAttackedSquares(Square squareOfPiece, boolean isWhite, byte piece) {
        // pawns are the only pieces to move differently when they capture
        if (PieceUtil.isPawn(piece)) {
            int step = isWhite ? 1 : -1;

            var pawnAttackSquares = Arrays.asList(
                    new Square(squareOfPiece.x() - 1, squareOfPiece.y() - step),
                    new Square(squareOfPiece.x() + 1, squareOfPiece.y() - step)
            );

            List<Square> squaresOnBoard = new ArrayList<>(2);
            for (Square square : pawnAttackSquares)
                if (field.getMoveChecker().isOnBoard(square.x(), square.y()))
                    squaresOnBoard.add(square);

            return squaresOnBoard;
        }

        return field.getMoveChecker().getTargetSquares(squareOfPiece, isWhite, piece, true);
    }

    private void addAttackSquares(List<Square> targetSquares, boolean isWhite) {
        HashMap<Square, Integer> attackMap = isWhite ? field.getWhiteAttackSquares() : field.getBlackAttackSquares();

        ArrayList<Runnable> undoActions = new ArrayList<>();

        for (Square target : targetSquares) {
            int oldCount = attackMap.getOrDefault(target, 0);
            int newCount = oldCount + 1;
            attackMap.put(target, newCount);

            undoActions.add(() -> {
                if (oldCount == 0) {
                    attackMap.remove(target);
                } else {
                    attackMap.put(target, oldCount);
                }
            });
        }

        field.getChangesInLastMove().add(new FieldChange(
                isWhite ? "whiteAttackSquares" : "blackAttackSquares",
                undo -> undoActions.forEach(Runnable::run)
        ));
    }

    private void removeAttackSquares(List<Square> targetSquares, boolean isWhite) {
        HashMap<Square, Integer> attackMap = isWhite ? field.getWhiteAttackSquares() : field.getBlackAttackSquares();

        ArrayList<Runnable> undoActions = new ArrayList<>();
        for (Square target : targetSquares) {
            int oldCount = attackMap.getOrDefault(target, 0);
            if (oldCount > 0) {
                int newCount = oldCount - 1;
                if (newCount > 0) {
                    attackMap.put(target, newCount);
                } else {
                    attackMap.remove(target);
                }

                undoActions.add(() -> attackMap.put(target, oldCount));
            }
        }

        field.getChangesInLastMove().add(new FieldChange(
                isWhite ? "whiteAttackSquares" : "blackAttackSquares",
                undo -> undoActions.forEach(Runnable::run)
        ));
    }

    /**
     * Finds the squares that the given player is attacking and how often they are attacked.
     */
    public HashMap<Square, Integer> findAttackedSquares(Player player) {
        var turnBefore = field.isBlackTurn();
        var blackTurn = player.equals(Player.BLACK);
        field.setBlackTurn(blackTurn);
        var board = field.getBoard();

        var startingSquares = new ArrayList<>(
                IntStream.range(0, board.length)
                        .filter(i -> PieceUtil.isBlack(board[i]) == blackTurn)
                        .mapToObj(Square::parseBoardIndex)
                        .toList()
        );

        var attackedSquares = new ArrayList<Square>();

        startingSquares.forEach(startingSquare ->
                attackedSquares.addAll(
                        getAttackedSquares(
                                startingSquare,
                                PieceUtil.isWhite(field.getPieceBySquare(startingSquare)),
                                field.getPieceBySquare(startingSquare)
                        )
                )
        );

        HashMap<Square, Integer> squareFrequency = new HashMap<>();
        for (Square square : attackedSquares)
            squareFrequency.put(square, squareFrequency.getOrDefault(square, 0) + 1);


        field.setBlackTurn(turnBefore);
        return squareFrequency;
    }

    public ArrayList<Pin> lookForPins(Player player) {
        boolean blackTurn = player != Player.WHITE;
        var kingPosition = getKingPositionOfPlayer(player);
        var kingX = kingPosition.x();
        var kingY = kingPosition.y();
        var pins = new ArrayList<Pin>();

        for (int[] dir : MoveChecker.slidingDirections) {
            Square thisPieceMayBePinned = null;

            for (int i = 1; i < 8; i++) {
                int x = kingX + dir[0] * i;
                int y = kingY + dir[1] * i;
                if (!moveChecker.isOnBoard(x, y)) break;
                byte piece = board[y * 8 + x];

                if (PieceUtil.isEmpty(piece)) continue;

                Square currentSquare = new Square(x, y);
                boolean isOpponent = PieceUtil.isWhite(piece) == blackTurn;

                if (!isOpponent) {
                    thisPieceMayBePinned = currentSquare;
                } else if (thisPieceMayBePinned != null) {
                    if (((dir[0] != 0 && dir[1] != 0) && (PieceUtil.isBishop(piece) || PieceUtil.isQueen(piece)))
                            || ((dir[0] == 0 || dir[1] == 0) && (PieceUtil.isRook(piece) || PieceUtil.isQueen(piece)))) {
                        pins.add(new Pin(
                                thisPieceMayBePinned,
                                currentSquare,
                                new ArrayList<>(Arrays.asList(dir, new int[]{dir[0] * -1, dir[1] * -1}))));
                    }
                }
            }
        }

        return pins;
    }

    private Square getKingPositionOfPlayer(Player player) {
        Square king1 = field.getCachedKingPositions().getFirst();
        boolean isWhiteKing1 = PieceUtil.isWhite(field.getPieceBySquare(king1));

        return (isWhiteKing1 == (player == Player.WHITE))
                ? king1
                : field.getCachedKingPositions().get(1);
    }

    public Check lookForCheck(Player player) {
        var kingPosition = getKingPositionOfPlayer(player == Player.WHITE ? Player.BLACK : Player.WHITE);
        boolean isStartWhite = PieceUtil.isWhite(field.getPieceBySquare(kingPosition));
        int kingX = kingPosition.x();
        int kingY = kingPosition.y();

        ArrayList<Check> checks = new ArrayList<>();

        for (int[] move : MoveChecker.knightMoves) {
            int x = kingX + move[0];
            int y = kingY + move[1];
            if (moveChecker.isOnBoard(x, y)) {
                byte piece = board[y * 8 + x];
                if (PieceUtil.isKnight(piece) && PieceUtil.isWhite(piece) != isStartWhite) {
                    checks.add(new Check(player, new ArrayList<>(), false));
                }
            }
        }


        for (int[] dir : MoveChecker.slidingDirections) {
            for (int i = 1; i < 8; i++) {
                int x = kingX + dir[0] * i;
                int y = kingY + dir[1] * i;
                if (!moveChecker.isOnBoard(x, y)) break;

                byte piece = board[y * 8 + x];
                if (PieceUtil.isEmpty(piece)) continue;

                boolean isOpponent = PieceUtil.isWhite(piece) != isStartWhite;
                if (!isOpponent) break;

                if ((dir[0] != 0 && dir[1] != 0) &&  // Diagonal
                        (PieceUtil.isBishop(piece) || PieceUtil.isQueen(piece))) {
                    checks.add(new Check(player, getPossibleBlockOrCaptureSquares(new Square(x, y), kingPosition), false));
                } else if ((dir[0] == 0 || dir[1] == 0) &&  // Straight
                        (PieceUtil.isRook(piece) || PieceUtil.isQueen(piece))) {
                    checks.add(new Check(player, getPossibleBlockOrCaptureSquares(new Square(x, y), kingPosition), false));
                }
                break; // Blocked by another piece
            }
        }

        int pawnDir = isStartWhite ? -1 : 1;  // White pawns attack up, black down
        int[] pawnXOffsets = {-1, 1};
        for (int xOffset : pawnXOffsets) {
            int x = kingX + xOffset;
            int y = kingY + pawnDir;
            if (moveChecker.isOnBoard(x, y)) {
                byte piece = board[y * 8 + x];
                if (PieceUtil.isPawn(piece) && PieceUtil.isWhite(piece) != isStartWhite) {
                    checks.add(new Check(player, new ArrayList<>(Arrays.asList(new Square(x, y))), false));
                }
            }
        }

        // we don't need king checks here as we don't look in the future and kings shouldn't touch

        if (checks.isEmpty())
            return null;
        if (checks.size() > 1)
            return new Check(player, new ArrayList<>(), true);
        return checks.getFirst();
    }

    private ArrayList<Square> getPossibleBlockOrCaptureSquares(Square attackerSquare, Square kingSquare) {
        ArrayList<Square> possibleSquares = new ArrayList<>();

        int attackerX = attackerSquare.x();
        int attackerY = attackerSquare.y();
        int kingX = kingSquare.x();
        int kingY = kingSquare.y();

        int dx = Integer.compare(attackerX, kingX);
        int dy = Integer.compare(attackerY, kingY);

        int currentX = kingX + dx;
        int currentY = kingY + dy;
        while (true) {
            Square square = new Square(currentX, currentY);
            possibleSquares.add(square);

            // break when the attacker's position is reached
            if (currentX == attackerX && currentY == attackerY) break;

            currentX += dx;
            currentY += dy;
        }

        return possibleSquares;
    }
}
