package dataaccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import model.UserData;
import model.AuthData;
import model.GameData;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;


    @Override
    public void createUser(UserData user) throws DataAccessException {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }


    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authTokens.put(auth.authToken(), auth);
    }
    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
        nextGameID = 1;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        games.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);
    }
}
