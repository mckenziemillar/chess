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
        AuthData auth = new AuthData(UUID.randomUUID().toString(), "testUser");
        dataAccess.createUser(new UserData("testUser", "pass", "test@test.com"));
        dataAccess.createAuth(auth);
        AuthData retrievedAuth = dataAccess.getAuth(auth.authToken());
        assertNotNull(retrievedAuth);
        assertEquals(auth.authToken(), retrievedAuth.authToken());
    }

    @Test
    void createAuth_negative_userNotFound() {
        AuthData auth = new AuthData(UUID.randomUUID().toString(), "nonExistentUser");
        assertThrows(DataAccessException.class, () -> dataAccess.createAuth(auth));
    }

    // getAuth Tests
    @Test
    void getAuth_positive() throws DataAccessException {
        AuthData auth = new AuthData(UUID.randomUUID().toString(), "testUser");
        dataAccess.createUser(new UserData("testUser", "pass", "test@test.com"));
        dataAccess.createAuth(auth);
        AuthData retrievedAuth = dataAccess.getAuth(auth.authToken());
        assertNotNull(retrievedAuth);
        assertEquals(auth.authToken(), retrievedAuth.authToken());
    }

    @Test
    void getAuth_negative_authNotFound() throws DataAccessException {
        AuthData retrievedAuth = dataAccess.getAuth("nonExistentAuth");
        assertNull(retrievedAuth);
    }

    @Test
    void deleteAuth_positive() throws DataAccessException {
        AuthData auth = new AuthData(UUID.randomUUID().toString(), "testUser");
        dataAccess.createUser(new UserData("testUser", "pass", "test@test.com"));
        dataAccess.createAuth(auth);
        dataAccess.deleteAuth(auth.authToken());
        AuthData retrievedAuth = dataAccess.getAuth(auth.authToken());
        assertNull(retrievedAuth);
    }

    @Test
    void deleteAuth_negative_authNotFound() throws DataAccessException {
        assertDoesNotThrow(() -> dataAccess.deleteAuth("nonExistentAuth"));
    }

    @Test
    void createGame_positive() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(1, "whiteUser", "blackUser", "testGame", chessGame);
        dataAccess.createUser(new UserData("whiteUser", "pass", "white@test.com"));
        dataAccess.createUser(new UserData("blackUser", "pass", "black@test.com"));
        dataAccess.createGame(game.gameName());
        GameData retrievedGame = dataAccess.getGame(1);
        assertNotNull(retrievedGame);
        assertEquals(game.gameName(), retrievedGame.gameName());
    }

    @Test
    void createGame_negative_userNotFound() {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(1, "nonExistentUser", "blackUser", "testGame", chessGame);
        assertThrows(DataAccessException.class, () -> dataAccess.createGame(game.gameName()));
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
        ChessGame chessGame1 = new ChessGame();
        ChessGame chessGame2 = new ChessGame();
        GameData game1 = new GameData(1, "whiteUser1", "blackUser1", "game1", chessGame1);
        GameData game2 = new GameData(2, "whiteUser2", "blackUser2", "game2", chessGame2);
        dataAccess.createUser(new UserData("whiteUser1", "pass", "white1@test.com"));
        dataAccess.createUser(new UserData("blackUser1", "pass", "black1@test.com"));
        dataAccess.createUser(new UserData("whiteUser2", "pass", "white2@test.com"));
        dataAccess.createUser(new UserData("blackUser2", "pass", "black2@test.com"));
        dataAccess.createGame(game1.gameName());
        dataAccess.createGame(game2.gameName());
        Collection<GameData> games = dataAccess.listGames();
        assertNotNull(games);
        assertEquals(2, games.size());
    }

    // clear Test
    @Test
    void clear_positive() throws DataAccessException {
        ChessGame chessGame1 = new ChessGame();
        GameData game1 = new GameData(1, "whiteUser1", "blackUser1", "game1", chessGame1);
        dataAccess.createUser(new UserData("whiteUser1", "pass", "white1@test.com"));
        dataAccess.createUser(new UserData("blackUser1", "pass", "black1@test.com"));
        dataAccess.createGame(game1.gameName());
        dataAccess.clear();
        assertEquals(0,dataAccess.listGames().size());
        assertNull(dataAccess.getUser("whiteUser1"));
        assertNull(dataAccess.getAuth("notARealAuth"));
    }
}
