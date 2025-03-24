package at.htlhl.chess.entities;

import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.BoardViewController;
import at.htlhl.chess.gui.util.StockfishConnector;

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
