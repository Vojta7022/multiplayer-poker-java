package cz.cvut.fel.pjv.mosteji1.poker.server;

import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.TableRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.common.game.GameParameters;
import cz.cvut.fel.pjv.mosteji1.poker.common.game.Table;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ServerEndpoint;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import static java.lang.Thread.interrupted;

/**
 * {@code Server} listens for client connections and manages the poker game logic.
 * It handles player registration, game start, and communication between players.
 */
public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    // List of ServerEndpoint instances representing connected clients
    private final List<ServerEndpoint> serverEndpoints = Collections.synchronizedList(new ArrayList<>());
    // Game table instance
    private final Table table;
    // Scanner for reading console input
    private final Scanner scanner = new Scanner(System.in);
    // BlockingQueue for handling commands
    private final BlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
    // Flag indicating whether the game has started
    private boolean gameStarted = false;

    /**
     * Constructs and initializes the server, including the game table and server socket.
     * Starts listening for client connections and command input.
     */
    public Server() {
        table = new Table(this, commandQueue);
    }

    /**
     * Returns the game table managed by the server.
     *
     * @return the {@link Table} instance
     */
    public Table getTable() {
        return table;
    }

    /**
     * Starts the server socket, accepts client connections, and waits for console commands
     * to start or stop the game.
     */
    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(GameParameters.PORT);

            String serverIp = getExternalIP();
            System.out.println("Server started on IP: " + serverIp + ", port " + GameParameters.PORT);

            // Accepting incoming client connections
            logger.info("Waiting for clients to connect...");

            Thread acceptClients = acceptClients(serverSocket);

            while (true) {
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("start")) {
                    acceptClients.interrupt();
                    gameStarted = true;
                    startGame();
                } else if (command.equalsIgnoreCase("exit")) {
                    logger.info("Exiting server...");
                    System.out.println("Exiting server...");
                    serverSocket.close();
                    shutdown(serverSocket);
                    return;
                } else {
                    System.out.println("Unknown command. Use 'start' to start the game or 'exit' to exit.");
                }
            }

        } catch (IOException e) {
            logger.severe("Error starting server: " + e.getMessage());
        }
    }

    // Shuts down the server and closes all client connections.
    private void shutdown(ServerSocket serverSocket) {
        try {
            for (ServerEndpoint endpoint : serverEndpoints) {
                endpoint.close();
            }
            serverSocket.close();
        } catch (IOException e) {
            logger.severe("Error shutting down server: " + e.getMessage());
        }
    }

    // Accepts incoming client connections and starts a new thread for each client.
    private Thread acceptClients(ServerSocket serverSocket) {
        Thread acceptClients = new Thread(() -> {
            Socket clientSocket;
            while (!interrupted()) {
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    if (Thread.currentThread().isInterrupted()) {
                        logger.info("Accept thread interrupted, stopping...");
                        break;
                    }
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
        return acceptClients;
    }

    // Starts the game when enough players are connected, initializes the game table and manages the game loop.
    private void startGame() {
        if (serverEndpoints.size() >= GameParameters.MIN_PLAYERS && serverEndpoints.size() <= GameParameters.MAX_PLAYERS) {
            List<String> takenNames = new ArrayList<>();
            for (ServerEndpoint serverEndpoint : serverEndpoints) {
                String name = serverEndpoint.getName();
                while (takenNames.contains(name)) {
                    serverEndpoint.setName(name + "2");
                    System.out.println("Name already taken, changing to " + name + "2");
                    name = serverEndpoint.getName();
                }
                takenNames.add(name);
            }
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
            try {
                while (!table.isGameWon()) {
                    table.startRound();

                    // Wait 5 seconds before the next round
                    Thread.sleep(5*1000);
                }
            } catch (InterruptedException e) {
                logger.severe("Game loop interrupted: " + e.getMessage());
            }
        });
        gameLoop.start();
    }

    /**
     * Adds a new client handler to the list of connected clients.
     *
     * @param serverEndpoint the endpoint representing the connected client
     */
    public void addClientHandler(ServerEndpoint serverEndpoint) {
        serverEndpoints.add(serverEndpoint);
        logger.info("New client added.");
        System.out.println("New client added.");
    }

    /**
     * Creates a {@link TableRepresentation} tailored to the given player, containing
     * personalized and general information about the game state.
     *
     * @param receiver the player receiving the table representation
     * @param playersIndex the index of the player in the players list
     * @return a {@link TableRepresentation} object
     */
    public TableRepresentation getTableRepresentation(Player receiver, int playersIndex) {
        TableRepresentation ret = new TableRepresentation();

        for (Player player : table.getPlayers()) {
            ret.addPlayer(player.getName(), player.getAvatarIndex(), player.getChips(), player.getBet(), player.hasFolded(), player.isAllIn(), player.getHand());
        }

        ret.setPotSize(table.getPotSize());
        ret.setBetThreshold(table.getBetThreshold());
        ret.setDealerIndex(table.getDealerIndex());
        ret.setMyHand(new ArrayList<>(receiver.getHand()));
        ret.setCommunityCards(new ArrayList<>(table.getCommunityCards()));
        ret.setWaitingForIndex(table.getWaitingForIndex());
        ret.setYourIndex(playersIndex);
        ret.setChatMessages(new ArrayList<>(table.getChatMessages()));
        ret.setGameStarted(gameStarted);

        return ret;
    }

    /**
     * Sends an updated table representation to all connected players.
     */
    public synchronized void sendUpdatesToAllPlayers() {
        try {
            Thread.sleep(200);
        }
        catch (InterruptedException e) {
            logger.severe("Error during sleep: " + e.getMessage());
        }

        for (Player player : table.getPlayers()) {
            TableRepresentation representation = getTableRepresentation(player, table.getPlayers().indexOf(player));
            try {
                player.getEndpoint().sendTableRepresentation(representation);
                logger.info("Sent table representation to player " + player.getName());
            } catch (IOException e) {
                logger.severe("Error sending update to player " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    // Returns the  IP address on which the server is running on.
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
            logger.severe("Error getting external IP: " + e.getMessage());
        }
        return "Unknown IP";
    }

    /**
     * Returns the command queue used for communication between server and game logic.
     *
     * @return a blocking queue of server commands
     */
    public BlockingQueue <String> getCommandQueue() {
        return commandQueue;
    }

    /**
     * Returns whether the game has started.
     *
     * @return {@code true} if the game has started; {@code false} otherwise
     */
    public boolean isGameStarted() {
        return gameStarted;
    }
}
