package at.htlhl.chess.engine;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.GameState;
import at.htlhl.chess.boardlogic.Move;

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
        int eval;
        try {
            eval = negaMax(maxDepth);
        } catch (InterruptedException e) {
            return null;
        }
        return currentBestMove;
    }

    int negaMax(int depth) throws InterruptedException {
        if (depth == 0) return evaluateCurrentPosition();
        if (Thread.interrupted()) throw new InterruptedException();
        int bestScore = Integer.MIN_VALUE;
        for (var move : field.getLegalMoves()) {
            field.forceMove(move, false);
            var eval = -negaMax(depth - 1);
            field.undoMove();
            if (eval > bestScore) {
                bestScore = eval;
                if (depth == maxDepth)
                    currentBestMove = move;
            }
        }
        return bestScore;
    }


    private int evaluateCurrentPosition() {
        if (field.getGameState() != GameState.NOT_DECIDED) {
            if (field.getGameState() == GameState.DRAW)
                return 0;
            if (field.getGameState() == GameState.BLACK_WIN)
                return field.isBlackTurn() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            if (field.getGameState() == GameState.WHITE_WIN)
                return field.isBlackTurn() ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        return field.isBlackTurn() ? -field.getPieceEvaluation() : field.getPieceEvaluation();
    }
}