package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;
    private TestDataAccess testDataAccess;

    @BeforeEach
    void setUp() {
        testDataAccess = new TestDataAccess();
        authService = new AuthService(testDataAccess);
    }

    @Test
    void login_invalidUsername_throwsDataAccessException() {
        String username = "nonexistentUser";
        String password = "testPassword";

        assertThrows(DataAccessException.class, () -> authService.login(username, password));
        assertTrue(testDataAccess.auths.isEmpty());
    }

    @Test
    void getAuth_validAuthToken_returnsAuthData() throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, "testUser");
        testDataAccess.createAuth(authData);

        AuthData result = authService.getAuth(authToken);

        assertEquals(authData, result);
    }

    @Test
    void login_validCredentials_returnsAuthData() throws DataAccessException {
        String username = "testUser";
        String password = "testPassword";
        UserData user = new UserData(username, password, "test@example.com");
        testDataAccess.createUser(user); // Use createUser method
        AuthData result = authService.login(username, password);

        assertNotNull(result);
        assertEquals(username, result.username());
        assertNotNull(testDataAccess.getAuth(result.authToken()));
    }

    @Test
    void getAuth_invalidAuthToken_returnsNull() throws DataAccessException {
        String authToken = "invalidAuthToken";

        AuthData result = authService.getAuth(authToken);

        assertNull(result);
    }

    @Test
    void logout_validAuthToken_deletesAuthData() throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, "testUser");
        testDataAccess.createAuth(authData);

        authService.logout(authToken);

        assertNull(testDataAccess.getAuth(authToken));
    }

    @Test
    void logout_invalidAuthToken_throwsDataAccessException() {
        String authToken = "invalidAuthToken";

        assertThrows(DataAccessException.class, () -> authService.logout(authToken));
    }

    // Test Double implementation
    private static class TestDataAccess implements DataAccess {
        Map<String, UserData> users = new HashMap<>();
        Map<String, AuthData> auths = new HashMap<>();
        Map<Integer, GameData> games = new HashMap<>();

        @Override
        public void clear() throws DataAccessException {
            users.clear();
            auths.clear();
            games.clear();
        }

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
            auths.put(auth.authToken(), auth);
        }

        @Override
        public AuthData getAuth(String authToken) throws DataAccessException {
            return auths.get(authToken);
        }

        @Override
        public void deleteAuth(String authToken) throws DataAccessException {
            auths.remove(authToken);
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
            return games.values();
        }
    }
}