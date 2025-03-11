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
        field.resetBoard(); // Start with the initial position
    }

    @Test
    public void testBasicPawnMove() {
        // Test a basic white pawn move (e2-e4)
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
        // Setup a position where a pawn can capture
        field.trySetFEN("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2");

        // Test white pawn capturing black pawn (e4xd5)
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
        // Test a knight move (Nb1-c3)
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
        // Test an invalid move (trying to move a pawn too far)
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
        // Setup a position where castling is possible
        field.trySetFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQK2R w KQkq - 0 1");

        // Test white king castling kingside (e1-g1)
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
        // Setup a position where queenside castling is possible
        field.trySetFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/R3KBNR w KQkq - 0 1");

        // Test white king castling queenside (e1-c1)
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
        // Setup a position where en passant is possible
        field.trySetFEN("rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3");

        // Test white pawn capturing en passant (e5xf6)
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
        // Setup a position where en passant is possible
        field.trySetFEN("8/8/8/K1pP2r1/8/8/8/8 w - c6 0 1");

        // Test white pawn capturing en passant (e5xf6)
        Square start = Square.parseString("d5");
        Square target = Square.parseString("c6");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertFalse(result, "en passant move with discovered check should not be possible");
    }

    @Test
    public void testPawnPromotion() {
        // Setup a position where pawn promotion is possible
        field.trySetFEN("rnbqkbnr/ppppppP1/8/8/8/8/PPPPPPP1/RNBQKBNR w KQkq - 0 1");

        // Test white pawn promotion to queen (g7-h8Q)
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
        // Setup a position where a move results in check
        field.trySetFEN("rnbqkbnr/ppp3pp/5p2/3pN3/4P3/8/PPPP1PPP/RNBQKB1R w KQkq - 0 4");

        // Move white queen to check black king (Qd1-h5+)
        Square start = Square.parseString("d1");
        Square target = Square.parseString("h5");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid check move should return true");
        assertEquals(Player.BLACK, field.getKingInCheck(), "Black king should be in check");
    }

    @Test
    public void testGameEndedCheckmate() {
        // Setup a position where the next move is checkmate (Scholar's mate)
        field.trySetFEN("rnbqkbnr/pppp1ppp/8/4p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR b KQkq - 3 3");

        // Black's move (any legal move)
        Square start = Square.parseString("b8");
        Square target = Square.parseString("c6");
        Move blackMove = new Move(start, target);
        field.move(blackMove);

        // White's checkmate move (Qf3xf7#)
        start = Square.parseString("f3");
        target = Square.parseString("f7");
        Move whiteMove = new Move(start, target);
        field.move(whiteMove);

        assertEquals(GameState.WHITE_WIN, field.getGameState(), "Game should end in white's victory");
    }

    @Test
    public void testGameEndedStalemate() {
        // Setup a position where the next move leads to stalemate
        field.trySetFEN("k7/8/1Q6/8/8/8/8/7K b - - 0 1");

        assertEquals(GameState.DRAW, field.getGameState(), "Game should end in draw due to stalemate");
    }

    @Test
    public void testPerftInitialPosition() {
        // Test the number of legal moves from the initial position
        field.resetBoard();
        List<Square> startingSquares = getAllPieceSquares(field, false);
        int moveCount = countLegalMoves(field, startingSquares);

        assertEquals(20, moveCount, "Initial position should have 20 legal moves");
    }

    @Test
    public void testPerftPosition2() {
        // Test position 2 (kiwipete)
        field.trySetFEN("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        List<Square> startingSquares = getAllPieceSquares(field, false);
        int moveCount = countLegalMoves(field, startingSquares);

        assertEquals(48, moveCount, "Kiwipete position should have 48 legal moves");
    }

    @Test
    public void testPerftPosition3() {
        // Test position 3
        field.trySetFEN("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1");
        List<Square> startingSquares = getAllPieceSquares(field, false);
        int moveCount = countLegalMoves(field, startingSquares);

        assertEquals(14, moveCount, "Position 3 should have 14 legal moves");
    }

    // Added new test cases below

    @Test
    public void testBishopMove() {
        // Setup a position where a bishop can move
        field.trySetFEN("rnbqkbnr/pppp2pp/5p2/4p3/8/4PN2/PPPP1PPP/RNBQKB1R w KQkq - 0 2");

        // Test white bishop move (Bf1-c4)
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
        // Setup a position where a rook can move
        field.trySetFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/R3KBNR w KQkq - 0 1");

        // Test white rook move (Ra1-d1)
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
        // Setup a position where the queen can move
        field.trySetFEN("rnbqkbnr/pppp1ppp/8/4p3/8/2P5/PP1PPPPP/RNBQKBNR w KQkq - 0 2");

        // Test white queen move diagonally (Qd1-a4)
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
        // Setup a position where the king can move
        field.trySetFEN("rnbqkbnr/pppppppp/8/8/8/4P3/PPPP1PPP/RNBQKBNR w KQkq - 0 2");

        // Test white king move (Ke1-e2)
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
        // Setup a position where pawn promotion is possible
        field.trySetFEN("rnbqkb1r/ppppp1Pp/5n2/8/8/8/PPPPPPP1/RNBQKBNR w KQkq - 0 1");

        // Test white pawn promotion to knight (g7-h8N)
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
        // Setup a position where black can make an en passant capture
        field.trySetFEN("rnbqkbnr/ppp1pppp/8/8/3Pp3/8/PPP2PPP/RNBQKBNR b KQkq d3 0 3");

        // Test black pawn capturing en passant (e4xd3)
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
        // Test if en passant flag is set correctly after a double pawn move
        field.resetBoard();

        // Move white pawn d2-d4
        Square start = Square.parseString("d2");
        Square target = Square.parseString("d4");
        Move move = new Move(start, target);
        boolean result = field.move(move);

        assertTrue(result, "Valid double pawn move should return true");
        assertEquals("d3", field.getPossibleEnPassantSquare().toString(), "En passant target should be set to d3");

        // Make a black move that doesn't capture en passant
        start = Square.parseString("e7");
        target = Square.parseString("e5");
        move = new Move(start, target);
        field.move(move);

        // En passant target should now be e6
        assertEquals("e6", field.getPossibleEnPassantSquare().toString(), "En passant target should be updated to e6");
    }

    @Test
    public void testFoolsMate() {
        // Test the shortest possible checkmate: Fool's Mate
        field.resetBoard();

        // 1. f3
        Move move = new Move(Square.parseString("f2"), Square.parseString("f3"));
        field.move(move);

        // 1... e5
        move = new Move(Square.parseString("e7"), Square.parseString("e5"));
        field.move(move);

        // 2. g4
        move = new Move(Square.parseString("g2"), Square.parseString("g4"));
        field.move(move);

        // 2... Qh4#
        move = new Move(Square.parseString("d8"), Square.parseString("h4"));
        field.move(move);

        assertEquals(GameState.BLACK_WIN, field.getGameState(), "Game should end in black's victory (Fool's Mate)");
        assertEquals(Player.WHITE, field.getKingInCheck(), "White king should be in check");
    }

    @Test
    public void testDiscoveredCheck() {
        // Setup a position where a discovered check is possible
        field.trySetFEN("rnb3nr/ppp3pp/2k5/8/1bB5/2Q5/PPPP1PPP/RNB1K1NR w - - 2 3");

        // Move the white bishop to reveal check from the queen (Bc4-e2+)
        Square start = Square.parseString("c4");
        Square target = Square.parseString("e2");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid discovered check move should return true");
        assertEquals(Player.BLACK, field.getKingInCheck(), "Black king should be in check via discovered check");
    }

    @Test
    public void testPinDetection() {
        // Setup a position with a pinned knight
        field.trySetFEN("rnbq3r/pppp1kpp/5n2/2b1p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR b KQ - 3 3");

        // Attempt to move the pinned knight
        Square start = Square.parseString("f6");
        Square target = Square.parseString("d5");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertFalse(result, "Moving a pinned piece should return false");
        assertEquals(PieceUtil.BLACK_KNIGHT, field.getPieceBySquare(start), "Pinned knight should remain on its square");
    }

    @Test
    public void testSmootheredMatePosition() {
        // Setup a smoothered mate position
        field.trySetFEN("6rk/6pp/8/4N3/8/8/8/5K2 w - - 0 1");

        // Knight moves to give smoothered mate (Ne5-f7#)
        Square start = Square.parseString("e5");
        Square target = Square.parseString("f7");
        Move move = new Move(start, target);

        boolean result = field.move(move);

        assertTrue(result, "Valid smoothered mate move should return true");
        assertEquals(GameState.WHITE_WIN, field.getGameState(), "Game should end in white's victory via smoothered mate");
    }

    @Test
    public void testPieceCapture() {
        // Setup a position with a capturable piece
        field.trySetFEN("rnbqkbnr/ppp1pppp/8/3p4/3PP3/8/PPP2PPP/RNBQKBNR b KQkq - 0 2");

        // Black pawn captures white pawn (d5xe4)
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
        // Setup a position with insufficient material (just kings)
        field.trySetFEN("8/8/8/4k3/8/8/8/4K3 w - - 0 1");

        assertEquals(GameState.DRAW, field.getGameState(), "Game should be a draw due to insufficient material");
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