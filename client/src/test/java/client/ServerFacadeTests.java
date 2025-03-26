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



    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverUrl = "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() {
        serverFacade = new ServerFacade(serverUrl);
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

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

}
