package dataaccess;

import dataaccess.daoclasses.GameDataDAO;
import model.GameData;

import chess.ChessGame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDataDAO implements GameDataDAO {

    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public int createGame(String gameName) throws DataAccessException {
        ChessGame chessGame = new ChessGame();

        // Create a new GameData object with the provided gameName and auto-generated gameID
        GameData gameWithID = new GameData(nextGameID++, null, null, gameName, chessGame);

        // Store the game in the map
        games.put(gameWithID.gameID(), gameWithID);

        // Return the generated gameID
        return gameWithID.gameID();

    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (games.containsKey(game.gameID())) {
            games.put(game.gameID(), game);
        } else {
            throw new DataAccessException("Error: game not found");
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }


    public void clear() {
        games.clear();
        nextGameID = 1;
    }
}