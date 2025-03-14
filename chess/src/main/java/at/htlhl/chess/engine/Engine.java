package at.htlhl.chess.engine;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;

public class Engine {

    private Field field;

    private Move currentBestMove = null;


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
        maxDepth = 3;
        var eval = getEvalOfBestMove(maxDepth);
        return currentBestMove;
    }
    private int maxDepth;

    private int getEvalOfBestMove(int depth) {

        if (depth == 0) return evaluateCurrentPosition();

        var allMoves = field.getMoveChecker().getAllLegalMoves();
        boolean blackTurn = field.isBlackTurn();
        var bestScore = blackTurn ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        var before = field.clone();
        for (Move move : allMoves) {
            field.forceMove(move, false);
            var eval = getEvalOfBestMove(depth - 1);
            field = before.clone();
            if (blackTurn ? eval < bestScore : eval > bestScore) {
                bestScore = eval;
                if (depth == maxDepth)
                    currentBestMove = move;
            }
        }
        return bestScore;
    }


    private int evaluateCurrentPosition() {
        return field.getPieceEvaluation();
    }
}