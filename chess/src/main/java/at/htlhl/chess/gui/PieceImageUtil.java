package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.util.PieceUtil;
import javafx.scene.image.Image;

import java.net.URL;

/**
 * Utility class for loading and retrieving images of chess pieces.
 * Provides static methods to access piece images based on their type and color,
 * and preloads all piece images from disk at class initialization.
 */
public class PieceImageUtil {

    /**
     * Retrieves the {@link Image} corresponding to the specified chess piece.
     *
     * @param piece The byte value representing the chess piece, as defined in {@link PieceUtil}.
     * @return The {@link Image} of the specified piece, or null if the piece is empty.
     * @throws RuntimeException if the piece value is invalid.
     */
    public static Image getImage(byte piece) {
        if (PieceUtil.isEmpty(piece)) return null;

        return switch (piece) {
            case PieceUtil.WHITE_PAWN -> WHITE_PAWN_IMAGE;
            case PieceUtil.WHITE_KNIGHT -> WHITE_KNIGHT_IMAGE;
            case PieceUtil.WHITE_BISHOP -> WHITE_BISHOP_IMAGE;
            case PieceUtil.WHITE_ROOK -> WHITE_ROOK_IMAGE;
            case PieceUtil.WHITE_QUEEN -> WHITE_QUEEN_IMAGE;
            case PieceUtil.WHITE_KING -> WHITE_KING_IMAGE;
            case PieceUtil.BLACK_PAWN -> BLACK_PAWN_IMAGE;
            case PieceUtil.BLACK_KNIGHT -> BLACK_KNIGHT_IMAGE;
            case PieceUtil.BLACK_BISHOP -> BLACK_BISHOP_IMAGE;
            case PieceUtil.BLACK_ROOK -> BLACK_ROOK_IMAGE;
            case PieceUtil.BLACK_QUEEN -> BLACK_QUEEN_IMAGE;
            case PieceUtil.BLACK_KING -> BLACK_KING_IMAGE;
            default -> throw new RuntimeException("Invalid Piece: " + piece);
        };
    }

    /**
     * Loads an image for a specific chess piece from disk based on its string representation.
     * Images are expected to be located in the "/at/htlhl/chess/gui/sprites/" resource directory.
     *
     * @param piece The byte value representing the chess piece, as defined in {@link PieceUtil}.
     * @return The loaded {@link Image}, or null if the piece is empty.
     * @throws RuntimeException if the image file for the piece is not found.
     */
    public static Image loadImageFromDisk(byte piece) {
        String name = PieceUtil.toString(piece);
        if (name == null) return null;
        URL resource = PieceUtil.class.getResource("/at/htlhl/chess/gui/sprites/" + name + ".png");
        if (resource == null) throw new RuntimeException(name + ".png not found.");
        return new Image(resource.toExternalForm());
    }

    public static final Image WHITE_PAWN_IMAGE = loadImageFromDisk(PieceUtil.WHITE_PAWN);
    public static final Image WHITE_KNIGHT_IMAGE = loadImageFromDisk(PieceUtil.WHITE_KNIGHT);
    public static final Image WHITE_BISHOP_IMAGE = loadImageFromDisk(PieceUtil.WHITE_BISHOP);
    public static final Image WHITE_ROOK_IMAGE = loadImageFromDisk(PieceUtil.WHITE_ROOK);
    public static final Image WHITE_QUEEN_IMAGE = loadImageFromDisk(PieceUtil.WHITE_QUEEN);
    public static final Image WHITE_KING_IMAGE = loadImageFromDisk(PieceUtil.WHITE_KING);
    public static final Image BLACK_PAWN_IMAGE = loadImageFromDisk(PieceUtil.BLACK_PAWN);
    public static final Image BLACK_KNIGHT_IMAGE = loadImageFromDisk(PieceUtil.BLACK_KNIGHT);
    public static final Image BLACK_BISHOP_IMAGE = loadImageFromDisk(PieceUtil.BLACK_BISHOP);
    public static final Image BLACK_ROOK_IMAGE = loadImageFromDisk(PieceUtil.BLACK_ROOK);
    public static final Image BLACK_QUEEN_IMAGE = loadImageFromDisk(PieceUtil.BLACK_QUEEN);
    public static final Image BLACK_KING_IMAGE = loadImageFromDisk(PieceUtil.BLACK_KING);
}
