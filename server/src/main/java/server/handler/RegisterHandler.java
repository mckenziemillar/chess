package server.handler;
import dataaccess.DataAccessException;
import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import service.UserService;
import spark.Request;
import spark.Response;
public class RegisterHandler {
    private final UserService userService;
    private final Gson gson;

    public RegisterHandler() {
        this.userService = new UserService();
        this.gson = new Gson();
    }

    public Object register(Request req, Response res) {
        try {
            UserData userData = gson.fromJson(req.body(), UserData.class);
            AuthData authData = userService.register(userData);
            res.status(200);
            return gson.toJson(authData);
        } catch (DataAccessException e) {
            if (e.getMessage().equals("Error: already taken")){
                res.status(403);
                return gson.toJson(new ErrorMessage(e.getMessage()));
            }
            res.status(400);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(400);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record ErrorMessage(String message) {}
}
