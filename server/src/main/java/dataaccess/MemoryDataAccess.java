package dataaccess;

import java.util.HashMap;
import java.util.Map;
import model.UserData;
import model.AuthData;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, Object> data = new HashMap<>();
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();

    // ... other methods ...

    @Override
    public void createUser(UserData user) throws DataAccessException {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authTokens.put(auth.authToken(), auth);
    }
    @Override
    public void clear() {
        data.clear();
    }
}
