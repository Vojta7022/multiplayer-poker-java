package cz.cvut.fel.pjv.mosteji1.poker.client.network;

import cz.cvut.fel.pjv.mosteji1.poker.client.graphics.PokerTableView;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientEndpoint {
    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
    private PrintWriter output;
    private final String name;
    private final int avatarIndex;

    public ClientEndpoint(String serverAddress, int serverPort, String name, int avatarIndex) throws IOException {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.name = name;
        this.avatarIndex = avatarIndex;
    }

    public void start() throws IOException {
        try {
            // Connect to the server
            socket = new Socket(serverAddress, serverPort);

            // Initialize the output stream
            output = new PrintWriter(socket.getOutputStream(), true);

            // Send initial data to the server
            output.println(name);
            output.println(avatarIndex);
            System.out.println("Sent name and avatar index to the server: " + name + ", " + avatarIndex);

        } catch (IOException e) {
            System.err.println("Error connecting to the server: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    public boolean isClosed() {
        return socket == null;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getName() {
        return name;
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public void setPokerTableView(PokerTableView tableView) {
    }
}
