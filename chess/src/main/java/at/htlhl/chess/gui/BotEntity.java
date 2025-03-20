package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.util.EngineConnector;

public class BotEntity extends PlayingEntity {

    EngineConnector connector;

    public BotEntity(Player player, BoardViewController boardViewController) {
        super(player, boardViewController);
        connector = new EngineConnector(boardViewController.getField(), boardViewController::addArrow);
    }

    @Override
    protected boolean move(Move move) {
        boolean success = super.move(move);
        return success;
    }

    @Override
    protected void allowMove(){
        super.allowMove();
//        Move bestMove = connector.drawBestMove();
        // TODO refactor
        connector.stopCurrentExecutions();
        connector = new EngineConnector(boardViewController.getField(), this::move);
        connector.drawBestMove();
//        move(bestMove);

    }

    @Override
    public boolean tryMove(Move move) {
        return false;
    }

}
