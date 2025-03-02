package chess;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class MoveCalculatorHelper {
    public static Collection<ChessMove> moveStraight(ChessBoard board, ChessPosition myPosition){
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

    public static Collection<ChessMove> moveDiagonal(ChessBoard board, ChessPosition myPosition){
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
                } else {
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

    public static boolean posIsOnBoard(ChessPosition pos){
        int row = pos.getRow();
        int col = pos.getColumn();
        return (row >= 1 && row <= 8 && col >=1 && col <= 8);
    }


}
