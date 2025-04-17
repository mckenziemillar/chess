package server.handler;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

    private final GameService gameService;
    private final AuthService authService;
    private final Gson gson = new Gson();

    // WebSocket Session Management
    private final Map<Integer, Set<Session>> gameSessions = new HashMap<>();
    private final Map<Session, AuthData> sessionAuthMap = new HashMap<>();
    //private final Map<Integer, Boolean> gameIsOver = new ConcurrentHashMap<>();

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
        sessionAuthMap.remove(session);
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
                    handleMakeMoveCommand(session, command); // Pass the message for JSON parsing
                    break;
                case LEAVE:
                    handleLeaveCommand(session, command);
                    break;
                case RESIGN:
                    handleResignCommand(session, command);
                    break;
                case REDRAW_BOARD:
                    handleRedrawBoardCommand(session, command);
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
            sessionAuthMap.put(session, authData);

            String roleInfo = "an observer"; // Default to observer
            if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) {
                roleInfo = "white";
            } else if (gameData.blackUsername() != null && gameData.blackUsername().equals(username)) {
                roleInfo = "black";
            }

            //sendNotificationToAll(gameID, authToken, null, username + " connected to the game");
            Set<Session> sessions = gameSessions.get(gameID);
            if (sessions != null) {
                for (Session otherSession : sessions) {
                    if (otherSession != session) { // Don't send the notification back to the connecting client
                        ServerMessage notificationMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                        notificationMessage.setMessage(username + " connected to the game as " + roleInfo);
                        sendMessage(otherSession, notificationMessage);
                    }
                }
            }


        } catch (DataAccessException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleMakeMoveCommand(Session session, UserGameCommand command) { // Removed 'String message' param

        try {
            // 1. Get Move and Auth Token from Command
            ChessMove move = command.getMove(); // Use the getter
            String authToken = command.getAuthToken();
            int gameID = command.getGameID();

            // Validate command components
            if (move == null) {
                sendError(session, "Error: bad request - Move data missing in command object.");
                return;
            }
            if (authToken == null) {
                sendError(session, "Error: bad request - Auth token missing in command object.");
                return;
            }
            if (gameID <= 0) { // Basic check for gameID validity
                sendError(session, "Error: bad request - Invalid game ID in command object.");
                return;
            }


            // 2. Authenticate User
            AuthData authData = authService.getAuth(authToken);
            if (authData == null) {
                sendError(session, "Error: Unauthorized - Invalid auth token.");
                return;
            }
            String moverUsername = authData.username();


            // 3. Pre-Move Checks (Game Exists? Already Over? Is Player?)
            GameData initialGameData = gameService.dataAccess.getGame(gameID);
            if (initialGameData == null) {
                sendError(session, "Error: bad request - Game not found (ID: " + gameID + ").");
                return;
            }
            if (initialGameData.game() == null) {
                sendError(session, "Error: internal server error - Game state object is missing for game " + gameID + ".");
                return;
            }
            // Check if game was *already* over before this move attempt
            if (initialGameData.game().getGameOver()) {
                sendError(session, "Error: bad request - Game is already over.");
                return;
            }
            // Check if the sender is actually a player in this game
            if (!moverUsername.equals(initialGameData.whiteUsername()) && !moverUsername.equals(initialGameData.blackUsername())) {
                sendError(session, "Error: Forbidden - Observers (" + moverUsername + ") " +
                        "cannot make moves in game " + gameID + ".");
                return;
            }


            // 4. Attempt to make the move via the service
            GameData updatedGameData = null;
            try {
                // GameService.makeMove should validate turn, move legality, execute, persist, and set game over if needed
                updatedGameData = gameService.makeMove(authToken, gameID, move);
            } catch (DataAccessException e) {
                // Catch specific exceptions related to invalid move, wrong turn, etc.
                System.err.println("SERVER INFO: Invalid move attempt in game " + gameID + ": " + e.getMessage());
                sendError(session, "Error making move: " + e.getMessage()); // Send the specific reason back
                return; // Stop processing
            }


            // 5. Post-Move State Checks and Notifications
            if (updatedGameData != null && updatedGameData.game() != null) {
                ChessGame updatedGame = updatedGameData.game();

                // ***** 5a. Send LOAD_GAME to everyone ONCE *****
                sendLoadGameToAll(gameID, updatedGameData);

                // 5b. Check game end conditions / check state
                ChessGame.TeamColor potentiallyAffectedTeam = updatedGame.getTeamTurn(); // Whose turn is next
                String affectedPlayerUsername = (potentiallyAffectedTeam == ChessGame.TeamColor.WHITE) ?
                        updatedGameData.whiteUsername() : updatedGameData.blackUsername();

                boolean isNowGameOver = updatedGame.getGameOver(); // Check if makeMove marked it over
                String specialNotification = null;

                // Checkmate
                if (updatedGame.isInCheckmate(potentiallyAffectedTeam)) {
                    isNowGameOver = true;
                    String winnerUsername = moverUsername;
                    specialNotification = "Checkmate! " + affectedPlayerUsername + " is defeated. " + winnerUsername + " wins!";
                }
                // Stalemate
                else if (updatedGame.isInStalemate(potentiallyAffectedTeam)) {
                    isNowGameOver = true;
                    specialNotification = "Stalemate! The game is a draw.";
                }
                // Check (only if game didn't end)
                else if (updatedGame.isInCheck(potentiallyAffectedTeam)) {
                    specialNotification = affectedPlayerUsername + " is in check!";
                }

                // Ensure persisted state reflects game over if checkmate/stalemate occurred
                // (Ideally, gameService.makeMove handles this persistence reliably)
                if (isNowGameOver && !initialGameData.game().getGameOver()) { // Check if it *just* ended
                    ensureAndPersistGameOverState(updatedGameData, gameID, true); // Call helper
                }

                // ***** 5c. Send Notifications (only ONE notification path executes) *****

                ChessPiece pieceMoved = null;
                String pieceTypeName = "piece"; // Default value
                if (initialGameData.game() != null && move.getStartPosition() != null) {
                    // Get the game state *before* the move was made
                    ChessGame gameBeforeMove = initialGameData.game();
                    // Get the piece at the starting square
                    pieceMoved = gameBeforeMove.getPiece(move.getStartPosition());
                    if (pieceMoved != null) {
                        // Get the type name (e.g., "PAWN", "ROOK")
                        pieceTypeName = pieceMoved.getPieceType().toString();
                    }
                }
                String detailedMoveNotification = String.format("%s moved a %s from %s to %s%s",
                        moverUsername, // The player who made the move
                        pieceTypeName, // The type of the piece
                        positionToString(move.getStartPosition()), // e.g., "h2"
                        positionToString(move.getEndPosition()),   // e.g., "h3"
                        (move.getPromotionPiece() != null ? " promoting to " + move.getPromotionPiece() : "") // Add promotion info if applicable
                );

                String moveDescription = describeMoveForNotification(move);
                //sendNotificationToAllButOne(session, gameID, moverUsername + " made move " + moveDescription);
                sendNotificationToAllButOne(session, gameID, detailedMoveNotification);
                if (specialNotification != null) {
                    // Send checkmate/stalemate/check notification to ALL
                    sendNotificationToAll(gameID, specialNotification);
                }
                // If game is over but not by checkmate/stalemate (e.g. resignation handled elsewhere), no message needed here.

            } else {
                // This case suggests an issue within gameService.makeMove if it returned null without error
                System.err.println("SERVER WARNING: gameService.makeMove returned null without throwing an exception for gameID: " + gameID);
                sendError(session, "Error: Failed to process move state update.");
            }

        } catch (DataAccessException dae) {
            // Catch errors from initial getGame or getAuth
            System.err.println("SERVER ERROR: DataAccess error during make move setup for game " + command.getGameID() + ": " + dae.getMessage());
            sendError(session, "Error accessing game or user data.");
        } catch (Exception e) { // Catch any other unexpected errors
            System.err.println("SERVER ERROR: Unexpected exception in handleMakeMoveCommand for game " + command.getGameID() + ": " + e.getMessage());
            e.printStackTrace();
            sendError(session, "Error processing move: An internal server error occurred.");
        }
    }

    private void ensureAndPersistGameOverState(GameData gameData, int gameID, boolean detectedGameOver) {
        if (!detectedGameOver) {
            return; // Nothing to do if we didn't detect game over here
        }

        ChessGame game = gameData.game();
        // Check if the game object reflects the game over status
        if (!game.getGameOver()) {
            System.err.println("SERVER WARNING: Game over condition (Checkmate/Stalemate/Resign) detected, " +
                    "but game object not marked as over by service for game " + gameID + ". Forcing update.");
            game.setGameOver(true); // Mark it here
        }

        // Persist the potentially updated game state
        try {
            gameService.dataAccess.updateGame(gameData);
        } catch (DataAccessException dae) {
            System.err.println("Failed to persist game over state for game " + gameID + ": " + dae.getMessage());
            // Consider if more robust error handling is needed here
        }
    }


    private String describeMoveForNotification(ChessMove move) {
        // Example: "from a2 to a4"
        if (move == null) {
            return "[invalid move]";
        }
        return "from " + positionToString(move.getStartPosition()) +
                " to " + positionToString(move.getEndPosition()) +
                (move.getPromotionPiece() != null ? " promoting to " + move.getPromotionPiece() : "");
    }


    private String positionToString(ChessPosition pos) {
        if (pos == null) {
            return "[unknown]";
        }
        char file = (char) ('a' + pos.getColumn() - 1);
        char rank = (char) ('1' + pos.getRow() - 1);
        return "" + file + rank;
    }

    private void handleLeaveCommand(Session session, UserGameCommand command) {

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
        System.out.println("handleResignCommand for gameID: " + command.getGameID());

        try {
            AuthData authData = getAuthDataForSession(session);
            String username = authData.username();
            Integer gameID = command.getGameID();
            GameData gameData = gameService.dataAccess.getGame(gameID);
            if (gameData == null || (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername()))) {
                // If the user is not a player (likely an observer), send an error
                sendError(session, "Error: bad request - Observers cannot resign.");
                return;
            }
            ChessGame game = gameData.game();
            boolean isGameOver = game.getGameOver();


            if (isGameOver) {
                sendError(session, "Error: bad request - Game is already over");
                return;
            }
            ChessGame gameNew= gameData.game();
            gameNew.setGameOver(true);
            GameData gameDataNew = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameNew);
            gameService.dataAccess.updateGame(gameDataNew);
            gameService.resignGame(command.getAuthToken(), gameID);

            // 1. Mark the game as over due to resignation

            gameService.resignGame(command.getAuthToken(), command.getGameID()); // Assuming you have a resignGame method

            //gameService.dataAccess.updateGame(updatedGameData);

            ServerMessage resignAcknowledgement = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            resignAcknowledgement.setMessage("You have resigned from the game.");
            sendMessage(session, resignAcknowledgement);
            // 2. Send a NOTIFICATION message to all clients about the resignation
            sendNotificationToAll(command.getGameID(), command.getAuthToken(), null, "resigned from the game");

        } catch (DataAccessException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleRedrawBoardCommand(Session session, UserGameCommand command) {
        try {
            GameData gameData = gameService.dataAccess.getGame(command.getGameID());
            if (gameData != null) {
                ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
                loadGameMessage.setGame(gameData);
                sendMessage(session, loadGameMessage);
            } else {
                sendError(session, "Error: Game not found.");
            }
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
    private void sendNotificationToAll(int gameID, String notificationText) {
        Collection<Session> sessions = getSessionsForGame(gameID);
        if (sessions != null) {
            ServerMessage notificationMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notificationMessage.setMessage(notificationText);
            List<Session> sessionList = new ArrayList<>(sessions);
            for (Session sess : sessionList) {
                if (sess.isOpen()){ sendMessage(sess, notificationMessage); }
                else { removeSession(sess); }
            }
        } else {
            System.out.println("No sessions found for game " + gameID + " to send notification.");
        }
    }

    private void sendNotificationToAllButOne(Session senderSession, int gameID, String notificationText) {
        Collection<Session> sessions = getSessionsForGame(gameID);
        if (sessions != null) {
            ServerMessage notificationMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notificationMessage.setMessage(notificationText);
            List<Session> sessionList = new ArrayList<>(sessions);
            for (Session sess : sessionList) {
                if (sess.isOpen() && !sess.equals(senderSession)) { sendMessage(sess, notificationMessage); }
                else if (!sess.isOpen()){ removeSession(sess); }
            }
        } else {
            System.out.println("No sessions found for game " + gameID + " to send notification.");
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
                    String moveDescription = describeMove(move);
                    message = username + " moved " + moveDescription;
                } else {
                    message = username + " " + action;
                }
                notificationMessage.setMessage(message);

                // 4. Send the message to each session
                for (Session session : sessions) {
                    AuthData sessionAuth = getAuthDataForSession(session);
                    if (sessionAuth != null && !sessionAuth.authToken().equals(authToken)) { // Send to everyone EXCEPT the mover
                        sendMessage(session, notificationMessage);
                    }
                }


            } catch (DataAccessException e) {
                System.err.println("Error retrieving auth data for notification: " + e.getMessage());
            }
        }
    }

    private AuthData getAuthDataForSession(Session session) {
        return sessionAuthMap.get(session);
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