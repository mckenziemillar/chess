package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTests {

    private UserService userService;
    private TestDataAccess testDataAccess;

    @BeforeEach
    void setUp() {
        testDataAccess = new TestDataAccess();
        userService = new UserService(testDataAccess);
    }

    @Test
    void register_validUserData_returnsAuthData() throws DataAccessException {
        UserData userData = new UserData("testUser", "testPassword", "test@example.com");
        AuthData result = userService.register(userData);

        assertNotNull(result);
        assertEquals(userData.username(), result.username());
        assertNotNull(testDataAccess.getAuth(result.authToken()));
        assertNotNull(testDataAccess.getUser(userData.username()));
    }

    @Test
    void register_usernameAlreadyTaken_throwsDataAccessException() throws DataAccessException {
        UserData existingUser = new UserData("existingUser", "password", "existing@example.com");
        testDataAccess.createUser(existingUser);

        UserData newUser = new UserData("existingUser", "newPassword", "new@example.com");
        assertThrows(DataAccessException.class, () -> userService.register(newUser));
        assertNull(testDataAccess.getAuth(newUser.username()));
    }

    // Test Double implementation
    private static class TestDataAccess implements DataAccess {
        Map<String, UserData> users = new HashMap<>();
        Map<String, AuthData> auths = new HashMap<>();

        @Override
        public void clear() throws DataAccessException {
            users.clear();
            auths.clear();
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
        public void createGame(model.GameData game) throws DataAccessException {

        }

        @Override
        public model.GameData getGame(int gameID) throws DataAccessException {
            return null;
        }

        @Override
        public java.util.Collection<model.GameData> listGames() throws DataAccessException {
            return null;
        }
    }
}