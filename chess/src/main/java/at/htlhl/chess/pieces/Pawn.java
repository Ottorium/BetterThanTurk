package at.htlhl.chess.pieces;

import at.htlhl.chess.Move;
import at.htlhl.chess.Piece;

public class Pawn extends Piece {
    @Override
    public Move[] getMoves(Piece[][] board) {
        return new Move[0];
    }
}
