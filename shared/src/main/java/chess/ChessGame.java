package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;
    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() { return teamTurn; }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) { teamTurn = team; }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        ArrayList<ChessMove> validMoves = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPosition);
        if(piece == null){ return null; }
        moves.addAll(piece.pieceMoves(board, startPosition));
        for(ChessMove move : moves){
            if(!putsTeamInCheck(board, move, piece.getTeamColor())){
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    private static boolean putsTeamInCheck(ChessBoard board, ChessMove move, TeamColor team){
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessPiece piece = board.getPiece(startPos);
        ChessBoard tempBoard = new ChessBoard(board);
        tempBoard.removePiece(startPos);
        tempBoard.addPiece(endPos, piece);
        return isInCheck(team, tempBoard);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessPiece piece = board.getPiece(startPos);
        if (piece == null || piece.getTeamColor() != teamTurn || (!validMoves(startPos).contains(move))) {
            throw new InvalidMoveException("Invalid move. ");
        }
        board.removePiece(startPos);
        ChessPiece newPiece;
        if(move.getPromotionPiece() != null){
            newPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        } else {
            newPiece = piece;
        }
        board.addPiece(endPos, newPiece);
        if(teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, this.board);
    }

    private static boolean isInCheck(TeamColor teamColor, ChessBoard tempBoard) {
        ChessPosition kingPos = findKingPos(teamColor, tempBoard);
        Collection<ChessMove> possibleMoves;
        for(int r = 1; r <= 8; r++){
            for(int c = 1; c <= 8; c++){
                ChessPosition pos = new ChessPosition(r, c);
                if (isPieceAttackingKing(tempBoard, pos, teamColor, kingPos)) {
                    return true;
                }
            }
        }
        return false;
    }
    private static boolean isPieceAttackingKing(ChessBoard board, ChessPosition piecePos, TeamColor kingColor, ChessPosition kingPos) {
        ChessPiece piece = board.getPiece(piecePos);
        if (piece != null && piece.getTeamColor() != kingColor) {
            Collection<ChessMove> possibleMoves = piece.pieceMoves(board, piecePos);
            for (ChessMove move : possibleMoves) {
                if (move.getEndPosition().equals(kingPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ChessPosition findKingPos(ChessGame.TeamColor teamColor, ChessBoard board) {
        for(int r = 1; r <= 8; r++){
            for(int c = 1; c <= 8; c++){
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && (piece.getPieceType() == ChessPiece.PieceType.KING) && (piece.getTeamColor() == teamColor)){
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isTeamWithoutMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && isTeamWithoutMoves(teamColor);
    }

    private boolean isTeamWithoutMoves(TeamColor teamColor) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                if (hasValidMove(board, pos, teamColor)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasValidMove(ChessBoard board, ChessPosition position, TeamColor teamColor) {
        ChessPiece piece = board.getPiece(position);
        if (piece != null && piece.getTeamColor() == teamColor) {
            Collection<ChessMove> possibleMoves = piece.pieceMoves(board, position);
            for (ChessMove move : possibleMoves) {
                if (!putsTeamInCheck(board, move, teamColor)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
