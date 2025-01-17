package chess;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class KnightMovesCalculator implements MovesCalculator{
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition){
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


    private boolean posIsOnBoard(ChessPosition pos){
        int row = pos.getRow();
        int col = pos.getColumn();
        return (row >= 1 && row <= 8 && col >=1 && col <= 8);
    }

}
