package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.util.ChessBoardInteractionHandler;

public class PlayerEntity extends PlayingEntity{

    ChessBoardInteractionHandler chessBoardInteractionHandler;

    public PlayerEntity(Player player, BoardViewController boardViewController) {
        super(player, boardViewController);
        this.chessBoardInteractionHandler = new ChessBoardInteractionHandler(this);
    }

    private void initInteractions() {

    }
    @Override
    protected boolean move() {
        return false;
    }

    @Override
    public boolean tryMove() {
        boolean success = move();
        return success;
    }

    @Override
    protected void allowMove() {
        super.allowMove();
    }
}
