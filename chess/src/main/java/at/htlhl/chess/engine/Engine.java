package at.htlhl.chess.engine;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.GameState;
import at.htlhl.chess.boardlogic.Move;

import java.util.ArrayList;

public class Engine {

    private Field field;

    private ArrayList<EvaluatedMove> evaluatedMoves = null;
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
        return getBestMoves().getFirst().move();
    }

    public ArrayList<EvaluatedMove> getBestMoves() {
        evaluatedMoves = new ArrayList<>(15);
        evaluatedPositions = 0;
        executedMoves = 0;
        maxDepth = 5;
        var timeBefore = System.nanoTime();
        try {
            minimax(maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            return null;
        }
        var nanoTime = System.nanoTime() - timeBefore;

        evaluatedMoves.sort((m1, m2) ->
                field.isBlackTurn() ? Integer.compare(m1.evaluation(), m2.evaluation()) : Integer.compare(m2.evaluation(), m1.evaluation())
        );

        System.out.println("Engine finished calculating. Results:\n{\n" +
                "Depth: " + maxDepth +
                "\nTime elapsed: " + nanoTime + " ns" + " (=" + nanoTime / 1_000_000 + " ms)" +
                "\nMoves Executed: " + executedMoves +
                "\nPositions Evaluated: " + evaluatedPositions +
                "\nTime per move: " + nanoTime / executedMoves + " ns" +
                "\nEvaluation: " + evaluatedMoves.getFirst().evaluation() +
                "\nBest Moves: " + evaluatedMoves +
                "\n}\n");

        return evaluatedMoves;
    }

    private int minimax(int depth, int alpha, int beta) throws InterruptedException {
        if (depth == 0 || field.getGameState() != GameState.NOT_DECIDED) return evaluateCurrentPosition(depth);
        if (Thread.interrupted()) throw new InterruptedException();

        boolean isBlacksTurn = field.isBlackTurn();
        int bestScore = isBlacksTurn ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        var moves = field.getLegalMoves();
        orderMoves(moves);
        for (var move : moves) {

            field.forceMove(move, false);
            executedMoves++;
            var eval = minimax(depth - 1, alpha, beta);
            field.undoMove();

            if (isBlacksTurn ? eval < bestScore : eval > bestScore)
                bestScore = eval;
            if (depth == maxDepth)
                evaluatedMoves.add(new EvaluatedMove(move, eval));

            //alpha-beta pruning
            if (isBlacksTurn)
                beta = Math.min(beta, eval);
            else
                alpha = Math.max(alpha, eval);
            if (beta <= alpha) break;
        }
        return bestScore;
    }

    private void orderMoves(ArrayList<Move> legalMoves) {
        legalMoves.sort((move1, move2) -> {
            if (move1.isCapture() != move2.isCapture()) return move1.isCapture() ? -1 : 1;
            return 0;
        });
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