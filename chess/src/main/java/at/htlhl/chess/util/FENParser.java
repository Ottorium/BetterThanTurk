package at.htlhl.chess.util;

import at.htlhl.chess.Square;

import java.util.Dictionary;
import java.util.Hashtable;

public class FENParser {

    private final String fen;

    public FENParser(String fen) {
        if (fen.split(" ").length != 6) throw new InvalidFENException(fen);
        this.fen = fen;
    }


    private final Dictionary<Character, Byte> fenPieceMap = new Hashtable<>();

    {
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

    public byte parseRochadeInformation() {
        String rochadeString = fen.split(" ")[2];
        if (rochadeString.length() >= 5) throw new InvalidFENException(fen);

        byte rochadeByte = 0;
        if (rochadeString.contains("k")) rochadeByte = RochadeUtil.add(rochadeByte, RochadeUtil.BLACK_KING_SIDE);
        if (rochadeString.contains("K")) rochadeByte = RochadeUtil.add(rochadeByte, RochadeUtil.WHITE_KING_SIDE);
        if (rochadeString.contains("q")) rochadeByte = RochadeUtil.add(rochadeByte, RochadeUtil.BLACK_QUEEN_SIDE);
        if (rochadeString.contains("Q")) rochadeByte = RochadeUtil.add(rochadeByte, RochadeUtil.WHITE_QUEEN_SIDE);
        return rochadeByte;
    }

    public Square parsePossibleEnPassantMove() {
        return Square.parseString(fen.split(" ")[3]);
    }

    public int parsePlayedHalfMovesSinceLastPawnMoveOrCapture() {
        return Integer.parseInt(fen.split(" ")[4]);
    }

    public int parseNumberOfNextMove() {
        return Integer.parseInt(fen.split(" ")[5]);
    }
}
