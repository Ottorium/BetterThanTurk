package at.htlhl.chess.util;

import at.htlhl.chess.Square;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FENParserTest {
    private static final String INITIAL_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String EARLY_GAME_FEN = "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3";
    private static final String EN_PASSANT_POSITION_FEN = "rnbqkb1r/pppp1ppp/5n2/4p3/3P4/2N5/PPP1PPPP/R1BQKBNR w KQkq e6 0 3";
    private static final String PARTIAL_CASTLING_FEN = "rnbq1bnr/ppppkppp/8/8/8/8/PPPP1PPP/RNBQ1RK1 b - - 2 6";
    private static final String COMPLEX_CASTLING_FEN = "r3k2r/ppp2ppp/2n1b3/3np3/2B5/2N1P3/PPP2PPP/2KR3R b q - 14 12";
    private static final String ENDGAME_FEN = "4k3/8/8/8/8/3K4/8/8 b - - 50 60";

    @Test
    void constructor_ValidFEN_ShouldNotThrowException() {
        assertDoesNotThrow(() -> new FENParser(INITIAL_POSITION_FEN));
        assertDoesNotThrow(() -> new FENParser(COMPLEX_CASTLING_FEN));
        assertDoesNotThrow(() -> new FENParser(ENDGAME_FEN));
    }

    @Test
    void parseBoard_ComplexPosition_ShouldReturnCorrectBoard() {
        FENParser parser = new FENParser(COMPLEX_CASTLING_FEN);
        byte[][] board = parser.parseBoard();
        assertEquals(PieceUtil.BLACK_ROOK, board[0][0]);
        assertEquals(PieceUtil.BLACK_KING, board[0][4]);
        assertEquals(PieceUtil.BLACK_ROOK, board[0][7]);
        assertEquals(PieceUtil.WHITE_KING, board[7][2]);
        assertEquals(PieceUtil.WHITE_ROOK, board[7][3]);
        assertEquals(PieceUtil.WHITE_ROOK, board[7][7]);
    }

    @Test
    void parseIsBlacksTurn_VariousPositions_ShouldReturnCorrectTurn() {
        assertFalse(new FENParser(INITIAL_POSITION_FEN).parseIsBlacksTurn());
        assertFalse(new FENParser(EN_PASSANT_POSITION_FEN).parseIsBlacksTurn());
        assertTrue(new FENParser(PARTIAL_CASTLING_FEN).parseIsBlacksTurn());
        assertTrue(new FENParser(COMPLEX_CASTLING_FEN).parseIsBlacksTurn());
        assertTrue(new FENParser(ENDGAME_FEN).parseIsBlacksTurn());
    }

    @Test
    void parseCastlingInformation_VariousPositions_ShouldReturnCorrectRights() {
        byte initialCastling = new FENParser(INITIAL_POSITION_FEN).parseCastlingInformation();
        assertTrue(CastlingUtil.hasFlag(initialCastling, CastlingUtil.WHITE_KING_SIDE));
        assertTrue(CastlingUtil.hasFlag(initialCastling, CastlingUtil.WHITE_QUEEN_SIDE));
        assertTrue(CastlingUtil.hasFlag(initialCastling, CastlingUtil.BLACK_KING_SIDE));
        assertTrue(CastlingUtil.hasFlag(initialCastling, CastlingUtil.BLACK_QUEEN_SIDE));

        byte complexCastling = new FENParser(COMPLEX_CASTLING_FEN).parseCastlingInformation();
        assertFalse(CastlingUtil.hasFlag(complexCastling, CastlingUtil.WHITE_KING_SIDE));
        assertFalse(CastlingUtil.hasFlag(complexCastling, CastlingUtil.WHITE_QUEEN_SIDE));
        assertFalse(CastlingUtil.hasFlag(complexCastling, CastlingUtil.BLACK_KING_SIDE));
        assertTrue(CastlingUtil.hasFlag(complexCastling, CastlingUtil.BLACK_QUEEN_SIDE));

        byte endgameCastling = new FENParser(ENDGAME_FEN).parseCastlingInformation();
        assertEquals(CastlingUtil.INITIAL_NO_RIGHTS, endgameCastling);
    }

    @Test
    void parsePossibleEnPassantMove_VariousPositions_ShouldReturnCorrectSquare() {
        FENParser enPassantParser = new FENParser(EN_PASSANT_POSITION_FEN);
        assertEquals(Square.parseString("e6"), enPassantParser.parsePossibleEnPassantMove());
        assertNull(new FENParser(INITIAL_POSITION_FEN).parsePossibleEnPassantMove());
        assertNull(new FENParser(COMPLEX_CASTLING_FEN).parsePossibleEnPassantMove());
        assertNull(new FENParser(ENDGAME_FEN).parsePossibleEnPassantMove());
    }

    @Test
    void parsePlayedHalfMovesSinceLastPawnMoveOrCapture_VariousPositions_ShouldReturnCorrectNumber() {
        assertEquals(0, new FENParser(INITIAL_POSITION_FEN).parsePlayedHalfMovesSinceLastPawnMoveOrCapture());
        assertEquals(2, new FENParser(EARLY_GAME_FEN).parsePlayedHalfMovesSinceLastPawnMoveOrCapture());
        assertEquals(14, new FENParser(COMPLEX_CASTLING_FEN).parsePlayedHalfMovesSinceLastPawnMoveOrCapture());
        assertEquals(50, new FENParser(ENDGAME_FEN).parsePlayedHalfMovesSinceLastPawnMoveOrCapture());
    }

    @Test
    void parseNumberOfNextMove_VariousPositions_ShouldReturnCorrectNumber() {
        assertEquals(1, new FENParser(INITIAL_POSITION_FEN).parseNumberOfNextMove());
        assertEquals(3, new FENParser(EARLY_GAME_FEN).parseNumberOfNextMove());
        assertEquals(12, new FENParser(COMPLEX_CASTLING_FEN).parseNumberOfNextMove());
        assertEquals(60, new FENParser(ENDGAME_FEN).parseNumberOfNextMove());
    }
}
