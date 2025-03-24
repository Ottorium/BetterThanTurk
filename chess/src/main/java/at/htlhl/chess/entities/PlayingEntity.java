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

    public boolean move(Move move) {
        if (move == null) {
            return false;
        }
        return boardViewController.makeMove(move, this);
    }

    public void allowMove() {
    }

    public BoardViewController getBoardViewController() {
        return boardViewController;
    }

    public boolean isMyMove() {
        return (boardViewController.getField().isBlackTurn() && player.equals(Player.BLACK)) ||
                (boardViewController.getField().isBlackTurn() == false && player.equals(Player.WHITE));
    }

    public void removeInteractions() {
    }

    public void shutdown() {
    }

    public enum Type {
        PLAYER, CUSTOM_BOT, STOCKFISH
    }
}
