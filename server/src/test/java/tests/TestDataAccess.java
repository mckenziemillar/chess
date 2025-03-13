package tests;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public class TestDataAccess implements DataAccess {
    private final MySqlDataAccess mySqlDataAccess;

    public TestDataAccess() throws DataAccessException {
        this.mySqlDataAccess = new MySqlDataAccess();
    }

    @Override
    public void clear() throws DataAccessException {
        mySqlDataAccess.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        mySqlDataAccess.createUser(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return mySqlDataAccess.getUser(username);
    }

    @Override
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        return mySqlDataAccess.createAuth(auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return mySqlDataAccess.getAuth(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        mySqlDataAccess.deleteAuth(authToken);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        mySqlDataAccess.updateGame(game);
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        return mySqlDataAccess.createGame(gameName);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return mySqlDataAccess.getGame(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return mySqlDataAccess.listGames();
    }
}