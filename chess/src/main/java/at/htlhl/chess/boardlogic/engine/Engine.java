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

    public Move getBestMove(String fen) {
        field.trySetFEN(fen);
        return getBestMove();
    }

    public Move getBestMove() {
        var moves = field.getMoveChecker().getAllLegalMoves();
        return moves.isEmpty() ? null : moves.getFirst();
    }
 }
