package at.htlhl.chess;

import java.util.Objects;

public record Move(Square startingSquare, Square targetSquare) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return Objects.equals(targetSquare, move.targetSquare) && Objects.equals(startingSquare, move.startingSquare);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startingSquare, targetSquare);
    }
}
