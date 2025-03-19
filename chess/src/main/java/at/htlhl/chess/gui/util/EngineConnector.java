package at.htlhl.chess.gui.util;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.engine.Engine;
import javafx.application.Platform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EngineConnector {

    private final Consumer<Move> drawArrowCallback;
    private Engine engine;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public EngineConnector(Field field, Consumer<Move> drawArrow) {
        this.drawArrowCallback = drawArrow;
        engine = new Engine(field);
    }

    public void drawBestMove() {
        drawArrowCallback.accept(null);
        executor.submit(() -> {
            var bestMove = engine.getBestMove();
            Platform.runLater(() -> drawArrowCallback.accept(bestMove));
        });
    }

    public void stopCurrentExecutions() {
        shutdown();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Shuts down the Engine. Use this right before the {@link EngineConnector} goes out of scope (fuck java for not having destructors)
     */
    public void shutdown() {
        executor.shutdownNow();
        executor = null;
    }

    public Engine getEngine() {
        return engine;
    }
}
