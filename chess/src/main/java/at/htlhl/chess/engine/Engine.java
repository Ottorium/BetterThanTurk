package at.htlhl.chess.engine;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.GameState;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.util.PieceUtil;

import java.util.ArrayList;

public class Engine {

    private static final int[][] whitePieceSquareTables = {
            // PAWN
            {
                    0,   0,   0,   0,   0,   0,   0,   0,
                    11,  14,  16,  19,  19,  16,  14,  11,
                    4,   7,  10,  13,  13,  10,   7,   4,
                    1,   4,   7,  12,  12,   7,   4,   1,
                    0,   1,   5,  11,  11,   5,   1,   0,
                    0,   5,   2,   0,   0,   2,   5,   0,
                    0,   0,   0, -50, -50,   0,   0,   0,
                    0,   0,   0,   0,   0,   0,   0,   0
            },
            // KNIGHT
            {
                    -25, -20, -15, -15, -15, -15, -20, -25,
                    -20, -10,   0,   3,   3,   0, -10, -20,
                    -15,   3,   5,   8,   8,   5,   3, -15,
                    -15,   5,   8,  10,  10,   8,   5, -15,
                    -15,   5,   8,  10,  10,   8,   5, -15,
                    -15,   3,   5,   8,   8,   5,   3, -15,
                    -20, -10,   0,   3,   3,   0, -10, -20,
                    -25, -20, -15, -15, -15, -15, -20, -25
            },
            // BISHOP
            {
                    -10,  -5,  -5,  -5,  -5,  -5,  -5, -10,
                    -5,    0,   0,   0,   0,   0,   0,  -5,
                    -5,    6,   7,   8,   8,   7,   6,  -5,
                    -5,   10,  10,   8,   8,  10,  10,  -5,
                    -5,    0,  10,   8,   8,  10,   0,  -5,
                    -5,   10,   6,  10,  10,   6,  10,  -5,
                    -5,   10,   0,   0,   0,   0,  10,  -5,
                    -10,  -5, -10,  -5,  -5, -10,  -5, -10
            },
            // ROOK
            {
                    0,   3,   3,   3,   3,   3,   3,   0,
                    3,   5,   5,   5,   5,   5,   5,   3,
                    -3,  0,   0,   0,   0,   0,   0,  -3,
                    -3,  0,   0,   0,   0,   0,   0,  -3,
                    -3,  0,   0,   0,   0,   0,   0,  -3,
                    -3,  0,   0,   0,   0,   0,   0,  -3,
                    -3,  0,   0,   0,   0,   0,   0,  -3,
                    0,   0,   0,   0,   0,   0,   0,   0
            },
            // QUEEN
            {
                    -5,  -3,  -3,  -2,  -2,  -3,  -3,  -5,
                    -3,   0,   0,   2,   2,   0,   0,  -3,
                    -3,   0,   2,   4,   4,   2,   0,  -3,
                    -2,   0,   4,   5,   5,   4,   0,  -2,
                    -2,   0,   4,   5,   5,   4,   0,  -2,
                    -3,   0,   2,   4,   4,   2,   0,  -3,
                    -3,   0,   0,   2,   2,   0,   0,  -3,
                    -5,  -3,  -3,  -2,  -2,  -3,  -3,  -5
            },
            // KING
            {
                    -15, -20, -20, -25, -25, -20, -20, -15,
                    -15, -20, -20, -25, -25, -20, -20, -15,
                    -15, -20, -20, -25, -25, -20, -20, -15,
                    -10, -15, -15, -20, -20, -15, -15, -10,
                    -5, -10, -10, -15, -15, -10, -10,  -5,
                    0,   0,  -5, -10, -10,  -5,   0,   0,
                    10,  10,   0,  -3,  -3,   0,  10,  10,
                    15,  20,  10,   0,   0,   5,  20,  15
            }
    };

    private static final int[][] blackPieceSquareTables = {
            // PAWN
            {
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   0, -50, -50,   0,   0,   0,
                    0,   5,   2,   0,   0,   2,   5,   0,
                    0,   1,   5,  11,  11,   5,   1,   0,
                    1,   4,   7,  12,  12,   7,   4,   1,
                    4,   7,  10,  13,  13,  10,   7,   4,
                    11,  14,  16,  19,  19,  16,  14,  11,
                    0,   0,   0,   0,   0,   0,   0,   0
            },
            // KNIGHT
            {
                    -25, -20, -15, -15, -15, -15, -20, -25,
                    -20, -10,   0,   3,   3,   0, -10, -20,
                    -15,   3,   5,   8,   8,   5,   3, -15,
                    -15,   5,   8,  10,  10,   8,   5, -15,
                    -15,   5,   8,  10,  10,   8,   5, -15,
                    -15,   3,   5,   8,   8,   5,   3, -15,
                    -20, -10,   0,   3,   3,   0, -10, -20,
                    -25, -20, -15, -15, -15, -15, -20, -25
            },
            // BISHOP
            {
                    -10,  -5,  -5,  -5,  -5,  -5,  -5, -10,
                    -5,   10,   0,   0,   0,   0,  10,  -5,
                    -5,   10,   6,  10,  10,   6,  10,  -5,
                    -5,    0,  10,  8,    8,  10,   0,  -5,
                    -5,   10,  10,  8,    8,  10,  10,  -5,
                    -5,    6,   7,   8,   8,   7,   6,  -5,
                    -5,    0,   0,   0,   0,   0,   0,  -5,
                    -10,  -5, -10,  -5,  -5, -10,  -5, -10
            },
            // ROOK
            {
                    0,   0,   0,   0,   0,   0,   0,   0,
                    -3,  0,   0,   0,   0,   0,   0,  -3,
                    -3,  0,   0,   0,   0,   0,   0,  -3,
                    -3,  0,   0,   0,   0,   0,   0,  -3,
                    -3,  0,   0,   0,   0,   0,   0,  -3,
                    -3,  0,   0,   0,   0,   0,   0,  -3,
                    3,   5,   5,   5,   5,   5,   5,   3,
                    0,   3,   3,   3,   3,   3,   3,   0
            },
            // QUEEN
            {
                    -5,  -3,  -3,  -2,  -2,  -3,  -3,  -5,
                    -3,   0,   0,   2,   2,   0,   0,  -3,
                    -3,   0,   2,   4,   4,   2,   0,  -3,
                    -2,   0,   4,   5,   5,   4,   0,  -2,
                    -2,   0,   4,   5,   5,   4,   0,  -2,
                    -3,   0,   2,   4,   4,   2,   0,  -3,
                    -3,   0,   0,   2,   2,   0,   0,  -3,
                    -5,  -3,  -3,  -2,  -2,  -3,  -3,  -5
            },
            // KING
            {
                    15,  20,  10,   0,   0,   5,  20,  15,
                    10,  10,   0,  -3,  -3,   0,  10,  10,
                    0,   0,  -5, -10, -10,  -5,   0,   0,
                    -5, -10, -10, -15, -15, -10, -10,  -5,
                    -10, -15, -15, -20, -20, -15, -15, -10,
                    -15, -20, -20, -25, -25, -20, -20, -15,
                    -15, -20, -20, -25, -25, -20, -20, -15,
                    -15, -20, -20, -25, -25, -20, -20, -15
            }
    };
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
        maxDepth = 7;
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

            int eval;
            try {
                field.forceMove(move, false);
                executedMoves++;
                eval = minimax(depth - 1, alpha, beta);
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
                positional += whitePieceSquareTables[pieceType][i];
            else
                positional -= blackPieceSquareTables[pieceType][i];
        }

        return material + positional;
    }
}