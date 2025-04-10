package service;

import chess.*;
import service.AuthService;
import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.GameData;
import model.AuthData;

import java.util.Collection;
import java.util.UUID;
public class GameService {
    public final DataAccess dataAccess;
    private final AuthService authService;
    //private int nextGameID = 1;

    public GameService() {
        this.dataAccess = new MemoryDataAccess();
        this.authService = new AuthService();
    }

    public GameService(DataAccess dataAccess, AuthService authService){
        this.dataAccess = dataAccess;
        this.authService = authService;
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return dataAccess.listGames();
    }

    public GameData createGame(String authToken, String gameName) throws DataAccessException {
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        ChessGame chessGame = new ChessGame();
        int id = dataAccess.createGame(gameName);
        return new GameData(id, null, null, gameName, chessGame);
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Error: bad request");
        }

        String username = authData.username();

        if (playerColor == null){
            throw new DataAccessException("Error: bad request");
        }

        if (playerColor.equalsIgnoreCase("WHITE")) {
            if (gameData.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            gameData = new GameData(gameData.gameID(), username, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else if (playerColor.equalsIgnoreCase("BLACK")) {
            if (gameData.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), username, gameData.gameName(), gameData.game());
        } else {
            throw new DataAccessException("Error: bad request");
        }

        dataAccess.updateGame(gameData); // Update the game in storage
    }

    public GameData makeMove(String authToken, int gameID, ChessMove move) throws DataAccessException {
        // 1. Authenticate the user
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        String username = authData.username();

        // 2. Retrieve the GameData
        GameData gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Error: bad request - Game not found");
        }
        if (gameData.isGameOver()) { // Assuming you have a boolean flag or method to check game over
            throw new DataAccessException("Error: bad request - Game is over");
        }


        // 3. Validate the move
        // Assuming you have chess logic classes to validate the move
        ChessGame game = gameData.game();
        if (!isValidMove(game, move, username, gameID)) {
            throw new DataAccessException("Error: bad request - Invalid move");
        }


        // 4. Update the GameData
        try {
            game.makeMove(move); // Assuming ChessGame has a makeMove method
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        GameData updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(),
                gameData.blackUsername(), gameData.gameName(), game);

        // 5. Persist the updated GameData
        dataAccess.updateGame(updatedGameData);

        return updatedGameData;
    }

    private boolean isValidMove(ChessGame game, ChessMove move, String username, int gameID) {
        if (!isCorrectTurn(game, username, move, gameID)) {
            return false;
        }

        // 2. Validate the move using ChessGame's validMoves method
        ChessPosition startPosition = move.getStartPosition();
        Collection<ChessMove> validMoves = game.validMoves(startPosition);
        if (validMoves == null || !validMoves.contains(move)) {
            return false;
        }

        // If all checks pass, the move is valid
        return true;
    }

    private boolean isCorrectTurn(ChessGame game, String username, ChessMove move, int gameID) {
        ChessGame.TeamColor currentTurn = game.getTeamTurn();
        ChessPiece piece = game.getBoard().getPiece(move.getStartPosition()); // Assuming ChessMove move is accessible here

        if (piece == null) {
            return false; // No piece at the start position
        }

        ChessGame.TeamColor pieceColor = piece.getTeamColor();
        ChessGame.TeamColor playerColor = getPlayerColor(gameID, username);

        if (playerColor == null) {
            return false; // Could not determine player's color
        }
        if (playerColor != pieceColor) {
            return false; // Player is trying to move the opponent's piece
        }

        if (currentTurn != playerColor) {
            return false; // It's not the player's turn
        }

        return true;
    }

    private ChessGame.TeamColor getPlayerColor(int gameID, String username) {
        GameData gameData = null; // Assuming ChessGame has getGameId()
        try {
            gameData = dataAccess.getGame(gameID);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        if (gameData != null) {
            if (username.equals(gameData.whiteUsername())) {
                return ChessGame.TeamColor.WHITE;
            } else if (username.equals(gameData.blackUsername())) {
                return ChessGame.TeamColor.BLACK;
            }
        }
        return null; // Observer or invalid username
    }

    public void leaveGame(String authToken, int gameID) throws DataAccessException {
        // 1. Authenticate the user
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        String username = authData.username();

        // 2. Retrieve the GameData
        GameData gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Error: bad request - Game not found");
        }

        // 3. Update the GameData to remove the user
        String whiteUsername = gameData.whiteUsername();
        String blackUsername = gameData.blackUsername();
        GameData updatedGameData = gameData; // Start with the original data

        if (username.equals(whiteUsername)) {
            updatedGameData = new GameData(gameData.gameID(), null, blackUsername, gameData.gameName(), gameData.game());
        } else if (username.equals(blackUsername)) {
            updatedGameData = new GameData(gameData.gameID(), whiteUsername, null, gameData.gameName(), gameData.game());
        } else {
            // User is not a player, simply return the game
            return;
        }

        // 4. Persist the updated GameData
        dataAccess.updateGame(updatedGameData);
    }

    public void resignGame(String authToken, int gameID) throws DataAccessException {
        // 1. Authenticate the user
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // 2. Retrieve the GameData
        GameData gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Error: bad request - Game not found");
        }

        // 3. Update the GameData to mark the game as over
        // Assuming you have a method in GameData to set the game over
        // and a corresponding field (e.g., gameOver)
        // gameData.setGameOver(true);
        GameData updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(),
                gameData.blackUsername(), gameData.gameName(), gameData.game());

        // 4. Persist the updated GameData
        dataAccess.updateGame(updatedGameData);
    }
}
