package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;

public abstract class PlayingEntity {
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
        return boardViewController.makeMove(move, this);
    }

    protected void allowMove() {
    }

    public abstract boolean tryMove(Move move);

    public BoardViewController getBoardViewController() {
        return boardViewController;
    }

    public boolean isMyMove() {
        return (boardViewController.getField().isBlackTurn() && player.equals(Player.BLACK)) ||
                (boardViewController.getField().isBlackTurn() == false && player.equals(Player.WHITE));
    }

    public void removeInteractions() {
    }

    ;

    public void shutdown() {
    }

    public enum Type {
        PLAYER, CUSTOM_BOT, STOCKFISH
    }
}
