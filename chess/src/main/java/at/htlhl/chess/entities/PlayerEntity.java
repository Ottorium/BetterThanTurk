package at.htlhl.chess.entities;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.BoardViewController;
import at.htlhl.chess.gui.util.ChessBoardInteractionHandler;

public class PlayerEntity extends PlayingEntity {

    ChessBoardInteractionHandler chessBoardInteractionHandler;

    public PlayerEntity(Player player, BoardViewController boardViewController) {
        super(player, boardViewController);
        this.chessBoardInteractionHandler = new ChessBoardInteractionHandler(this);
    }

    @Override
    public boolean move(Move move) {
        return super.move(move);
    }

    /**
     * Removes events handler from objects
     */
    @Override
    public void shutdown() {
        chessBoardInteractionHandler.removeInteractions();
    }
}
