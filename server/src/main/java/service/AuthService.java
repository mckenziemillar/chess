package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;

import java.util.UUID;
public class AuthService {
    private final DataAccess dataAccess;

    public AuthService() {
        this.dataAccess = new MemoryDataAccess();
    }

    public AuthService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData login(String username, String password) throws DataAccessException {
        UserData user = dataAccess.getUser(username);
        if (user == null || !user.password().equals(password)) {
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
