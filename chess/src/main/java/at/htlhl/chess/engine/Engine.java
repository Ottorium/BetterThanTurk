package at.htlhl.chess.engine;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.GameState;
import at.htlhl.chess.boardlogic.Move;

public class Engine {

    private Field field;

    private Move currentBestMove = null;
    private int maxDepth;
    private int executedMoves = 0;

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
        executedMoves = 0;
        maxDepth = 4;
        var timeBefore = System.nanoTime();
        int eval;
        try {
            eval = negaMax(maxDepth);
        } catch (InterruptedException e) {
            return null;
        }
        var nanoTime = System.nanoTime() - timeBefore;
        System.out.println("Engine finished calculating. Results:\n{\n" +
                "Depth: " + maxDepth +
                "\nTime elapsed: " + nanoTime + " ns" + " (=" + nanoTime / 1_000_000 + " ms)" +
                "\nMoves Executed: " + executedMoves +
                "\nTime per move: " + nanoTime / executedMoves + " ns" +
                "\nEvaluation: " + eval +
                "\nBest Move: " + currentBestMove +
                "\n}\n");
        return currentBestMove;
    }

    int negaMax(int depth) throws InterruptedException {
        if (depth == 0) return evaluateCurrentPosition();
        if (Thread.interrupted()) throw new InterruptedException();
        int bestScore = Integer.MIN_VALUE;
        for (var move : field.getLegalMoves()) {
            field.forceMove(move, false);
            executedMoves++;
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