package server.handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import service.AuthService;
import spark.Request;
import spark.Response;

public class LoginHandler {

    private final AuthService authService;
    private final Gson gson;

    public LoginHandler(AuthService authService) {
        this.authService = authService;
        this.gson = new Gson();
    }

    public Object login(Request req, Response res) {
        try {
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            AuthData authData = authService.login(loginRequest.username(), loginRequest.password());
            res.status(200);
            return gson.toJson(authData);
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorMessage("Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record LoginRequest(String username, String password) {}
    private record ErrorMessage(String message) {}
}