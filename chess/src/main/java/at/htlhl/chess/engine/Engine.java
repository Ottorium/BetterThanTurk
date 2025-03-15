package at.htlhl.chess.engine;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;

import java.util.ArrayList;

public class Engine {

    private Field field;

    private Move currentBestMove = null;
    private int maxDepth;

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
        currentBestMove = null;
        maxDepth = 4;
        var eval = negaMax(maxDepth);
        return currentBestMove;
    }

    int negaMax(int depth) {
        if (depth == 0) return evaluateCurrentPosition();
        int bestScore = Integer.MIN_VALUE;
        for (var move : field.getLegalMoves()) {
            field.forceMove(move, false);
            var eval = -negaMax(depth - 1);
            field.undoMove(move);
            if (eval > bestScore) {
                bestScore = eval;
                if (depth == maxDepth)
                    currentBestMove = move;
            }
        }
        return bestScore;
    }


    private int evaluateCurrentPosition() {
        return field.isBlackTurn() ? -field.getPieceEvaluation() : field.getPieceEvaluation();
    }
}