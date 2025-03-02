package chess;
import java.util.Collection;
import java.util.ArrayList;

public class BishopMovesCalculator implements MovesCalculator{
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves;
        moves = MoveCalculatorHelper.moveDiagonal(board, myPosition);
        return moves;
    }
}
