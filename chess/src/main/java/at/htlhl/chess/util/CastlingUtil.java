package at.htlhl.chess.util;

public final class CastlingUtil {
    public static final byte WHITE_KING_SIDE = ((byte) 0x1);
    public static final byte WHITE_QUEEN_SIDE = ((byte) 0x2);
    public static final byte BLACK_KING_SIDE = ((byte) 0x4);
    public static final byte BLACK_QUEEN_SIDE = ((byte) 0x8);

    public static boolean hasFlag(byte flags, byte flag) {
        return (flags & flag) != 0;
    }

    public static byte add(byte currentFlags, byte flagToAdd) {
        return (byte) (currentFlags | flagToAdd);
    }

    public static byte remove(byte currentFlags, byte flagToRemove) {
        return (byte) (currentFlags & ~flagToRemove);
    }
}