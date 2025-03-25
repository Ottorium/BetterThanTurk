package at.htlhl.chess.boardlogic;

import at.htlhl.chess.boardlogic.util.PieceUtil;

import java.util.Objects;

public class Move {
    private Square startingSquare;
    private Square targetSquare;
    private byte promotionPiece;
    private boolean isCastlingMove;
    private boolean isEnPassantMove;
    private boolean isLegal;
    private Square possibleEnPassantSquare;
    private byte capturedPiece;

    public Move(Square startingSquare, Square targetSquare) {
        this.startingSquare = startingSquare;
        this.targetSquare = targetSquare;
        this.promotionPiece = PieceUtil.EMPTY;
        this.isLegal = false;
        this.isCastlingMove = false;
        this.possibleEnPassantSquare = null;
        this.capturedPiece = PieceUtil.EMPTY;
    }

    @Override
    public String toString() {
        return startingSquare.toString() + "-" + targetSquare.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;
        return Objects.equals(startingSquare, move.startingSquare) &&
                Objects.equals(targetSquare, move.targetSquare) &&
                promotionPiece == move.promotionPiece &&
                isCastlingMove == move.isCastlingMove &&
                isEnPassantMove == move.isEnPassantMove &&
                isLegal == move.isLegal &&
                Objects.equals(possibleEnPassantSquare, move.possibleEnPassantSquare) &&
                capturedPiece == move.capturedPiece;
    }

    public Move clone() {
        Move clonedMove = new Move(
                new Square(this.startingSquare.x(), this.startingSquare.y()),
                new Square(this.targetSquare.x(), this.targetSquare.y())
        );

        clonedMove.promotionPiece = this.promotionPiece;
        clonedMove.isCastlingMove = this.isCastlingMove;
        clonedMove.isEnPassantMove = this.isEnPassantMove;
        clonedMove.isLegal = this.isLegal;
        clonedMove.capturedPiece = this.capturedPiece;
        if (this.possibleEnPassantSquare != null)
            clonedMove.possibleEnPassantSquare = new Square(this.possibleEnPassantSquare.x(), this.possibleEnPassantSquare.y());

        return clonedMove;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                startingSquare,
                targetSquare,
                promotionPiece,
                isCastlingMove,
                isEnPassantMove,
                isLegal,
                possibleEnPassantSquare,
                capturedPiece
        );
    }

    public Square getStartingSquare() {
        return startingSquare;
    }

    public void setStartingSquare(Square startingSquare) {
        this.startingSquare = startingSquare;
    }

    public Square getTargetSquare() {
        return targetSquare;
    }

    public void setTargetSquare(Square targetSquare) {
        this.targetSquare = targetSquare;
    }

    public byte getPromotionPiece() {
        return promotionPiece;
    }

    public void setPromotionPiece(byte promotionPiece) {
        this.promotionPiece = promotionPiece;
    }

    public boolean isCastlingMove() {
        return isCastlingMove;
    }

    public void setCastlingMove(boolean castlingMove) {
        isCastlingMove = castlingMove;
    }

    public boolean isLegal() {
        return isLegal;
    }

    public void setLegal(boolean legal) {
        isLegal = legal;
    }

    public Square getPossibleEnPassantSquare() {
        return possibleEnPassantSquare;
    }

    public void setPossibleEnPassantSquare(Square possibleEnPassantSquare) {
        this.possibleEnPassantSquare = possibleEnPassantSquare;
    }

    public boolean isEnPassantMove() {
        return isEnPassantMove;
    }

    public void setEnPassantMove(boolean enPassantMove) {
        isEnPassantMove = enPassantMove;
    }

    public boolean isCapture() {
        return capturedPiece != PieceUtil.EMPTY;
    }

    public byte getCapturedPiece() {
        return capturedPiece;
    }

    public void setCapturedPiece(byte capturedPiece) {
        this.capturedPiece = capturedPiece;
    }

    public int[] getDirection(boolean isKnightMove) {
        if (startingSquare == null || targetSquare == null) {
            return new int[]{0, 0};
        }

        int dx = targetSquare.x() - startingSquare.x();
        int dy = targetSquare.y() - startingSquare.y();

        if (isKnightMove)
            return new int[]{dx, dy};

        if (dx != 0)
            dx = dx > 0 ? 1 : -1;
        if (dy != 0)
            dy = dy > 0 ? 1 : -1;

        return new int[]{dx, dy};
    }

    /**
     * parses a move from a string like a1a2
     */
    public static Move valueOf(String text){
        Square startingSquare = Square.parseString(text.substring(0, 2));
        Square endingSquare = Square.parseString(text.substring(2, 4));
        Move move = new Move(startingSquare, endingSquare);
        if (text.length() == 5){
            move.setPromotionPiece(PieceUtil.getPieceByName(text.charAt(4)));
        }
        return move;
    }
}
