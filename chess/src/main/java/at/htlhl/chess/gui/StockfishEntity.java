package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.util.UCIClient;
import javafx.application.Platform;

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
        client.start(ChessApplication.prop.getProperty("stockfish_path"));
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
        client.close();
        executor.shutdownNow();
        executor = null;
    }
}
