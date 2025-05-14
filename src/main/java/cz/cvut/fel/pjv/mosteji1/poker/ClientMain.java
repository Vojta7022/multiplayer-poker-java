
package cz.cvut.fel.pjv.mosteji1.poker;

import cz.cvut.fel.pjv.mosteji1.poker.client.GameState;
import cz.cvut.fel.pjv.mosteji1.poker.client.graphics.MenuView;
import cz.cvut.fel.pjv.mosteji1.poker.client.network.ClientEndpoint;
import cz.cvut.fel.pjv.mosteji1.poker.common.game.GameParameters;
import cz.cvut.fel.pjv.mosteji1.poker.client.graphics.PokerTableView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ClientMain extends Application {

    private GameState gameState = GameState.MENU;
    private Stage primaryStage;
    public static final List<Image> sprites = new ArrayList<>();
    public static final List<Image> avatars = new ArrayList<>();
    private ClientEndpoint myEndpoint;
    private int avatarIndex;
    private String name;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        graphicsInitialize();
        showMenuScene();
    }

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

    private void showMenuScene() {

        MenuView menuView = new MenuView();

        menuView.connectButton.setOnAction(_ -> {
            String ip = MenuView.ipField.getText();
            String portStr = MenuView.portField.getText();
            name = MenuView.playerNameField.getText();
            avatarIndex = MenuView.avatarComboBox.getSelectionModel().getSelectedIndex();

            // TODO: Zkusit navázat spojení
            boolean connected = tryConnect(ip, portStr);

            if (connected) {
                gameState = GameState.PLAYING;
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

    private void showTableScene() {
        PokerTableView tableView = new PokerTableView(this.myEndpoint);
        myEndpoint.setPokerTableView(tableView);
        Scene tableScene = new Scene(tableView);
        tableScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        primaryStage.setScene(tableScene);
        primaryStage.setTitle("Poker Client - Game");
        primaryStage.setMaximized(true);
    }

    private boolean tryConnect(String ip, String portStr) {
        // TODO: Reálná logika připojení přes socket
        try {
            myEndpoint = new ClientEndpoint(ip, Integer.parseInt(portStr), name, avatarIndex);
            myEndpoint.start();
            System.out.println("Connecting to IP: " + ip + ", port: " + portStr);

            return true;
        } catch (NumberFormatException e) {
            MenuView.statusLabel.setText("Invalid port number.");
            System.err.println("Invalid port number: " + portStr);
            return false;
        } catch (IOException e) {
            MenuView.statusLabel.setText("Could not connect to server.");
            System.err.println("Error connecting to the server: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
