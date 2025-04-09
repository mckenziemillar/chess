package server.handler;

import chess.ChessMove;
import com.google.gson.JsonObject;
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
import model.AuthData;
import model.GameData;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@WebSocket
public class WebSocketHandler {

    private final GameService gameService;
    private final AuthService authService;
    private final Gson gson = new Gson();

    // WebSocket Session Management
    private final Map<Integer, Set<Session>> gameSessions = new HashMap<>();

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
        removeSession(session); // Remove the session on close
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
                    handleConnectCommand(session, command);
                    break;
                case MAKE_MOVE:
                    handleMakeMoveCommand(session, command, message); // Pass the message for JSON parsing
                    break;
                case LEAVE:
                    handleLeaveCommand(session, command);
                    break;
                case RESIGN:
                    handleResignCommand(session, command);
                    break;
                default:
                    sendError(session, "Error: Unknown command type: " + command.getCommandType());
                    break;
            }
        } catch (Exception e) {
            sendError(session, "Error processing WebSocket message: " + e.getMessage());
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
                sendError(session, "Error: bad request - Game not found");
                return;
            }

            // 3. Send LOAD_GAME message to the connecting client
            ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGameMessage.setGame(gameData); // Assuming you have a setGame method in ServerMessage
            sendMessage(session, loadGameMessage);

            // 4. Send NOTIFICATION message to other clients about the new connection
            // This part requires you to track sessions. For now, let's just print a message.
            addSession(gameID, session);
            sendNotificationToAll(gameID, authToken, null, username + " connected to the game");

        } catch (DataAccessException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleMakeMoveCommand(Session session, UserGameCommand command, String message) {
        System.out.println("MAKE_MOVE command received: " + message);

        try {
            // 1. Parse the JSON string to a JsonObject
            JsonObject jsonCommand = gson.fromJson(message, JsonObject.class);

            // 2. Extract the "move" JsonObject
            JsonObject moveObject = jsonCommand.getAsJsonObject("move");

            // 3. Deserialize the move JsonObject to a ChessMove
            ChessMove move = gson.fromJson(moveObject, ChessMove.class);

            // 4. Handle potential errors during deserialization
            if (move == null) {
                sendError(session, "Error: bad request - Invalid move format");
                return;
            }

            // 5. Validate the move using gameService (which also updates the game state)
            GameData updatedGameData = gameService.makeMove(command.getAuthToken(), command.getGameID(), move); // Assuming GameService has a makeMove method
            if (updatedGameData == null) {
                sendError(session, "Error: bad request - Invalid move");
                return;
            }

            // 6. Send a LOAD_GAME message to all clients
            sendLoadGameToAll(command.getGameID(), updatedGameData);

            // 7. Send a NOTIFICATION message to other clients about the move
            sendNotificationToAll(command.getGameID(), command.getAuthToken(), move, describeMove(move));

        } catch (DataAccessException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) { // Catch generic Exception to handle JsonParseException and other potential issues
            sendError(session, "Error processing WebSocket message: " + e.getMessage());
        }
    }

    private void handleLeaveCommand(Session session, UserGameCommand command) {
        System.out.println("LEAVE command received");

        try {
            // 1. Remove the user from the game
            gameService.leaveGame(command.getAuthToken(), command.getGameID()); // Assuming you have a leaveGame method
            removeSession(session); // Remove the session

            // 2. Send a NOTIFICATION message to other clients about the user leaving
            sendNotificationToAll(command.getGameID(), command.getAuthToken(), null, "left the game");

        } catch (DataAccessException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleResignCommand(Session session, UserGameCommand command) {
        // Handle RESIGN command
        System.out.println("RESIGN command received");

        try {
            // 1. Mark the game as over due to resignation
            gameService.resignGame(command.getAuthToken(), command.getGameID()); // Assuming you have a resignGame method

            // 2. Send a NOTIFICATION message to all clients about the resignation
            sendNotificationToAll(command.getGameID(), command.getAuthToken(), null, "resigned from the game");

        } catch (DataAccessException e) {
            sendError(session, e.getMessage());
        }
    }

    private void sendLoadGameToAll(int gameID, GameData gameData) {
        // 1. Get all sessions for the game
        Collection<Session> sessions = getSessionsForGame(gameID); // You'll need to implement this

        if (sessions != null) {
            // 2. Create LOAD_GAME message
            ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGameMessage.setGame(gameData);

            // 3. Send the message to each session
            for (Session session : sessions) {
                sendMessage(session, loadGameMessage);
            }
        }
    }

    private void sendNotificationToAll(int gameID, String authToken, ChessMove move, String action) {
        // 1. Get all sessions for the game
        Collection<Session> sessions = getSessionsForGame(gameID); // You'll need to implement this

        if (sessions != null) {
            try {
                // 2. Get the username of the player
                AuthData authData = authService.getAuth(authToken);
                if (authData == null) {
                    System.err.println("Error: Could not retrieve username for notification.");
                    return; // Don't send notification if username retrieval fails
                }
                String username = authData.username();

                // 3. Create NOTIFICATION message
                ServerMessage notificationMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                String message;
                if (move != null) {
                    String moveDescription = describeMove(move); // You'll need to implement this
                    message = username + " moved " + moveDescription;
                } else {
                    message = username + " " + action;
                }
                notificationMessage.setMessage(message);

                // 4. Send the message to each session
                for (Session session : sessions) {
                    sendMessage(session, notificationMessage);
                }
            } catch (DataAccessException e) {
                System.err.println("Error retrieving auth data for notification: " + e.getMessage());
            }
        }
    }

    private String describeMove(ChessMove move) {
        // Implement logic to describe the move in a human-readable format
        // Example: "Pawn from A2 to A4", "Rook from H1 to H3", etc.
        // This will depend on the fields in your ChessMove class.
        // For now, let's return a placeholder:
        return "a piece";
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

    private void addSession(int gameID, Session session) {
        gameSessions.computeIfAbsent(gameID, k -> new HashSet<>()).add(session);
    }

    private void removeSession(Session session) {
        for (Set<Session> sessions : gameSessions.values()) {
            sessions.remove(session);
        }
    }

    private Collection<Session> getSessionsForGame(int gameID) {
        return gameSessions.get(gameID);
    }
}