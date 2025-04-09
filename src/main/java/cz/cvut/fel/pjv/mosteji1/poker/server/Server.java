package cz.cvut.fel.pjv.mosteji1.poker.server;

import cz.cvut.fel.pjv.mosteji1.poker.server.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int PORT = 12345;  // Port pro server
    private List<ClientHandler> clientHandlers;  // Seznam všech připojených klientů
    private ServerSocket serverSocket;

    public Server() {
        this.clientHandlers = new ArrayList<>();
    }

    public void startServer() {
        try {
            // Otevření serverového socketu
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server spuštěn na portu " + PORT);

            // Akceptování připojení klientů
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nový klient připojen: " + clientSocket.getInetAddress());

                // Vytvoření a spuštění nového klienta (Handler)
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                addClientHandler(clientHandler);

                // Spuštění nového vlákna pro komunikaci s klientem
                new Thread(String.valueOf(clientHandler)).start();
            }
        } catch (IOException e) {
            System.err.println("Chyba při spuštění serveru: " + e.getMessage());
        }
    }

    public void addClientHandler(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
        System.out.println("Přidán nový klient.");
    }

    public void broadcastMessage(String message) {
        // Poslání zprávy všem připojeným klientům
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.isActive()) {
                clientHandler.sendMessage(message);
            }
        }
    }

    public void sendMessageToClient(ClientHandler clientHandler, String message) {
        // Poslání zprávy konkrétnímu klientovi
        clientHandler.sendMessage(message);
    }

    public void startGame() {
        // Tato metoda by mohla spustit hru, rozdání karet, začátek sázek apod.
        broadcastMessage("Hra začíná!");
        // Další logika pro rozjezd hry...
    }
}
