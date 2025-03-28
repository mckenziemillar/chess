package tests;

import chess.ChessGame;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AuthService;
import service.GameService;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTests {

    private GameService gameService;
    private TestDataAccess testDataAccess;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        try {
            testDataAccess = new TestDataAccess();
            authService = new AuthService(testDataAccess);
            gameService = new GameService(testDataAccess, authService);

            // Create a default test user in the setUp method
            String testUsername = "testUser";
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void listGamesValidAuthTokenReturnsGames() throws DataAccessException {
        String authToken = createAuthToken("testUser");
        GameData game1 = createGameData(1, "Game1");
        GameData game2 = createGameData(2, "Game2");
        testDataAccess.createGame(game1.gameName());
        testDataAccess.createGame(game2.gameName());

        Collection<GameData> games = gameService.listGames(authToken);

        assertEquals(2, games.size());
    }

    @Test
    void listGamesInvalidAuthTokenThrowsDataAccessException() {
        assertThrows(DataAccessException.class, () -> gameService.listGames("invalidToken"));
    }

    @Test
    void createGameValidAuthTokenReturnsGameData() throws DataAccessException {
        String authToken = createAuthToken("testUser");
        String gameName = "NewGame";

        GameData game = gameService.createGame(authToken, gameName);

        assertNotNull(game);
        assertEquals(gameName, game.gameName());
        assertNotNull(testDataAccess.getGame(game.gameID()));
    }

    @Test
    void createGameInvalidAuthTokenThrowsDataAccessException() {
        assertThrows(DataAccessException.class, () -> gameService.createGame("invalidToken", "Game"));
    }

    @Test
    void joinGameValidWhitePlayerJoinsGame() throws DataAccessException {
        String authToken = createAuthToken("testUser");
        GameData game = createGameData(1, "Game");
        testDataAccess.createGame(game.gameName());

        gameService.joinGame(authToken, 1, "WHITE");

        GameData updatedGame = testDataAccess.getGame(1);
        assertNotNull(updatedGame);
        assertEquals("testUser", updatedGame.whiteUsername());
    }

    @Test
    void joinGameValidBlackPlayerJoinsGame() throws DataAccessException {
        String authToken = createAuthToken("testUser");
        GameData game = createGameData(1, "Game");
        testDataAccess.createGame(game.gameName());

        gameService.joinGame(authToken, 1, "BLACK");

        GameData updatedGame = testDataAccess.getGame(1);
        assertNotNull(updatedGame);
        assertEquals("testUser", updatedGame.blackUsername());
    }

    @Test
    void joinGameInvalidAuthTokenThrowsDataAccessException() {
        assertThrows(DataAccessException.class, () -> gameService.joinGame("invalidToken", 1, "WHITE"));
    }

    @Test
    void joinGameGameNotFoundThrowsDataAccessException() throws DataAccessException {
        String authToken = createAuthToken("testUser");
        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, 1, "WHITE"));
    }

    @Test
    void joinGameWhiteAlreadyTakenThrowsDataAccessException() throws DataAccessException {
        String authToken1 = createAuthToken("user1");
        String authToken2 = createAuthToken("user2");
        GameData game = createGameData(1, "Game");
        game = new GameData(1, "user1", null, "Game", new ChessGame());
        testDataAccess.createGame(game.gameName());
        testDataAccess.updateGame(game);

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken2, 1, "WHITE"));
    }

    @Test
    void joinGameBlackAlreadyTakenThrowsDataAccessException() throws DataAccessException {
        String authToken1 = createAuthToken("user1");
        String authToken2 = createAuthToken("user2");
        GameData game = createGameData(1, "Game");
        game = new GameData(1, null, "user1", "Game", new ChessGame());
        testDataAccess.createGame(game.gameName());
        testDataAccess.updateGame(game);

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken2, 1, "BLACK"));
    }

    @Test
    void joinGameInvalidColorThrowsDataAccessException() throws DataAccessException{
        String authToken = createAuthToken("user1");
        GameData game = createGameData(1, "Game");
        testDataAccess.createGame(game.gameName());

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, 1, "PURPLE"));
    }

    @Test
    void joinGameNullColorThrowsDataAccessException() throws DataAccessException{
        String authToken = createAuthToken("user1");
        GameData game = createGameData(1, "Game");
        testDataAccess.createGame(game.gameName());

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, 1, null));
    }

    private String createAuthToken(String username) throws DataAccessException {
        AuthData authData = new AuthData(UUID.randomUUID().toString(), username);
        return testDataAccess.createAuth(authData).authToken();
    }

    private GameData createGameData(int gameID, String gameName) {
        return new GameData(gameID, null, null, gameName, new ChessGame());
    }
}