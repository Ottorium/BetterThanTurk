package at.htlhl.chess.boardlogic.util;

import at.htlhl.chess.boardlogic.Square;

import java.security.InvalidParameterException;
import java.util.Dictionary;
import java.util.Hashtable;

public class FENParser {

    private static final Dictionary<Character, Byte> fenPieceMap = new Hashtable<>();
    private static final Dictionary<Byte, Character> pieceFenMap = new Hashtable<>();

    static {
        fenPieceMap.put('p', PieceUtil.BLACK_PAWN);
        fenPieceMap.put('P', PieceUtil.WHITE_PAWN);
        fenPieceMap.put('b', PieceUtil.BLACK_BISHOP);
        fenPieceMap.put('B', PieceUtil.WHITE_BISHOP);
        fenPieceMap.put('n', PieceUtil.BLACK_KNIGHT);
        fenPieceMap.put('N', PieceUtil.WHITE_KNIGHT);
        fenPieceMap.put('r', PieceUtil.BLACK_ROOK);
        fenPieceMap.put('R', PieceUtil.WHITE_ROOK);
        fenPieceMap.put('q', PieceUtil.BLACK_QUEEN);
        fenPieceMap.put('Q', PieceUtil.WHITE_QUEEN);
        fenPieceMap.put('k', PieceUtil.BLACK_KING);
        fenPieceMap.put('K', PieceUtil.WHITE_KING);

        pieceFenMap.put(PieceUtil.BLACK_PAWN, 'p');
        pieceFenMap.put(PieceUtil.WHITE_PAWN, 'P');
        pieceFenMap.put(PieceUtil.BLACK_BISHOP, 'b');
        pieceFenMap.put(PieceUtil.WHITE_BISHOP, 'B');
        pieceFenMap.put(PieceUtil.BLACK_KNIGHT, 'n');
        pieceFenMap.put(PieceUtil.WHITE_KNIGHT, 'N');
        pieceFenMap.put(PieceUtil.BLACK_ROOK, 'r');
        pieceFenMap.put(PieceUtil.WHITE_ROOK, 'R');
        pieceFenMap.put(PieceUtil.BLACK_QUEEN, 'q');
        pieceFenMap.put(PieceUtil.WHITE_QUEEN, 'Q');
        pieceFenMap.put(PieceUtil.BLACK_KING, 'k');
        pieceFenMap.put(PieceUtil.WHITE_KING, 'K');
    }

    private final String fen;

    public FENParser(String fen) {
        if (fen.split(" ").length != 6) throw new InvalidFENException(fen);
        this.fen = fen;
    }

    /**
     * Makes a fen, representing the given board
     * @return the fen
     */
    public static String exportToFEN(
            byte[][] board,
            boolean blackTurn,
            byte castlingInformation,
            Square possibleEnPassantSquare,
            int playedHalfMovesSinceLastPawnMoveOrCapture,
            int numberOfNextMove) {

        StringBuilder fen = new StringBuilder();


        for (int row = 0; row <= 7; row++) {
            int emptySquares = 0;
            for (int col = 0; col < 8; col++) {
                byte piece = board[row][col];
                if (piece == PieceUtil.EMPTY) {
                    emptySquares++;
                    continue;
                }

                if (emptySquares > 0) {
                    fen.append(emptySquares);
                    emptySquares = 0;
                }
                fen.append(pieceFenMap.get(piece));

            }
            if (emptySquares > 0)
                fen.append(emptySquares);
            if (row != 7)
                fen.append('/');
        }

        fen.append(" ").append(blackTurn ? 'b' : 'w');

        String castling = "";
        if (CastlingUtil.hasFlag(castlingInformation, CastlingUtil.WHITE_KING_SIDE)) castling += "K";
        if (CastlingUtil.hasFlag(castlingInformation, CastlingUtil.WHITE_QUEEN_SIDE)) castling += "Q";
        if (CastlingUtil.hasFlag(castlingInformation, CastlingUtil.BLACK_KING_SIDE)) castling += "k";
        if (CastlingUtil.hasFlag(castlingInformation, CastlingUtil.BLACK_QUEEN_SIDE)) castling += "q";
        fen.append(" ").append(castling.isEmpty() ? "-" : castling);

        fen.append(" ").append(possibleEnPassantSquare != null ? possibleEnPassantSquare.toString() : "-");

        fen.append(" ").append(playedHalfMovesSinceLastPawnMoveOrCapture);

        fen.append(" ").append(numberOfNextMove);

        return fen.toString();
    }

    public byte[][] parseBoard() {
        var newBoard = new byte[8][8];
        int currentRow = 0;
        var rows = fen.split(" ")[0].split("/");
        if (rows.length != 8) throw new InvalidFENException("FEN Invalid: " + fen);

        for (String row : rows) {
            int currentColumn = 0;
            for (char c : row.toCharArray()) {
                if (Character.isDigit(c)) {
                    currentColumn += Integer.parseInt(Character.toString(c));
                    continue;
                }
                newBoard[currentRow][currentColumn] = fenPieceMap.get(c);
                currentColumn++;
            }
            currentRow++;
        }


        return newBoard;
    }

    public boolean parseIsBlacksTurn() {
        char bWChar = fen.split(" ")[1].toCharArray()[0];
        if (bWChar == 'b') return true;
        else if (bWChar == 'w') return false;
        else throw new InvalidFENException(fen);
    }

    public byte parseCastlingInformation() {
        String castlingString = fen.split(" ")[2];
        if (castlingString.length() >= 5) throw new InvalidFENException(fen);

        byte castlingByte = CastlingUtil.INITIAL_NO_RIGHTS;
        if (castlingString.contains("k")) castlingByte = CastlingUtil.add(castlingByte, CastlingUtil.BLACK_KING_SIDE);
        if (castlingString.contains("K")) castlingByte = CastlingUtil.add(castlingByte, CastlingUtil.WHITE_KING_SIDE);
        if (castlingString.contains("q")) castlingByte = CastlingUtil.add(castlingByte, CastlingUtil.BLACK_QUEEN_SIDE);
        if (castlingString.contains("Q")) castlingByte = CastlingUtil.add(castlingByte, CastlingUtil.WHITE_QUEEN_SIDE);
        return castlingByte;
    }

    public Square parsePossibleEnPassantMove() {
        var s = fen.split(" ")[3];
        if (s.equals("-")) return null;
        try {
            return Square.parseString(s);
        } catch (InvalidParameterException e) {
            throw new InvalidFENException(fen);
        }
    }

    public int parsePlayedHalfMovesSinceLastPawnMoveOrCapture() {
        try {
            return Integer.parseInt(fen.split(" ")[4]);
        } catch (NumberFormatException e) {
            throw new InvalidFENException(fen);
        }
    }

    public int parseNumberOfNextMove() {
        try {
            return Integer.parseInt(fen.split(" ")[5]);
        } catch (NumberFormatException e) {
            throw new InvalidFENException(fen);
        }
    }
}