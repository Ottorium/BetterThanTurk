package at.htlhl.chess.boardlogic.util;

public class InvalidFENException extends RuntimeException {
    public InvalidFENException(String message) {
        super(message);
    }
}
