package at.htlhl.chess;

import javafx.scene.image.Image;

public abstract class Piece {
    public Image image;
    public Player player;

    public abstract Move[] getMoves(Piece[][] board);
}