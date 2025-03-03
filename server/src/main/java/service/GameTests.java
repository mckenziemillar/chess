package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTests {

    private GameService gameService;
    private TestDataAccess testDataAccess;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        testDataAccess = new TestDataAccess();
        authService = new AuthService(testDataAccess);
        gameService = new GameService(testDataAccess);
    }

    @Test
    void listGames_validAuthToken_returnsGames() throws DataAccessException {
        String authToken = createAuthToken("testUser");
        GameData game1 = createGameData(1, "Game1");
        GameData game2 = createGameData(2, "Game2");
        testDataAccess.createGame(game1);
        testDataAccess.createGame(game2);

        Collection<GameData> games = gameService.listGames(authToken);

        assertEquals(2, games.size());
    }

    @Test
    void listGames_invalidAuthToken_throwsDataAccessException() {
        assertThrows(DataAccessException.class, () -> gameService.listGames("invalidToken"));
    }

    @Test
    void createGame_validAuthToken_returnsGameData() throws DataAccessException {
        String authToken = createAuthToken("testUser");
        String gameName = "NewGame";

        GameData game = gameService.createGame(authToken, gameName);

        assertNotNull(game);
        assertEquals(gameName, game.gameName());
        assertNotNull(testDataAccess.getGame(game.gameID()));
    }

    @Test
    void createGame_invalidAuthToken_throwsDataAccessException() {
        assertThrows(DataAccessException.class, () -> gameService.createGame("invalidToken", "Game"));
    }

    @Test
    void joinGame_validWhitePlayer_joinsGame() throws DataAccessException {
        String authToken = createAuthToken("testUser");
        GameData game = createGameData(1, "Game");
        testDataAccess.createGame(game);

        gameService.joinGame(authToken, 1, "WHITE");

        GameData updatedGame = testDataAccess.getGame(1);
        assertNotNull(updatedGame);
        assertEquals("testUser", updatedGame.whiteUsername());
    }

    @Test
    void joinGame_validBlackPlayer_joinsGame() throws DataAccessException {
        String authToken = createAuthToken("testUser");
        GameData game = createGameData(1, "Game");
        testDataAccess.createGame(game);

        gameService.joinGame(authToken, 1, "BLACK");

        GameData updatedGame = testDataAccess.getGame(1);
        assertNotNull(updatedGame);
        assertEquals("testUser", updatedGame.blackUsername());
    }

    @Test
    void joinGame_invalidAuthToken_throwsDataAccessException() {
        assertThrows(DataAccessException.class, () -> gameService.joinGame("invalidToken", 1, "WHITE"));
    }

    @Test
    void joinGame_gameNotFound_throwsDataAccessException() throws DataAccessException {
        String authToken = createAuthToken("testUser");
        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, 1, "WHITE"));
    }

    @Test
    void joinGame_whiteAlreadyTaken_throwsDataAccessException() throws DataAccessException {
        String authToken1 = createAuthToken("user1");
        String authToken2 = createAuthToken("user2");
        GameData game = createGameData(1, "Game");
        game = new GameData(1, "user1", null, "Game", new ChessGame());
        testDataAccess.createGame(game);

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken2, 1, "WHITE"));
    }

    @Test
    void joinGame_blackAlreadyTaken_throwsDataAccessException() throws DataAccessException {
        String authToken1 = createAuthToken("user1");
        String authToken2 = createAuthToken("user2");
        GameData game = createGameData(1, "Game");
        game = new GameData(1, null, "user1", "Game", new ChessGame());
        testDataAccess.createGame(game);

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken2, 1, "BLACK"));
    }

    @Test
    void joinGame_invalidColor_throwsDataAccessException() throws DataAccessException{
        String authToken = createAuthToken("user1");
        GameData game = createGameData(1, "Game");
        testDataAccess.createGame(game);

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, 1, "PURPLE"));
    }

    @Test
    void joinGame_nullColor_throwsDataAccessException() throws DataAccessException{
        String authToken = createAuthToken("user1");
        GameData game = createGameData(1, "Game");
        testDataAccess.createGame(game);

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, 1, null));
    }

    private String createAuthToken(String username) throws DataAccessException {
        AuthData authData = new AuthData(UUID.randomUUID().toString(), username);
        testDataAccess.createAuth(authData);
        return authData.authToken();
    }

    private GameData createGameData(int gameID, String gameName) {
        return new GameData(gameID, null, null, gameName, new ChessGame());
    }

    private static class TestDataAccess implements DataAccess {
        Map<String, AuthData> auths = new HashMap<>();
        Map<Integer, GameData> games = new HashMap<>();

        @Override
        public void clear() throws DataAccessException {
            auths.clear();
            games.clear();
        }

        @Override
        public void createUser(model.UserData user) throws DataAccessException {
        }

        @Override
        public model.UserData getUser(String username) throws DataAccessException {
            return null;
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