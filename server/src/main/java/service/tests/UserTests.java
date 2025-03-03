package service.tests;

import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.UserService;
import service.tests.TestDataAccess;

import static org.junit.jupiter.api.Assertions.*;

class UserTests {

    private UserService userService;
    private TestDataAccess testDataAccess;

    @BeforeEach
    void setUp() {
        testDataAccess = new TestDataAccess();
        userService = new UserService(testDataAccess);
    }

    @Test
    void registerValidUserDataReturnsAuthData() throws DataAccessException {
        UserData userData = new UserData("testUser", "testPassword", "test@example.com");
        AuthData result = userService.register(userData);

        assertNotNull(result);
        assertEquals(userData.username(), result.username());
        assertNotNull(testDataAccess.getAuth(result.authToken()));
        assertNotNull(testDataAccess.getUser(userData.username()));
    }

    @Test
    void registerUsernameAlreadyTakenThrowsDataAccessException() throws DataAccessException {
        UserData existingUser = new UserData("existingUser", "password", "existing@example.com");
        testDataAccess.createUser(existingUser);

        UserData newUser = new UserData("existingUser", "newPassword", "new@example.com");
        assertThrows(DataAccessException.class, () -> userService.register(newUser));
        assertNull(testDataAccess.getAuth(newUser.username()));
    }
}