package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Square;
import at.htlhl.chess.engine.Engine;
import javafx.application.Platform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class EngineConnector {

    private final BiConsumer<Square, Square> drawArrowCallback;
    private Engine engine;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public EngineConnector(Field field, BiConsumer<Square, Square> drawArrow) {
        this.drawArrowCallback = drawArrow;
        engine = new Engine(field);
    }

    public void drawBestMove() {
        drawArrowCallback.accept(null, null);
        executor.submit(() -> {
            var bestMove = engine.getBestMove();
            Platform.runLater(() -> {
                if (bestMove == null) {
                    drawArrowCallback.accept(null, null);
                } else {
                    drawArrowCallback.accept(bestMove.getStartingSquare(), bestMove.getTargetSquare());
                }
            });
        });
    }

    public void stopCurrentExecutions() {
        executor.shutdownNow();
        executor = Executors.newSingleThreadExecutor();
    }

    public Engine getEngine() {
        return engine;
    }
}
