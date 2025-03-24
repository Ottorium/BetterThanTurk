package at.htlhl.chess.entities;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.BoardViewController;
import at.htlhl.chess.gui.ChessApplication;
import at.htlhl.chess.gui.util.StockfishConnector;
import at.htlhl.chess.gui.util.UCIClient;
import javafx.application.Platform;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StockfishEntity extends PlayingEntity {

    StockfishConnector connector;

    public StockfishEntity(Player player, BoardViewController boardViewController) {
        super(player, boardViewController);
        connectToStockfish();
    }

    private void connectToStockfish() {
        connector = new StockfishConnector(boardViewController);
    }

    @Override
    public void allowMove() {
        super.allowMove();
        connector.suggestMove(this::move);
    }

    @Override
    public void shutdown() {
       connector.shutdown();
    }
}
