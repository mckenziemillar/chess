package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private PieceType pieceType;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.pieceType = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece.PieceType pieceType = board.getPiece(myPosition).getPieceType();
        List<ChessMove> moves = new ArrayList<ChessMove>();
        switch (pieceType) {
            case PAWN -> moves.addAll(movePawn(board, myPosition)); // done
            case ROOK -> moves.addAll(moveRook(board, myPosition)); // done
            case KNIGHT -> moves.addAll(moveKnight(board, myPosition)); //todo
            case BISHOP -> moves.addAll(moveBishop(board, myPosition)); //done
            case QUEEN -> {
                moves.addAll(moveBishop(board, myPosition));
                moves.addAll(moveRook(board, myPosition));
            } //done
            case KING -> moves.addAll(moveKing(board, myPosition)); // todo: account for other pieces
        }
        return moves;
    }

    private Collection<ChessMove> moveKing(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<ChessMove>();

        for(int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){
                int newRow = myPosition.getRow() + i;
                int newCol = myPosition.getColumn() + j;
                ChessPosition newPos = new ChessPosition(newRow, newCol);
                if(!posIsOnBoard(newPos) || (board.getPiece(newPos) != null
                        && board.getPiece(newPos).getTeamColor() == board.getPiece(myPosition).getTeamColor())){
                    continue;
                } else {
                    moves.add(new ChessMove(myPosition, newPos, null));
                }
            }
        }
        return moves;
    }
    private Collection<ChessMove> moveRook(ChessBoard board, ChessPosition myPosition){
        List<ChessMove> moves = new ArrayList<ChessMove>();
        for(int j = 0; j < 4; j++){
            for(int i = 1; i < 8; i++){
                int newRow = myPosition.getRow();
                int newCol = myPosition.getColumn();
                if (j == 0) {
                    newRow += i;
                }
                if (j == 1) {
                    newCol -= i;
                }
                if (j == 2) {
                    newRow -= i;
                }
                if (j == 3) {
                    newCol += i;
                }
                ChessPosition newPos = new ChessPosition(newRow, newCol);
                if(!posIsOnBoard(newPos)){
                    continue;
                }
                ChessPiece otherPiece = board.getPiece(new ChessPosition(newRow, newCol));
                ChessPiece myPiece = board.getPiece(myPosition);
                if(otherPiece != null){
                    if (otherPiece.getTeamColor() != myPiece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    }
                    break;
                } else {
                    moves.add(new ChessMove(myPosition, newPos, null));
                }
            }

        }
        return moves;
    }

    private Collection<ChessMove> moveBishop(ChessBoard board, ChessPosition myPosition){
        List<ChessMove> moves = new ArrayList<ChessMove>();
        for(int j = 0; j < 4; j++){
            for(int i = 1; i < 8; i++){
                int newRow = myPosition.getRow();
                int newCol = myPosition.getColumn();
                if (j == 0) {
                    newRow += i;
                    newCol += i;
                } else if (j == 1) {
                    newRow -= i;
                    newCol -= i;
                } else if (j == 2) {
                    newRow += i;
                    newCol -= i;
                } else if (j == 3) {
                    newRow -= i;
                    newCol += i;
                }

                ChessPosition newPos = new ChessPosition(newRow, newCol);
                if(!posIsOnBoard(newPos)){
                    continue;
                }
                ChessPiece otherPiece = board.getPiece(new ChessPosition(newRow, newCol));
                ChessPiece myPiece = board.getPiece(myPosition);
                if(otherPiece != null){
                    if (otherPiece.getTeamColor() != myPiece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    }
                    break;
                } else {
                    moves.add(new ChessMove(myPosition, newPos, null));
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> moveKnight(ChessBoard board, ChessPosition myPosition){
        List<ChessMove> moves = new ArrayList<ChessMove>();
        for(int j = 0; j < 8; j++){
            int newRow = myPosition.getRow();
            int newCol = myPosition.getColumn();
            if (j == 0) {
                newRow += 2;
                newCol += 1;
            } else if (j == 1) {
                newRow += 2;
                newCol -= 1;
            } else if (j == 2) {
                newRow -= 2;
                newCol += 1;
            } else if (j == 3) {
                newRow -= 2;
                newCol -= 1;
            } else if (j == 4) {
                newRow += 1;
                newCol += 2;
            } else if (j == 5) {
                newRow += 1;
                newCol -= 2;
            } else if (j == 6) {
                newRow -= 1;
                newCol += 2;
            } else {
                newRow -= 1;
                newCol -= 2;
            }
            ChessPosition newPos = new ChessPosition(newRow, newCol);
            if(!posIsOnBoard(newPos)){
                continue;
            }
            ChessPiece otherPiece = board.getPiece(new ChessPosition(newRow, newCol));
            ChessPiece myPiece = board.getPiece(myPosition);
            if(otherPiece != null){
                if (otherPiece.getTeamColor() != myPiece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPos, null));
                }
                continue;
            } else {
                moves.add(new ChessMove(myPosition, newPos, null));
            }
        }
        return moves;

    }
    private Collection<ChessMove> movePawn(ChessBoard board, ChessPosition myPosition){
        List<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPiece.PieceType[] promotions = {PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT, PieceType.QUEEN};
        int colorInt = 1;
        if(board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK){
            colorInt = -1;
        }
        for(int i = 0; i < 2; i++){
            for(int j = -1; j < 2; j++){
                int newRow = myPosition.getRow() + i*colorInt;
                int newCol = myPosition.getColumn() + j;
                ChessPosition newPos = new ChessPosition(newRow, newCol);
                if(!posIsOnBoard(newPos)){
                    continue;
                }
                if(j != 0){
                    if(board.getPiece(newPos) == null) {
                        continue;
                    }else if(board.getPiece(newPos).getTeamColor() == board.getPiece(myPosition).getTeamColor()){
                        continue;
                    }
                } else {
                    if(board.getPiece(newPos) != null){
                        continue;
                    }
                }
                if(posIsOnBoard(newPos)){
                    if(promotionNeeded(board, myPosition, newPos)){
                        for(var promo: promotions) {moves.add(new ChessMove(myPosition, newPos, promo));}
                    } else {
                    moves.add(new ChessMove(myPosition, newPos, null));
                    }
                }
            }
        }
        if(myPosition.getRow() == 2){
            int newRow = myPosition.getRow() + 2;
            ChessPosition newPos = new ChessPosition(newRow, myPosition.getColumn());
            if(board.getPiece(newPos) != null) {
                moves.add(new ChessMove(myPosition, newPos, null));
            }
        }
        return moves;
    }



    private boolean posIsOnBoard(ChessPosition pos){
        int row = pos.getRow();
        int col = pos.getColumn();
        return (row >= 1 && row <= 8 && col >=1 && col <= 8);
    }

    private boolean promotionNeeded(ChessBoard board, ChessPosition myPosition, ChessPosition newPos){
        return((board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE && newPos.getRow() == 1)
                || (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK && newPos.getRow() == 8));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ChessPiece piece = (ChessPiece) obj;

        return this.getPieceType() == piece.getPieceType() &&
                this.getTeamColor() == piece.getTeamColor();
    }

}
