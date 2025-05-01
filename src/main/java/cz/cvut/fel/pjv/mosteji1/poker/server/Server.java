package cz.cvut.fel.pjv.mosteji1.poker.server;

import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.TableRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.game.GameParameters;
import cz.cvut.fel.pjv.mosteji1.poker.common.game.Table;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ServerEndpoint;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import static java.lang.Thread.interrupted;

public class Server {

    private static final int PORT = 12345;
    private final List<ServerEndpoint> serverEndpoints = new ArrayList<>();
    private Table table;
    private final Scanner scanner = new Scanner(System.in);

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

            Thread acceptClients = new Thread(() -> {
                while (!interrupted()) {
                    Socket clientSocket = null;
                    try {
                        clientSocket = serverSocket.accept();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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
            });

            acceptClients.start();

            while (true) {
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("start")) {
                    acceptClients.interrupt();
                    startGame();
                } else if (command.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting server...");
                    System.exit(0);
                } else {
                    System.out.println("Unknown command. Use 'start' to start the game or 'exit' to exit.");
                }
            }

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    private void startGame() {
        if (serverEndpoints.size() >= GameParameters.MIN_PLAYERS && serverEndpoints.size() <= GameParameters.MAX_PLAYERS) {
            table = new Table(this);
            table.addPlayers(serverEndpoints);

            System.out.println("Game started with " + serverEndpoints.size() + " players.");
        }
        else {
            System.out.println("Invalid number of players. Game cannot start.");
            return;
        }
        // Send initial table representation to all players
        sendUpdatesToAllPlayers();

        // Start the game loop
        while (!table.isGameWon()) {
            table.startRound();
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
        /*ret.setDealerIndex( ( table.getDealerIndex() - playersIndex ) % table.getPlayers().size() );*/
        ret.setDealerIndex(table.getDealerIndex());
        ret.setMyHand(receiver.getHand());
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

    public List<ServerEndpoint> getServerEndpoints() {
        return serverEndpoints;
    }

    private String getExternalIP() {
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                if (network.isLoopback() || !network.isUp()) {
                    continue; // skip loopback or inactive interfaces
                }

                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
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
