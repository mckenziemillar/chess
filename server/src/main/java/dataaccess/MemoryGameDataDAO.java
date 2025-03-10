package dataaccess;

import dataaccess.DataAccessException;
import dataaccess.daoclasses.GameDataDAO;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDataDAO implements GameDataDAO {

    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public int createGame(GameData game) throws DataAccessException {
        if (games.containsKey(game.gameID())) {
            games.put(game.gameID(), game); //Update the game.
            return game.gameID();
        }


        //int gameID = nextGameID++;
        GameData gameWithID = new GameData(nextGameID++, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(gameWithID.gameID(), gameWithID);
        return gameWithID.gameID();
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