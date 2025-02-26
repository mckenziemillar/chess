package server.handler;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.GameService;
import spark.Request;
import spark.Response;

public class JoinGameHandler {
    private final GameService gameService;
    private final Gson gson;

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
        this.gson = new Gson();
    }

    public Object joinGame(Request req, Response res) {
        try {
            JoinGameRequest joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            String authToken = req.headers("Authorization");
            gameService.joinGame(authToken, joinGameRequest.gameID(), joinGameRequest.playerColor());
            res.status(200);
            return "{}"; // Empty JSON object for success
        } catch (DataAccessException e) {
            if (e.getMessage().equals("Error: unauthorized")) {
                res.status(401);
            } else if (e.getMessage().equals("Error: bad request")){
                res.status(400);
            } else if (e.getMessage().equals("Error: already taken")){
                res.status(403);
            } else{
                res.status(500);
            }
            return gson.toJson(new ErrorMessage(e.getMessage()));
        }
    }

    private record JoinGameRequest(int gameID, String playerColor) {}
    private record ErrorMessage(String message) {}
}
