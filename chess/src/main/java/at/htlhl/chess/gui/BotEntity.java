package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.util.EngineConnector;

public class BotEntity extends PlayingEntity {

    EngineConnector connector;

    public BotEntity(Player player, BoardViewController boardViewController) {
        super(player, boardViewController);
        connector = new EngineConnector(boardViewController.getField());
    }

    @Override
    public boolean move(Move move) {
        return super.move(move);
    }

    @Override
    protected void allowMove() {
        super.allowMove();
        if (isMyMove() == true) {
            startEngine();
        }
    }

    private void startEngine() {
        connector.stopCurrentExecutions();
        connector = new EngineConnector(boardViewController.getField());
        connector.suggestMove(this::suggestMove);
    }

    /**
     * Will be called by an engine
     */
    private void suggestMove(Move move) {
        if (move == null) return;
        // move is best move here
        move(move);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        connector.shutdown();
    }
}
