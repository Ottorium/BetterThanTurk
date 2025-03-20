package at.htlhl.chess.gui;

public abstract class PlayingEntity {
    protected boolean myMove = false;
    protected BoardViewController boardViewController;
    protected abstract void move();
    protected void allowMove(){
        myMove = true;
    }
}
