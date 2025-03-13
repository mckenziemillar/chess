package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import chess.ChessGame;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlDataAccess implements DataAccess{
    private int gameId = 1;
    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
        //generate the tables if they don't exist
        //createTableIfNotExist
    }
    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.createStatement()) {
            // Disable foreign key checks
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Truncate tables
            statement.execute("TRUNCATE TABLE AuthTokens");
            statement.execute("TRUNCATE TABLE Games");
            statement.execute("TRUNCATE TABLE Users");

            // Re-enable foreign key checks
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");

        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to clear database: %s", ex.getMessage()));
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        String sql = "INSERT INTO Users (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, user.username());
            preparedStatement.setString(2, hashedPassword); // Remember to hash passwords!
            preparedStatement.setString(3, user.email());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to create user: %s", ex.getMessage()));
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new UserData(resultSet.getString("username"), resultSet.getString("password"), resultSet.getString("email"));
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to get user: %s", ex.getMessage()));
        }
    }

    public boolean verifyUser(String username, String providedClearTextPassword) throws DataAccessException{
        UserData user = getUser(username);
        if (user == null) {
            return false; // User not found
        }
        return BCrypt.checkpw(providedClearTextPassword, user.password());
    }

    @Override
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO AuthTokens (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, auth.authToken());
            preparedStatement.setString(2, auth.username());
            preparedStatement.executeUpdate();
            return auth;
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to create auth: %s", ex.getMessage()));
        }

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT * FROM AuthTokens WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, authToken);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new AuthData(resultSet.getString("authToken"), resultSet.getString("username"));
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to get auth: %s", ex.getMessage()));
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM AuthTokens WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, authToken);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to delete auth: %s", ex.getMessage()));
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        Gson gson = new Gson();
        String gameJson = gson.toJson(game.game()); // Serialize the ChessGame
        String sql = "UPDATE Games SET whiteUsername = ?, blackUsername = ?, gameName = ?, gameData = ? WHERE gameID = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, game.whiteUsername());
            preparedStatement.setString(2, game.blackUsername());
            preparedStatement.setString(3, game.gameName());
            preparedStatement.setString(4, gameJson);
            preparedStatement.setInt(5, game.gameID());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to update game: %s", ex.getMessage()));
        }
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        Gson gson = new Gson();
        ChessGame chessGame = new ChessGame(); //Create a new ChessGame
        String gameJson = gson.toJson(chessGame); //Convert the chess game to Json.
        gameId++;
        GameData game = new GameData(gameId, null, null, gameName, chessGame); // Create a new GameData object.

        String sql = "INSERT INTO Games (whiteUsername, blackUsername, gameName, gameData) VALUES (?, ?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, game.whiteUsername());
            preparedStatement.setString(2, game.blackUsername());
            preparedStatement.setString(3, game.gameName());
            preparedStatement.setString(4, gameJson);
            preparedStatement.executeUpdate();
            var rs = preparedStatement.getGeneratedKeys();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to create game: %s", ex.getMessage()));
        }
        return -1;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM Games WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, gameID);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Gson gson = new Gson();
                    return new GameData(resultSet.getInt("gameID"), resultSet.getString("whiteUsername"), resultSet.getString("blackUsername"), resultSet.getString("gameName"), gson.fromJson(resultSet.getString("gameData"), chess.ChessGame.class));
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to get game: %s", ex.getMessage()));
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String sql = "SELECT * FROM Games";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql);
             var resultSet = preparedStatement.executeQuery()) {
            Collection<GameData> games = new ArrayList<>();
            Gson gson = new Gson();
            while (resultSet.next()) {
                games.add(new GameData(resultSet.getInt("gameID"), resultSet.getString("whiteUsername"), resultSet.getString("blackUsername"), resultSet.getString("gameName"), gson.fromJson(resultSet.getString("gameData"), chess.ChessGame.class)));
            }
            return games;
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to list games: %s", ex.getMessage()));
        }
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
