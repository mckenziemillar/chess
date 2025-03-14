package tests;

import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ClearService;

import static org.junit.jupiter.api.Assertions.*;

class ClearTests {

    private ClearService clearService;
    private TestDataAccess testDataAccess;

    @BeforeEach
    void setUp() {
        try {
            testDataAccess = new TestDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        clearService = new ClearService(testDataAccess);
    }

    @Test
    void clearDataCleared() throws DataAccessException {
        // Add some data to the test data access using the DAO methods
        UserData user = new UserData("user1", "pass1", "email1");
        testDataAccess.createUser(user);

        AuthData auth = new AuthData("auth1", "user1");
        testDataAccess.createAuth(auth);

        GameData game = new GameData(1, "user1", null, "game1", new chess.ChessGame());
        testDataAccess.createGame(game.gameName());

        clearService.clear();

        // Verify that the data is cleared using the DAO methods
        assertNull(testDataAccess.getUser("user1"));
        assertNull(testDataAccess.getAuth("auth1"));
        assertNull(testDataAccess.getGame(1));
        assertTrue(testDataAccess.listGames().isEmpty());
    }

    // Test Double implementation
}