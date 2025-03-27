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