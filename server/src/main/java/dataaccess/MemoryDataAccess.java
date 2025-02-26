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
        System.out.println("User created: " + user.username());
        System.out.println("Current users map: " + users);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        System.out.println("Attempting to retrieve user: " + username);
        System.out.println("Current users map: " + users);
        UserData userData = users.get(username);
        if(userData == null){
            System.out.println("User not found");
        } else {
            System.out.println("User found: " + userData.username());
        }
        return userData;
    }


    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authTokens.put(auth.authToken(), auth);
        System.out.println("Auth token created: " + auth.authToken());
        System.out.println("Current authTokens map: " + authTokens);
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
        System.out.println("Attempting to retrieve auth token: " + authToken);
        System.out.println("Current authTokens map: " + authTokens); // Print the entire map
        AuthData auth = authTokens.get(authToken);
        if (auth == null) {
            System.out.println("Auth token not found.");
        } else {
            System.out.println("Auth token found: " + auth.authToken());
        }
        return auth;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);
    }
}
