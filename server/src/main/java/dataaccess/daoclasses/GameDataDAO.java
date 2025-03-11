package dataaccess.daoclasses;
import dataaccess.DataAccessException;
import model.GameData;

import java.util.List;

public interface GameDataDAO {
    int createGame(String gameName) throws DataAccessException; // Create a new game, return the gameID

    void updateGame(GameData game) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException; // Read game by ID
    List<GameData> listGames() throws DataAccessException; // Read all games
}
