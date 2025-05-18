package cz.cvut.fel.pjv.mosteji1.poker.client.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Handles the network communication from the client's side.
 * Connects to the server, sends the player's name and avatar index,
 * and provides methods to send messages to the server.
 */
public class ClientEndpoint {

    private static final Logger logger = Logger.getLogger(ClientEndpoint.class.getName());

    // Server address and port
    private final String serverAddress;
    private final int serverPort;
    // Socket for communication with the server
    private Socket socket;
    // Streams for input and output
    private PrintWriter output;
    private final String name;
    // Player's avatar index
    private final int avatarIndex;

    /**
     * Constructs a new ClientEndpoint with the given server information and player identity.
     *
     * @param serverAddress the IP address or hostname of the server
     * @param serverPort the port on which the server is listening
     * @param name the player's name
     * @param avatarIndex the index of the selected avatar
     * @throws IOException if socket initialization fails
     */
    public ClientEndpoint(String serverAddress, int serverPort, String name, int avatarIndex) throws IOException {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.name = name;
        this.avatarIndex = avatarIndex;
    }

    /**
     * Connects to the server and sends the initial player data (name and avatar index).
     *
     * @throws IOException if the connection or stream initialization fails
     */
    public void start() throws IOException {
        try {
            // Connect to the server
            socket = new Socket(serverAddress, serverPort);

            // Initialize the output stream
            output = new PrintWriter(socket.getOutputStream(), true);

            // Send initial data to the server
            output.println(name);
            output.println(avatarIndex);
            logger.info("Sent name and avatar index to the server: " + name + ", " + avatarIndex);

        } catch (IOException e) {
            logger.severe("Error connecting to the server: " + e.getMessage());
        }
    }

    /**
     * Sends a message to the server.
     *
     * @param message the message to send
     */
    public void sendMessage(String message) {
        output.println(message);
    }

    /**
     * Checks if the socket was never initialized (i.e., client never connected).
     *
     * @return true if the socket is null
     */
    public boolean isClosed() {
        return socket == null;
    }

    /**
     * Returns the socket connected to the server.
     *
     * @return the socket, or null if not connected
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Returns the name of the connected player.
     *
     * @return the player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks whether the client is currently connected to the server.
     *
     * @return true if the socket is not null and not closed
     */
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
}
