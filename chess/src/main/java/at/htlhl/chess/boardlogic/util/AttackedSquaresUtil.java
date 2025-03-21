package at.htlhl.chess.boardlogic.util;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.boardlogic.Square;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AttackedSquaresUtil {

    private Field field;
    MoveChecker moveChecker;

    public AttackedSquaresUtil(Field field) {
        this.field = field;
        moveChecker = field.getMoveChecker();
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
        List<Square> targetSquaresToRemove = moveChecker.getTargetSquares(startingSquare, isWhite, movedPiece);
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
        HashMap<int[], Boolean> directionsToAddTargetSquaresIn = getDirectionsOfChangedAttackSquares(startingSquare, isWhite);
        for (int[] dir : directionsToAddTargetSquaresIn.keySet()) {
            ArrayList<Square> targetSquaresToAddBecauseMovedPieceGaveMoreVision = new ArrayList<>();
            boolean isOpponent = directionsToAddTargetSquaresIn.get(dir);
            for (int i =  isOpponent ? 1 : 0; i < 8; i++) {
                int x = startingSquare.x() + dir[0] * i;
                int y = startingSquare.y() + dir[1] * i;
                if (!moveChecker.isOnBoard(x, y)) break;
                byte piece = field.getBoard()[y * 8 + x];
                if (PieceUtil.isEmpty(piece) == false) {
                    // if the piece is a different color than the attacking piece
                    if (isOpponent == (PieceUtil.isWhite(piece) == isWhite))
                        targetSquaresToAddBecauseMovedPieceGaveMoreVision.add(new Square(x, y));
                    if (i != 0)
                        break;
                }
                targetSquaresToAddBecauseMovedPieceGaveMoreVision.add(new Square(x, y));
            }
            addAttackSquares(targetSquaresToAddBecauseMovedPieceGaveMoreVision, isOpponent != isWhite);
        }

        // go out from target square and update the sliding pieces that got blocked
        HashMap<int[], Boolean> directionsToRemoveTargetSquaresIn = getDirectionsOfChangedAttackSquares(targetSquare, isWhite);
        for (int[] dir : directionsToRemoveTargetSquaresIn.keySet()) {
            ArrayList<Square> targetSquaresToRemoveBecauseMovedPieceBlockedVision = new ArrayList<>();
            boolean isOpponent = directionsToRemoveTargetSquaresIn.get(dir);
            for (int i =  isOpponent ? 1 : 0; i < 8; i++) {
                int x = targetSquare.x() + dir[0] * i;
                int y = targetSquare.y() + dir[1] * i;
                if (!moveChecker.isOnBoard(x, y)) break;
                byte piece = field.getBoard()[y * 8 + x];
                if (PieceUtil.isEmpty(piece) == false) {
                    // if the piece is a different color than the attacking piece
                    if (isOpponent == (PieceUtil.isWhite(piece) == isWhite))
                        targetSquaresToRemoveBecauseMovedPieceBlockedVision.add(new Square(x, y));
                    if (i != 0)
                        break;
                }
                targetSquaresToRemoveBecauseMovedPieceBlockedVision.add(new Square(x, y));
            }

            if (move.isCapture() && isOpponent)
                addAttackSquares(new ArrayList<>(List.of(move.getTargetSquare())), !isWhite);

            removeAttackSquares(targetSquaresToRemoveBecauseMovedPieceBlockedVision, isOpponent != isWhite);
        }

        // if it is a castling move, remove the rooks attack squares and add its new ones

        // if it is an en passant move, remove the captured pawn's attacks

        // if it is a promotion, add the attacks of the promoted piece
    }

    private HashMap<int[], Boolean> getDirectionsOfChangedAttackSquares(Square changedSquare, boolean isWhite) {
        var directions = new HashMap<int[], Boolean>();
        for (int[] dir : MoveChecker.slidingDirections) {
            for (int i = 1; i < 8; i++) {
                int x = changedSquare.x() + dir[0] * i;
                int y = changedSquare.y() + dir[1] * i;
                if (!moveChecker.isOnBoard(x, y)) break;

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

        return field.getMoveChecker().getTargetSquares(squareOfPiece, isWhite, piece);
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
        field.setBlackTurn(player.equals(Player.BLACK));

        var startingSquares = new ArrayList<>(
                field.getMoveChecker().getAllLegalMoves()
                        .stream()
                        .map(Move::getStartingSquare)
                        .distinct()
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
}
