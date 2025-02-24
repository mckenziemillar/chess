package dataaccess.DAOClasses;

import dataaccess.DataAccessException;
import model.UserData;

public interface UserDataDAO {
    void createUser(UserData user) throws DataAccessException; // Create a new user
    UserData getUser(String username) throws DataAccessException; // Read user by username
    void updateUser(UserData user) throws DataAccessException; // Update user details (if needed)
    void deleteUser(String username) throws DataAccessException; // Delete a user
    void clearUsers() throws DataAccessException; //Delete all users (For testing)
}
