package at.htlhl.chess.boardlogic.util;

/**
 * Utility class for managing castling rights in a chess game using bitwise flags.
 * Each castling right (kingside/queenside for both colors) is represented by a single bit.
 */
public final class CastlingUtil {

    public static final byte INITIAL_NO_RIGHTS = ((byte) 0x0);
    public static final byte WHITE_KING_SIDE = ((byte) 0x1);
    public static final byte WHITE_QUEEN_SIDE = ((byte) 0x2);
    public static final byte BLACK_KING_SIDE = ((byte) 0x4);
    public static final byte BLACK_QUEEN_SIDE = ((byte) 0x8);

    /**
     * Checks if a specific castling right is enabled in the given flags.
     *
     * @param flags The current castling rights flags
     * @param flag The specific castling right to check for (one of the static flag constants)
     * @return true if the specified castling right is enabled, false otherwise
     */
    public static boolean hasFlag(byte flags, byte flag) {
        return (flags & flag) != 0;
    }

    /**
     * Adds a castling right to the current flags using bitwise OR operation.
     *
     * @param currentFlags The current castling rights flags
     * @param flagToAdd The castling right to add (one of the static flag constants)
     * @return A new byte with the specified castling right enabled
     */
    public static byte add(byte currentFlags, byte flagToAdd) {
        return (byte) (currentFlags | flagToAdd);
    }

    /**
     * Removes a castling right from the current flags using bitwise AND and NOT operations.
     *
     * @param currentFlags The current castling rights flags
     * @param flagToRemove The castling right to remove (one of the static flag constants)
     * @return A new byte with the specified castling right disabled
     */
    public static byte remove(byte currentFlags, byte flagToRemove) {
        return (byte) (currentFlags & ~flagToRemove);
    }
}