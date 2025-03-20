package at.htlhl.chess.engine;

import at.htlhl.chess.boardlogic.Move;

public record EvaluatedMove(Move move, int evaluation) {
}
