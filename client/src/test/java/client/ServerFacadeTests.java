package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.AuthData;
import model.GameData;
import ui.ServerFacade;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;




public class ServerFacadeTests {

    private static Server server;
    private static int port;
    private static String serverUrl;
    private ServerFacade serverFacade;
    private static final Random random = new Random();
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
        testUsername = "testUser_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
        AuthData authData = serverFacade.register(testUsername, "testPassword", testUsername + "@example.com");
        testAuthToken = authData.authToken();
        serverFacade.setAuthToken(testAuthToken); // Set the authToken for subsequent calls
    }

    @Test
    void registerSuccess() throws Exception {
        String uniqueUsername = "testUser_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
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
    void createGameSuccess() throws Exception {
        GameData gameData = serverFacade.createGame(testGameName);
        assertNotNull(gameData);
        assertTrue(gameData.gameID() > 0);
        assertEquals(testGameName, gameData.gameName());
        assertNull(gameData.whiteUsername());
        assertNull(gameData.blackUsername());
        //testGameId = gameData.gameID();
    }

    @Test
    void createGameFailureUnauthorized() {
        ServerFacade unauthorizedFacade = new ServerFacade(serverUrl);
        try {
            unauthorizedFacade.createGame("Unauthorized Game");
            fail("Expected an exception for unauthorized access");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Create game failed"));
        }
    }

    @Test
    void listGamesSuccess() throws Exception {
        GameData createdGame = serverFacade.createGame("Game to List");
        assertNotNull(createdGame);

        GameData[] games = serverFacade.listGames();
        assertNotNull(games);
        assertTrue(games.length >= 1);
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
        GameData[] games = serverFacade.listGames();
        assertNotNull(games);
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
