package dataaccess;

import dataaccess.DataAccessException;
import dataaccess.daoclasses.UserDataDAO;
import model.UserData;
import java.util.Map;
import java.util.HashMap;

public class MemoryUserDataDAO implements UserDataDAO {

    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Error: already taken"); // Handle duplicate usernames
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    public void clear() {
        users.clear();
    }
}