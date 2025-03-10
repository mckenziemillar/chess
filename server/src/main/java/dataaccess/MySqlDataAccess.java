package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.Collection;

public class MySqlDataAccess implements DataAccess{

    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }
    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement("TRUNCATE TABLE Users");
             var preparedStatement2 = conn.prepareStatement("TRUNCATE TABLE AuthTokens");
             var preparedStatement3 = conn.prepareStatement("TRUNCATE TABLE Games")) {
            preparedStatement.executeUpdate();
            preparedStatement2.executeUpdate();
            preparedStatement3.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to clear database: %s", ex.getMessage()));
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO Users (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, user.username());
            preparedStatement.setString(2, user.password()); // Remember to hash passwords!
            preparedStatement.setString(3, user.email());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to create user: %s", ex.getMessage()));
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        Gson gson = new Gson();
        String gameJson = gson.toJson(game);
        String sql = "INSERT INTO Games (whiteUsername, blackUsername, gameName, gameData) VALUES (?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, game.whiteUsername());
            preparedStatement.setString(2, game.blackUsername());
            preparedStatement.setString(3, game.gameName());
            preparedStatement.setString(4, gameJson);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to create game: %s", ex.getMessage()));
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return null;
    }

    private final String[] createStatements = {
            """
        CREATE TABLE IF NOT EXISTS Users (
            username VARCHAR(255) PRIMARY KEY,
            password VARCHAR(255),
            email VARCHAR(255)
        )
        """,
            """
        CREATE TABLE IF NOT EXISTS AuthTokens (
            authToken VARCHAR(255) PRIMARY KEY,
            username VARCHAR(255),
            FOREIGN KEY (username) REFERENCES Users(username)
        )
        """,
            """
        CREATE TABLE IF NOT EXISTS Games (
            gameID INT AUTO_INCREMENT PRIMARY KEY,
            whiteUsername VARCHAR(255),
            blackUsername VARCHAR(255),
            gameName VARCHAR(255),
            gameData TEXT,
            FOREIGN KEY (whiteUsername) REFERENCES Users(username),
            FOREIGN KEY (blackUsername) REFERENCES Users(username)
        )
        """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
