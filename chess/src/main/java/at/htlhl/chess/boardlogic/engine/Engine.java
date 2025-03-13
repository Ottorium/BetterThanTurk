package at.htlhl.chess.boardlogic.engine;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;

public class Engine {

    private Field field;


    public Engine() {
        this(new Field());
    }

    public Engine(Field field) {
        this.field = field.clone();
    }

    public void setField(Field field) {
        this.field = field.clone();
    }

    public Move getBestMove(String fen) {
        field.trySetFEN(fen);
        return getBestMove();
    }

    public Move getBestMove() {
        if (field.isBlackTurn()) return null;
        Move bestMove = null;
        var bestScore = Integer.MIN_VALUE;
        for (Move move : field.getMoveChecker().getAllLegalMoves()) {
            var score = maxi(3);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        System.out.println(bestScore);
        System.out.println(bestMove);
        return bestMove;
    }

    int maxi(int depth) {
        if (depth == 0) return evaluateCurrentPosition();
        int max = Integer.MIN_VALUE;
        for (var move : field.getMoveChecker().getAllLegalMoves()) {
            Field before = field.clone();
            field.forceMove(move, false);
            var score = mini(depth - 1);
            max = Math.max(score, max);
            field = before;
        }
        return max;
    }

    int mini(int depth) {
        if (depth == 0) return -evaluateCurrentPosition();
        int min = Integer.MAX_VALUE;
        for (var move : field.getMoveChecker().getAllLegalMoves()) {
            Field before = field.clone();
            field.forceMove(move, false);
            var score = maxi(depth - 1);
            min = Math.min(min, score);
            field = before;
        }
        return min;
    }


    private int evaluateCurrentPosition() {
        return field.getPieceEvaluation();
    }
}