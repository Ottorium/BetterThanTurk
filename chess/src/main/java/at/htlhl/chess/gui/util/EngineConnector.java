package at.htlhl.chess.gui.util;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.engine.EvaluatedMove;

import java.util.List;
import java.util.function.Consumer;

public abstract class EngineConnector {
    public abstract void suggestMove(Consumer<Move> moveCallback);
    public abstract void suggestMoves(Consumer<List<EvaluatedMove>> movesCallback);
    public abstract void stopCurrentExecutions();
    public abstract void shutdown();
    public enum Type{
        CUSTOM, STOCKFISH
    }
}
