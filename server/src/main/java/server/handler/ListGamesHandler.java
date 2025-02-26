package server.handler;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.Collection;
public class ListGamesHandler {
    private final GameService gameService;
    private final Gson gson;

    public ListGamesHandler() {
        this.gameService = new GameService();
        this.gson = new Gson();
    }

    public Object listGames(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            Collection<GameData> games = gameService.listGames(authToken);
            res.status(200);
            return gson.toJson(new ListGamesResult(games));
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorMessage("Error: unauthorized"));
        } catch (Exception e){
            res.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record ListGamesResult(Collection<GameData> games) {}
    private record ErrorMessage(String message) {}
}
