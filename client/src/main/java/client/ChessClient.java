package client;

import model.GameData;
import model.AuthData;
import ui.EscapeSequences;
import ui.ServerFacade;
import chess.*;

import java.util.Scanner;

public class ChessClient {

    private final ServerFacade serverFacade;
    private boolean loggedIn = false;
    private String username = null; // Store logged-in username
    // Potentially store a mapping of listed game numbers to game IDs

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
                } else if (command.equalsIgnoreCase("quit")) {
                    break;
                }
            }
            System.out.println(); // Add an empty line for better readability
        }
        scanner.close();
        System.out.println("Exiting Chess Client.");
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
                    displayPreloginHelp();
                    break;
                case "quit":
                    break;
                case "login":
                    System.out.print("Username: ");
                    String loginUsername = scanner.nextLine();
                    System.out.print("Password: ");
                    String loginPassword = scanner.nextLine();
                    AuthData loginAuth = serverFacade.login(loginUsername, loginPassword);
                    loggedIn = true;
                    username = loginAuth.username();
                    System.out.println("Successfully logged in as " + username + ".");
                    break;
                case "register":
                    System.out.print("Username: ");
                    String registerUsername = scanner.nextLine();
                    System.out.print("Password: ");
                    String registerPassword = scanner.nextLine();
                    System.out.print("Email: ");
                    String registerEmail = scanner.nextLine();
                    AuthData registerAuth = serverFacade.register(registerUsername, registerPassword, registerEmail);
                    loggedIn = true;
                    username = registerAuth.username();
                    System.out.println("Successfully registered and logged in as " + username + ".");
                    break;
                default:
                    System.out.println("Invalid command. Type 'help' for available commands.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage()); // Display user-friendly error
        }
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
                    displayPostloginHelp();
                    break;
                case "logout":
                    serverFacade.logout();
                    System.out.println("Successfully logged out.");
                    break;
                case "create game":
                    System.out.print("Enter the name for the new game: ");
                    String gameName = scanner.nextLine().trim();
                    if (gameName.isEmpty()) {
                        System.out.println("Error: Game name cannot be empty. Please enter a name.");
                    } else {
                        try {
                            GameData newGame = serverFacade.createGame(gameName);
                            System.out.println("Created game: " + newGame.gameName() + ".");
                        } catch (Exception e) {
                            System.out.println("Error creating game: " + e.getMessage());
                        }
                }
                break;
                case "list games":
                    GameData[] games = serverFacade.listGames();
                    if (games != null && games.length > 0) {
                        System.out.println("Existing Games:");
                        gameListings.clear();
                        for (int i = 0; i < games.length; i++) {
                            GameData game = games[i];
                            gameListings.put(i + 1, game);
                            System.out.printf("%d. %s (White: %s, Black: %s)\n",
                                    i + 1, game.gameName(),
                                    game.whiteUsername() == null ? "Available" : game.whiteUsername(),
                                    game.blackUsername() == null ? "Available" : game.blackUsername());
                        }
                    } else {
                        System.out.println("No games currently available.");
                    }
                    break;
                case "play game":
                    System.out.print("Enter the number of the game to join: ");
                    populateGameListings();
                    if (scanner.hasNextInt()) {
                        int gameNumber = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        GameData selectedGame = gameListings.get(gameNumber);
                        if (selectedGame != null) {
                            System.out.print("Enter the color you want to play (white/black): ");
                            String colorChoice = scanner.nextLine().trim().toLowerCase();
                            if (colorChoice.equals("white") || colorChoice.equals("black")) {
                                serverFacade.joinGame(selectedGame.gameID(), colorChoice.toUpperCase());
                                System.out.println("Joined game " + selectedGame.gameName() + " as " + colorChoice + ".");
                                // *** DRAW INITIAL BOARD HERE (based on colorChoice) ***
                                drawInitialBoard(colorChoice);
                            } else {
                                System.out.println("Invalid color choice. Please enter 'white' or 'black'.");
                            }
                        } else {
                            System.out.println("Invalid game number.");
                        }
                    } else {
                        System.out.println("Invalid input. Please enter a number.");
                        scanner.nextLine(); // Consume invalid input
                    }
                    break;
                case "observe game":
                    System.out.print("Enter the number of the game to observe: ");
                    if (scanner.hasNextInt()) {
                        int gameNumber = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        populateGameListings();
                        GameData selectedGame = gameListings.get(gameNumber);
                        if (selectedGame != null) {
                            serverFacade.observeGame(selectedGame.gameID());
                            System.out.println("Observing game " + selectedGame.gameName() + ".");
                            // *** DRAW INITIAL BOARD HERE (from white's perspective) ***
                            drawInitialBoard("white");
                        } else {
                            System.out.println("Invalid game number.");
                        }
                    } else {
                        System.out.println("Invalid input. Please enter a number.");
                        scanner.nextLine(); // Consume invalid input
                    }
                    break;
                case "quit":
                    break;
                default:
                    System.out.println("Invalid command. Type 'help' for available commands.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage()); // Display user-friendly error
        }
    }

    private void populateGameListings() {
        try {
            GameData[] games = serverFacade.listGames();
            if (games != null) {
                gameListings.clear();
                for (int i = 0; i < games.length; i++) {
                    gameListings.put(i + 1, games[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching game list: " + e.getMessage());
            // Decide if you want to set gameListings to empty or handle the error differently
            gameListings.clear();
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
                    String textColor = (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? whitePieceColor : (piece != null ? blackPieceColor : EscapeSequences.SET_TEXT_COLOR_BLACK);
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
                    //boolean isLight = (row + (col - 'a' + 1)) % 2 != 0;
                    String bgColor = isLight ? lightSquareBg : darkSquareBg;
                    String textColor = (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? whitePieceColor : (piece != null ? blackPieceColor : EscapeSequences.SET_TEXT_COLOR_WHITE);
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