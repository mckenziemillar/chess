package server;
import dataaccess.DataAccessException;
import dataaccess.MySqlDataAccess;
import server.handler.*;
import dataaccess.MemoryDataAccess;
import service.ClearService;
import service.UserService;
import service.AuthService;
import service.GameService;
import dataaccess.DatabaseManager;
import spark.*;

public class Server {

    public int run(int desiredPort) {

        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        MySqlDataAccess dataAccess = null;
        try {
            dataAccess = new MySqlDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        UserService userService = new UserService(dataAccess);
        AuthService authService = new AuthService(dataAccess);
        GameService gameService = new GameService(dataAccess, authService);
        ClearService clearService = new ClearService(dataAccess);

        RegisterHandler registerHandler = new RegisterHandler(userService);
        LoginHandler loginHandler = new LoginHandler(authService);
        ClearHandler clearHandler = new ClearHandler(clearService);
        CreateGameHandler createGameHandler = new CreateGameHandler(gameService);
        JoinGameHandler joinGameHandler = new JoinGameHandler(gameService);
        LogoutHandler logoutHandler = new LogoutHandler(authService);
        ListGamesHandler listGamesHandler = new ListGamesHandler(gameService);

        // Register your endpoints and handle exceptions here.
        Spark.get("/", (req, res) -> {
            return "Hello, world! ♕ 240 Chess Server is running.";
        });
        Spark.post("/user", registerHandler::register);
        Spark.post("/session", loginHandler::login);
        Spark.delete("/session", logoutHandler::logout);
        Spark.post("/game", createGameHandler::createGame);
        Spark.put("/game", joinGameHandler::joinGame);
        Spark.get("/game", listGamesHandler::listGames);
        Spark.delete("/db", clearHandler::clear);


        Spark.webSocket("/ws", new WebSocketHandler(gameService, authService));

        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
