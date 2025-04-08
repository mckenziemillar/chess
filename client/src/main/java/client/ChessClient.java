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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ChessClient {

    private final ServerFacade serverFacade;
    private boolean loggedIn = false;
    private String username = null;
    private String authToken = null;
    private final Gson gson = new Gson();
    private WebSocket gameWebSocket;

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
        if (gameWebSocket != null) {
            gameWebSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client is closing");
            gameWebSocket = null;
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

    private void handlePlayGame(Scanner scanner) {
        System.out.print("Enter the number of the game to join: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine(); // Consume invalid input
            return;
        }
        int gameNumber = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        GameData selectedGame = gameListings.get(gameNumber);
        if (selectedGame == null) {
            System.out.println("Invalid game number.");
            return;
        }
        System.out.print("Enter the color you want to play (white/black): ");
        String colorChoice = scanner.nextLine().trim().toLowerCase();
        if (!colorChoice.equals("white") && !colorChoice.equals("black")) {
            System.out.println("Invalid color choice. Please enter 'white' or 'black'.");
            return;
        }
        try {
            serverFacade.joinGame(selectedGame.gameID(), colorChoice.toUpperCase());
            System.out.println("Joined game " + selectedGame.gameName() + " as " + colorChoice + ".");
            drawInitialBoard(colorChoice);
            connectWebSocket(selectedGame.gameID());
        } catch (Exception e) {
            System.out.println("Error joining game: ");
        }
    }

    private void handleObserveGame(Scanner scanner) {
        System.out.print("Enter the number of the game to observe: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();
            return;
        }
        int gameNumber = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        GameData selectedGame = gameListings.get(gameNumber);
        if (selectedGame == null) {
            System.out.println("Invalid game number.");
            return;
        }
        try {
            serverFacade.observeGame(selectedGame.gameID());
            System.out.println("Observing game " + selectedGame.gameName() + ".");
            drawInitialBoard("white");
            connectWebSocket(selectedGame.gameID());
        } catch (Exception e) {
            System.out.println("Error observing game: " + e.getMessage());
        }
    }

    private void connectWebSocket(int gameID) {
        String websocketUrl = "ws://localhost:8080/ws"; // Use 'ws' for WebSocket
        HttpClient client = HttpClient.newHttpClient();
        CompletableFuture<WebSocket> wsFuture = client.newWebSocketBuilder()
                .buildAsync(URI.create(websocketUrl), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        System.out.println("WebSocket connection opened.");
                        gameWebSocket = webSocket;
                        sendConnectMessage(gameWebSocket, gameID);
                        webSocket.request(1); // Start receiving messages
                    }

                    //@Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data) {
                        System.out.println("Received WebSocket message: " + data);
                        handleWebSocketMessage(data.toString());
                        webSocket.request(1);
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        System.out.println("WebSocket connection closed: " + statusCode + " " + reason);
                        gameWebSocket = null;
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        System.err.println("WebSocket error: " + error.getMessage());
                        gameWebSocket = null;
                    }
                });

        try {
            gameWebSocket = wsFuture.get(); // Block until connection is established
        } catch (Exception e) {
            System.err.println("Error establishing WebSocket connection: " + e.getMessage());
        }
    }


    private void sendConnectMessage(WebSocket webSocket, int gameID) {
        if (authToken == null) {
            System.err.println("Error: Not logged in, cannot send CONNECT message.");
            return;
        }
        UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        String jsonCommand = gson.toJson(connectCommand);
        System.out.println("Sending WebSocket CONNECT message: " + jsonCommand);
        webSocket.sendText(jsonCommand, true);
    }

    private void handleWebSocketMessage(String message) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME:
                    System.out.println("Received LOAD_GAME message: " + message);
                    // TODO: Implement logic to update the game board based on this message
                    break;
                case NOTIFICATION:
                    // Assuming you'll create a specific NotificationMessage class with a 'message' field
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
                    System.out.println("Received unknown WebSocket message type: " + serverMessage.getServerMessageType());
            }
        } catch (JsonParseException e) {
            System.err.println("Error parsing WebSocket message: " + e.getMessage());
            System.err.println("Raw message: " + message);
        }
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
}