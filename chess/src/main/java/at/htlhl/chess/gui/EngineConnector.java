package at.htlhl.chess.gui;

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
        executor.shutdownNow();
        executor = Executors.newSingleThreadExecutor();
    }

    public Engine getEngine() {
        return engine;
    }
}
