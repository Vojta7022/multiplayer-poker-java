package cz.cvut.fel.pjv.mosteji1.poker.server.network;

import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.TableRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.server.Server;

import java.io.*;
import java.net.Socket;

public class ServerEndpoint implements Runnable {
    private final Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private ObjectOutputStream objectOutputStream;
    private boolean isActive;
    private final Server parentServer;

    private String name;
    private int avatarIndex;

    public ServerEndpoint(Socket socket, Server server) {
        this.clientSocket = socket;
        this.parentServer = server;
        this.isActive = true;
        try {
            this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.output = new PrintWriter(clientSocket.getOutputStream(), true);
            this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            // name and avatarIndex are read from the client
            this.name = input.readLine();
            this.avatarIndex = Integer.parseInt(input.readLine());
            System.out.println("Client connected: " + name + ", Avatar index: " + avatarIndex);
        } catch (IOException e) {
            System.err.println("Error in input and output initialization for client: " + e.getMessage());
            this.isActive = false;
        }
    }

    public void run() {
        try {
            // Main loop for reading messages from the client
            while (isActive) {
                String message = input.readLine();
                if (message == null) {
                    break;  // If the message is null, the client has disconnected
                }

                // Handling the accepted message
                handleClientMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Error communicating with client: " + e.getMessage());
        } finally {
            try {
                // Closing socket and streams
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void handleClientMessage(String message) {

        // Don't process messages if the game has not started
        if (!parentServer.isGameStarted()) {
            System.out.println("Game has not started yet. Ignoring message: " + message);
            return;
        }

        // Check if the message is from the correct player whose turn it is
        int currentPlayerIndex = parentServer.getTable().getWaitingForIndex();
        int thisPlayerIndex = parentServer.getTable().getPlayers().indexOf(parentServer.getTable().getPlayerByName(name));
        if (currentPlayerIndex != thisPlayerIndex) {
            System.out.println("It's not " + name + "'s turn. Ignoring message: " + message);
            return;
        }

        String[] messageParts = message.split(" ", 3);

        switch (messageParts[0]) {
            // Handle chat messages
            case "CHAT":
                String chatMessage = message.substring(5);
                System.out.println("Chat message from client " + name + ": " + chatMessage);
                parentServer.getTable().appendMessageToChat(name + ": " + chatMessage);
                parentServer.sendUpdatesToAllPlayers();
                break;

            case "FOLD":
                System.out.println(name + " has requested to fold.");
                parentServer.getCommandQueue().offer("FOLD");
                parentServer.sendUpdatesToAllPlayers();
                break;

            case "CALL":
                System.out.println(name + " has requested to call.");
                parentServer.getCommandQueue().offer("CALL");
                parentServer.sendUpdatesToAllPlayers();
                break;

            case "CHECK":
                System.out.println(name + " has requested to check.");
                parentServer.getCommandQueue().offer("CHECK");
                parentServer.sendUpdatesToAllPlayers();
                break;

            case "RAISE":
                int raiseAmount = Integer.parseInt(messageParts[1]);
                System.out.println(name + " has requested to raise by " + raiseAmount);
                parentServer.getCommandQueue().offer("RAISE");
                parentServer.getCommandQueue().offer(String.valueOf(raiseAmount));
                parentServer.sendUpdatesToAllPlayers();
                break;

            case "ALLIN":
                System.out.println(name + " has requested to go all-in.");
                parentServer.getCommandQueue().offer("ALLIN");
                parentServer.sendUpdatesToAllPlayers();
                break;

            default:
                // Unknown command
                sendMessage("Unknown command: " + message);
        }
    }

    public void sendMessage(String message) {
        // Send a message to the client
        output.println(message);
    }

    public void sendTableRepresentation(TableRepresentation tableRepresentation) throws IOException {
        objectOutputStream.writeObject(tableRepresentation);
        objectOutputStream.flush();
    }

    public String getName() {
        return name;
    }

    public int getAvatarIndex() {
        return avatarIndex;
    }
}