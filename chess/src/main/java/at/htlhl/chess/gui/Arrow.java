package at.htlhl.chess.gui;

import at.htlhl.chess.boardlogic.Move;
import at.htlhl.chess.boardlogic.Square;
import at.htlhl.chess.boardlogic.util.PieceUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class Arrow {
    private Square startingSquare;
    private Square endSquare;
    private byte promotionPiece = 0;
    private Color color = Color.rgb(110, 110, 110);
    private double modifier;

    public Arrow(Square startingSquare, Square endSquare) {
        this(startingSquare, endSquare, 1);
    }

    public Arrow(Square startingSquare, Square endSquare, double modifier) {
        this.startingSquare = startingSquare;
        this.endSquare = endSquare;
        this.modifier = modifier;
    }

    public Square getStartingSquare() {
        return startingSquare;
    }

    public void setStartingSquare(Square startingSquare) {
        this.startingSquare = startingSquare;
    }

    public Square getEndSquare() {
        return endSquare;
    }

    public void setEndSquare(Square endSquare) {
        this.endSquare = endSquare;
    }

    public byte getPromotionPiece() {
        return promotionPiece;
    }

    public void setPromotionPiece(byte promotionPiece) {
        this.promotionPiece = promotionPiece;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Draws the arrow on arrowPane
     *
     * @param controller
     */

    public void draw(BoardViewController controller) {
        Line arrowBody = new Line();
        Polygon arrowHead = new Polygon();
        Pane arrowPane = controller.getArrowPane();
        DoubleBinding squareSizeBinding = controller.squareSizeBindingProperty();

        // Set up basic bindings for start and end points
        DoubleBinding startX = squareSizeBinding.multiply(startingSquare.x() + 0.5);
        DoubleBinding startY = squareSizeBinding.multiply(startingSquare.y() + 0.5);
        DoubleBinding endX = squareSizeBinding.multiply(endSquare.x() + 0.5);
        DoubleBinding endY = squareSizeBinding.multiply(endSquare.y() + 0.5);

        // Calculate direction vector and derived values as bindings
        DoubleBinding dx = endX.subtract(startX);
        DoubleBinding dy = endY.subtract(startY);
        DoubleBinding length = Bindings.createDoubleBinding(() -> Math.sqrt(dx.get() * dx.get() + dy.get() * dy.get()), dx, dy);
        DoubleBinding dirX = Bindings.createDoubleBinding(() -> length.get() > 0 ? dx.get() / length.get() : 0, dx, length);
        DoubleBinding dirY = Bindings.createDoubleBinding(() -> length.get() > 0 ? dy.get() / length.get() : 0, dy, length);
        DoubleBinding perpX = Bindings.createDoubleBinding(() -> -dirY.get(), dirY);
        DoubleBinding perpY = Bindings.createDoubleBinding(() -> dirX.get(), dirX);

        // Arrow dimensions
        DoubleBinding thickness = squareSizeBinding.multiply(0.1).multiply(modifier);
        DoubleBinding arrowHeadLength = squareSizeBinding.multiply(0.3).multiply(modifier);
        DoubleBinding arrowHeadWidth = squareSizeBinding.multiply(0.2).multiply(modifier);
        DoubleBinding bodyEndX = Bindings.createDoubleBinding(() -> endX.get() - dirX.get() * arrowHeadLength.get(), endX, dirX, arrowHeadLength);
        DoubleBinding bodyEndY = Bindings.createDoubleBinding(() -> endY.get() - dirY.get() * arrowHeadLength.get(), endY, dirY, arrowHeadLength);

        // Configure arrow body
        arrowBody.startXProperty().bind(startX);
        arrowBody.startYProperty().bind(startY);
        arrowBody.endXProperty().bind(bodyEndX);
        arrowBody.endYProperty().bind(bodyEndY);
        arrowBody.setStroke(color);
        arrowBody.strokeWidthProperty().bind(thickness);

        // Set up arrow head points updater
        InvalidationListener pointsUpdater = obs -> {
            arrowHead.getPoints().clear();
            arrowHead.getPoints().addAll(
                    endX.get(), endY.get(),
                    bodyEndX.get() + perpX.get() * arrowHeadWidth.get(), bodyEndY.get() + perpY.get() * arrowHeadWidth.get(),
                    bodyEndX.get() - perpX.get() * arrowHeadWidth.get(), bodyEndY.get() - perpY.get() * arrowHeadWidth.get()
            );
        };

        // Register listeners and initialize
        endX.addListener(pointsUpdater);
        endY.addListener(pointsUpdater);
        bodyEndX.addListener(pointsUpdater);
        bodyEndY.addListener(pointsUpdater);
        perpX.addListener(pointsUpdater);
        perpY.addListener(pointsUpdater);
        arrowHeadWidth.addListener(pointsUpdater);
        pointsUpdater.invalidated(null);

        arrowHead.setFill(color);

        arrowPane.getChildren().addAll(arrowBody, arrowHead);

        // draw promotion piece if exists
        if (PieceUtil.isEmpty(promotionPiece) == false) {
            controller.drawPiece(promotionPiece, endSquare, 40);
        }
    }

}
