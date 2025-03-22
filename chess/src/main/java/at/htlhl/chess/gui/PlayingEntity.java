package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;

public abstract class PlayingEntity {
    protected boolean myMove = false;
    protected Player player;
    protected BoardViewController boardViewController;

    public PlayingEntity(Player player, BoardViewController boardViewController) {
        this.player = player;
        this.boardViewController = boardViewController;
    }

    protected boolean move(Move move) {
        if (move == null) {
            return false;
        }
        boolean success = boardViewController.makeMove(move, this);
        if (success) {
            myMove = false;
        }
        return success;
    }

    protected void allowMove() {
        myMove = true;
    }

    /**
     * tries to call a move execution
     * @param move move to execute
     * @return true if success
     */
    public abstract boolean tryMove(Move move);

    public BoardViewController getBoardViewController() {
        return boardViewController;
    }

    public boolean isMyMove() {
        return myMove;
    }
}
