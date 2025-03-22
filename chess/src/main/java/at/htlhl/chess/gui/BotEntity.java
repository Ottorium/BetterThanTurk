package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.util.EngineConnector;
import javafx.application.Platform;

public class BotEntity extends PlayingEntity {

    EngineConnector connector;

    public BotEntity(Player player, BoardViewController boardViewController) {
        super(player, boardViewController);
        connector = new EngineConnector(boardViewController.getField(), boardViewController::addArrow);
    }

    @Override
    protected boolean move(Move move) {
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
        connector = new EngineConnector(boardViewController.getField(), this::suggestMove);
        connector.suggestMove();
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
    public boolean tryMove(Move move) {
        return false;
    }

}
