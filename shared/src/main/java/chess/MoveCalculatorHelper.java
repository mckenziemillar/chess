package chess;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class MoveCalculatorHelper {
    public static Collection<ChessMove> moveStraight(ChessBoard board, ChessPosition myPosition) {
        return generateMoves(board, myPosition, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
    }

    public static Collection<ChessMove> moveDiagonal(ChessBoard board, ChessPosition myPosition) {
        return generateMoves(board, myPosition, new int[][]{{1, 1}, {-1, -1}, {1, -1}, {-1, 1}});
    }

    private static Collection<ChessMove> generateMoves(ChessBoard board, ChessPosition myPosition, int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);

        for (int[] direction : directions) {
            int dRow = direction[0], dCol = direction[1];
            for (int i = 1; i < 8; i++) {
                int newRow = myPosition.getRow() + i * dRow;
                int newCol = myPosition.getColumn() + i * dCol;
                ChessPosition newPos = new ChessPosition(newRow, newCol);

                if (!posIsOnBoard(newPos)) {
                    break;
                }

                ChessPiece otherPiece = board.getPiece(newPos);
                if (otherPiece != null) {
                    if (otherPiece.getTeamColor() != myPiece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    }
                    break;
                }
                moves.add(new ChessMove(myPosition, newPos, null));
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
