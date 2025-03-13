package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import java.util.UUID;
public class AuthService {
    private final DataAccess dataAccess;

    public AuthService() {
        try {
            this.dataAccess = new MySqlDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public AuthService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData login(String username, String password) throws DataAccessException {
        UserData user = dataAccess.getUser(username);
        if (user == null || !dataAccess.verifyUser(username, password)) {
            throw new DataAccessException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authData = dataAccess.createAuth(authData);
        return authData;
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return dataAccess.getAuth(authToken);
    }

    public void logout(String authToken) throws DataAccessException{
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null){
            throw new DataAccessException("Error: unauthorized");
        }
        dataAccess.deleteAuth(authToken);
    }
}
