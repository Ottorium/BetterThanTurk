package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;

public class BotEntity extends PlayingEntity {

    public BotEntity(Player player, BoardViewController boardViewController) {
        super(player, boardViewController);
    }

    @Override
    protected boolean move(Move move) {
        boolean success = super.move(move);
        return success;
    }

}
