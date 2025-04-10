package cz.cvut.fel.pjv.mosteji1.poker.server;

import cz.cvut.fel.pjv.mosteji1.poker.common.game.Table;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ServerEndpoint;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int POnRT = 12345;  // Port pro server
    private List<ServerEndpoint> serverEndpoints;  // Seznam všech připojených klientů
    private ServerSocket serverSocket;
    private Table table;

    public Server() {
        this.serverEndpoints = new ArrayList<>();
    }

    public void startServer() {
        try {
            // Otevření serverového socketu
            serverSocket = new ServerSocket(POnRT);
            System.out.println("Server spuštěn na portu " + POnRT);

            // Akceptování připojení klientů
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nový klient připojen: " + clientSocket.getInetAddress());

                // Vytvoření a spuštění nového klienta (Handler)
                ServerEndpoint serverEndpoint = new ServerEndpoint(clientSocket, this);
                addClientHandler(serverEndpoint);

                // Spuštění nového vlákna pro komunikaci s klientem
                new Thread(String.valueOf(serverEndpoint)).start();
            }
        } catch (IOException e) {
            System.err.println("Chyba při spuštění serveru: " + e.getMessage());
        }
    }

    public void addClientHandler(ServerEndpoint serverEndpoint) {
        serverEndpoints.add(serverEndpoint);
        System.out.println("Přidán nový klient.");
    }

    public void broadcastMessage(String message) {
        // Poslání zprávy všem připojeným klientům
        for (ServerEndpoint serverEndpoint : serverEndpoints) {
            if (serverEndpoint.isActive()) {
                serverEndpoint.sendMessage(message);
            }
        }
    }

    public void sendMessageToClient(ServerEndpoint serverEndpoint, String message) {
        // Poslání zprávy konkrétnímu klientovi
        serverEndpoint.sendMessage(message);
    }

    public void startGame() {
        // Tato metoda by mohla spustit hru, rozdání karet, začátek sázek apod.
        table = new Table();
        broadcastMessage("Hra začíná!");
        // Další logika pro rozjezd hry...
    }
}
