package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class DataAccessTests {
    private MySqlDataAccess dataAccess;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();
    }

    @Test
    void createUser_positive() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@example.com");
        dataAccess.createUser(user);
        UserData retrievedUser = dataAccess.getUser("testUser");
        assertNotNull(retrievedUser);
        assertEquals(user.username(), retrievedUser.username());
        assertEquals(user.email(), retrievedUser.email());
        // Do not check password directly, as it's hashed.
    }

    @Test
    void createUser_negative_duplicateUsername() {
        UserData user1 = new UserData("duplicateUser", "password", "test1@example.com");
        UserData user2 = new UserData("duplicateUser", "password", "test2@example.com");
        assertDoesNotThrow(() -> dataAccess.createUser(user1));
        assertThrows(DataAccessException.class, () -> dataAccess.createUser(user2));
    }

    // getUser Tests
    @Test
    void getUser_positive() throws DataAccessException {
        UserData user = new UserData("existingUser", "password", "exist@example.com");
        dataAccess.createUser(user);
        UserData retrievedUser = dataAccess.getUser("existingUser");
        assertNotNull(retrievedUser);
        assertEquals(user.username(), retrievedUser.username());
    }

    @Test
    void getUser_negative_userNotFound() throws DataAccessException {
        UserData retrievedUser = dataAccess.getUser("nonExistentUser");
        assertNull(retrievedUser);
    }

    // createAuth Tests
    @Test
    void createAuth_positive() throws DataAccessException {

    }

    @Test
    void createAuth_negative_userNotFound() {

    }

    // getAuth Tests
    @Test
    void getAuth_positive() throws DataAccessException {

    }

    @Test
    void getAuth_negative_authNotFound() throws DataAccessException {

    }

    // deleteAuth Tests
    @Test
    void deleteAuth_positive() throws DataAccessException {

    }

    @Test
    void deleteAuth_negative_authNotFound() throws DataAccessException {

    }

    // createGame Tests
    @Test
    void createGame_positive() throws DataAccessException {

    }

    @Test
    void createGame_negative_userNotFound() {

    }

    // getGame Tests
    @Test
    void getGame_positive() throws DataAccessException {

    }

    @Test
    void getGame_negative_gameNotFound() throws DataAccessException {

    }

    // listGames Tests
    @Test
    void listGames_positive() throws DataAccessException {

    }

    // clear Test
    @Test
    void clear_positive() throws DataAccessException {
        ChessGame chessGame1 = new ChessGame();
        GameData game1 = new GameData(1, "whiteUser1", "blackUser1", "game1", chessGame1);
        dataAccess.createUser(new UserData("whiteUser1", "pass", "white1@test.com"));
        dataAccess.createUser(new UserData("blackUser1", "pass", "black1@test.com"));
        dataAccess.createGame(game1);
        dataAccess.clear();
        assertEquals(0,dataAccess.listGames().size());
        assertNull(dataAccess.getUser("whiteUser1"));
        assertNull(dataAccess.getAuth("notARealAuth"));
    }
}
