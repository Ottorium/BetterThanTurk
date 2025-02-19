package at.htlhl.chess.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

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
        final int imageWidth = 1885;
        final int imageHeight = 605;
        int widthOfOnePiece = imageWidth / 6;
        int heightOfOnePiece = imageHeight / 2;

        int startPixelX = 0;
        int startPixelY = isBlack(piece) ? heightOfOnePiece : 0;
        piece = (byte) (piece & ~BLACK);

        if (piece == 0) return null;

        while (piece != 1) {
            startPixelX += widthOfOnePiece;
            piece >>= 1;
        }

        WritableImage croppedImage = new WritableImage(widthOfOnePiece, heightOfOnePiece);


        Image spriteSheet = new Image(PieceUtil.class.getResource("/at/htlhl/chess/resources/pieces.png").toExternalForm());

        PixelReader pixelReader = spriteSheet.getPixelReader();
        PixelWriter pixelWriter = croppedImage.getPixelWriter();
        pixelWriter.setPixels(
                0,
                0,
                widthOfOnePiece,
                heightOfOnePiece,
                pixelReader,
                startPixelX,
                startPixelY
        );

        return croppedImage;
    }
}