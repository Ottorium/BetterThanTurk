package at.htlhl.chess.util;

import at.htlhl.chess.Move;

import java.security.InvalidParameterException;

public class FENParser {

    private String fen;

    public FENParser(String fen) {
        this.fen = fen;
    }

    public byte[][] parseBoard() {
        return null;
    }

    public boolean parseIsBlacksTurn() {
        return false;
    }

    public byte parseRochadeInformation() {
        return 0;
    }

    public Move parsePossibleEnPassantMove() {
        return null;
    }

    public int parsePlayedHalfMovesSinceLastPawnMoveOrCapture() {
        return 0;
    }

    public int parseNumberOfNextMove() {
        return 0;
    }
}
