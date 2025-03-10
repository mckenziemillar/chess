package dataaccess;

import dataaccess.DataAccessException;
import dataaccess.daoclasses.AuthDataDAO;
import model.AuthData;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDataDAO implements AuthDataDAO {
    private final Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        auth = new AuthData(UUID.randomUUID().toString(), auth.username());
        authTokens.put(auth.authToken(), auth);
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);
    }

    public void clear() {
        authTokens.clear();
    }
}