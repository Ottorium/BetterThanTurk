package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.util.UCIClient;
import javafx.application.Platform;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StockfishEntity extends PlayingEntity {

    UCIClient client = new UCIClient();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public StockfishEntity(Player player, BoardViewController boardViewController) {
        super(player, boardViewController);
        connectToStockfish();
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
    protected void allowMove() {
        super.allowMove();
        suggestMove();
    }

    private void suggestMove() {
        executor.submit(() -> {
            client.setPosition(boardViewController.getField().getFEN());
            Move move = client.getBestMove();
            if (move != null) {
                Platform.runLater(() -> move(move));
            }
        });
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (client.isAlive()){
            client.close();
        }
        executor.shutdownNow();
        executor = null;
    }
}
