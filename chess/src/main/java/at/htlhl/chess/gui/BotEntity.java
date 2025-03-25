package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.util.CustomEngineConnector;

public class BotEntity extends PlayingEntity {

    CustomEngineConnector connector;

    public BotEntity(Player player, BoardViewController boardViewController) {
        super(player, boardViewController);
        connector = new CustomEngineConnector(boardViewController.getField());
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
        connector.renewExecutions();
        connector = new CustomEngineConnector(boardViewController.getField());
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
