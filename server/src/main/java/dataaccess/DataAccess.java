package dataaccess;
import model.AuthData;
import model.UserData;
import model.GameData;

import java.util.Collection;

public interface DataAccess {
    void clear() throws DataAccessException;
    public void createUser(UserData user) throws DataAccessException;
    public UserData getUser(String username) throws DataAccessException;
    public AuthData createAuth(AuthData auth) throws DataAccessException;
    public AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    int createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;

    boolean verifyUser(String username, String password);
}

