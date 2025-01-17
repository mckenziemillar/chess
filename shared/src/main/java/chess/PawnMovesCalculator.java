package chess;

import chess.*;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class PawnMovesCalculator implements MovesCalculator{
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition){
        List<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPiece.PieceType[] promotions = {ChessPiece.PieceType.ROOK, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.QUEEN};
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
        ChessGame.TeamColor myColor = board.getPiece(myPosition).getTeamColor();
        if((myColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2)||
                (myColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7)){
            int newRow = myPosition.getRow() + colorInt*2;
            ChessPosition newPos = new ChessPosition(newRow, myPosition.getColumn());
            if(board.getPiece(newPos) == null && (board.getPiece(new ChessPosition(newRow - (colorInt), myPosition.getColumn())) == null)) {
                moves.add(new ChessMove(myPosition, newPos, null));
            }
        }
        return moves;
    }

    private boolean promotionNeeded(ChessBoard board, ChessPosition myPosition, ChessPosition newPos){
        return((board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE && newPos.getRow() == 8)
                || (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK && newPos.getRow() == 1));
    }

    private boolean posIsOnBoard(ChessPosition pos){
        int row = pos.getRow();
        int col = pos.getColumn();
        return (row >= 1 && row <= 8 && col >=1 && col <= 8);
    }

}
