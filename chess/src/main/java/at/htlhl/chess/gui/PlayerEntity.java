package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.util.ChessBoardInteractionHandler;

public class PlayerEntity extends PlayingEntity {

    ChessBoardInteractionHandler chessBoardInteractionHandler;

    public PlayerEntity(Player player, BoardViewController boardViewController) {
        super(player, boardViewController);
        this.chessBoardInteractionHandler = new ChessBoardInteractionHandler(this);
    }

    private void initInteractions() {

    }

    @Override
    protected boolean move(Move move) {
        boolean success = super.move(move);
        return success;
    }

    @Override
    public boolean tryMove(Move move) {
        boolean success = move(move);
        return success;
    }

    @Override
    protected void allowMove() {
        super.allowMove();
    }
}
