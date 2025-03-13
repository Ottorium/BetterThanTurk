package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Square;
import at.htlhl.chess.boardlogic.engine.Engine;

import java.util.function.BiConsumer;

public class EngineConnector {

    private final BiConsumer<Square, Square> drawArrowCallback;
    private Engine engine;

    public EngineConnector(Field field, BiConsumer<Square, Square> drawArrow) {
        this.drawArrowCallback = drawArrow;
        engine = new Engine(field);
    }

    public void calculateBestMove() {
        var bestMove = engine.getBestMove();
        drawArrowCallback.accept(bestMove.getStartingSquare(), bestMove.getTargetSquare());
    }
}
