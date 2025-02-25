package at.htlhl.chess.boardlogic.util;

public final class PieceUtil {

    private PieceUtil() {
    }

    public static final byte KING_MASK = 0x1;
    public static final byte QUEEN_MASK = 0x2;
    public static final byte BISHOP_MASK = 0x4;
    public static final byte KNIGHT_MASK = 0x8;
    public static final byte ROOK_MASK = 0x10;
    public static final byte PAWN_MASK = 0x20;

    public static final byte BLACK = (byte) 0x40;

    public static final byte EMPTY = (byte) 0;

    public static final byte WHITE_PAWN = PAWN_MASK;
    public static final byte WHITE_KNIGHT = KNIGHT_MASK;
    public static final byte WHITE_BISHOP = BISHOP_MASK;
    public static final byte WHITE_ROOK = ROOK_MASK;
    public static final byte WHITE_QUEEN = QUEEN_MASK;
    public static final byte WHITE_KING = KING_MASK;
    public static final byte BLACK_PAWN = (byte) (PAWN_MASK | BLACK);
    public static final byte BLACK_KNIGHT = (byte) (KNIGHT_MASK | BLACK);
    public static final byte BLACK_BISHOP = (byte) (BISHOP_MASK | BLACK);
    public static final byte BLACK_ROOK = (byte) (ROOK_MASK | BLACK);
    public static final byte BLACK_QUEEN = (byte) (QUEEN_MASK | BLACK);
    public static final byte BLACK_KING = (byte) (KING_MASK | BLACK);


    public static boolean isBlack(byte binaryInformation) {
        return (binaryInformation & BLACK) != 0;
    }

    public static boolean isEmpty(byte binaryInformation) {
        return binaryInformation == EMPTY;
    }

    public static boolean isWhite(byte binaryInformation) {
        return (binaryInformation & BLACK) == 0;
    }

    public static boolean isPawn(byte binaryInformation) {
        return (binaryInformation & PAWN_MASK) != 0;
    }

    public static boolean isKnight(byte binaryInformation) {
        return (binaryInformation & KNIGHT_MASK) != 0;
    }

    public static boolean isBishop(byte binaryInformation) {
        return (binaryInformation & BISHOP_MASK) != 0;
    }

    public static boolean isRook(byte binaryInformation) {
        return (binaryInformation & ROOK_MASK) != 0;
    }

    public static boolean isQueen(byte binaryInformation) {
        return (binaryInformation & QUEEN_MASK) != 0;
    }

    public static boolean isKing(byte binaryInformation) {
        return (binaryInformation & KING_MASK) != 0;
    }



    public static String toString(byte piece) {
        String name = PieceUtil.isBlack(piece) ? "black_" : "white_";

        if (PieceUtil.isPawn(piece)) name += "pawn";
        else if (PieceUtil.isBishop(piece)) name += "bishop";
        else if (PieceUtil.isRook(piece)) name += "rook";
        else if (PieceUtil.isKnight(piece)) name += "knight";
        else if (PieceUtil.isQueen(piece)) name += "queen";
        else if (PieceUtil.isKing(piece)) name += "king";
        else return null;

        return name;
    }
}