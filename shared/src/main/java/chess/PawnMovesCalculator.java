package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator implements MovesCalculator{
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition){
        ArrayList<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int colorInt = 0;
        ChessGame.TeamColor color = board.getPiece(myPosition).getTeamColor();
        switch(color){
            case BLACK -> colorInt = -1;
            case WHITE -> colorInt = 1;
        }
        if(row == 2 && color == ChessGame.TeamColor.WHITE
                || row == 7 && color == ChessGame.TeamColor.BLACK){
            ChessPosition pos1 = new ChessPosition(row + (colorInt), col);
            ChessPosition pos2 = new ChessPosition(row + (2*colorInt), col);
            if(board.getPiece(pos1) == null){
                moves.add(new ChessMove(myPosition, pos1, null));
                if(board.getPiece(pos2) == null){
                    moves.add(new ChessMove(myPosition, pos2, null));
                }
            }
        } else {
            ChessPosition pos = new ChessPosition(row + (colorInt), col);
            if(MoveCalculatorHelper.posIsOnBoard(pos) && board.getPiece(pos) == null){
                if(needsPromotion(color, pos)){
                    for(ChessPiece.PieceType type:ChessPiece.PieceType.values()){
                        if(type == ChessPiece.PieceType.PAWN || type == ChessPiece.PieceType.KING){
                            continue;
                        }
                        moves.add(new ChessMove(myPosition, pos, type));
                    }
                } else { moves.add(new ChessMove(myPosition, pos, null)); }
            }
        }
        ChessPosition posA = new ChessPosition(row + (colorInt), col + 1);
        ChessPosition posB = new ChessPosition(row + (colorInt), col - 1);
        if(MoveCalculatorHelper.posIsOnBoard(posA) && board.getPiece(posA) != null
                && board.getPiece(posA).getTeamColor() != board.getPiece(myPosition).getTeamColor()){
            if(needsPromotion(color, posA)){
                for(ChessPiece.PieceType type:ChessPiece.PieceType.values()){
                    if(type == ChessPiece.PieceType.PAWN || type == ChessPiece.PieceType.KING){
                        continue;
                    }
                    moves.add(new ChessMove(myPosition, posA, type));
                }
            } else { moves.add(new ChessMove(myPosition, posA, null)); }

        }
        if(MoveCalculatorHelper.posIsOnBoard(posB) && board.getPiece(posB) != null
                && board.getPiece(posB).getTeamColor() != board.getPiece(myPosition).getTeamColor()){
            if(needsPromotion(color, posB)){
                for(ChessPiece.PieceType type:ChessPiece.PieceType.values()){
                    if(type == ChessPiece.PieceType.PAWN || type == ChessPiece.PieceType.KING){
                        continue;
                    }
                    moves.add(new ChessMove(myPosition, posB, type));
                }
            } else { moves.add(new ChessMove(myPosition, posB, null)); }
        }
        return moves;
    }

    private boolean needsPromotion(ChessGame.TeamColor color, ChessPosition pos){
        return ((color == ChessGame.TeamColor.WHITE && pos.getRow() == 8) || (color == ChessGame.TeamColor.BLACK && pos.getRow() == 1));
    }
}
