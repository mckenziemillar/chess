package chess;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class KingMovesCalculator implements MovesCalculator{
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition){
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

    private boolean posIsOnBoard(ChessPosition pos){
        int row = pos.getRow();
        int col = pos.getColumn();
        return (row >= 1 && row <= 8 && col >=1 && col <= 8);
    }

}
