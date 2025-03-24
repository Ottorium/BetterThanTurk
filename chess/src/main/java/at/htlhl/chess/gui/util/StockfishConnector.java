package at.htlhl.chess.gui.util;

import at.htlhl.chess.boardlogic.Field;
import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.engine.EvaluatedMove;
import at.htlhl.chess.gui.BoardViewController;
import at.htlhl.chess.gui.ChessApplication;
import javafx.application.Platform;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class StockfishConnector extends EngineConnector {

    private UCIClient client = new UCIClient();
    private BoardViewController boardViewController;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public StockfishConnector(BoardViewController boardViewController) {
        this.boardViewController = boardViewController;
        executor.execute(this::connectToStockfish);
    }

    private void connectToStockfish() {
        try {
            client.start(ChessApplication.prop.getProperty("stockfish_path"));
        } catch (IOException e) {
            System.err.println("Stockfish connection failed: " + e.getMessage());
            boardViewController.alertWithNewGame("Stockfish connection failed", e.getMessage());
        }
    }

    @Override
    public void suggestMove(Consumer<Move> moveCallback) {
        executor.submit(() -> {
            client.setPosition(boardViewController.getField().getFEN());
            Move move = client.getBestMove();
            if (move != null) {
                Platform.runLater(() -> moveCallback.accept(move));
            }
        });
    }

    @Override
    public void suggestMoves(Consumer<List<EvaluatedMove>> movesCallback) {
        executor.submit(() -> {
            client.setPosition(boardViewController.getField().getFEN());
            List<EvaluatedMove> moves = client.getBestMoves();
            if (moves != null) {
                Platform.runLater(() -> movesCallback.accept(moves));
            }
        });
    }

    @Override
    public void stopCurrentExecutions() {

    }

    @Override
    public void shutdown() {
        if (client.isAlive()) {
            client.close();
        }
        executor.shutdownNow();
        executor = null;
    }
}
