package dataaccess;

import java.util.Collection;

import dataaccess.daoclasses.AuthDataDAO;
import dataaccess.daoclasses.GameDataDAO;
import dataaccess.daoclasses.UserDataDAO;

import model.UserData;
import model.AuthData;
import model.GameData;

public class MemoryDataAccess implements DataAccess{
    private final UserDataDAO userDataDAO = new MemoryUserDataDAO();
    private final AuthDataDAO authDataDAO = new MemoryAuthDataDAO();
    private final GameDataDAO gameDataDAO = new MemoryGameDataDAO();


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
    public void clear() {
        MemoryAuthDataDAO tempAuth = (MemoryAuthDataDAO) authDataDAO;
        tempAuth.clear();
        MemoryGameDataDAO tempGame = (MemoryGameDataDAO) gameDataDAO;
        tempGame.clear();
        MemoryUserDataDAO tempUser = (MemoryUserDataDAO) userDataDAO;
        tempUser.clear();
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        return gameDataDAO.createGame(gameName);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        gameDataDAO.updateGame(game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameDataDAO.getGame(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return gameDataDAO.listGames();
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDataDAO.getAuth(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authDataDAO.deleteAuth(authToken);
    }

}
