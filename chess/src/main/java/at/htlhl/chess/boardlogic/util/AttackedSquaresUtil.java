package at.htlhl.chess.boardlogic.util;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.boardlogic.Square;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AttackedSquaresUtil {

    private Field field;

    public AttackedSquaresUtil(Field field) {
        this.field = field;
    }

    /**
     * Updates the cached Attack squares
     */
    public void updateCachedAttackSquares(Move move) {
        byte piece = field.getPieceBySquare(move.getTargetSquare());
        boolean isWhite = PieceUtil.isWhite(piece);

        // update the piece that got moved
        List<Square> targetSquaresToRemove = field.getMoveChecker().getTargetSquares(move.getStartingSquare(), isWhite, piece);
        List<Square> targetSquaresToAdd = field.getMoveChecker().getTargetSquares(move.getTargetSquare(), isWhite, piece);
        removeAttackSquares(targetSquaresToRemove, isWhite);
        addAttackSquares(targetSquaresToAdd, isWhite);

        // remove the captured piece's attack squares
        if (move.isCapture()) {
            byte capturedPiece = move.getCapturedPiece();
            List<Square> targetSquaresToRemove2 = field.getMoveChecker().getTargetSquares(move.getTargetSquare(), isWhite == false, capturedPiece);
            removeAttackSquares(targetSquaresToRemove2, isWhite);
        }

        // go out from starting square and update the sliding pieces that now have more vision

        // go out from target square and update the sliding pieces that got blocked

        // if it is a castling move, remove the rooks attack squares and add its new ones

        // if it is an en passant move, remove the captured pawn's attacks

        // if it is a promotion, add the attacks of the promoted piece
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

        var attackedSquares = new ArrayList<>(
                field.getMoveChecker().getAllLegalMoves()
                        .stream()
                        .map(Move::getTargetSquare)
                        .toList()
        );

        HashMap<Square, Integer> squareFrequency = new HashMap<>();
        for (Square square : attackedSquares)
            squareFrequency.put(square, squareFrequency.getOrDefault(square, 0) + 1);


        field.setBlackTurn(turnBefore);
        return squareFrequency;
    }
}
