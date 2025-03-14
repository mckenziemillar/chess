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
    private UserData createTestUser(String username) {
        return new UserData(username, "pass", username + "@test.com");
    }


    private AuthData createAndInsertAuth(String username) throws DataAccessException {
        UserData user = createTestUser(username);
        dataAccess.createUser(user);
        AuthData auth = new AuthData(UUID.randomUUID().toString(), username);
        dataAccess.createAuth(auth);
        return auth;
    }

    private GameData createAndInsertGame(String gameName, String whiteUsername, String blackUsername) throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        dataAccess.createUser(createTestUser(whiteUsername));
        dataAccess.createUser(createTestUser(blackUsername));
        dataAccess.createGame(gameName);
        return new GameData(1, whiteUsername, blackUsername, gameName, chessGame);
    }

    @Test
    void createUserPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@example.com");
        dataAccess.createUser(user);
        UserData retrievedUser = dataAccess.getUser("testUser");
        assertNotNull(retrievedUser);
        assertEquals(user.username(), retrievedUser.username());
        assertEquals(user.email(), retrievedUser.email());
        // Do not check password directly, as it's hashed.
    }

    @Test
    void createUserNegativeDuplicateUsername() {
        UserData user1 = new UserData("duplicateUser", "password", "test1@example.com");
        UserData user2 = new UserData("duplicateUser", "password", "test2@example.com");
        assertDoesNotThrow(() -> dataAccess.createUser(user1));
        assertThrows(DataAccessException.class, () -> dataAccess.createUser(user2));
    }

    // getUser Tests
    @Test
    void getUserPositive() throws DataAccessException {
        UserData user = new UserData("existingUser", "password", "exist@example.com");
        dataAccess.createUser(user);
        UserData retrievedUser = dataAccess.getUser("existingUser");
        assertNotNull(retrievedUser);
        assertEquals(user.username(), retrievedUser.username());
    }

    @Test
    void getUserNegativeUserNotFound() throws DataAccessException {
        UserData retrievedUser = dataAccess.getUser("nonExistentUser");
        assertNull(retrievedUser);
    }

    // createAuth Tests
    @Test
    void getAuthPositive() throws DataAccessException {
        AuthData auth = createAndInsertAuth("testUser");
        AuthData retrievedAuth = dataAccess.getAuth(auth.authToken());
        assertNotNull(retrievedAuth);
        assertEquals(auth.authToken(), retrievedAuth.authToken());
    }

    @Test
    void getAuthNegativeAuthNotFound() throws DataAccessException {
        AuthData retrievedAuth = dataAccess.getAuth("nonExistentAuth");
        assertNull(retrievedAuth);
    }

    @Test
    void deleteAuthPositive() throws DataAccessException {
        AuthData auth = createAndInsertAuth("testUser");
        dataAccess.deleteAuth(auth.authToken());
        AuthData retrievedAuth = dataAccess.getAuth(auth.authToken());
        assertNull(retrievedAuth);
    }

    @Test
    void deleteAuthNegativeAuthNotFound() throws DataAccessException {
        assertDoesNotThrow(() -> dataAccess.deleteAuth("nonExistentAuth"));
    }

    @Test
    void createGamePositive() throws DataAccessException {
        String gameName = "testGame";
        String whiteUser = "whiteUser";
        String blackUser = "blackUser";
        GameData game = createAndInsertGame(gameName, whiteUser, blackUser);
        assertNotNull(game);
        assertEquals(gameName, game.gameName());
        assertEquals(whiteUser, game.whiteUsername());
        assertEquals(blackUser, game.blackUsername());
        assertNotNull(game.game()); // Ensure a ChessGame object was created
    }

    @Test
    void createGameNegativeUserNotFound() {
        assertThrows(DataAccessException.class, () -> dataAccess.createGame(null));
    }

    // getGame Tests
    @Test
    void getGamePositive() throws DataAccessException {
        GameData game = createAndInsertGame("testGame", "whiteUser", "blackUser");
        GameData retrievedGame = dataAccess.getGame(1);
        assertNotNull(retrievedGame);
        assertEquals(game.gameName(), retrievedGame.gameName());
    }

    @Test
    void getGameNegativeGameNotFound() throws DataAccessException {
        GameData retrievedGame = dataAccess.getGame(999);
        assertNull(retrievedGame);
    }

    // listGames Tests
    @Test
    void listGamesPositive() throws DataAccessException {
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
    void clearPositive() throws DataAccessException {
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
