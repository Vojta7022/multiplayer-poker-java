package cz.cvut.fel.pjv.mosteji1.poker.server.network;

import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.TableRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;
import cz.cvut.fel.pjv.mosteji1.poker.server.Server;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * {@code ServerEndpoint} handles communication with a connected client.
 * It manages the input and output streams, processes commands from the client,
 * and sends updates back to the client.
 */
public class ServerEndpoint implements Runnable {

    private static final Logger logger = Logger.getLogger(ServerEndpoint.class.getName());

    // Socket for communication with the client
    private final Socket clientSocket;
    // Streams for input and output
    private BufferedReader input;
    private PrintWriter output;
    private ObjectOutputStream objectOutputStream;

    // Flag to indicate if the endpoint is active
    private boolean isActive;
    // Reference to the parent server
    private final Server parentServer;
    // Client's name and avatar index
    private String name;
    private int avatarIndex;
    private Player playerPTR;

    /**
     * Constructs a new server endpoint for a connected client.
     *
     * @param socket the socket connected to the client
     * @param server the parent server that manages this endpoint
     */
    public ServerEndpoint(Socket socket, Server server) {
        this.clientSocket = socket;
        this.parentServer = server;
        this.isActive = true;
    }

    /**
     * Starts the communication loop with the client.
     * Handles login data, message receiving, and proper cleanup.
     */
    @Override
    public void run() {
        try {
            // Initialize input and output streams
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Read the client's name and avatar index
            try {
                name = input.readLine();
                if (name == null) throw new IOException("Client disconnected before sending name");
                String avatarStr = input.readLine();
                if (avatarStr == null) throw new IOException("Client disconnected before sending avatar index");
                try {
                    avatarIndex = Integer.parseInt(avatarStr);
                } catch (NumberFormatException e) {
                    logger.severe("Invalid avatar index received from client: " + avatarStr);
                    close();
                    return;
                }
                logger.info("Client connected: " + name + ", Avatar index: " + avatarIndex);
            } catch (IOException e) {
                logger.severe("Error reading initial client data: " + e.getMessage());
                close();
                return;
            }

            // Main loop for reading messages from the client
            String message;
            while (isActive && (message = input.readLine()) != null) {
                handleClientMessage(message);
            }
        } catch (IOException e) {
            if (isActive) {
                logger.severe("Communication error with client " + name + ": " + e.getMessage());
            }
        } finally {
            close();
        }
    }

    // Parses and handles commands sent by the client
    private void handleClientMessage(String message) {
        String[] parts = message.split(" ", 2);
        String command = parts[0];

        if (!parentServer.isGameStarted()) {
            logger.warning("Game has not started yet. Ignoring message: " + message);
            return;
        }

        int currentPlayerIndex = parentServer.getTable().getWaitingForIndex();
        int thisPlayerIndex = parentServer.getTable().getPlayers().indexOf(playerPTR);
        if (currentPlayerIndex != thisPlayerIndex && !command.equals("CHAT")) {
            logger.warning("It's not " + name + "'s turn. Ignoring message: " + message);
            return;
        }

        switch (command) {
            case "CHAT" -> {
                if (parts.length < 2) {
                    logger.warning("CHAT command requires a message.");
                    return;
                }
                handleChatMessage(parts[1]);
            }
            case "FOLD" -> handleFold();
            case "CALL" -> handleCall();
            case "CHECK" -> handleCheck();
            case "RAISE" -> {
                if (parts.length < 2) {
                    logger.warning("RAISE command requires an amount.");
                    return;
                }
                try {
                    int raiseAmount = Integer.parseInt(parts[1]);
                    if (raiseAmount <= 0) {
                        logger.warning("Raise amount must be positive.");
                        return;
                    }
                    handleRaise(raiseAmount);
                } catch (NumberFormatException e) {
                    logger.severe("Invalid raise amount: " + parts[1]);
                }
            }
            case "ALLIN" -> handleAllIn();
            default -> logger.warning("Unknown command: " + message);
        }
    }

    // Handles chat messages from the client
    private void handleChatMessage(String chatMessage) {
        logger.fine("Chat message from client " + name + ": " + chatMessage);
        parentServer.getTable().appendMessageToChat(name + ": " + chatMessage, false);
        parentServer.sendUpdatesToAllPlayers();
    }

    // Handles fold commands from the client
    private void handleFold() {
        logger.fine(name + " has requested to fold.");
        if (!parentServer.getCommandQueue().offer("FOLD")) {
            logger.severe("Server busy, fold command not accepted.");
            return;
        }
        parentServer.sendUpdatesToAllPlayers();
    }

    // Handles call commands from the client
    private void handleCall() {
        logger.fine(name + " has requested to call.");
        if (!parentServer.getCommandQueue().offer("CALL")) {
            logger.severe("Server busy, call command not accepted.");
            return;
        }
        parentServer.sendUpdatesToAllPlayers();
    }

    // Handles check commands from the client
    private void handleCheck() {
        logger.fine(name + " has requested to check.");
        if (!parentServer.getCommandQueue().offer("CHECK")) {
            logger.severe("Server busy, check command not accepted.");
            return;
        }
        parentServer.sendUpdatesToAllPlayers();
    }

    // Handles raise commands from the client
    private void handleRaise(int amount) {
        logger.fine(name + " has requested to raise by " + amount);
        if (!parentServer.getCommandQueue().offer("RAISE") || !parentServer.getCommandQueue().offer(String.valueOf(amount))) {
            logger.severe("Server busy, raise command not accepted.");
            return;
        }
        parentServer.sendUpdatesToAllPlayers();
    }

    // Handles all-in commands from the client
    private void handleAllIn() {
        logger.fine(name + " has requested to go all-in.");
        if (!parentServer.getCommandQueue().offer("ALLIN")) {
            logger.severe("Server busy, all-in command not accepted.");
            return;
        }
        parentServer.sendUpdatesToAllPlayers();
    }

    /**
     * Sends the current table representation to the client.
     *
     * @param tableRepresentation the current state of the game table
     * @throws IOException if sending fails due to I/O error
     */
    public void sendTableRepresentation(TableRepresentation tableRepresentation) throws IOException {
        try {
            objectOutputStream.writeObject(tableRepresentation);
            objectOutputStream.flush();
        } catch (NotSerializableException e) {
            logger.severe("Serialization error in sendTableRepresentation: " + e.getMessage());
        }
    }

    /**
     * Returns the name of the connected player.
     *
     * @return the player name
     */
    public String getName() {
        return name;
    }

    /**
     * Renames the ServerEndpoint's name, used for avoiding duplicate names.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the avatar index selected by the client.
     *
     * @return avatar index
     */
    public int getAvatarIndex() {
        return avatarIndex;
    }

    /**
     * Closes all streams and the socket to the client.
     */
    public void close() {
        isActive = false;
        try {
            if (input != null) input.close();
        } catch (IOException e) {
            logger.severe("Error closing input: " + e.getMessage());
        }
        if (output != null) output.close();
        try {
            if (objectOutputStream != null) objectOutputStream.close();
        } catch (IOException e) {
            logger.severe("Error closing ObjectOutputStream: " + e.getMessage());
        }
        try {
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            logger.severe("Error closing socket: " + e.getMessage());
        }
    }

    /**
     * Sets the player object associated with this endpoint.
     *
     * @param player the player object to associate
     */
    public void setPlayerPTR(Player player) {
        this.playerPTR = player;
    }

    public Boolean getSocket() {
        return clientSocket != null && !clientSocket.isClosed();
    }
}