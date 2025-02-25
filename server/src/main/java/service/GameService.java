package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.GameData;
import chess.ChessGame;

import java.util.UUID;
public class GameService {
    private final DataAccess dataAccess;
    private int nextGameID = 1;

    public GameService() {
        this.dataAccess = new MemoryDataAccess();
    }

    public GameService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public GameData createGame(String gameName) throws DataAccessException {
        int gameID = nextGameID++;
        ChessGame chessGame = new ChessGame();
        GameData gameData = new GameData(gameID, null, null, gameName, chessGame);
        dataAccess.createGame(gameData);
        return gameData;
    }
}
