package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.util.PieceUtil;
import javafx.scene.image.Image;

import java.net.URL;

public class PieceImageUtil {
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
