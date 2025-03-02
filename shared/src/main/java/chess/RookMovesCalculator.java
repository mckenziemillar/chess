package chess;

import java.util.Collection;
import java.util.ArrayList;

public class RookMovesCalculator implements MovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> moves;
        moves = MoveCalculatorHelper.moveStraight(board, myPosition);
        return moves;
    }

}
