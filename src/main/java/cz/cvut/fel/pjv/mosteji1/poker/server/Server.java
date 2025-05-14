package cz.cvut.fel.pjv.mosteji1.poker.server;

import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.TableRepresentation;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.interrupted;

public class Server {

    private static final int PORT = 12345;
    private final List<ServerEndpoint> serverEndpoints = new ArrayList<>();
    private final Table table;
    private final Scanner scanner = new Scanner(System.in);
    private final BlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
    private boolean gameStarted = false;

    public Server() {
        table = new Table(this, commandQueue);
        startServer();
    }

    public Table getTable() {
        return table;
    }

    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            String serverIp = getExternalIP();
            System.out.println("Server started on IP: " + serverIp + ", port " + PORT);

            // Accepting incoming client connections
            System.out.println("Waiting for clients to connect...");

            Thread acceptClients = new Thread(() -> {
                Socket clientSocket;
                while (!interrupted()) {
                    try {
                        clientSocket = serverSocket.accept();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("New client connected: " + clientSocket.getInetAddress());

                    ServerEndpoint serverEndpoint = new ServerEndpoint(clientSocket, this);
                    addClientHandler(serverEndpoint);

                    // Start a new thread for the client
                    new Thread(serverEndpoint).start();
                }
            });

            acceptClients.start();

            while (true) {
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("start")) {
                    acceptClients.interrupt();
                    gameStarted = true;
                    startGame();
                } else if (command.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting server...");
                    serverSocket.close();
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
        Thread gameLoop = new Thread(() -> {
            while (!table.isGameWon()) {
                try {
                    while (!table.isGameWon()) {
                        table.startRound();
                    }
                } catch (InterruptedException e) {
                    System.err.println("Game loop interrupted: " + e.getMessage());
                }
            }
        });
        gameLoop.start();
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

        System.out.println("Hand: " + receiver.getHand());
        ret.setPotSize(table.getPotSize());
        ret.setBetThreshold(table.getBetThreshold());
        /*ret.setDealerIndex( ( table.getDealerIndex() - playersIndex ) % table.getPlayers().size() );*/
        ret.setDealerIndex(table.getDealerIndex());
        ret.setMyHand(new ArrayList<>(receiver.getHand()));
        ret.setCommunityCards(new ArrayList<>(table.getCommunityCards()));
        ret.setWaitingForIndex(table.getWaitingForIndex());
        ret.setYourIndex(playersIndex);
        ret.setChatMessages(new ArrayList<>(table.getChatMessages()));
        System.out.println("Hand in representation: " + ret.getMyHand());

        return ret;
    }

    public void sendUpdatesToAllPlayers() {
        System.out.println("Sending updates to all players...");
        for (Player player : table.getPlayers()) {
            TableRepresentation representation = getTableRepresentation(player, table.getPlayers().indexOf(player));
            try {
                player.getEndpoint().sendTableRepresentation(representation);
                System.out.println("Sent table representation to player " + player.getName());
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
                if (!network.isUp() || network.isLoopback()) {
                    continue;
                }

                // Prefer Wi-Fi adapter
                if (!network.getDisplayName().toLowerCase().contains("wi-fi")) {
                    continue;
                }

                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }

            // If not, any active IPv4 from any adapter
            networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                if (!network.isUp() || network.isLoopback()) {
                    continue;
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

    public BlockingQueue <String> getCommandQueue() {
        return commandQueue;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }
}
