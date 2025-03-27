package ui;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import model.AuthData;
import model.GameData;

public class ServerFacade {

    private final String serverUrl;
    private final HttpClient httpClient;
    private final Gson gson;
    private String authToken;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.authToken = null;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        URI uri = URI.create(serverUrl + "/user");
        String jsonBody = gson.toJson(new RegisterRequest(username, password, email));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), AuthData.class);
        } else {
            throw new Exception("Registration failed: " + response.body()); // Or handle errors more specifically
        }
    }


    public AuthData login(String username, String password) throws Exception {
        URI uri = URI.create(serverUrl + "/session");
        String jsonBody = gson.toJson(new LoginRequest(username, password));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            AuthData authData = gson.fromJson(response.body(), AuthData.class);
            this.authToken = authData.authToken(); // Store the authToken.
            return authData;
        } else {
            throw new Exception("Login failed: " + response.body());
        }
    }

    public GameData createGame(String gameName) throws Exception {
        URI uri = URI.create(serverUrl + "/game");
        String jsonBody = gson.toJson(new CreateGameRequest(gameName));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("authorization", authToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), GameData.class);
        } else {
            throw new Exception("Create game failed: " + response.body());
        }
    }




    public GameData[] listGames() throws Exception {
        URI uri = URI.create(serverUrl + "/game");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("authorization", authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), GameData[].class);
        } else {
            throw new Exception("List games failed: " + response.body());
        }
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    // TODO: Implement logout, joinGame, observeGame

    public record RegisterRequest(String username, String password, String email) {}
    public record LoginRequest(String username, String password) {}
    public record CreateGameRequest(String gameName) {}
    //public record AuthData(String authToken, String username) {}
    //public record GameData(int gameID, String gameName, String whiteUsername, String blackUsername) {}
}