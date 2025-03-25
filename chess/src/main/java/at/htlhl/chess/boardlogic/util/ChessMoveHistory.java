package at.htlhl.chess.boardlogic.util;

import java.util.ArrayDeque;
import java.util.Deque;

public class ChessMoveHistory {
    private final Deque<String> moveHistory;
    private final int maxNumberOfMoves = 10;

    public ChessMoveHistory() {
        this.moveHistory = new ArrayDeque<>();
    }

    public void addMove(String fenNotation) {
        moveHistory.addFirst(fenNotation);
        if (moveHistory.size() > maxNumberOfMoves) {
            moveHistory.removeLast();
        }
    }

    public String[] getLastMoves() {
        return moveHistory.toArray(new String[0]);
    }
}
