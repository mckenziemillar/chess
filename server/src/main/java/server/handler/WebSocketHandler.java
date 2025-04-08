package server.handler;

import dataaccess.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.AuthService;
import service.GameService;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import model.GameData;
import model.AuthData;
import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final GameService gameService;
    private final AuthService authService;
    private final Gson gson = new Gson();

    public WebSocketHandler(GameService gameService, AuthService authService) {
        this.gameService = gameService;
        this.authService = authService;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session.getRemoteAddress().getAddress());
        // Handle new connection (e.g., authenticate, add to a connection list)
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + statusCode + " - " + reason);
        // Handle disconnection logic (e.g., remove user from game)
    }

    @OnWebSocketError
    public void onError(Session session, Throwable cause) {
        System.err.println("WebSocket error: " + cause.getMessage());
        // Handle errors
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received message: " + message);
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            // Process the user game command based on its type
            switch (command.getCommandType()) {
                case CONNECT:
                    Integer gameID = command.getGameID();
                    String authToken = command.getAuthToken();
                    System.out.println("CONNECT command received for game ID: " + gameID + " with authToken: " + authToken);
                    // TODO: Authenticate the user using authToken
                    // TODO: Load the game state using gameID
                    // TODO: Send a LOAD_GAME message back to the client
                    // TODO: Send a NOTIFICATION message to other clients about the new connection
                    break;
                case MAKE_MOVE:
                    System.out.println("MAKE_MOVE command received: " + message);
                    // TODO: Deserialize the ChessMove from the command
                    // TODO: Validate the move using gameService
                    // TODO: Update the game state
                    // TODO: Send a LOAD_GAME message to all clients
                    // TODO: Send a NOTIFICATION message to other clients about the move
                    break;
                case LEAVE:
                    System.out.println("LEAVE command received");
                    // TODO: Remove the user from the game
                    // TODO: Send a NOTIFICATION message to other clients about the user leaving
                    break;
                case RESIGN:
                    // Handle RESIGN command
                    System.out.println("RESIGN command received");
                    // TODO: Mark the game as over due to resignation
                    // TODO: Send a NOTIFICATION message to all clients about the resignation
                    break;
                default:
                    System.out.println("Unknown command type: " + command.getCommandType());
                    // TODO: Send an ERROR message back to the client
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing WebSocket message: " + e.getMessage());
            // Send an error message back to the client
        }
    }

    private void handleConnectCommand(Session session, UserGameCommand command) {
        Integer gameID = command.getGameID();
        String authToken = command.getAuthToken();
        System.out.println("CONNECT command received for game ID: " + gameID + " with authToken: " + authToken);

        try {
            // 1. Authenticate the user
            AuthData authData = authService.getAuth(authToken);
            if (authData == null) {
                sendError(session, "Error: unauthorized");
                return;
            }
            String username = authData.username();

            // 2. Load the game state
            GameData gameData = gameService.dataAccess.getGame(gameID); // Assuming you have getGame in DataAccess
            if (gameData == null) {
                sendError(session, "Error: bad request");
                return;
            }

            // 3. Send LOAD_GAME message to the connecting client
            ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGameMessage.setGame(gameData); // Assuming you have a setGame method in ServerMessage
            sendMessage(session, loadGameMessage);

            // 4. Send NOTIFICATION message to other clients (This part requires you to track sessions)
            // For now, let's assume you have a way to get all sessions for a game (e.g., from GameService)
            // You'll need to implement this part based on how you manage WebSocket sessions
            // Collection<Session> otherSessions = gameService.getSessionsForGame(gameID); // You'll need to implement this
            // if (otherSessions != null) {
            //     for (Session otherSession : otherSessions) {
            //         if (!otherSession.equals(session)) {
            //             ServerMessage notificationMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            //             notificationMessage.setMessage(username + " connected to the game"); // Customize the message
            //             sendMessage(otherSession, notificationMessage);
            //         }
            //     }
            // }

        } catch (DataAccessException e) {
            sendError(session, e.getMessage());
        }
    }

    private void sendMessage(Session session, ServerMessage message) {
        try {
            String jsonMessage = gson.toJson(message);
            session.getRemote().sendString(jsonMessage);
            System.out.println("Sent message: " + jsonMessage);
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void sendError(Session session, String errorMessage) {
        ServerMessage errorMessageObject = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        errorMessageObject.setErrorMessage(errorMessage);
        sendMessage(session, errorMessageObject);
    }
}