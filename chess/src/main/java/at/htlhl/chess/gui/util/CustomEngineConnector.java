package at.htlhl.chess.gui.util;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.engine.Engine;
import at.htlhl.chess.engine.EvaluatedMove;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CustomEngineConnector extends EngineConnector {

    private Engine engine;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public CustomEngineConnector(Field field) {
        engine = new Engine(field);
    }

    public void suggestMove(Consumer<Move> moveCallback) {
        moveCallback.accept(null);
        executor.submit(() -> {
            Move bestMove = engine.getBestMove();
            Platform.runLater(() -> moveCallback.accept(bestMove));
        });
    }

    public void suggestMoves(Consumer<List<EvaluatedMove>> movesCallback){
        movesCallback.accept(null);
        executor.submit(() -> {
            ArrayList<EvaluatedMove> bestMove = engine.getBestMoves();
            Platform.runLater(() -> movesCallback.accept(bestMove));
        });
    }

    public void stopCurrentExecutions() {
        shutdown();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Shuts down the Engine. Use this right before the {@link CustomEngineConnector} goes out of scope (fuck java for not having destructors)
     */
    public void shutdown() {
        executor.shutdownNow();
        executor = null;
    }

    public Engine getEngine() {
        return engine;
    }
}
