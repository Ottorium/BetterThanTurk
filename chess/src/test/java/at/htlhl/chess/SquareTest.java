package at.htlhl.chess;

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
}