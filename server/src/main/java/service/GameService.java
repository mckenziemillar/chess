package service;

import service.AuthService;
import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.GameData;
import model.AuthData;
import chess.ChessGame;

import java.util.Collection;
import java.util.UUID;
public class GameService {
    private final DataAccess dataAccess;
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
}
