package at.htlhl.chess.boardlogic;

import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;

public class SquareTest {

    @Test
    void testParseString() {
        Square square = Square.parseString("e2");
        assertEquals(4, square.x());
        assertEquals(6, square.y());
    }

    @Test
    void testParseStringUpperCase() {
        Square square = Square.parseString("E2");
        assertEquals(4, square.x());
        assertEquals(6, square.y());
    }

    @Test
    void testParseStringInvalidLength() {
        assertThrows(InvalidParameterException.class, () -> Square.parseString("e22"));
    }

    @Test
    void testParseStringInvalidCharacter() {
        assertThrows(NumberFormatException.class, () -> Square.parseString("ea"));
    }

    @Test
    void testToStringMiddlePosition() {
        Square square = new Square(4, 3);  // e5 position
        assertEquals("e5", square.toString());
    }

    @Test
    void testToStringBottomLeft() {
        Square square = new Square(0, 7);  // a1 position
        assertEquals("a1", square.toString());
    }

    @Test
    void testToStringTopRight() {
        Square square = new Square(7, 0);  // h8 position
        assertEquals("h8", square.toString());
    }

    @Test
    void testToStringParseStringRoundTrip() {
        String position = "c4";
        Square square = Square.parseString(position);
        assertEquals(position, square.toString());
    }
}