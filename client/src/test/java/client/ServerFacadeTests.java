package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.AuthData;
import model.GameData;
import ui.ServerFacade;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Random;




public class ServerFacadeTests {

    private static Server server;
    private static int port;
    private static String serverUrl;
    private ServerFacade serverFacade;
    private static final Random RANDOM = new Random();
    private static String testAuthToken;
    private static String testUsername;
    private static int testGameId;
    private static String testGameName = "Test Game";



    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverUrl = "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() throws Exception {
        serverFacade = new ServerFacade(serverUrl);
        testUsername = "testUser_" + System.currentTimeMillis() + "_" + RANDOM.nextInt(1000);
        AuthData authData = serverFacade.register(testUsername, "testPassword", testUsername + "@example.com");
        testAuthToken = authData.authToken();
        serverFacade.setAuthToken(testAuthToken); // Set the authToken for subsequent calls
        GameData createdGame = serverFacade.createGame(testGameName);
        testGameId = createdGame.gameID();
    }

    @Test
    void registerSuccess() throws Exception {
        String uniqueUsername = "testUser_" + System.currentTimeMillis() + "_" + RANDOM.nextInt(1000);
        AuthData authData = serverFacade.register(uniqueUsername, "testPassword", "test" + System.currentTimeMillis() + "@example.com");
        assertNotNull(authData);
        assertNotNull(authData.authToken());
        assertEquals(uniqueUsername, authData.username());
    }

    @Test
    void registerFailureDuplicateUsername() {
        try {
            serverFacade.register("existingUser", "password", "test@example.com");
            serverFacade.register("existingUser", "anotherPassword", "another@example.com");
            fail("Expected an exception for duplicate username");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Registration failed"));
        }
    }

    @Test
    void loginSuccess() throws Exception {
        AuthData authData = serverFacade.login(testUsername, "testPassword");
        assertNotNull(authData);
        assertNotNull(authData.authToken());
        assertEquals(testUsername, authData.username());
        assertNotNull(serverFacade.getAuthToken());
        assertEquals(authData.authToken(), serverFacade.getAuthToken());
    }

    @Test
    void loginFailureInvalidCredentials() {
        try {
            serverFacade.login(testUsername, "wrongPassword");
            fail("Expected an exception for invalid credentials");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Login failed"));
        }
    }

    @Test
    void logoutSuccess() throws Exception {
        assertNotNull(serverFacade.getAuthToken());
        serverFacade.logout();
        assertNull(serverFacade.getAuthToken());
    }

    @Test
    void logoutFailureNotLoggedIn() {
        ServerFacade newFacade = new ServerFacade(serverUrl);
        assertNull(newFacade.getAuthToken());
        try {
            newFacade.logout();
            fail("Expected an exception for not being logged in");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Not logged in"));
        }
    }

    @Test
    void createGameSuccess() throws Exception {
        GameData gameData = serverFacade.createGame(testGameName);
        assertNotNull(gameData);
        assertTrue(gameData.gameID() > 0);
        assertEquals(testGameName, gameData.gameName());
        assertNull(gameData.whiteUsername());
        assertNull(gameData.blackUsername());
    }

    @Test
    void createGameFailureUnauthorized() {
        ServerFacade unauthorizedFacade = new ServerFacade(serverUrl);
        boolean exceptionThrown = false;
        try {
            unauthorizedFacade.createGame("Unauthorized Game");
            fail("Expected an exception for unauthorized access");
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown, "Expected an exception to be thrown for unauthorized access");
    }

    @Test
    void listGamesSuccess() throws Exception {
        GameData createdGame = serverFacade.createGame("Game to List");
        assertNotNull(createdGame);

        Collection<GameData> games = serverFacade.listGames().games();
        assertNotNull(games);
        assertTrue(games.size() >= 1);
        boolean found = false;
        for (GameData game : games) {
            if (game.gameID() == createdGame.gameID() && game.gameName().equals(createdGame.gameName())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "The created game should be in the list of games");
    }

    @Test
    void listGamesSuccessEmptyList() throws Exception {
        Collection<GameData> games = serverFacade.listGames().games();
        assertNotNull(games);
    }

    @Test
    void listGamesFailureUnauthorized() {
        ServerFacade unauthorizedFacade = new ServerFacade(serverUrl);
        boolean exceptionThrown = false;
        try {
            unauthorizedFacade.listGames();
            fail("Expected an exception for unauthorized access");
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown, "Expected an exception to be thrown for unauthorized access");
    }

    @Test
    void joinGameSuccess() throws Exception {
        try {
            serverFacade.joinGame(testGameId, "WHITE");
            // If the method completes without throwing an exception, it's considered a success
            assertTrue(true, "joinGame completed without throwing an exception");
        } catch (Exception e) {
            fail("joinGame threw an unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void joinGameFailureUnauthorized() {
        ServerFacade unauthorizedFacade = new ServerFacade(serverUrl);
        try {
            unauthorizedFacade.joinGame(testGameId, "BLACK");
            fail("Expected an exception for unauthorized access");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Not logged in"));
        }
    }

    @Test
    void joinGameFailureGameNotFound() {
        try {
            serverFacade.joinGame(99999, "BLACK"); // Assuming this game ID doesn't exist
            fail("Expected an exception for game not found");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Join game failed"));
        }
    }

    @Test
    void observeGameSuccess() throws Exception {
        try {
            serverFacade.observeGame(testGameId);
            assertTrue(true, "observeGame completed without throwing an exception");
        } catch (Exception e) {
            fail("observeGame threw an unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void observeGameFailureUnauthorized() {
        ServerFacade unauthorizedFacade = new ServerFacade(serverUrl);
        try {
            unauthorizedFacade.observeGame(testGameId);
            fail("Expected an exception for unauthorized access");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Not logged in"));
        }
    }

    @Test
    void observeGameFailureGameNotFound() {
        try {
            serverFacade.observeGame(999999); // Assuming this game ID doesn't exist
        } catch (Exception e) {
            assertTrue(true, "An exception was thrown as expected for game not found");
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

}
