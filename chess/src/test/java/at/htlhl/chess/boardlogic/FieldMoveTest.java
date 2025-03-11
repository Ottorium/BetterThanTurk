package at.htlhl.chess.boardlogic;

import at.htlhl.chess.boardlogic.util.PieceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FieldMoveTest {

    private Field field;

    @BeforeEach
    public void setUp() {
        field = new Field();
        field.resetBoard();
    }

    @Test
    public void testBasicPawnMove() {
        Square start = Square.parseString("e2");
        Square target = Square.parseString("e4");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid pawn move should return true");
        assertEquals(PieceUtil.WHITE_PAWN, field.getPieceBySquare(target), "Pawn should be moved to target square");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(start), "Starting square should be empty");
        assertTrue(field.isBlackTurn(), "Turn should switch to black after move");
    }

    @Test
    public void testPawnCapture() {
        field.trySetFEN("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2");

        Square start = Square.parseString("e4");
        Square target = Square.parseString("d5");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid pawn capture should return true");
        assertEquals(PieceUtil.WHITE_PAWN, field.getPieceBySquare(target), "Pawn should be moved to target square");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(start), "Starting square should be empty");
    }

    @Test
    public void testKnightMove() {
        Square start = Square.parseString("b1");
        Square target = Square.parseString("c3");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid knight move should return true");
        assertEquals(PieceUtil.WHITE_KNIGHT, field.getPieceBySquare(target), "Knight should be moved to target square");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(start), "Starting square should be empty");
    }

    @Test
    public void testInvalidMove() {
        Square start = Square.parseString("e2");
        Square target = Square.parseString("e5");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertFalse(result, "Invalid move should return false");
        assertEquals(PieceUtil.WHITE_PAWN, field.getPieceBySquare(start), "Pawn should remain on starting square");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(target), "Target square should remain empty");
        assertFalse(field.isBlackTurn(), "Turn should not switch after invalid move");
    }

    @Test
    public void testKingCastlingKingSide() {
        field.trySetFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQK2R w KQkq - 0 1");

        Square start = Square.parseString("e1");
        Square target = Square.parseString("g1");
        Move move = new Move(start, target);
        move.setCastlingMove(true);

        boolean result = field.move(move);

        assertTrue(result, "Valid kingside castling should return true");
        assertEquals(PieceUtil.WHITE_KING, field.getPieceBySquare(target), "King should be moved to target square");
        assertEquals(PieceUtil.WHITE_ROOK, field.getPieceBySquare(Square.parseString("f1")), "Rook should be moved to f1");
    }

    @Test
    public void testKingCastlingQueenSide() {
        field.trySetFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/R3KBNR w KQkq - 0 1");

        Square start = Square.parseString("e1");
        Square target = Square.parseString("c1");
        Move move = new Move(start, target);
        move.setCastlingMove(true);

        boolean result = field.move(move);

        assertTrue(result, "Valid queenside castling should return true");
        assertEquals(PieceUtil.WHITE_KING, field.getPieceBySquare(target), "King should be moved to target square");
        assertEquals(PieceUtil.WHITE_ROOK, field.getPieceBySquare(Square.parseString("d1")), "Rook should be moved to d1");
    }

    @Test
    public void testEnPassantCapture() {
        field.trySetFEN("rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3");

        Square start = Square.parseString("e5");
        Square target = Square.parseString("f6");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid en passant capture should return true");
        assertEquals(PieceUtil.WHITE_PAWN, field.getPieceBySquare(target), "Pawn should be moved to target square");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(start), "Starting square should be empty");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(Square.parseString("f5")), "Captured pawn square should be empty");
    }

    @Test
    public void testEnPassantDiscoveredCheck() {
        field.trySetFEN("8/8/8/K1pP2r1/8/8/8/8 w - c6 0 1");

        Square start = Square.parseString("d5");
        Square target = Square.parseString("c6");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertFalse(result, "en passant move with discovered check should not be possible");
    }

    @Test
    public void testPawnPromotion() {
        field.trySetFEN("rnbqkbnr/ppppppP1/8/8/8/8/PPPPPPP1/RNBQKBNR w KQkq - 0 1");

        Square start = Square.parseString("g7");
        Square target = Square.parseString("h8");
        Move move = new Move(start, target);
        move.setPromotionPiece(PieceUtil.WHITE_QUEEN);

        boolean result = field.move(move);

        assertTrue(result, "Valid pawn promotion should return true");
        assertEquals(PieceUtil.WHITE_QUEEN, field.getPieceBySquare(target), "Pawn should be promoted to queen");
    }

    @Test
    public void testCheckDetection() {
        field.trySetFEN("rnbqkbnr/ppp3pp/5p2/3pN3/4P3/8/PPPP1PPP/RNBQKB1R w KQkq - 0 4");

        Square start = Square.parseString("d1");
        Square target = Square.parseString("h5");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid check move should return true");
        assertEquals(Player.BLACK, field.getKingInCheck(), "Black king should be in check");
    }

    @Test
    public void testGameEndedCheckmate() {
        field.trySetFEN("rnbqkbnr/pppp1ppp/8/4p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR b KQkq - 3 3");

        Square start = Square.parseString("b8");
        Square target = Square.parseString("c6");
        Move blackMove = new Move(start, target);
        field.move(blackMove);

        start = Square.parseString("f3");
        target = Square.parseString("f7");
        Move whiteMove = new Move(start, target);
        field.move(whiteMove);

        assertEquals(GameState.WHITE_WIN, field.getGameState(), "Game should end in white's victory");
    }

    @Test
    public void testGameEndedStalemate() {
        field.trySetFEN("k7/8/1Q6/8/8/8/8/7K b - - 0 1");

        assertEquals(GameState.DRAW, field.getGameState(), "Game should end in draw due to stalemate");
    }

    @Test
    public void testPerftInitialPosition() {
        field.resetBoard();
        List<Square> startingSquares = getAllPieceSquares(field, false);
        int moveCount = countLegalMoves(field, startingSquares);

        assertEquals(20, moveCount, "Initial position should have 20 legal moves");
    }

    @Test
    public void testPerftPosition2() {
        field.trySetFEN("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        List<Square> startingSquares = getAllPieceSquares(field, false);
        int moveCount = countLegalMoves(field, startingSquares);

        assertEquals(48, moveCount, "Kiwipete position should have 48 legal moves");
    }

    @Test
    public void testPerftPosition3() {
        field.trySetFEN("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1");
        List<Square> startingSquares = getAllPieceSquares(field, false);
        int moveCount = countLegalMoves(field, startingSquares);

        assertEquals(14, moveCount, "Position 3 should have 14 legal moves");
    }

    @Test
    public void testBishopMove() {
        field.trySetFEN("rnbqkbnr/pppp2pp/5p2/4p3/8/4PN2/PPPP1PPP/RNBQKB1R w KQkq - 0 2");

        Square start = Square.parseString("f1");
        Square target = Square.parseString("c4");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid bishop move should return true");
        assertEquals(PieceUtil.WHITE_BISHOP, field.getPieceBySquare(target), "Bishop should be moved to target square");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(start), "Starting square should be empty");
    }

    @Test
    public void testRookMove() {
        field.trySetFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/R3KBNR w KQkq - 0 1");

        Square start = Square.parseString("a1");
        Square target = Square.parseString("d1");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid rook move should return true");
        assertEquals(PieceUtil.WHITE_ROOK, field.getPieceBySquare(target), "Rook should be moved to target square");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(start), "Starting square should be empty");
    }

    @Test
    public void testQueenMove() {
        field.trySetFEN("rnbqkbnr/pppp1ppp/8/4p3/8/2P5/PP1PPPPP/RNBQKBNR w KQkq - 0 2");

        Square start = Square.parseString("d1");
        Square target = Square.parseString("a4");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid queen diagonal move should return true");
        assertEquals(PieceUtil.WHITE_QUEEN, field.getPieceBySquare(target), "Queen should be moved to target square");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(start), "Starting square should be empty");
    }

    @Test
    public void testKingMove() {
        field.trySetFEN("rnbqkbnr/pppppppp/8/8/8/4P3/PPPP1PPP/RNBQKBNR w KQkq - 0 2");

        Square start = Square.parseString("e1");
        Square target = Square.parseString("e2");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid king move should return true");
        assertEquals(PieceUtil.WHITE_KING, field.getPieceBySquare(target), "King should be moved to target square");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(start), "Starting square should be empty");
    }

    @Test
    public void testPawnPromotionToKnight() {
        field.trySetFEN("rnbqkb1r/ppppp1Pp/5n2/8/8/8/PPPPPPP1/RNBQKBNR w KQkq - 0 1");

        Square start = Square.parseString("g7");
        Square target = Square.parseString("h8");
        Move move = new Move(start, target);
        move.setPromotionPiece(PieceUtil.WHITE_KNIGHT);

        boolean result = field.move(move);

        assertTrue(result, "Valid pawn promotion to knight should return true");
        assertEquals(PieceUtil.WHITE_KNIGHT, field.getPieceBySquare(target), "Pawn should be promoted to knight");
    }

    @Test
    public void testBlackEnPassantCapture() {
        field.trySetFEN("rnbqkbnr/ppp1pppp/8/8/3Pp3/8/PPP2PPP/RNBQKBNR b KQkq d3 0 3");

        Square start = Square.parseString("e4");
        Square target = Square.parseString("d3");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid black en passant capture should return true");
        assertEquals(PieceUtil.BLACK_PAWN, field.getPieceBySquare(target), "Black pawn should be moved to target square");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(start), "Starting square should be empty");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(Square.parseString("d4")), "Captured pawn square should be empty");
    }

    @Test
    public void testDoublePawnMoveAndEnPassantFlag() {
        field.resetBoard();

        Square start = Square.parseString("d2");
        Square target = Square.parseString("d4");
        Move move = new Move(start, target);
        boolean result = field.move(move);

        assertTrue(result, "Valid double pawn move should return true");
        assertEquals("d3", field.getPossibleEnPassantSquare().toString(), "En passant target should be set to d3");

        start = Square.parseString("e7");
        target = Square.parseString("e5");
        move = new Move(start, target);
        field.move(move);

        assertEquals("e6", field.getPossibleEnPassantSquare().toString(), "En passant target should be updated to e6");
    }

    @Test
    public void testFoolsMate() {
        field.resetBoard();

        Move move = new Move(Square.parseString("f2"), Square.parseString("f3"));
        field.move(move);

        move = new Move(Square.parseString("e7"), Square.parseString("e5"));
        field.move(move);

        move = new Move(Square.parseString("g2"), Square.parseString("g4"));
        field.move(move);

        move = new Move(Square.parseString("d8"), Square.parseString("h4"));
        field.move(move);

        assertEquals(GameState.BLACK_WIN, field.getGameState(), "Game should end in black's victory (Fool's Mate)");
        assertEquals(Player.WHITE, field.getKingInCheck(), "White king should be in check");
    }

    @Test
    public void testDiscoveredCheck() {
        field.trySetFEN("rnb3nr/ppp3pp/2k5/8/1bB5/2Q5/PPPP1PPP/RNB1K1NR w - - 2 3");

        Square start = Square.parseString("c4");
        Square target = Square.parseString("e2");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid discovered check move should return true");
        assertEquals(Player.BLACK, field.getKingInCheck(), "Black king should be in check via discovered check");
    }

    @Test
    public void testPinDetection() {
        field.trySetFEN("rnbq3r/pppp1kpp/5n2/2b1p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR b KQ - 3 3");

        Square start = Square.parseString("f6");
        Square target = Square.parseString("d5");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertFalse(result, "Moving a pinned piece should return false");
        assertEquals(PieceUtil.BLACK_KNIGHT, field.getPieceBySquare(start), "Pinned knight should remain on its square");
    }

    @Test
    public void testSmootheredMatePosition() {
        field.trySetFEN("6rk/6pp/8/4N3/8/8/8/5K2 w - - 0 1");

        Square start = Square.parseString("e5");
        Square target = Square.parseString("f7");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid smoothered mate move should return true");
        assertEquals(GameState.WHITE_WIN, field.getGameState(), "Game should end in white's victory via smoothered mate");
    }

    @Test
    public void testPieceCapture() {
        field.trySetFEN("rnbqkbnr/ppp1pppp/8/3p4/3PP3/8/PPP2PPP/RNBQKBNR b KQkq - 0 2");

        Square start = Square.parseString("d5");
        Square target = Square.parseString("e4");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid capture move should return true");
        assertEquals(PieceUtil.BLACK_PAWN, field.getPieceBySquare(target), "Black pawn should be on the target square");
        assertEquals(PieceUtil.EMPTY, field.getPieceBySquare(start), "Starting square should be empty");
    }

    @Test
    public void testDrawByInsufficientMaterial() {
        field.trySetFEN("8/8/8/4k3/8/8/8/4K3 w - - 0 1");

        assertEquals(GameState.DRAW, field.getGameState(), "Game should be a draw due to insufficient material");
    }

    @Test
    public void testCastlingThroughCheck() {
        field.trySetFEN("rn1qkb1r/ppp2ppp/3p1n2/4p3/2b1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 2 4");

        Square start = Square.parseString("e1");
        Square target = Square.parseString("g1");
        Move move = new Move(start, target);
        move.setCastlingMove(true);

        boolean result = field.move(move);

        assertFalse(result, "Castling through check should not be allowed");
        assertEquals(PieceUtil.WHITE_KING, field.getPieceBySquare(start), "King should remain on starting square");
        assertEquals(PieceUtil.WHITE_ROOK, field.getPieceBySquare(Square.parseString("h1")), "Rook should remain on its original square");
    }

    @Test
    public void testCastlingIntoCheck() {
        field.trySetFEN("r3k2r/pppq1ppp/2np1n2/4p1b1/2B1P3/2NP1N2/PPP2PPP/R3K2R w KQkq - 6 8");

        Square start = Square.parseString("e1");
        Square target = Square.parseString("c1");
        Move move = new Move(start, target);
        move.setCastlingMove(true);

        boolean result = field.move(move);

        assertFalse(result, "Castling into check should not be allowed");
        assertEquals(PieceUtil.WHITE_KING, field.getPieceBySquare(start), "King should remain on starting square");
        assertEquals(PieceUtil.WHITE_ROOK, field.getPieceBySquare(Square.parseString("a1")), "Rook should remain on its original square");
    }

    @Test
    public void testCastlingFromCheck() {
        field.trySetFEN("rnbqk2r/pppp1ppp/5n2/4p3/1b2P3/3P1N2/PPP2PPP/RNBQK2R w KQkq - 2 4");

        assertEquals(Player.WHITE, field.getKingInCheck(), "White king should be in check");

        Square start = Square.parseString("e1");
        Square target = Square.parseString("g1");
        Move move = new Move(start, target);
        move.setCastlingMove(true);

        boolean result = field.move(move);

        assertFalse(result, "Castling while in check should not be allowed");
        assertEquals(PieceUtil.WHITE_KING, field.getPieceBySquare(start), "King should remain on starting square");
        assertEquals(PieceUtil.WHITE_ROOK, field.getPieceBySquare(Square.parseString("h1")), "Rook should remain on its original square");
    }

    @Test
    public void testCastlingAfterKingMoved() {
        field.resetBoard();

        Square kingPos = Square.parseString("e1");
        Square tempPos = Square.parseString("e2");
        Move moveOut = new Move(kingPos, tempPos);
        field.move(moveOut);

        field.move(new Move(Square.parseString("e7"), Square.parseString("e5")));

        Move moveBack = new Move(tempPos, kingPos);
        field.move(moveBack);

        field.move(new Move(Square.parseString("d7"), Square.parseString("d5")));

        Square target = Square.parseString("g1");
        Move castling = new Move(kingPos, target);
        castling.setCastlingMove(true);

        boolean result = field.move(castling);

        assertFalse(result, "Castling after king has moved should not be allowed");
        assertEquals(PieceUtil.WHITE_KING, field.getPieceBySquare(kingPos), "King should remain on e1");
    }

    @Test
    public void testCastlingAfterRookMoved() {
        field.resetBoard();

        Square rookPos = Square.parseString("h1");
        Square tempPos = Square.parseString("h2");
        Move moveOut = new Move(rookPos, tempPos);
        field.move(moveOut);

        field.move(new Move(Square.parseString("e7"), Square.parseString("e5")));

        Move moveBack = new Move(tempPos, rookPos);
        field.move(moveBack);

        field.move(new Move(Square.parseString("d7"), Square.parseString("d5")));

        Square kingPos = Square.parseString("e1");
        Square target = Square.parseString("g1");
        Move castling = new Move(kingPos, target);
        castling.setCastlingMove(true);

        boolean result = field.move(castling);

        assertFalse(result, "Castling after rook has moved should not be allowed");
        assertEquals(PieceUtil.WHITE_KING, field.getPieceBySquare(kingPos), "King should remain on e1");
    }

    @Test
    public void testPiecesBlockingCastling() {
        field.trySetFEN("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3Kb1R w KQkq - 0 1");

        Square start = Square.parseString("e1");
        Square target = Square.parseString("g1");
        Move move = new Move(start, target);
        move.setCastlingMove(true);

        boolean result = field.move(move);

        assertFalse(result, "Castling should not be allowed when pieces block the path");
        assertEquals(PieceUtil.WHITE_KING, field.getPieceBySquare(start), "King should remain on starting square");
    }

    @Test
    public void testCheckmate() {
        field.trySetFEN("7k/5ppp/8/8/8/8/5PPP/R6K w - - 0 1");

        Square start = Square.parseString("a1");
        Square target = Square.parseString("a8");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Checkmate move should be valid");
        assertEquals(GameState.WHITE_WIN, field.getGameState(), "Game should end in white's victory");
        assertEquals(Player.BLACK, field.getKingInCheck(), "Black king should be in check");
    }

    @Test
    public void testMoveIntoCheck() {
        field.trySetFEN("rnbqkbnr/ppp2ppp/4p3/3p4/3P4/2N5/PPP1PPPP/R1BQKBNR w KQkq - 0 3");

        Square start = Square.parseString("e1");
        Square target = Square.parseString("e2");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertFalse(result, "Moving into check should not be allowed");
        assertEquals(PieceUtil.WHITE_KING, field.getPieceBySquare(start), "King should remain on starting square");
    }

    @Test
    public void testCastlingRightLossAfterRookCapture() {
        field.trySetFEN("r3k2r/1ppppppp/8/8/8/8/1PPPPPPP/R3K2R b KQkq - 0 1");

        Square start = Square.parseString("a8");
        Square target = Square.parseString("a1");
        Move move = new Move(start, target);
        assertTrue(field.move(move));

        field.move(new Move(Square.parseString("h2"), Square.parseString("h3")));
        field.move(new Move(Square.parseString("h7"), Square.parseString("h6")));

        start = Square.parseString("e1");
        target = Square.parseString("c1");
        move = new Move(start, target);
        move.setCastlingMove(true);

        boolean result = field.move(move);

        assertFalse(result, "Queenside castling should not be allowed after rook is captured");
    }

    @Test
    public void testPawnDoubleMoveThroughOccupiedSquare() {
        field.trySetFEN("rnbqkb1r/pppppppp/8/8/8/4n3/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        Square start = Square.parseString("e2");
        Square target = Square.parseString("e4");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertFalse(result, "Pawn double move through an occupied square should not be allowed");
        assertEquals(PieceUtil.WHITE_PAWN, field.getPieceBySquare(start), "Pawn should remain on starting square");
    }

    @Test
    public void testMoveBlockingCheck() {
        field.trySetFEN("rnb1kbnr/pppp1ppp/8/4p3/7q/5P2/PPPPP1PP/RNBQKBNR w KQkq - 0 1");

        assertEquals(Player.WHITE, field.getKingInCheck(), "White king should be in check");

        Square start = Square.parseString("g2");
        Square target = Square.parseString("g3");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid blocking move should return true");
        assertNull(field.getKingInCheck(), "No king should be in check after blocking");
    }

    @Test
    public void testDrawByThreefoldRepetition() {
        field.resetBoard();

        for (int i = 0; i < 3; i++) {
            field.move(new Move(Square.parseString("g1"), Square.parseString("f3")));
            field.move(new Move(Square.parseString("g8"), Square.parseString("f6")));
            field.move(new Move(Square.parseString("f3"), Square.parseString("g1")));
            field.move(new Move(Square.parseString("f6"), Square.parseString("g8")));
        }

        assertEquals(GameState.DRAW, field.getGameState(), "Game should be a draw due to threefold repetition");
    }

    @Test
    public void test50MoveRule() {
        field.trySetFEN("4k3/8/8/8/8/8/8/4K3 w - - 99 50");

        Square start = Square.parseString("e1");
        Square target = Square.parseString("e2");
        Move move = new Move(start, target);
        field.move(move);

        assertEquals(GameState.DRAW, field.getGameState(), "Game should be a draw due to 50-move rule");
    }


    // Helper method to count legal moves
    private int countLegalMoves(Field field, List<Square> startingSquares) {
        int count = 0;
        for (Square start : startingSquares) {
            List<Square> targets = field.getLegalTargetsForSquare(start);
            count += targets.size();
        }
        return count;
    }

    // Helper method to get all squares with pieces of the current player
    private List<Square> getAllPieceSquares(Field field, boolean isBlack) {
        List<Square> squares = new java.util.ArrayList<>();
        byte[][] board = field.getBoard();

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                byte piece = board[y][x];
                if (!PieceUtil.isEmpty(piece) &&
                        (isBlack ? PieceUtil.isBlack(piece) : PieceUtil.isWhite(piece))) {
                    squares.add(new Square(x, y));
                }
            }
        }

        return squares;
    }
}