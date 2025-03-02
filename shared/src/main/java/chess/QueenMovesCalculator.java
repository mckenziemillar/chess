package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class QueenMovesCalculator implements MovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<ChessMove>();
        moves.addAll(MoveCalculatorHelper.moveStraight(board, myPosition));
        moves.addAll(MoveCalculatorHelper.moveDiagonal(board, myPosition));
        return moves;
    }
}