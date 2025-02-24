package service;
import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import java.util.UUID;
public class UserService {
    private final DataAccess dataAccess;

    public UserService() {
        this.dataAccess = new MemoryDataAccess();
    }

    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData userData) throws DataAccessException {
        // Implement register logic here
        // ...
        if(dataAccess.getUser(userData.username()) != null){
            throw new DataAccessException("Error: already taken");
        }

        dataAccess.createUser(userData);
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, userData.username());
        dataAccess.createAuth(authData);

        return authData;
    }
}
