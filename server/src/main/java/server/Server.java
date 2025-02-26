package server;
import server.handler.ClearHandler;
import server.handler.CreateGameHandler;
import server.handler.RegisterHandler;
import server.handler.JoinGameHandler;
import com.google.gson.Gson;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.get("/", (req, res) -> {
            return "Hello, world! ♕ 240 Chess Server is running.";
        });
        Spark.post("/user", (req, res) -> new RegisterHandler().register(req, res));
        Spark.post("/game", (req, res) -> new CreateGameHandler().createGame(req, res));
        Spark.put("/game", (req, res) -> new JoinGameHandler().joinGame(req, res));
        Spark.delete("/db", (req, res) -> {
            return new ClearHandler().clear(req, res);
        });

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
