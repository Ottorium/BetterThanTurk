package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Player;
import at.htlhl.chess.gui.util.UCIClient;

public class StockfishEntity extends PlayingEntity {

    UCIClient client = new UCIClient();

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
        client.setPosition(boardViewController.getField().getFEN());
        Move move = client.getBestMove();
        if (move != null) {
            move(move);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        client.close();
    }
}
