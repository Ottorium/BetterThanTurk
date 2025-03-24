package at.htlhl.chess.gui.util;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.engine.EvaluatedMove;

import java.util.List;
import java.util.function.Consumer;

public abstract class EngineConnector {
    /**
     * Calls the moveCallback with the best found move
     */
    public abstract void suggestMove(Consumer<Move> moveCallback);

    /**
     * Calls the callback with the list of best found moves
     */
    public abstract void suggestMoves(Consumer<List<EvaluatedMove>> movesCallback);

    /**
     * renews an execution of the thread (basicly shutdown and connect again)
     */
    public abstract void renewExecutions();

    /**
     * closes threads, removes event listeners. MUST be called when closing a programm, to prevent some threads from staying alive
     */
    public abstract void shutdown();

    public enum Type {
        CUSTOM, STOCKFISH
    }
}
