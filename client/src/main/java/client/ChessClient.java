package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import model.GameData;
import model.AuthData;
import ui.EscapeSequences;
import ui.ServerFacade;
import chess.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class ChessClient extends Endpoint{

    private final ServerFacade serverFacade;
    private boolean loggedIn = false;
    private String username = null;
    private String authToken = null;
    private final Gson gson = new Gson();
    //private WebSocket gameWebSocket;
    private Session session;
    private ChessGame currentChessGame = null;
    private String playerColor = null;
    private int currentGameId = -1;
    private boolean displayingGame = false;

    public ChessClient(String serverUrl) {
        this.serverFacade = new ServerFacade(serverUrl);
    }

    public static void main(String[] args) {
        // Replace with your actual server URL (can be taken from config or user input)
        String serverUrl = "http://localhost:8080";
        ChessClient client = new ChessClient(serverUrl);
        client.run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (!loggedIn) {
                displayPreloginHelp();
                System.out.print(">> ");
                String command = scanner.nextLine().trim();
                handlePreloginCommand(command, scanner);
                if (command.equalsIgnoreCase("quit")) {
                    break;
                }
            } else {
                displayPostloginHelp();
                System.out.print(username + " >> ");
                String command = scanner.nextLine().trim();
                handlePostloginCommand(command, scanner);
                if (command.equalsIgnoreCase("logout")) {
                    loggedIn = false;
                    username = null;
                    authToken = null;
                    closeWebSocket();
                } else if (command.equalsIgnoreCase("quit")) {
                    closeWebSocket();
                    break;
                }
            }
            System.out.println();
        }
        scanner.close();
        System.out.println("Exiting Chess Client.");
    }
    private void closeWebSocket() {
        if (this.session != null && this.session.isOpen()) {
            try {
                this.session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Client logging out"));
            } catch (IOException e) {
                System.err.println("Error closing WebSocket session: " + e.getMessage());
            } finally {
                this.session = null;
            }
        } else {
            this.session = null;
        }
    }
    private void displayPreloginHelp() {
        System.out.println("\nChess Client - Prelogin");
        System.out.println("Available commands:");
        System.out.println("  help       - Display available commands");
        System.out.println("  quit       - Exit the program");
        System.out.println("  login      - Log in to an existing account");
        System.out.println("  register   - Create a new account");
    }

    private void handlePreloginCommand(String command, Scanner scanner) {
        try {
            switch (command.toLowerCase()) {
                case "help":
                    //displayPreloginHelp();
                    break;
                case "quit":
                    break;
                case "login":
                    handleLogin(scanner);
                    break;
                case "register":
                    handleRegister(scanner);
                    break;
                default:
                    System.out.println("Invalid command. Type 'help' for available commands.");
            }
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void handleLogin(Scanner scanner) {
        System.out.print("Username: ");
        String loginUsername = scanner.nextLine();
        System.out.print("Password: ");
        String loginPassword = scanner.nextLine();
        try {
            AuthData loginAuth = serverFacade.login(loginUsername, loginPassword);
            loggedIn = true;
            username = loginAuth.username();
            authToken = loginAuth.authToken();
            serverFacade.setAuthToken(authToken);
            System.out.println("Successfully logged in as " + username + ".");
        } catch (Exception e) {
            displayAuthError("Login failed:", e.getMessage());
        }
    }

    private void handleRegister(Scanner scanner) {
        System.out.print("Username: ");
        String registerUsername = scanner.nextLine();
        System.out.print("Password: ");
        String registerPassword = scanner.nextLine();
        System.out.print("Email: ");
        String registerEmail = scanner.nextLine();
        try {
            AuthData registerAuth = serverFacade.register(registerUsername, registerPassword, registerEmail);
            loggedIn = true;
            username = registerAuth.username();
            authToken = registerAuth.authToken();
            serverFacade.setAuthToken(authToken);
            System.out.println("Successfully registered and logged in as " + username + ".");
        } catch (Exception e) {
            displayAuthError("Registration failed:", e.getMessage());
        }
    }

    private void displayAuthError(String failureType, String errorMessage) {
        if (errorMessage.startsWith(failureType) && errorMessage.contains("{") && errorMessage.contains("}")) {
            try {
                String jsonString = errorMessage.substring(errorMessage.indexOf("{"), errorMessage.lastIndexOf("}") + 1);
                JsonObject errorJson = gson.fromJson(jsonString, JsonObject.class);
                if (errorJson.has("message")) {
                    System.out.println(errorJson.get("message").getAsString());
                    return;
                }
            } catch (JsonParseException ex) {
                System.out.println("Error: " + errorMessage);
                return;
            }
        }
        System.out.println("Error: " + errorMessage);
    }


    private java.util.Map<Integer, GameData> gameListings = new java.util.HashMap<>();


    private void displayPostloginHelp() {
        System.out.println("\nChess Client - Postlogin (Logged in as " + username + ")");
        System.out.println("Available commands:");
        System.out.println("  help        - Display available commands");
        System.out.println("  logout      - Log out of the current account");
        System.out.println("  create game - Create a new chess game");
        System.out.println("  list games  - List all existing chess games");
        System.out.println("  play game   - Join an existing game as a player");
        System.out.println("  observe game- Observe an existing game");
        System.out.println("  quit        - Exit the program");
    }
    private void handlePostloginCommand(String command, Scanner scanner) {
        try {
            //int gameID = -1;
            switch (command.toLowerCase()) {
                case "help":
                    //displayPostloginHelp();
                    break;
                case "logout":
                    serverFacade.logout();
                    System.out.println("Successfully logged out.");
                    break;
                case "create game":
                    handleCreateGame(scanner);
                    break;
                case "list games":
                    handleListGames();
                    break;
                case "play game":
                    handlePlayGame(scanner);
                    break;
                case "observe game":
                    handleObserveGame(scanner);
                    break;
                case "quit":
                    closeWebSocket();
                    break;
                default:
                    System.out.println("Invalid command. Type 'help' for available commands.");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleCreateGame(Scanner scanner) {
        System.out.print("Enter the name for the new game: ");
        String gameName = scanner.nextLine().trim();
        if (gameName.isEmpty()) {
            System.out.println("Error: Game name cannot be empty. Please enter a name.");
            return;
        }
        try {
            GameData newGame = serverFacade.createGame(gameName);
            System.out.println("Created game: " + newGame.gameName() + ".");
        } catch (Exception e) {
            System.out.println("Error creating game: " + e.getMessage());
        }
    }

    private void handleListGames() throws Exception {
        List<GameData> games = serverFacade.listGames().games();
        if (games == null || games.size() == 0) {
            System.out.println("No games currently available.");
            gameListings.clear();
            return;
        }
        System.out.println("Existing Games:");
        gameListings.clear();
        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);
            gameListings.put(i + 1, game);
            System.out.printf("%d. %s (White: %s, Black: %s)\n",
                    i + 1, game.gameName(),
                    game.whiteUsername() == null ? "Available" : game.whiteUsername(),
                    game.blackUsername() == null ? "Available" : game.blackUsername());
        }
    }

    private int handlePlayGame(Scanner scanner) {
        System.out.print("Enter the number of the game to join: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine(); // Consume invalid input
            return 0;
        }
        int gameNumber = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        GameData selectedGame = gameListings.get(gameNumber);
        if (selectedGame == null) {
            System.out.println("Invalid game number.");
            return gameNumber;
        }
        System.out.print("Enter the color you want to play (white/black): ");
        String colorChoice = scanner.nextLine().trim().toLowerCase();
        if (!colorChoice.equals("white") && !colorChoice.equals("black")) {
            System.out.println("Invalid color choice. Please enter 'white' or 'black'.");
            return gameNumber;
        }
        try {
            serverFacade.joinGame(selectedGame.gameID(), colorChoice.toUpperCase());
            System.out.println("Joined game " + selectedGame.gameName() + " as " + colorChoice + ".");
            displayingGame = true;
            this.playerColor = colorChoice;
            this.currentGameId = selectedGame.gameID();
            //drawInitialBoard(colorChoice);
            connectWebSocket(selectedGame.gameID());
            if (this.session != null && this.session.isOpen()) {
                gameNumber = selectedGame.gameID();
                handleGameplay(scanner, selectedGame.gameID());
            } else {
                System.out.println("Failed to establish WebSocket connection. Returning to menu.");
                // Optionally try to leave the game server-side if join succeeded but WS failed
            }
            //sendConnectMessage(selectedGame.gameID());
            //might need this idk
        } catch (Exception e) {
            System.out.println("Error joining game: ");
        }
        return gameNumber;
    }

    private int handleObserveGame(Scanner scanner) {
        System.out.print("Enter the number of the game to observe: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();
            return -1;
        }
        int gameNumber = scanner.nextInt();
        scanner.nextLine();
        GameData selectedGame = gameListings.get(gameNumber);
        if (selectedGame == null) {
            System.out.println("Invalid game number.");
            return -1;
        }
        try {
            serverFacade.observeGame(selectedGame.gameID());
            System.out.println("Observing game " + selectedGame.gameName() + ".");
            displayingGame = true;
            drawInitialBoard("white");
            this.currentGameId = selectedGame.gameID();
            connectWebSocket(selectedGame.gameID());
            if (this.session != null && this.session.isOpen()) {
                gameNumber = selectedGame.gameID();
                handleGameplay(scanner, selectedGame.gameID());
            } else {
                System.out.println("Failed to establish WebSocket connection for observation. Returning to menu.");
            }
        } catch (Exception e) {
            System.out.println("Error observing game: " + e.getMessage());
        }
        return gameNumber;
    }

    private void handleGameplay(Scanner scanner, int gameID) {
        System.out.println("\n--- Gameplay for Game: " + gameID + " ---");
        System.out.println("Available commands (type 'help'):");
        displayingGame = true;
        //GAMEPLAY LOOP
        while (loggedIn && this.session != null && this.session.isOpen()) {
            System.out.print(username + " (Game " + gameID + ") >> ");
            String command = scanner.nextLine().trim().toLowerCase();
            handleGameplayCommand(command, scanner, gameID, this.session);
        }
        System.out.println("Exiting gameplay for game " + gameID + ".");
        closeWebSocket();
        currentGameId = -1;
        //^this might not be necessary IDK
    }


    private void handleGameplayCommand(String command, Scanner scanner, int gameID, Session currentSession) {
        switch (command) {
            case "help":
                displayGameplayHelp();
                break;
            case "redraw board":
                handleRedrawBoard(gameID, currentSession);
                break;
            case "leave":
                handleLeave(gameID, currentSession);
                break;
            case "make move":
                handleMakeMove(scanner, gameID, currentSession);
                break;
            case "resign":
                handleResign(scanner, gameID, currentSession);
                break;
            case "highlight legal moves":
                handleHighlightLegalMoves(scanner, gameID, currentSession);
                break;
            default:
                System.out.println("Invalid gameplay command. Type 'help' for available commands.");
        }
    }
    private void displayGameplayHelp() {
        System.out.println("Available commands:");
        System.out.println("  help                    - Display available commands");
        System.out.println("  redraw board            - Redraw the chess board");
        System.out.println("  leave                   - Leave the game");
        System.out.println("  make move <start> <end> - Make a move (e.g., a2 a4)");
        System.out.println("  resign                  - Resign the game");
        System.out.println("  highlight legal moves   - Highlight legal moves for a piece");
    }

    private void handleRedrawBoard(int gameID, Session currentSession) {
        try {
            sendRedrawBoardRequest(gameID, currentSession);
        } catch (Exception e) {
            System.err.println("Error requesting board redraw: " + e.getMessage());
        }
    }

    private void sendRedrawBoardRequest(int gameID, Session currentSession) {
        UserGameCommand redrawCommand = new UserGameCommand(
                UserGameCommand.CommandType.REDRAW_BOARD,
                authToken,
                gameID
        );
        String jsonCommand = gson.toJson(redrawCommand);
        try {
            currentSession.getBasicRemote().sendText(jsonCommand);
        } catch (IOException e) {
            System.err.println("Error sending LEAVE command: " + e.getMessage());
        }
        System.out.println("Sent REDRAW_BOARD command to server.");
    }

    private void handleLeave(int gameID, Session currentSession) {
        sendLeaveCommand(gameID, currentSession);
        closeWebSocket();
        System.out.println("Left game. Returning to post-login menu.");
        currentGameId = -1;
        displayingGame = false;
    }

    private void sendLeaveCommand(int gameID, Session currentSession) {
        UserGameCommand leaveCommand = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        String jsonCommand = gson.toJson(leaveCommand);
        try {
            currentSession.getBasicRemote().sendText(jsonCommand);
        } catch (IOException e) {
            System.err.println("Error sending LEAVE command: " + e.getMessage());
        }
    }

    private void handleMakeMove(Scanner scanner, int gameID, Session currentSession) {
        System.out.print("Enter move (e.g., a2 a4): ");
        String moveInput = scanner.nextLine().trim();
        String[] parts = moveInput.split("\\s+");
        if (parts.length == 2) {
            try {
                ChessMove move = parseMove(parts[0], parts[1]);
                sendMakeMoveCommand(gameID, currentSession, move);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid move format: " + e.getMessage());
            }
        } else {
            System.out.println("Invalid move format. Please use: <start> <end> (e.g., a2 a4)");
        }
    }

    private ChessMove parseMove(String start, String end) {
        try {
            ChessPosition startPos = parsePosition(start);
            ChessPosition endPos = parsePosition(end);
            return new ChessMove(startPos, endPos, null); // Promotion is handled later
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid move format: " + e.getMessage());
            return null;
        }
    }

    private ChessPosition parsePosition(String position) {
        if (position.length() != 2) {
            throw new IllegalArgumentException("Invalid position format.");
        }
        char file = position.charAt(0);
        char rank = position.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Invalid position format.");
        }
        int col = file - 'a' + 1;
        int row = rank - '1' + 1;
        return new ChessPosition(row, col);
    }

    private void sendMakeMoveCommand(int gameID, Session currentSession, ChessMove move) {
        UserGameCommand moveCommand = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move);
        String jsonCommand = gson.toJson(moveCommand);
        try {
            currentSession.getBasicRemote().sendText(jsonCommand);
        } catch (IOException e) {
            System.err.println("Error sending LEAVE command: " + e.getMessage());
        }
    }
    private void handleResign(Scanner scanner, int gameID, Session currentSession) {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (confirmation.equals("yes")) {
            sendResignCommand(gameID, currentSession);
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void sendResignCommand(int gameID, Session currentSession) {
        UserGameCommand resignCommand = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        String jsonCommand = gson.toJson(resignCommand);
        try {
            currentSession.getBasicRemote().sendText(jsonCommand);
        } catch (IOException e) {
            System.err.println("Error sending LEAVE command: " + e.getMessage());
        }
    }

    private void handleHighlightLegalMoves(Scanner scanner, int gameID, Session currentSession) {
        System.out.print("Enter the square of the piece to highlight (e.g., a2): ");
        String square = scanner.nextLine().trim().toLowerCase();
        try {
            ChessPosition position = parsePosition(square);
            if (currentChessGame != null) {
                ChessBoard board = currentChessGame.getBoard();
                ChessPiece piece = board.getPiece(position);
                if (piece != null) {
                    Collection<ChessMove> legalMoves = currentChessGame.validMoves(position);
                    highlightMovesOnBoard(position, legalMoves);
                } else {
                    System.out.println("Error: No piece at " + square);
                }
            } else {
                System.out.println("No game in progress. Cannot highlight moves.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid square format: " + e.getMessage());
        }
    }

    private void highlightMovesOnBoard(ChessPosition selectedPosition, Collection<ChessMove> legalMoves) {
        drawBoard(legalMoves, selectedPosition);
    }

// Implement calculateLegalMoves to determine the valid moves


    private void connectWebSocket(int gameID) {
        String websocketUrl = "ws://localhost:8080/ws";
        try {
            URI socketURI = new URI(websocketUrl);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>(){
                @Override
                public void onMessage(String message){
                    System.out.println("onMessage recieved");
                    handleWebSocketMessage(message);
                }
            });
            sendConnectMessage(gameID);
        } catch (URISyntaxException e) {
            this.session = null;
            throw new RuntimeException(e);
        } catch (DeploymentException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endConfig){
        if (currentGameId != -1) {
            handleGameplay(new Scanner(System.in), currentGameId);
        }
    }


    private void sendConnectMessage(int gameID) {
        if (authToken == null) {
            System.err.println("Error: Not logged in, cannot send CONNECT message.");
            return;
        }
        UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        String jsonCommand = gson.toJson(connectCommand);
        System.out.println("Sending WebSocket CONNECT message: " + jsonCommand);
        try {
            this.session.getBasicRemote().sendText(jsonCommand);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleWebSocketMessage(String message) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME:
                    System.out.println("Received LOAD_GAME message: " + message);

                    Object gamePayload = serverMessage.getGame(); // Get the payload object
                    if (gamePayload != null) {
                        GameData gameData = null;
                        try {
                            // Convert the generic payload object back to JSON, then parse into GameData
                            String gameJson = gson.toJson(gamePayload);
                            gameData = gson.fromJson(gameJson, GameData.class);

                        } catch (JsonParseException | ClassCastException e) {
                            System.err.println("Error parsing GameData from LOAD_GAME payload: " + e.getMessage());
                            e.printStackTrace(); // Print full stack trace for details
                        }

                        // Now check the deserialized gameData
                        if (gameData != null && gameData.game() != null && gameData.game().getBoard() != null) {
                            this.currentChessGame = gameData.game();
                            String perspective = determinePerspective(gameData);
                            drawBoard(gameData, perspective); // Draw the received board
                        } else {
                            System.err.println("Error: LOAD_GAME data, game object, or board is null after parsing.");
                            if(gameData != null && gameData.game() == null) {
                                System.err.println("DEBUG: gameData.game() is null.");
                            }
                            if(gameData != null && gameData.game() != null && gameData.game().getBoard() == null) {
                                System.err.println("DEBUG: gameData.game().getBoard() is null.");
                            }
                        }
                    } else {
                        System.err.println("Error: LOAD_GAME message payload field is null.");
                    }
                    displayGameplayHelp();
                    System.out.print(username + " >> ");
                    break;
                case NOTIFICATION:
                    JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
                    if (jsonObject.has("message")) {
                        String notification = jsonObject.get("message").getAsString();
                        System.out.println("Notification: " + notification);
                    } else {
                        System.out.println("Received generic NOTIFICATION: " + message);
                    }
                    break;
                case ERROR:
                    // Assuming you'll create a specific ErrorMessage class with an 'errorMessage' field
                    JsonObject errorObject = gson.fromJson(message, JsonObject.class);
                    if (errorObject.has("errorMessage")) {
                        String errorMessage = errorObject.get("errorMessage").getAsString();
                        System.err.println("Server Error: " + errorMessage);
                    } else {
                        System.err.println("Received generic ERROR: " + message);
                    }
                    break;
                default:
                    System.out.println("Received unknown WebSocket message type: " +
                            serverMessage.getServerMessageType());
            }
        } catch (JsonParseException e) {
            System.err.println("Error parsing WebSocket message: " + e.getMessage());
            System.err.println("Raw message: " + message);
        }
    }

    private String determinePerspective(GameData gameData){
        if (this.playerColor != null) {
            return this.playerColor;
        }
        if (gameData != null && gameData.whiteUsername() != null && username.equals(gameData.whiteUsername())) {
            return "white";
        } else if (gameData != null && gameData.blackUsername() != null && username.equals(gameData.blackUsername())) {
            return "black";
        } else {
            return "white";
        }
    }

    private static class SquareAppearance {
        final String bgColor;
        final String textColor;
        final String pieceChar;

        SquareAppearance(String bgColor, String textColor, String pieceChar) {
            this.bgColor = bgColor;
            this.textColor = textColor;
            this.pieceChar = pieceChar;
        }
    }

    // Refactored helper method
    private SquareAppearance getSquareAppearance(ChessBoard chessBoard, ChessPosition pos, String perspective,
                                                 Collection<ChessMove> legalMoves, ChessPosition selectedPosition) {

        ChessPiece piece = chessBoard.getPiece(pos);
        String pieceChar = getPieceChar(piece);
        boolean isLight = (pos.getRow() + pos.getColumn()) % 2 != 0;

        // Define base colors (could be moved to constants)
        String lightSquareBg = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        String darkSquareBg = EscapeSequences.SET_BG_COLOR_DARK_GREY;
        String whitePieceColor = EscapeSequences.SET_TEXT_COLOR_RED;
        String blackPieceColor = EscapeSequences.SET_TEXT_COLOR_BLUE;
        String highlightColor = EscapeSequences.SET_BG_COLOR_GREEN;
        String defaultTextColor = perspective.equalsIgnoreCase("black") ? EscapeSequences.SET_TEXT_COLOR_WHITE : EscapeSequences.SET_TEXT_COLOR_BLACK;

        // Determine background color
        String bgColor = isLight ? lightSquareBg : darkSquareBg;
        if (selectedPosition != null && selectedPosition.equals(pos)) {
            bgColor = highlightColor;
        } else if (legalMoves != null) {
            for (ChessMove move : legalMoves) {
                if (move.getEndPosition().equals(pos)) {
                    bgColor = highlightColor;
                    break;
                }
            }
        }

        // Determine text color
        String textColor = defaultTextColor; // Default for empty squares based on perspective
        if (piece != null) {
            textColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? whitePieceColor : blackPieceColor;
        }


        return new SquareAppearance(bgColor, textColor, pieceChar);
    }

    private void drawInitialBoard(String perspective) {
        System.out.println("\nInitial Chessboard:");
        ChessGame board = new ChessGame();
        ChessBoard chessBoard = board.getBoard();

        String lightSquareBg = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        String darkSquareBg = EscapeSequences.SET_BG_COLOR_DARK_GREY;
        String whitePieceColor = EscapeSequences.SET_TEXT_COLOR_RED;
        String blackPieceColor = EscapeSequences.SET_TEXT_COLOR_BLUE;
        String reset = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR;
        String emptySquare = EscapeSequences.EMPTY;
        String rowLabelColor = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        String colLabelColor = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;

        if (perspective.equalsIgnoreCase("white") || perspective.equalsIgnoreCase("observe")) {
            // White's perspective
            System.out.println(colLabelColor + "  a  b  c  d  e  f  g  h" + reset);
            for (int row = 8; row >= 1; row--) {
                System.out.print(rowLabelColor + row + " " + reset);
                for (char colChar = 'a'; colChar <= 'h'; colChar++) {
                    int col = colChar - 'a' + 1;
                    ChessPosition pos = new ChessPosition(row, col);
                    ChessPiece piece = chessBoard.getPiece(pos);
                    String pieceChar = getPieceChar(piece);
                    //boolean isLight = (row + (col - 'a' + 1)) % 2 != 0;
                    boolean isLight = (row + col) % 2 != 0;
                    String bgColor = isLight ? lightSquareBg : darkSquareBg;
                    String textColor = (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                            ? whitePieceColor : (piece != null ? blackPieceColor : EscapeSequences.SET_TEXT_COLOR_BLACK);
                    System.out.print(bgColor + textColor + pieceChar + reset);
                }
                System.out.println();
            }
            System.out.println(colLabelColor + "  a  b  c  d  e  f  g  h" + reset);

        } else if (perspective.equalsIgnoreCase("black")) {
            // Black's perspective
            System.out.println(colLabelColor + "  h  g  f  e  d  c  b  a" + reset);
            for (int row = 1; row <= 8; row++) { // Level 2
                System.out.print(rowLabelColor + row + " " + reset);
                for (char colChar = 'h'; colChar >= 'a'; colChar--) { // Level 3
                    int col = colChar - 'a' + 1;
                    ChessPosition pos = new ChessPosition(row, col);
                    SquareAppearance appearance = getSquareAppearance(chessBoard, pos, perspective, null, null);
                    System.out.print(appearance.bgColor + appearance.textColor + appearance.pieceChar + reset);
                }
                System.out.println();
            }
            System.out.println(colLabelColor + "  h  g  f  e  d  c  b  a" + reset);
        } else {
            System.out.println("Invalid perspective: " + perspective);
        }
        System.out.println();
    }

    private String getPieceChar(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
        ChessGame.TeamColor color = piece.getTeamColor();
        ChessPiece.PieceType type = piece.getPieceType();
        if (color == ChessGame.TeamColor.WHITE) {
            switch (type) {
                case KING:
                    return EscapeSequences.WHITE_KING;
                case QUEEN:
                    return EscapeSequences.WHITE_QUEEN;
                case BISHOP:
                    return EscapeSequences.WHITE_BISHOP;
                case KNIGHT:
                    return EscapeSequences.WHITE_KNIGHT;
                case ROOK:
                    return EscapeSequences.WHITE_ROOK;
                case PAWN:
                    return EscapeSequences.WHITE_PAWN;
            }
        } else {
            switch (type) {
                case KING:
                    return EscapeSequences.BLACK_KING;
                case QUEEN:
                    return EscapeSequences.BLACK_QUEEN;
                case BISHOP:
                    return EscapeSequences.BLACK_BISHOP;
                case KNIGHT:
                    return EscapeSequences.BLACK_KNIGHT;
                case ROOK:
                    return EscapeSequences.BLACK_ROOK;
                case PAWN:
                    return EscapeSequences.BLACK_PAWN;
            }
        }
        return EscapeSequences.EMPTY; // Should not reach here
    }

    private void drawBoard(GameData gameData, String perspective) {
        if (!displayingGame) {
            return; // Don't draw if not displaying a game
        }
        System.out.println("\nCurrent Chessboard:");
        ChessBoard chessBoard = gameData.game().getBoard(); // Access the board from GameData
        String lightSquareBg = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        String darkSquareBg = EscapeSequences.SET_BG_COLOR_DARK_GREY;
        String whitePieceColor = EscapeSequences.SET_TEXT_COLOR_RED;
        String blackPieceColor = EscapeSequences.SET_TEXT_COLOR_BLUE;
        String reset = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR;
        String emptySquare = EscapeSequences.EMPTY;
        String rowLabelColor = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        String colLabelColor = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;

        if (perspective.equalsIgnoreCase("white") || perspective.equalsIgnoreCase("observe")) {
            // White's perspective
            System.out.println(colLabelColor + "  a  b  c  d  e  f  g  h" + reset);
            for (int row = 8; row >= 1; row--) {
                System.out.print(rowLabelColor + row + " " + reset);
                for (char colChar = 'a'; colChar <= 'h'; colChar++) {
                    int col = colChar - 'a' + 1;
                    ChessPosition pos = new ChessPosition(row, col);
                    ChessPiece piece = chessBoard.getPiece(pos);
                    String pieceChar = getPieceChar(piece);
                    //boolean isLight = (row + (col - 'a' + 1)) % 2 != 0;
                    boolean isLight = (row + col) % 2 != 0;
                    String bgColor = isLight ? lightSquareBg : darkSquareBg;
                    String textColor = (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                            ? whitePieceColor : (piece != null ? blackPieceColor : EscapeSequences.SET_TEXT_COLOR_BLACK);
                    System.out.print(bgColor + textColor + pieceChar + reset);
                }
                System.out.println();
            }
            System.out.println(colLabelColor + "  a  b  c  d  e  f  g  h" + reset);

        } else if (perspective.equalsIgnoreCase("black")) {
            // Black's perspective
            System.out.println(colLabelColor + "  h  g  f  e  d  c  b  a" + reset);
            for (int row = 1; row <= 8; row++) {
                System.out.print(rowLabelColor + row + " " + reset);
                for (char colChar = 'h'; colChar >= 'a'; colChar--) {
                    int col = colChar - 'a' + 1;
                    ChessPosition pos = new ChessPosition(row, col);
                    ChessPiece piece = chessBoard.getPiece(pos);
                    String pieceChar = getPieceChar(piece);
                    boolean isLight = (row + col) % 2 != 0;
                    String bgColor = isLight ? lightSquareBg : darkSquareBg;
                    String textColor = (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                            ? whitePieceColor : (piece != null ? blackPieceColor : EscapeSequences.SET_TEXT_COLOR_WHITE);
                    System.out.print(bgColor + textColor + pieceChar + reset);
                }
                System.out.println();
            }
            System.out.println(colLabelColor + "  h  g  f  e  d  c  b  a" + reset);
        } else {
            System.out.println("Invalid perspective: " + perspective);
        }
        System.out.println();
    }

    private void drawBoard(Collection<ChessMove> legalMoves, ChessPosition selectedPosition){
        if (!displayingGame) {
            return;
        }
        System.out.println("\nCurrent Chessboard:");
        ChessBoard chessBoard = currentChessGame.getBoard(); // Access the board from GameData
        String lightSquareBg = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        String darkSquareBg = EscapeSequences.SET_BG_COLOR_DARK_GREY;
        String whitePieceColor = EscapeSequences.SET_TEXT_COLOR_RED;
        String blackPieceColor = EscapeSequences.SET_TEXT_COLOR_BLUE;
        String reset = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR;
        String emptySquare = EscapeSequences.EMPTY;
        String rowLabelColor = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        String colLabelColor = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
        String highlightColor = EscapeSequences.SET_BG_COLOR_GREEN;

        System.out.println(colLabelColor + "  a  b  c  d  e  f  g  h" + reset);
        for (int row = 8; row >= 1; row--) {
            System.out.print(rowLabelColor + row + " " + reset);
            for (char colChar = 'a'; colChar <= 'h'; colChar++) {
                int col = colChar - 'a' + 1;
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = chessBoard.getPiece(pos);
                String pieceChar = getPieceChar(piece);
                boolean isLight = (row + col) % 2 != 0;
                String bgColor = isLight ? lightSquareBg : darkSquareBg;
                String textColor = (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                        ? whitePieceColor : (piece != null ? blackPieceColor : EscapeSequences.SET_TEXT_COLOR_BLACK);
                if (selectedPosition != null && selectedPosition.equals(pos)){
                    bgColor = highlightColor;
                } else{
                    for (ChessMove move : legalMoves){
                        if (move.getEndPosition().equals(pos)){
                            bgColor = highlightColor;
                            break;
                        }
                    }
                }
                System.out.print(bgColor + textColor + pieceChar + reset);
            }
            System.out.println();
        }
        System.out.println(colLabelColor + "  a  b  c  d  e  f  g  h" + reset);
        System.out.println();
    }
}