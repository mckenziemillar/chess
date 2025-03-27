package ui;
import client.ChessClient;
import java.util.Scanner;

public class Repl {

    private final ChessClient client;
    private final Scanner scanner;

    public Repl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
    }

    /*public void run() {
        while (true) {
            if (!client.isLoggedIn()) {
                client.displayPreloginHelp();
                System.out.print(">> ");
                String command = scanner.nextLine().trim();
                client.handlePreloginCommand(command, scanner);
                if (command.equalsIgnoreCase("quit")) {
                    break;
                }
            } else {
                client.displayPostloginHelp();
                System.out.print(client.getUsername() + " >> ");
                String command = scanner.nextLine().trim();
                client.handlePostloginCommand(command, scanner);
                if (command.equalsIgnoreCase("logout")) {
                    client.setLoggedIn(false);
                    client.setUsername(null);
                } else if (command.equalsIgnoreCase("quit")) {
                    break;
                }
            }
            System.out.println(); // Add an empty line for better readability
        }
        scanner.close();
        System.out.println("Exiting Chess Client.");
    }*/
}