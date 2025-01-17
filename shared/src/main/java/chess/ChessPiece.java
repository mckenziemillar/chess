package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private PieceType pieceType;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.pieceType = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece.PieceType pieceType = board.getPiece(myPosition).getPieceType();
        List<ChessMove> moves = new ArrayList<ChessMove>();
        switch (pieceType) {
            case PAWN -> moves.addAll(new PawnMovesCalculator().pieceMoves(board, myPosition));
            case ROOK -> moves.addAll(new RookMovesCalculator().pieceMoves(board, myPosition));
            case KNIGHT -> moves.addAll(new KnightMovesCalculator().pieceMoves(board, myPosition));
            case BISHOP -> moves.addAll(new BishopMovesCalculator().pieceMoves(board, myPosition));
            case QUEEN -> moves.addAll(new QueenMovesCalculator().pieceMoves(board, myPosition));
            case KING -> moves.addAll(new KingMovesCalculator().pieceMoves(board, myPosition));
        }
        return moves;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ChessPiece piece = (ChessPiece) obj;

        return this.getPieceType() == piece.getPieceType() &&
                this.getTeamColor() == piece.getTeamColor();
    }

}
