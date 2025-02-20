package at.htlhl.chess.util;

import javafx.scene.image.Image;

import java.io.FileNotFoundException;
import java.net.URL;

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

    public static Image getImage(byte piece) {
        if (isEmpty(piece)) return null;

        return switch (piece) {
            case WHITE_PAWN -> WHITE_PAWN_IMAGE;
            case WHITE_KNIGHT -> WHITE_KNIGHT_IMAGE;
            case WHITE_BISHOP -> WHITE_BISHOP_IMAGE;
            case WHITE_ROOK -> WHITE_ROOK_IMAGE;
            case WHITE_QUEEN -> WHITE_QUEEN_IMAGE;
            case WHITE_KING -> WHITE_KING_IMAGE;
            case BLACK_PAWN -> BLACK_PAWN_IMAGE;
            case BLACK_KNIGHT -> BLACK_KNIGHT_IMAGE;
            case BLACK_BISHOP -> BLACK_BISHOP_IMAGE;
            case BLACK_ROOK -> BLACK_ROOK_IMAGE;
            case BLACK_QUEEN -> BLACK_QUEEN_IMAGE;
            case BLACK_KING -> BLACK_KING_IMAGE;
            default -> throw new RuntimeException("Invalid Piece: " + piece);
        };
    }

    public static Image loadImageFromDisk(byte piece) {
        String name = PieceUtil.toString(piece);
        if (name == null) return null;
        URL resource = PieceUtil.class.getResource("/at/htlhl/chess/sprites/" + name + ".png");
        if (resource == null) throw new RuntimeException(name + ".png not found.");
        return new Image(resource.toExternalForm());
    }

    public static final Image WHITE_PAWN_IMAGE = loadImageFromDisk(WHITE_PAWN);
    public static final Image WHITE_KNIGHT_IMAGE = loadImageFromDisk(WHITE_KNIGHT);
    public static final Image WHITE_BISHOP_IMAGE = loadImageFromDisk(WHITE_BISHOP);
    public static final Image WHITE_ROOK_IMAGE = loadImageFromDisk(WHITE_ROOK);
    public static final Image WHITE_QUEEN_IMAGE = loadImageFromDisk(WHITE_QUEEN);
    public static final Image WHITE_KING_IMAGE = loadImageFromDisk(WHITE_KING);
    public static final Image BLACK_PAWN_IMAGE = loadImageFromDisk(BLACK_PAWN);
    public static final Image BLACK_KNIGHT_IMAGE = loadImageFromDisk(BLACK_KNIGHT);
    public static final Image BLACK_BISHOP_IMAGE = loadImageFromDisk(BLACK_BISHOP);
    public static final Image BLACK_ROOK_IMAGE = loadImageFromDisk(BLACK_ROOK);
    public static final Image BLACK_QUEEN_IMAGE = loadImageFromDisk(BLACK_QUEEN);
    public static final Image BLACK_KING_IMAGE = loadImageFromDisk(BLACK_KING);



    private static String toString(byte piece) {
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