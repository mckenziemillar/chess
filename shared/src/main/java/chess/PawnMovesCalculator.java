package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator implements MovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int colorInt = (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        ChessGame.TeamColor color = board.getPiece(myPosition).getTeamColor();

        addForwardMoves(board, myPosition, moves, row, col, colorInt, color);
        addCaptureMoves(board, myPosition, moves, row, col, colorInt, color);

        return moves;
    }

    private void addForwardMoves(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves, int row, int col, int colorInt, ChessGame.TeamColor color) {
        if ((row == 2 && color == ChessGame.TeamColor.WHITE) || (row == 7 && color == ChessGame.TeamColor.BLACK)) {
            addInitialDoubleMove(board, myPosition, moves, row, col, colorInt);
        } else {
            addSingleForwardMove(board, myPosition, moves, row, col, colorInt, color);
        }
    }

    private void addCaptureMoves(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves, int row, int col, int colorInt, ChessGame.TeamColor color) {
        addCaptureMove(board, myPosition, moves, row, col + 1, colorInt, color);
        addCaptureMove(board, myPosition, moves, row, col - 1, colorInt, color);
    }

    private void addInitialDoubleMove(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves, int row, int col, int colorInt) {
        ChessPosition pos1 = new ChessPosition(row + colorInt, col);
        ChessPosition pos2 = new ChessPosition(row + (2 * colorInt), col);
        if (board.getPiece(pos1) == null) {
            moves.add(new ChessMove(myPosition, pos1, null));
            if (board.getPiece(pos2) == null) {
                moves.add(new ChessMove(myPosition, pos2, null));
            }
        }
    }

    private void addSingleForwardMove(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves, int row, int col, int colorInt, ChessGame.TeamColor color) {
        ChessPosition pos = new ChessPosition(row + colorInt, col);
        if (MoveCalculatorHelper.posIsOnBoard(pos) && board.getPiece(pos) == null) {
            if (needsPromotion(color, pos)) {
                addPromotionMoves(myPosition, pos, moves);
            } else {
                moves.add(new ChessMove(myPosition, pos, null));
            }
        }
    }

    private void addCaptureMove(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves, int row, int col, int colorInt, ChessGame.TeamColor color) {
        ChessPosition pos = new ChessPosition(row + colorInt, col);
        if (MoveCalculatorHelper.posIsOnBoard(pos) && board.getPiece(pos) != null &&
                board.getPiece(pos).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
            if (needsPromotion(color, pos)) {
                addPromotionMoves(myPosition, pos, moves);
            } else {
                moves.add(new ChessMove(myPosition, pos, null));
            }
        }
    }

    private void addPromotionMoves(ChessPosition myPosition, ChessPosition pos, ArrayList<ChessMove> moves) {
        for (ChessPiece.PieceType type : ChessPiece.PieceType.values()) {
            if (type != ChessPiece.PieceType.PAWN && type != ChessPiece.PieceType.KING) {
                moves.add(new ChessMove(myPosition, pos, type));
            }
        }
    }

    private boolean needsPromotion(ChessGame.TeamColor color, ChessPosition pos) {
        return (color == ChessGame.TeamColor.WHITE && pos.getRow() == 8) || (color == ChessGame.TeamColor.BLACK && pos.getRow() == 1);
    }
}