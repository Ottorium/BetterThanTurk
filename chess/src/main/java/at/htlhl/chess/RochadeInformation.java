package at.htlhl.chess;

public enum RochadeInformation {
    WHITE_KING_SIDE((byte) 0x1),
    WHITE_QUEEN_SIDE((byte) 0x2),
    BLACK_KING_SIDE((byte) 0x4),
    BLACK_QUEEN_SIDE((byte) 0x8);

    private final byte rochadeInformationValue;

    RochadeInformation(byte b) {
        this.rochadeInformationValue = b;
    }

    public byte getRochadeInformationValue() {
        return rochadeInformationValue;
    }

    public static boolean hasFlag(byte flags, RochadeInformation flag) {
        return (flags & flag.rochadeInformationValue) != 0;
    }

    public static byte combine(RochadeInformation... flags) {
        byte result = 0;
        for (RochadeInformation flag : flags) {
            result |= flag.rochadeInformationValue;
        }
        return result;
    }

    // Remove a specific flag from combined flags
    public static byte remove(byte currentFlags, RochadeInformation flagToRemove) {
        return (byte) (currentFlags & ~flagToRemove.rochadeInformationValue);
    }
}