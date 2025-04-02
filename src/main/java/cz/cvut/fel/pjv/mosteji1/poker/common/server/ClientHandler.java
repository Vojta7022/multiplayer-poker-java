package cz.cvut.fel.pjv.mosteji1.poker.common.server;

import java.io.*;
import java.net.Socket;
import com.poker.server.Server;

public class ClientHandler {
    private final Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean isActive;
    private Server server;

    public ClientHandler(Socket socket, Server server) {
        this.clientSocket = socket;
        this.server = server;
        this.isActive = true;
        try {
            this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.output = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error in input and output initialization for client: " + e.getMessage());
            this.isActive = false;
        }
    }

    public void run() {
        try {
            // Hlavní smyčka pro příjem zpráv od klienta a jejich zpracování
            while (isActive) {
                String message = input.readLine();  // Čtení zpráv od klienta
                if (message == null) {
                    break;  // Pokud je zpráva null, znamená to, že klient se odpojil
                }

                System.out.println("Message accepted from client: " + message);

                // Zpracování přijaté zprávy (např. příkaz pro sázku, pozdrav atd.)
                handleClientMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Error communicating with client: " + e.getMessage());
        } finally {
            try {
                // Uzavření socketu, když klient odpojí
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void handleClientMessage(String message) {
        // Zde přidej logiku pro různé typy zpráv od klienta
        // Například: příkazy pro sázky, zobrazení karet, kontrolu hry apod.

        switch (message.toLowerCase()) {
            case "bet":
                // Zpracování sázky
                break;
            case "check":
                // Zpracování checku
                break;
            case "fold":
                // Zpracování foldování
                break;
            case "raise":
                // Zpracování raisu
                break;
            default:
                // Neznámý příkaz
                sendMessage("Neznámý příkaz.");
        }
    }

    public void sendMessage(String message) {
        // Posílání zpráv zpět klientovi
        output.println(message);
    }

    public boolean isActive() {
        return isActive;
    }
}
