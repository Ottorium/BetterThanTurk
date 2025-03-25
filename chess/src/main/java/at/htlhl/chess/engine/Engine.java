package at.htlhl.chess.engine;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.GameState;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.util.PieceUtil;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Engine {

    public static final long DEFAULT_THINKING_TIME = 5_000_000_000l;
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

    public ArrayList<EvaluatedMove> getBestMoves(long thinkingTimeNS) {
        evaluatedMoves = new ArrayList<>(15);
        evaluatedPositions = 0;
        executedMoves = 0;
        maxDepth = 2;
        var timeBefore = System.nanoTime();


        try {
            while (true) {
                firstIteration(maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, timeBefore + thinkingTimeNS);
                maxDepth++;
            }
        } catch (InterruptedException e) {
            return null;
        } catch (TimeoutException e) {

            var nanoTime = System.nanoTime() - timeBefore;
            System.out.println("Engine finished calculating. Results:\n{\n" +
                    "Depth: " + (maxDepth - 1) +
                    "\nTime elapsed: " + nanoTime + " ns" + " (=" + nanoTime / 1_000_000 + " ms)" +
                    "\nMoves Executed: " + executedMoves +
                    "\nPositions Evaluated: " + evaluatedPositions +
                    "\nTime per move: " + nanoTime / executedMoves + " ns" +
                    "\nEvaluation: " + evaluatedMoves.getFirst().evaluation() +
                    "\nBest Moves: " + evaluatedMoves +
                    "\n}\n");

            return evaluatedMoves;
        }
    }

    private void firstIteration(int maxDepth, int alpha, int beta, long endNanoTime) throws InterruptedException, TimeoutException{
        var newEvaluatedMoves = new ArrayList<EvaluatedMove>(30);
        boolean isBlacksTurn = field.isBlackTurn();
        int bestScore = isBlacksTurn ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        var moves = field.getLegalMoves();
        orderMoves(moves);
        for (var move : moves) {

            int eval;
            try {
                field.forceMove(move, false);
                executedMoves++;
                eval = minimax(maxDepth - 1, alpha, beta, endNanoTime);
            } catch (RuntimeException e) {
                // look at the exception message for further info
                eval = isBlacksTurn ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            }
            field.undoMove();

            if (isBlacksTurn ? eval < bestScore : eval > bestScore)
                bestScore = eval;

            newEvaluatedMoves.add(new EvaluatedMove(move, eval));

            //alpha-beta pruning
            if (isBlacksTurn)
                beta = Math.min(beta, eval);
            else
                alpha = Math.max(alpha, eval);
            if (beta <= alpha) {
                // TODO: add the rest of the moves to newEvaluatedMoves with worst score
                break;
            }
        }
        evaluatedMoves = newEvaluatedMoves;
        evaluatedMoves.sort((m1, m2) ->
                field.isBlackTurn() ? Integer.compare(m1.evaluation(), m2.evaluation()) : Integer.compare(m2.evaluation(), m1.evaluation())
        );
    }

    public ArrayList<EvaluatedMove> getBestMoves() {
        return getBestMoves(DEFAULT_THINKING_TIME);
    }

    private int minimax(int depth, int alpha, int beta, long endTime) throws InterruptedException, TimeoutException {
        if (depth == 0 || field.getGameState() != GameState.NOT_DECIDED) return evaluateCurrentPosition(depth);
        if (Thread.interrupted()) throw new InterruptedException();
        if (System.nanoTime() > endTime) throw new TimeoutException();

        boolean isBlacksTurn = field.isBlackTurn();
        int bestScore = isBlacksTurn ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        var moves = field.getLegalMoves();
        orderMoves(moves);
        for (var move : moves) {

            int eval;
            try {
                field.forceMove(move, false);
                executedMoves++;
                eval = minimax(depth - 1, alpha, beta, endTime);
            } catch (RuntimeException e) {
                // look at the exception message for further info
                eval = isBlacksTurn ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            }
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

        int material = field.getPieceEvaluation();
        int positional = getPositionalValue();
        return material + positional;
    }

    private int getPositionalValue() {
        int positional = 0;
        byte[] board = field.getBoard();
        for (int i = 0; i < 64; i++) {
            byte piece = board[i];
            if (piece == PieceUtil.EMPTY) continue;

            int pieceType;
            if (PieceUtil.isPawn(piece)) pieceType = 0;
            else if (PieceUtil.isKnight(piece)) pieceType = 1;
            else if (PieceUtil.isBishop(piece)) pieceType = 2;
            else if (PieceUtil.isRook(piece)) pieceType = 3;
            else if (PieceUtil.isQueen(piece)) pieceType = 4;
            else if (PieceUtil.isKing(piece)) pieceType = 5;
            else continue;

            if (PieceUtil.isWhite(piece))
                positional += PositionTables.whitePieceSquareTables[pieceType][i];
            else
                positional -= PositionTables.blackPieceSquareTables[pieceType][i];
        }
        return positional;
    }
}