package cz.cvut.fel.pjv.mosteji1.poker.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientEndpoint {
    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public ClientEndpoint(String serverAddress, int serverPort) throws IOException {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        start();
    }

    public void start() throws IOException {
        try {
            // Connect to the server
            socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to the server.");

            // Initialize input and output streams
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            // Start a thread to listen for messages from the server
            new Thread(this::listenForMessages).start();

            // Example: Sending a message to the server
            sendMessage("JOIN");

        } catch (IOException e) {
            System.err.println("Error connecting to the server: " + e.getMessage());
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                // Process the message locally
                processMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Error reading messages from server: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void processMessage(String message) {
        // Example of processing different types of messages
        if (message.startsWith("Player")) {
            System.out.println("Game update: " + message);
        } else if (message.startsWith("Hra začíná")) {
            System.out.println("Game started: " + message);
        } else {
            System.out.println("Server message: " + message);
        }
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    private void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
            System.out.println("Connection closed.");
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public boolean isClosed() {
        return socket == null;
    }

    public Socket getSocket() {
        return socket;
    }
}
