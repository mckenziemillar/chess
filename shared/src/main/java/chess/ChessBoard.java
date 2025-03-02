package chess;
import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    public ChessPiece[][] board;

    public ChessBoard() {
        board = new ChessPiece[8][8];

    }

    public ChessBoard(ChessBoard copy){
        board = new ChessPiece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j=0; j < 8; j++) {
                this.board[i][j] = copy.getBoard()[i][j];
            }
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        this.board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    public void removePiece(ChessPosition position) {
        this.board[position.getRow() - 1][position.getColumn() - 1] = null;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return this.board[position.getRow() - 1][position.getColumn() - 1];
    }

    public ChessPiece[][] getBoard() {
        return this.board;
    }


    @Override
    public String toString(){
        StringBuilder boardStr = new StringBuilder();
        for (int i = 7; i >= 0; i--) { // Iterate backwards
            ChessPiece[] row = board[i];
            for (ChessPiece piece : row) {
                if (piece == null) {
                    boardStr.append("| ");
                } else {
                    boardStr.append("|").append(piece);
                }
            }
            boardStr.append("|\n");
        }
        return boardStr.toString();
    }


    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;
        ChessGame.TeamColor black = ChessGame.TeamColor.BLACK;
        for(int i = 0; i < 2; i++){
            ChessGame.TeamColor color;
            if(i == 0){ color = black; }
            else {color = white;}
            setPawns(color);
            setRooks(color);
            setKnights(color);
            setBishops(color);
            setQueen(color);
            setKing(color);

        }

    }


    private void setPawns(ChessGame.TeamColor color) {
        int offset = 2;
        if (color == ChessGame.TeamColor.BLACK){
            offset = 7;
        }
        for(int i = 1; i <= 8; i++){
            ChessPosition pos = new ChessPosition(offset, i);
            addPiece(pos, new ChessPiece(color, ChessPiece.PieceType.PAWN));
        }
    }

    private void setRooks(ChessGame.TeamColor color){
        int offset = 1;
        if (color == ChessGame.TeamColor.BLACK){
            offset = 8;
        }
        ChessPosition pos1 = new ChessPosition(offset, 1);
        ChessPosition pos2 = new ChessPosition(offset, 8);
        addPiece(pos1, new ChessPiece(color, ChessPiece.PieceType.ROOK));
        addPiece(pos2, new ChessPiece(color, ChessPiece.PieceType.ROOK));
    }
    private void setKnights(ChessGame.TeamColor color){
        int offset = 1;
        if (color == ChessGame.TeamColor.BLACK){
            offset = 8;
        }
        ChessPosition pos1 = new ChessPosition(offset, 2);
        ChessPosition pos2 = new ChessPosition(offset, 7);
        addPiece(pos1, new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
        addPiece(pos2, new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
    }

    private void setBishops(ChessGame.TeamColor color){
        int offset = 1;
        if (color == ChessGame.TeamColor.BLACK){
            offset = 8;
        }
        ChessPosition pos1 = new ChessPosition(offset, 3);
        ChessPosition pos2 = new ChessPosition(offset, 6);
        addPiece(pos1, new ChessPiece(color, ChessPiece.PieceType.BISHOP));
        addPiece(pos2, new ChessPiece(color, ChessPiece.PieceType.BISHOP));
    }

    private void setQueen(ChessGame.TeamColor color){
        int offset = 1;
        if (color == ChessGame.TeamColor.BLACK){
            offset = 8;
        }
        ChessPosition pos = new ChessPosition(offset, 4);
        addPiece(pos, new ChessPiece(color, ChessPiece.PieceType.QUEEN));
    }

    private void setKing(ChessGame.TeamColor color){
        int offset = 1;
        if (color == ChessGame.TeamColor.BLACK){
            offset = 8;
        }
        ChessPosition pos = new ChessPosition(offset, 5);
        addPiece(pos, new ChessPiece(color, ChessPiece.PieceType.KING));
    }

    public void print(){
        System.out.println("Board: ");
        for(int i = 7; i >= 0; i--){
            System.out.println("\n");
            for(int j = 0; j < 8; j++){
                if(board[i][j] == null){
                    System.out.print(" empty ");
                } else {
                    System.out.print(" " + board[i][j].getPieceType() + "(" + board[i][j].getTeamColor() + ") ");
                }
            }
        }
        System.out.print("\n");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (getClass() != obj.getClass()){
            return false;
        }

        ChessBoard that = (ChessBoard) obj;


        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece thisPiece = this.board[i][j];
                ChessPiece thatPiece = that.board[i][j];

                // Check for nulls and equality
                if (thisPiece == null && thatPiece != null || thisPiece != null && thatPiece == null) {
                    return false;
                }
                if (thisPiece != null && !thisPiece.equals(thatPiece)) {
                    return false;
                }
            }
        }

        return true;
    }


}
