package at.htlhl.chess.boardlogic.util;

import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.boardlogic.Square;

import java.util.ArrayList;

public class Check {
    private Player playerInCheck;
    private boolean isDoubleCheck;
    private ArrayList<Square> possibleBlockingOrCapturingSquares;
    private ArrayList<int[]> directionsFromWhichTheChecksAreComingIfSlidingPiece;

    public ArrayList<int[]> getDirectionsFromWhichTheChecksAreComingIfSlidingPiece() {
        return directionsFromWhichTheChecksAreComingIfSlidingPiece;
    }

    public void setDirectionsFromWhichTheChecksAreComingIfSlidingPiece(ArrayList<int[]> directionsFromWhichTheChecksAreComingIfSlidingPiece) {
        this.directionsFromWhichTheChecksAreComingIfSlidingPiece = directionsFromWhichTheChecksAreComingIfSlidingPiece;
    }

    public Check(Player playerInCheck, ArrayList<Square> possibleBlockingSquares, ArrayList<int[]> directionsFromWhichTheChecksAreComing, boolean isDoubleCheck) {
        this.playerInCheck = playerInCheck;
        this.possibleBlockingOrCapturingSquares = possibleBlockingSquares;
        this.directionsFromWhichTheChecksAreComingIfSlidingPiece = directionsFromWhichTheChecksAreComing;
        this.isDoubleCheck = isDoubleCheck;
    }

    public Player getPlayerInCheck() {
        return playerInCheck;
    }

    public void setPlayerInCheck(Player playerInCheck) {
        this.playerInCheck = playerInCheck;
    }

    public boolean isDoubleCheck() {
        return isDoubleCheck;
    }

    public void setDoubleCheck(boolean doubleCheck) {
        isDoubleCheck = doubleCheck;
    }

    public ArrayList<Square> getPossibleBlockingOrCapturingSquares() {
        return possibleBlockingOrCapturingSquares;
    }

    public void setPossibleBlockingOrCapturingSquares(ArrayList<Square> possibleBlockingOrCapturingSquares) {
        this.possibleBlockingOrCapturingSquares = possibleBlockingOrCapturingSquares;
    }

    public Check clone() {
        return new Check(playerInCheck, possibleBlockingOrCapturingSquares, directionsFromWhichTheChecksAreComingIfSlidingPiece, isDoubleCheck);
    }
}
