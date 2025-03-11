package tests;

import dataaccess.*;
import dataaccess.daoclasses.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public class TestDataAccess implements DataAccess {
    private final UserDataDAO userDataDAO = new MemoryUserDataDAO();
    private final AuthDataDAO authDataDAO = new MemoryAuthDataDAO();
    private final GameDataDAO gameDataDAO = new MemoryGameDataDAO();

    @Override
    public void clear() throws DataAccessException {
        ((MemoryUserDataDAO) userDataDAO).clear();
        ((MemoryAuthDataDAO) authDataDAO).clear();
        ((MemoryGameDataDAO) gameDataDAO).clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        userDataDAO.createUser(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return userDataDAO.getUser(username);
    }

    @Override
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        return authDataDAO.createAuth(auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDataDAO.getAuth(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authDataDAO.deleteAuth(authToken);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        gameDataDAO.updateGame(game);
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        return gameDataDAO.createGame(gameName);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameDataDAO.getGame(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return gameDataDAO.listGames();
    }
}