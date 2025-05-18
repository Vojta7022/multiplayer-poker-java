
package cz.cvut.fel.pjv.mosteji1.poker;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.pjv.mosteji1.poker.client.graphics.MenuView;
import cz.cvut.fel.pjv.mosteji1.poker.client.network.ClientEndpoint;
import cz.cvut.fel.pjv.mosteji1.poker.common.game.GameParameters;
import cz.cvut.fel.pjv.mosteji1.poker.client.graphics.PokerTableView;
import cz.cvut.fel.pjv.mosteji1.poker.utils.MyUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.*;

/**
 * Entry point for the client-side of the poker application using JavaFX.
 * <p>
 * This class initializes the user interface, loads graphical resources,
 * manages connection to the server, and switches between the menu and
 * game table scenes.
 *
 * @see javafx.application.Application
 * @see cz.cvut.fel.pjv.mosteji1.poker.client.network.ClientEndpoint
 */
public class ClientMain extends Application {

    private static final Logger logger = Logger.getLogger(ClientMain.class.getName());

    // JavaFX application entry point
    private Stage primaryStage;
    /** List of card sprites used in the game. */
    public static final List<Image> sprites = new ArrayList<>();
    /** List of avatar images used in the game. */
    public static final List<Image> avatars = new ArrayList<>();
    // Client endpoint for network communication
    private ClientEndpoint myEndpoint;
    // User data
    private int avatarIndex;
    private String name;

    /**
     * Initializes the JavaFX application and displays the menu.
     *
     * @param stage The primary stage for this application.
     */
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        graphicsInitialize();
        showMenuScene();
    }

    // Initializes the graphical resources (sprites and avatars) used in the game.
    private void graphicsInitialize() {
        // Cards
        for (int i = 0; i < GameParameters.CARD_COUNT; i++) {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/card_" + i + ".png")));
            sprites.add(image);
        }

        // Other images
        sprites.add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/card_back.png"))));   // 52
        sprites.add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/card_placeholder.png")))); // 53
        sprites.add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/button_absent.png"))));  // 54
        sprites.add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/button_present.png")))); // 55
        sprites.add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/menu_background.png")))); // 56

        // Avatars
        for (int i = 0; i < GameParameters.AVATAR_COUNT; i++) {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/avatars/" + (i + 1) + ".png")));
            avatars.add(image);
        }
    }

    // Loads the menu scene for the user to connect to the server.
    private void showMenuScene() {

        MenuView menuView = new MenuView();

        menuView.connectButton.setOnAction(_ -> {
            String ip = MenuView.ipField.getText();
            String portStr = MenuView.portField.getText();
            name = MenuView.playerNameField.getText();
            avatarIndex = MenuView.avatarComboBox.getSelectionModel().getSelectedIndex();

            saveGameData(ip, portStr, name, avatarIndex);

            boolean connected = tryConnect(ip, portStr);

            if (connected) {
                showTableScene();
            } else {
                MenuView.statusLabel.setText("Could not connect.");
            }
        });

        Scene menuScene = new Scene(menuView);
        menuScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        primaryStage.setTitle("Poker Client - Connection");
        primaryStage.setScene(menuScene);
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    // Displays the poker table scene after a successful connection.
    private void showTableScene() {
        PokerTableView tableView = new PokerTableView(this.myEndpoint);
        Scene tableScene = new Scene(tableView);
        tableScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        primaryStage.setScene(tableScene);
        primaryStage.setTitle("Poker Client - Game");
        primaryStage.setMaximized(true);
    }

    // Attempts to connect to the server using the provided IP and port.
    private boolean tryConnect(String ip, String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            myEndpoint = new ClientEndpoint(ip, port, name, avatarIndex);
            myEndpoint.start();

            // Check if the connection is established
            if (myEndpoint.isConnected()) {
                logger.info("Successfully connected to IP: " + ip + ", port: " + portStr);
                return true;
            } else {
               logger.severe("Failed to connect to IP: " + ip + ", port: " + portStr);
                return false;
            }
        } catch (NumberFormatException e) {
            MenuView.statusLabel.setText("Invalid port number.");
            logger.severe("Invalid port number: " + portStr);
            return false;
        } catch (IOException e) {
            MenuView.statusLabel.setText("Could not connect to server.");
            logger.severe("Error connecting to the server: " + e.getMessage());
            return false;
        }
    }

    // Saves the game data (IP, port, player name, and avatar index) to a JSON file.
    private void saveGameData(String ip, String port, String playerName, int avatarIndex) {
        ObjectMapper objectMapper = new ObjectMapper();

        String resourcePath = "src/main/resources/game_data.json";
        File file = new File(resourcePath);

        try {
            Map<String, String> data = Map.of(
                    "ip", ip,
                    "port", port,
                    "playerName", playerName,
                    "avatarIndex", String.valueOf(avatarIndex)
            );

            objectMapper.writeValue(file, data);
            logger.info("Game data saved successfully.");
        } catch (IOException e) {
            logger.severe("Error saving game data: " + e.getMessage());
        }
    }

    /**
     * Main method that launches the JavaFX application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {

        MyUtils.initializeLogger(args);

        launch(args);
    }
}
