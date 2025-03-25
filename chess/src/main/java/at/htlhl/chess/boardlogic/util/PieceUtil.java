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
    public static final byte WHITE = (byte) 0x80;

    public static final byte EMPTY = (byte) 0;

    public static final byte WHITE_PAWN = (byte) (PAWN_MASK | WHITE);
    public static final byte WHITE_KNIGHT = (byte) (KNIGHT_MASK | WHITE);
    public static final byte WHITE_BISHOP = (byte) (BISHOP_MASK | WHITE);
    public static final byte WHITE_ROOK = (byte) (ROOK_MASK | WHITE);
    public static final byte WHITE_QUEEN = (byte) (QUEEN_MASK | WHITE);
    public static final byte WHITE_KING = (byte) (KING_MASK | WHITE);
    public static final byte BLACK_PAWN = (byte) (PAWN_MASK | BLACK);
    public static final byte BLACK_KNIGHT = (byte) (KNIGHT_MASK | BLACK);
    public static final byte BLACK_BISHOP = (byte) (BISHOP_MASK | BLACK);
    public static final byte BLACK_ROOK = (byte) (ROOK_MASK | BLACK);
    public static final byte BLACK_QUEEN = (byte) (QUEEN_MASK | BLACK);
    public static final byte BLACK_KING = (byte) (KING_MASK | BLACK);

    public static final int RELATIVE_KING_VALUE = Integer.MAX_VALUE - 100_000; // not the max value to avoid overflows
    public static final int RELATIVE_QUEEN_VALUE = 950;
    public static final int RELATIVE_BISHOP_VALUE = 333;
    public static final int RELATIVE_KNIGHT_VALUE = 305;
    public static final int RELATIVE_ROOK_VALUE = 563;
    public static final int RELATIVE_PAWN_VALUE = 100;


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

    public static int getRelativeValue(byte piece) {
        if (isEmpty(piece)) return 0;
        int value;
        if (isKing(piece)) value = RELATIVE_KING_VALUE;
        else if (isQueen(piece)) value = RELATIVE_QUEEN_VALUE;
        else if (isRook(piece)) value = RELATIVE_ROOK_VALUE;
        else if (isBishop(piece)) value = RELATIVE_BISHOP_VALUE;
        else if (isKnight(piece)) value = RELATIVE_KNIGHT_VALUE;
        else if (isPawn(piece)) value = RELATIVE_PAWN_VALUE;
        else throw new UnsupportedOperationException("Invalid Piece");

        return isWhite(piece) ? -value : value;
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

    public static byte getPieceByName(char name) {
        switch (name) {
            // White pieces (uppercase)
            case 'K':
                return WHITE_KING;
            case 'Q':
                return WHITE_QUEEN;
            case 'R':
                return WHITE_ROOK;
            case 'B':
                return WHITE_BISHOP;
            case 'N':
                return WHITE_KNIGHT;
            case 'P':
                return WHITE_PAWN;
            // Black pieces (lowercase)
            case 'k':
                return BLACK_KING;
            case 'q':
                return BLACK_QUEEN;
            case 'r':
                return BLACK_ROOK;
            case 'b':
                return BLACK_BISHOP;
            case 'n':
                return BLACK_KNIGHT;
            case 'p':
                return BLACK_PAWN;
            default:
                throw new IllegalArgumentException("Invalid piece character: " + name);
        }
    }
}