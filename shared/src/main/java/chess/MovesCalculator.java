package chess;
import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public interface MovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}
