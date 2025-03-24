package at.htlhl.chess.entities;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.BoardViewController;

public abstract class PlayingEntity {
    protected Player player;
    protected BoardViewController boardViewController;

    public PlayingEntity(Player player, BoardViewController boardViewController) {
        this.player = player;
        this.boardViewController = boardViewController;
    }

    /**
     * Is used to call a move on board
     *
     * @return
     */
    public boolean move(Move move) {
        if (move == null) {
            return false;
        }
        return boardViewController.makeMove(move, this);
    }

    /**
     * Like a trigger, that is used for bots to tell them that they can move
     */
    public void allowMove() {
    }

    public BoardViewController getBoardViewController() {
        return boardViewController;
    }

    /**
     * looks, if it is this player's time to move
     *
     * @return true if it is it's move
     */
    public boolean isMyMove() {
        return (boardViewController.getField().isBlackTurn() && player.equals(Player.BLACK)) ||
                (boardViewController.getField().isBlackTurn() == false && player.equals(Player.WHITE));
    }


    /**
     * closes threads, removes event listeners. MUST be called when closing a programm, to prevent some threads from staying alive
     */
    public void shutdown() {
    }

    public enum Type {
        PLAYER, CUSTOM_BOT, STOCKFISH
    }
}
