package at.htlhl.chess.boardlogic;

import java.security.InvalidParameterException;
import java.util.Objects;

public record Square(int x, int y) {

    public static Square parseString(String s) {
        char[] charArr = s.toCharArray();
        if (charArr.length != 2) throw new InvalidParameterException("Invalid Spuare String: " + s);

        var c = charArr[0];
        if (Character.isUpperCase(c)) c = (char) (c + 32);
        var x = c - 97;

        var y = 8 - Integer.parseInt(Character.toString(charArr[1]));

        return new Square(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Square square = (Square) o;
        return x == square.x && y == square.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
