package at.htlhl.chess.util;

public class InvalidFENException extends RuntimeException {
    public InvalidFENException(String message) {
        super(message);
    }
}
