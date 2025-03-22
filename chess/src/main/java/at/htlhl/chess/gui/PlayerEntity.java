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

    @Override
    protected boolean move(Move move) {
        return super.move(move);
    }

    @Override
    public boolean tryMove(Move move) {
        return move(move);
    }

    /**
     * Removes events handler from objects
     */
    @Override
    public void removeInteractions() {
        chessBoardInteractionHandler.removeInteractions();
    }
}
