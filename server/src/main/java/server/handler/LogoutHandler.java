package server.handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.AuthService;
import spark.Request;
import spark.Response;

public class LogoutHandler {

    private final AuthService authService;
    private final Gson gson;

    public LogoutHandler() {
        this.authService = new AuthService();
        this.gson = new Gson();
    }

    public Object logout(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            authService.logout(authToken);
            res.status(200);
            return "{}"; // Empty JSON object for success
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorMessage("Error: unauthorized"));
        } catch (Exception e){
            res.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record ErrorMessage(String message) {}
}