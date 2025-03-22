package at.htlhl.chess.boardlogic.util;

import at.htlhl.chess.boardlogic.Square;

import java.util.ArrayList;

public class Pin {
    private Square pinnedPiece;
    private Square attackingPiece;
    private ArrayList<int[]> allowedMoveDirections;

    public Pin(Square pinnedPiece, Square attackingPiece, ArrayList<int[]> allowedMoveDirections) {
        this.pinnedPiece = pinnedPiece;
        this.attackingPiece = attackingPiece;
        this.allowedMoveDirections = allowedMoveDirections;
    }

    public Square getPinnedPiece() {
        return pinnedPiece;
    }

    public void setPinnedPiece(Square pinnedPiece) {
        this.pinnedPiece = pinnedPiece;
    }

    public Square getAttackingPiece() {
        return attackingPiece;
    }

    public void setAttackingPiece(Square attackingPiece) {
        this.attackingPiece = attackingPiece;
    }

    public ArrayList<int[]> getAllowedMoveDirections() {
        return allowedMoveDirections;
    }

    public void setAllowedMoveDirections(ArrayList<int[]> allowedMoveDirections) {
        this.allowedMoveDirections = allowedMoveDirections;
    }

    public Pin clone() {
        return new Pin(this.pinnedPiece, this.attackingPiece, this.allowedMoveDirections);
    }
}
