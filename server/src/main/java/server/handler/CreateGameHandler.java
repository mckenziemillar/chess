package server.handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;
public class CreateGameHandler {
    private final GameService gameService;
    private final Gson gson;

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
        this.gson = new Gson();
    }

    public Object createGame(Request req, Response res) {
        try {
            CreateGameRequest createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
            GameData gameData = gameService.createGame(createGameRequest.gameName());
            res.status(200);
            return gson.toJson(new CreateGameResult(gameData.gameID()));
        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }

    }

    private record CreateGameRequest(String gameName) {}
    private record CreateGameResult(int gameID) {}
    private record ErrorMessage(String message) {}
}
