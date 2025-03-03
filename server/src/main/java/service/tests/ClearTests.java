package service.tests;

import dataaccess.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ClearService;

import static org.junit.jupiter.api.Assertions.*;

class ClearTests {

    private ClearService clearService;
    private TestDataAccess testDataAccess;

    @BeforeEach
    void setUp() {
        testDataAccess = new TestDataAccess();
        clearService = new ClearService(testDataAccess);
    }

    @Test
    void clearDataCleared() throws DataAccessException {
        // Add some data to the test data access
        testDataAccess.users.put("user1", new model.UserData("user1", "pass1", "email1"));
        testDataAccess.auths.put("auth1", new model.AuthData("auth1", "user1"));
        testDataAccess.games.put(1, new model.GameData(1, "user1", null, "game1", new chess.ChessGame()));

        clearService.clear();

        assertTrue(testDataAccess.users.isEmpty());
        assertTrue(testDataAccess.auths.isEmpty());
        assertTrue(testDataAccess.games.isEmpty());
    }

    // Test Double implementation
}