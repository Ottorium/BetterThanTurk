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
    private Player appearedCheck;
    private Square possibleEnPassantSquare;

    public Move(Square startingSquare, Square targetSquare) {
        this.startingSquare = startingSquare;
        this.targetSquare = targetSquare;
        this.promotionPiece = PieceUtil.QUEEN_MASK;
        this.isLegal = false;
        this.isCastlingMove = false;
        this.appearedCheck = null;
        this.possibleEnPassantSquare = null;
    }

    @Override
    public String toString() {
        return startingSquare.toString() + "-" + targetSquare.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return Objects.equals(targetSquare, move.targetSquare) && Objects.equals(startingSquare, move.startingSquare);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startingSquare, targetSquare);
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

    public Player getAppearedCheck() {
        return appearedCheck;
    }

    public void setAppearedCheck(Player appearedCheck) {
        this.appearedCheck = appearedCheck;
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
}
