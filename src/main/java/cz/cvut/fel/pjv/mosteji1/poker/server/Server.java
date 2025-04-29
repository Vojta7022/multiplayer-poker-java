package cz.cvut.fel.pjv.mosteji1.poker.server;

import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.TableRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.game.Table;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ServerEndpoint;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int PORT = 12345;  // Port pro server
    private List<ServerEndpoint> serverEndpoints;  // Seznam všech připojených klientů
    private ServerSocket serverSocket;
    private Table table;

    public Server() {
        this.serverEndpoints = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            serverEndpoints.add(new ServerEndpoint(new Socket(), this));
        }
    }

    public void startServer() {
        try {
            // Otevření serverového socketu
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server spuštěn na portu " + PORT);

            Thread tableThread = new Thread(() -> {
                table = new Table(this);
            });

            tableThread.start();

            // Akceptování připojení klientů
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nový klient připojen: " + clientSocket.getInetAddress());

                // Vytvoření a spuštění nového klienta (Handler)
                ServerEndpoint serverEndpoint = new ServerEndpoint(clientSocket, this);
                addClientHandler(serverEndpoint);

                // Spuštění nového vlákna pro komunikaci s klientem
                new Thread(String.valueOf(serverEndpoint)){
                    @Override
                    public void run() {
                        serverEndpoint.run();
                    }

                }.start();
            }
        } catch (IOException e) {
            System.err.println("Error when launching server: " + e.getMessage());
        }
    }

    public void addClientHandler(ServerEndpoint serverEndpoint) {
        serverEndpoints.add(serverEndpoint);
        System.out.println("New client added.");
    }

    public TableRepresentation getTableRepresentation(Player receiver, int playersIndex) {
        TableRepresentation ret = new TableRepresentation();

        for (Player player : table.getPlayers()) {
            ret.addPlayer(player.getName(), player.getAvatarIndex(), player.getChips(), player.getBet(), player.hasFolded(), player.isAllIn());
        }

        ret.setPotSize(table.getPotSize());
        ret.setBetThreshold(table.getBetThreshold());
        ret.setDealerIndex( ( table.getDealerIndex() - playersIndex ) % table.getPlayers().size() );    // send index relative to receiver
        ret.setMyHand(receiver.getHand().toArray(new Card[2]));
        ret.setCommunityCards(table.getCommunityCards());
        ret.setWaitingForIndex(table.getWaitingForIndex());

        return ret;
    }

    public void sendUpdatesToAllPlayers() {
        for (Player player : table.getPlayers()) {
            TableRepresentation representation = getTableRepresentation(player, table.getPlayers().indexOf(player));
            try {
                player.getEndpoint().sendTableRepresentation(representation);
            } catch (IOException e) {
                System.err.println("Chyba při odesílání aktualizace hráči " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    public void sendMessageToClient(ServerEndpoint serverEndpoint, String message) {
        // Poslání zprávy konkrétnímu klientovi
        serverEndpoint.sendMessage(message);
    }

    public void startGame() {
//        // Inicializace herního stolu
//        table = new Table(serverEndpoints);
//        table.initializeGame();
//
//        // Rozdání karet hráčům
//        table.dealCardsToPlayers();
//
//        // Oznámení hráčům, že hra začíná
//        broadcastMessage("Hra začíná! Karty byly rozdány.");
//
//        // Zahájení prvního kola sázek
//        table.startBettingRound();
//
//        // Další logika hry (např. další kola sázek, odhalení karet, určení vítěze)
//        while (!table.isGameOver()) {
//            table.startBettingRound();
//            table.revealNextCommunityCard();
//        }
//
//        // Oznámení vítěze
//        String winner = table.determineWinner();
//        broadcastMessage("Hra skončila! Vítězem je: " + winner);
    }



    public List<ServerEndpoint> getServerEndpoints() {
        return serverEndpoints;
    }
}
