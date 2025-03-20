package at.htlhl.chess.engine;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.GameState;
import at.htlhl.chess.boardlogic.Move;

public class Engine {

    private Field field;

    private Move currentBestMove = null;
    private int maxDepth;
    private int executedMoves = 0;
    private int evaluatedPositions = 0;

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
        evaluatedPositions = 0;
        executedMoves = 0;
        maxDepth = 5;
        var timeBefore = System.nanoTime();
        int eval;
        try {
            eval = minimax(maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            return null;
        }
        var nanoTime = System.nanoTime() - timeBefore;
        System.out.println("Engine finished calculating. Results:\n{\n" +
                "Depth: " + maxDepth +
                "\nTime elapsed: " + nanoTime + " ns" + " (=" + nanoTime / 1_000_000 + " ms)" +
                "\nMoves Executed: " + executedMoves +
                "\nPositions Evaluated: " + evaluatedPositions +
                "\nTime per move: " + nanoTime / executedMoves + " ns" +
                "\nEvaluation: " + eval +
                "\nBest Move: " + currentBestMove +
                "\n}\n");
        return currentBestMove;
    }

    private int minimax(int depth, int alpha, int beta) throws InterruptedException {
        if (depth == 0 || field.getGameState() != GameState.NOT_DECIDED) return evaluateCurrentPosition(depth);
        if (Thread.interrupted()) throw new InterruptedException();

        boolean isBlacksTurn = field.isBlackTurn();
        int bestScore = isBlacksTurn ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        for (var move : field.getLegalMoves()) {
            field.forceMove(move, false);
            executedMoves++;
            var eval = minimax(depth - 1, alpha, beta);
            field.undoMove();
            if (isBlacksTurn ? eval < bestScore : eval > bestScore) {
                bestScore = eval;
                if (depth == maxDepth)
                    currentBestMove = move;
            }

            //alpha-beta pruning
            if (isBlacksTurn) beta = Math.min(beta, eval);
            else alpha = Math.max(alpha, eval);

            if (beta <= alpha) break;
        }
        return bestScore;
    }

    private int evaluateCurrentPosition(int depth) {
        evaluatedPositions++;
        if (field.getGameState() != GameState.NOT_DECIDED) {
            if (field.getGameState() == GameState.DRAW)
                return 0;
            if (field.getGameState() == GameState.BLACK_WIN)
                return Integer.MIN_VALUE + maxDepth - depth;
            if (field.getGameState() == GameState.WHITE_WIN)
                return Integer.MAX_VALUE - maxDepth + depth;
        }
        return field.getPieceEvaluation();
    }
}