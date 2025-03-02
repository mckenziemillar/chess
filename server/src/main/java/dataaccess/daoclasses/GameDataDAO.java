package dataaccess.daoclasses;
import dataaccess.DataAccessException;
import model.GameData;

import java.util.List;

public interface GameDataDAO {
    int createGame(GameData game) throws DataAccessException; // Create a new game, return the gameID
    GameData getGame(int gameID) throws DataAccessException; // Read game by ID
    List<GameData> listGames() throws DataAccessException; // Read all games
    void updateGame(GameData game) throws DataAccessException; // Update game details (e.g., add players, update game state)
    void deleteGame(int gameID) throws DataAccessException; // Delete a game
    void clearGames() throws DataAccessException; //Delete all games (For testing)
}
