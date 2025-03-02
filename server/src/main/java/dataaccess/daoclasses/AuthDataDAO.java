package dataaccess.daoclasses;
import dataaccess.DataAccessException;
import model.AuthData;
public interface AuthDataDAO {
    void createAuth(AuthData auth) throws DataAccessException; // Create a new auth token entry
    AuthData getAuth(String authToken) throws DataAccessException; // Read auth info by token
    void deleteAuth(String authToken) throws DataAccessException; // Delete an auth token (logout)
    void clearAuths() throws DataAccessException; //Delete all auths (For testing)

}
