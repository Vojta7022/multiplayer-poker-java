package cz.cvut.fel.pjv.mosteji1.poker.server;

import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.TableRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.game.Table;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ServerEndpoint;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Server {

    private static final int PORT = 12345;  // Port pro server
    private final List<ServerEndpoint> serverEndpoints = new ArrayList<>();  // Seznam všech připojených klientů
    private Table table;

    public Server() {
        startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT);)
        {
            String serverIp = getExternalIP();
            System.out.println("Server started on IP: " + serverIp + ", port " + PORT);

            // Accepting incoming client connections
            System.out.println("Waiting for clients to connect...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ServerEndpoint serverEndpoint = new ServerEndpoint(clientSocket, this);
                addClientHandler(serverEndpoint);

                // Start a new thread for the client
                new Thread(String.valueOf(serverEndpoint)){
                    @Override
                    public void run() {
                        serverEndpoint.run();
                    }

                }.start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }

        table = new Table(this);
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
        /*ret.setDealerIndex( ( table.getDealerIndex() - playersIndex ) % table.getPlayers().size() );*/
        ret.setDealerIndex(table.getDealerIndex());
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
                System.err.println("Error sending update to player " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    public void sendMessageToClient(ServerEndpoint serverEndpoint, String message) {
        // Sending a message to a specific client
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

    private String getExternalIP() {
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address instanceof java.net.Inet4Address) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "Unknown IP";
    }
}
