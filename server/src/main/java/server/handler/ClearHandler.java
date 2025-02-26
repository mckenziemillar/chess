package server.handler;
import com.google.gson.Gson;
import service.ClearService;
import spark.Request;
import spark.Response;
public class ClearHandler {
    private final ClearService clearService;
    private final Gson gson;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
        this.gson = new Gson();
    }

    public Object clear(Request req, Response res) {
        try {
            clearService.clear();
            res.status(200);
            return "{}"; // Empty JSON object
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record ErrorMessage(String message) {}
}
