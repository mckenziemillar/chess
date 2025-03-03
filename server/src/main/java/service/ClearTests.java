package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClearTests {

    private ClearService clearService;
    private TestDataAccess testDataAccess;

    @BeforeEach
    void setUp() {
        testDataAccess = new TestDataAccess();
        clearService = new ClearService(testDataAccess);
    }

    @Test
    void clear_dataCleared() throws DataAccessException {
        // Add some data to the test data access
        testDataAccess.users.put("user1", new model.UserData("user1", "pass1", "email1"));
        testDataAccess.auths.put("auth1", new model.AuthData("auth1", "user1"));
        testDataAccess.games.put(1, new model.GameData(1, "user1", null, "game1", new chess.ChessGame()));

        clearService.clear();

        assertTrue(testDataAccess.users.isEmpty());
        assertTrue(testDataAccess.auths.isEmpty());
        assertTrue(testDataAccess.games.isEmpty());
    }

    // Test Double implementation
    private static class TestDataAccess implements DataAccess {
        Map<String, model.UserData> users = new HashMap<>();
        Map<String, model.AuthData> auths = new HashMap<>();
        Map<Integer, model.GameData> games = new HashMap<>();

        @Override
        public void clear() throws DataAccessException {
            users.clear();
            auths.clear();
            games.clear();
        }

        @Override
        public void createUser(model.UserData user) throws DataAccessException {
            users.put(user.username(), user);
        }

        @Override
        public model.UserData getUser(String username) throws DataAccessException {
            return users.get(username);
        }

        @Override
        public void createAuth(model.AuthData auth) throws DataAccessException {
            auths.put(auth.authToken(), auth);
        }

        @Override
        public model.AuthData getAuth(String authToken) throws DataAccessException {
            return auths.get(authToken);
        }

        @Override
        public void deleteAuth(String authToken) throws DataAccessException {
            auths.remove(authToken);
        }

        @Override
        public void createGame(model.GameData game) throws DataAccessException {
            games.put(game.gameID(), game);
        }

        @Override
        public model.GameData getGame(int gameID) throws DataAccessException {
            return games.get(gameID);
        }

        @Override
        public java.util.Collection<model.GameData> listGames() throws DataAccessException {
            return games.values();
        }
    }
}