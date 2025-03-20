package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Player;

public abstract class PlayingEntity {
    protected boolean myMove = false;
    protected Player player;
    protected BoardViewController boardViewController;
    protected abstract boolean move();
    public PlayingEntity(Player player, BoardViewController boardViewController) {
        this.player = player;
        this.boardViewController = boardViewController;
    }
    protected void allowMove(){
        myMove = true;
    }
    public boolean tryMove () {
        return false;
    }

    public BoardViewController getBoardViewController() {
        return boardViewController;
    }

    public boolean isMyMove() {
        return myMove;
    }
}
