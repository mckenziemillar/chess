package service.tests;

import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AuthService;

import static org.junit.jupiter.api.Assertions.*;

class AuthTests {

    private AuthService authService;
    private TestDataAccess testDataAccess;

    @BeforeEach
    void setUp() {
        testDataAccess = new TestDataAccess();
        authService = new AuthService(testDataAccess);
    }

    @Test
    void loginInvalidUsernameThrowsDataAccessException() throws DataAccessException {
        String username = "nonexistentUser";
        String password = "testPassword";

        assertThrows(DataAccessException.class, () -> authService.login(username, password));
    }

    @Test
    void getAuthValidAuthTokenReturnsAuthData() throws DataAccessException {
        String authToken = "validAuthToken";
        AuthData authData = new AuthData(authToken, "testUser");
        testDataAccess.createAuth(authData);

        AuthData result = authService.getAuth(authToken);

        assertEquals(authData, result);
    }

    @Test
    void loginValidCredentialsReturnsAuthData() throws DataAccessException {
        String username = "testUser";
        String password = "testPassword";
        UserData user = new UserData(username, password, "test@example.com");
        testDataAccess.createUser(user);

        AuthData result = authService.login(username, password);

        assertNotNull(result);
        assertEquals(username, result.username());
        assertNotNull(testDataAccess.getAuth(result.authToken()));
    }

    @Test
    void getAuthInvalidAuthTokenReturnsNull() throws DataAccessException {
        String authToken = "invalidAuthToken";

        AuthData result = authService.getAuth(authToken);

        assertNull(result);
    }

    @Test
    void logoutValidAuthTokenDeletesAuthData() throws DataAccessException {
        String authToken = "validAuthToken";
        AuthData authData = new AuthData(authToken, "testUser");
        testDataAccess.createAuth(authData);

        authService.logout(authToken);

        assertNull(testDataAccess.getAuth(authToken));
    }

    @Test
    void logoutInvalidAuthTokenThrowsDataAccessException() {
        String authToken = "invalidAuthToken";

        assertThrows(DataAccessException.class, () -> authService.logout(authToken));
    }
}