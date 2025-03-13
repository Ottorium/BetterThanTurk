package at.htlhl.chess.boardlogic.engine;

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
        var eval = getEvalOfBestMove(3);
        return currentBestMove;
    }

    private int getEvalOfBestMove(int depth) {

        if (depth == 0) return evaluateCurrentPosition();

        var allMoves = field.getMoveChecker().getAllLegalMoves();
        currentBestMove = null;
        boolean blackTurn = field.isBlackTurn();
        var bestScore = blackTurn ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        for (Move move : allMoves) {
            var before = field.clone();
            field.forceMove(move, false);
            var eval = evaluateCurrentPosition();
            field = before;
            if (blackTurn ? eval < bestScore : eval > bestScore) {
                bestScore = eval;
                currentBestMove = move;
            }
        }
        return bestScore;
    }


    private int evaluateCurrentPosition() {
        return field.getPieceEvaluation();
    }
}